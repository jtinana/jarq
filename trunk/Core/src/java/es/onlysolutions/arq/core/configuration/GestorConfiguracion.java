package es.onlysolutions.arq.core.configuration;

import es.onlysolutions.arq.core.configuration.exception.PropertyNotFoundException;
import es.onlysolutions.arq.core.log.LoggerGenerator;
import org.apache.commons.configuration.*;
import org.apache.commons.logging.Log;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.beans.factory.BeanCreationException;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Implementacion concreta de la configuracion. Obtiene los parametros primero de la configuracion de base de datos,
 * y en caso de no existir, lo trata de obtener del fichero de configuracion.
 * En todos los motodos se lanza una PropertyNotFoundException si la propiedad no existe.
 *
 * @see es.onlysolutions.arq.core.configuration.exception.PropertyNotFoundException
 */
public class GestorConfiguracion
{
    /**
     * Configuracion compuesta de la que obtener las propiedades. Se instancia sin parametros.
     */
    private static final CompositeConfiguration compositeConfiguration = new CompositeConfiguration();

    /**
     * Constante que se inicializara con el nombre de la aplicacion.
     * Se utiliza para la generacion de los loggers de la arquitectura, para realizar
     * un logue correcto en caso de convivir en dos aplicaicones dentro del mismo servidor de aplicaciones.
     */
    private static String APPLICATION_NAME = "NO_DEFINIDA";

    /**
     * El logger de la clase. Se instancia una vez leido el nombre de la aplicacion.
     */
    private static Log logger;

    /**
     * Constructor para el gestor. Spring instanciara la clase con los parametros adecuados en el constructor.
     *
     * @param dataSource           El datasource por el que obtener conexiones.
     * @param table                La tabla de la base de datos de la que obtener la configuracion.
     * @param keyColumn            La columna que se utilizaro como clave.
     * @param valueColumn          La columna con el valor del parometro.
     * @param urlPropertyFile      URL al fichero de configuracion que cargar como fuente secundaria.
     * @param urlLog4jPropertyFile Ruta al fichero de log4j.
     * @param applicationName      El nombre de la aplicacion actual.
     * @throws ConfigurationException Si ocurre algon error al cargar la configuracion.
     */
    public GestorConfiguracion(DataSource dataSource, String table, String keyColumn, String valueColumn, String urlPropertyFile, String urlLog4jPropertyFile, String applicationName) throws ConfigurationException
    {
        validateApplicationName(applicationName);
        APPLICATION_NAME = applicationName;
        logger = LoggerGenerator.getLogger(this.getClass());


        checkParam("dataSource", dataSource);
        checkParam("table", table);
        checkParam("keyColumn", keyColumn);
        checkParam("valueColumn", valueColumn);
        checkParam("urlPropertyFile", urlPropertyFile);
        checkParam("urlLog4jPropertyFile", urlLog4jPropertyFile);

        Configuration databaseConfig = new DatabaseConfiguration(dataSource, table, keyColumn, valueColumn);
        Configuration propConfig = new PropertiesConfiguration(urlPropertyFile);

        compositeConfiguration.addConfiguration(databaseConfig);
        compositeConfiguration.addConfiguration(propConfig);

        InputStream inLog4J = this.getClass().getResourceAsStream('/' + urlLog4jPropertyFile);

        if (inLog4J == null)
        {
            throw new BeanCreationException("No se ha podido resolver a un fichero en el ClassPath la URL: " + urlLog4jPropertyFile);
        }

        Properties propsLog4J = new Properties();
        try
        {
            propsLog4J.load(inLog4J);
        }
        catch (IOException e)
        {
            logger.error(e);
            throw new ConfigurationException("No se ha podido abrir el InputStream al fichero: " + urlLog4jPropertyFile, e);
        }

        PropertyConfigurator.configure(propsLog4J);

        if (logger.isDebugEnabled())
        {
            logger.debug("Se cargan las propiedades para Log4J: " + propsLog4J);
        }

        if (logger.isInfoEnabled())
        {
            logger.info("Configurado correctamente el Gestor de Configuracion.");
            logger.info("DataSource: " + dataSource);
            logger.info("Table: " + table);
            logger.info("KeyColumn: " + keyColumn);
            logger.info("ValueColumn: " + valueColumn);
            logger.info("UrlPropertyFile: " + urlPropertyFile);
            logger.info("UrlLog4jPropertyFile: " + urlLog4jPropertyFile);
            logger.info("Application Name: " + applicationName);
        }
    }

    /**
     * Valida que el nombre de la aplicacion sea correcto y se pueda utilizar correctamente.
     *
     * @param applicationName El nombre de la aplicacion.
     */
    private void validateApplicationName(String applicationName)
    {
        if (applicationName == null || applicationName.length() == 0)
        {
            throw new PropertyNotFoundException("El nombre de la aplicacion no puede ser vacio");
        }

        if (applicationName.indexOf(' ') != -1)
        {
            throw new PropertyNotFoundException("El nombre de la aplicacion no puede contener espacios");
        }

        if (applicationName.indexOf("ñ") != -1 || applicationName.indexOf("ñ") != -1)
        {
            throw new PropertyNotFoundException("El nombre de la aplicacion no puede contener la 'ñ'");
        }

        if (applicationName.indexOf('.') != -1)
        {
            throw new PropertyNotFoundException("El nombre de la aplicacion no puede contener el caracter '.'");
        }

        if (applicationName.indexOf(',') != -1)
        {
            throw new PropertyNotFoundException("El nombre de la aplicacion no puede contener el caracter ','");
        }

    }

    /**
     * Obtiene una propiedad de tipo String leida de la configuracion.
     *
     * @param propertyName El nombre de la propiedad a buscar.
     * @return La propiedad obtenida de la configuracion con formato String.
     */
    static String getString(String propertyName)
    {
        String rVal;

        rVal = compositeConfiguration.getString(propertyName);
        checkPropertyNotNull(propertyName, rVal);

        return rVal;
    }

    /**
     * Obtiene una propiedad de tipo Integer de la configuracion.
     *
     * @param propertyName El nombre de la propiedad a buscar.
     * @return La propiedad en formato Integer.
     */
    static Integer getInteger(String propertyName)
    {
        Integer rVal;

        rVal = compositeConfiguration.getInteger(propertyName, null);
        checkPropertyNotNull(propertyName, rVal);

        return rVal;
    }

    /**
     * Comprueba que la propiedad pasada como parametro no sea nula.
     *
     * @param propertyName El nombre de la propiedad a comprobar.
     * @param propertyObj  El resultado de la propiedad.
     */
    private static void checkPropertyNotNull(String propertyName, Object propertyObj)
    {
        if (propertyObj == null)
        {
            throw new PropertyNotFoundException("La propiedad: " + propertyName + " no se encuentra en ninguna configuracion");
        }
    }

    /**
     * Comprueba si un parametro se ha pasado a nulo o no. Lo utilizaremos para el constructor, y comprobar si
     * se ha configurado correctamente en Spring la clase.
     * Adicionalmente, indica mediante Warnings si la propiedad es cadena vacia.
     *
     * @param paramName  El nombre del parametro que no se ha configurado correctamente.
     * @param paramValue El valor a comprobar si es nulo.
     */
    private void checkParam(String paramName, Object paramValue)
    {
        if (paramValue == null)
        {
            throw new PropertyNotFoundException("La propiedad: " + paramName + " se ha pasado nula. Revise la configuracion de Spring");
        }
        else if (paramValue instanceof String)
        {
            String strParamValue = (String) paramValue;
            if (strParamValue.length() == 0)
            {
                logger.warn("El parametro: " + paramName + " se ha pasado como cadena vacia.");
            }
        }
    }

    /**
     * Devuelve en nombre de la aplicacion actual.
     *
     * @return El nombre de la aplicacion actual.
     */
    static String getApplicationName()
    {
        return APPLICATION_NAME;
    }
}

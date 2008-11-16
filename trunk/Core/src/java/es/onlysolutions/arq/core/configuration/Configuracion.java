package es.onlysolutions.arq.core.configuration;

import es.onlysolutions.arq.core.log.LoggerGenerator;
import org.apache.commons.logging.Log;

/**
 * Clase con metodos estaticos para obtener los parametros de la configuracion.
 * Delega toda la logica en la clase GestorConfiguracion.
 * En caso de no encontrar una propieda se obtiene una PropertyNotFoundException.
 *
 * @see es.onlysolutions.arq.core.configuration.GestorConfiguracion
 * @see es.onlysolutions.arq.core.configuration.exception.PropertyNotFoundException
 */
public class Configuracion
{

    /**
     * El log de la clase.
     */
    private static final Log logger = LoggerGenerator.getLogger(Configuracion.class);

    /**
     * Metodo para obtener una propiedad de tipo String.
     *
     * @param propertyName El nombre de la propiedad.
     * @return El valor de la propiedad.
     */
    public static String getString(String propertyName)
    {
        String propValue = GestorConfiguracion.getString(propertyName);

        if (logger.isDebugEnabled())
        {
            logger.debug("Obtenido el valor: " + propValue + " para la propiedad: " + propertyName);
        }

        return propValue;
    }

    /**
     * Metodo para obtener una propiedad de tipo String.
     *
     * @param propertyName El nombre de la propiedad.
     * @return El valor de la propiedad.
     */
    public static Integer getInteger(String propertyName)
    {
        Integer intValue = GestorConfiguracion.getInteger(propertyName);

        if (logger.isDebugEnabled())
        {
            logger.debug("Obtenido el valor: " + intValue + " para la propiedad: " + propertyName);
        }

        return intValue;
    }

    /**
     * Obtiene una propiedad por su nombre. este metodo se utiliza como puente con la
     * antigua configuracion. Sera eliminado en posteriores versiones de la aplicacion.
     *
     * @param propertyName El nombre de la propiedad.
     * @return La propiedad como un String.
     * @deprecated Utilizar los metodos concretos por tipo expuestos en esta misma clase.
     */
    public static String getPropiedad(String propertyName)
    {
        String strValue = GestorConfiguracion.getString(propertyName);

        if (logger.isDebugEnabled())
        {
            logger.debug("Obtenido el valor: " + strValue + " para la propiedad: " + propertyName);
        }

        return strValue;
    }

    /**
     * Obtiene una propiedad booleana de la configuracion. Realiza una conversion de la siguiente forma:<br>
     * Devuelve true si la propiedad es el literal 'true', 'verdadero', '1', 'si'<br>
     * Devuelve false en cualquier otro caso.
     * Tener en cuenta que es case sesitive.
     *
     * @param propertyName El nombre de la propiedad a obtener.
     * @return El valor booleano convertido segun la especificacion del metodo.
     */
    public static boolean getBoolean(String propertyName)
    {
        String strValue = GestorConfiguracion.getString(propertyName).toLowerCase();

        if ("1".equals(strValue) || "true".equals(propertyName) || "si".equals(propertyName) || "verdadero".equals(propertyName))
        {
            return true;
        }
        else
        {
            return false;
        }

    }

    /**
     * Obtiene el nombre de la aplicacion actual.
     *
     * @return El nombre de la aplicacion.
     */
    public static String getCurrentApplicationName()
    {
        return GestorConfiguracion.getApplicationName();
    }
}

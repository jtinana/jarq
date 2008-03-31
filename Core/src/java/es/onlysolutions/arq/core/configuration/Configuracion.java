package es.onlysolutions.arq.core.configuration;

import es.onlysolutions.arq.core.log.LoggerGenerator;
import org.apache.commons.logging.Log;

/**
 * Clase con m�todos est�ticos para obtener los par�metros de la configuraci�n.
 * Delega toda la l�gica en la clase GestorConfiguracion.
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
     * M�todo para obtener una propiedad de tipo String.
     *
     * @param propertyName El nombre de la propiedad.
     * @return El valor de la propiedad.
     */
    public static final String getString(String propertyName)
    {
        String propValue = GestorConfiguracion.getString(propertyName);

        if (logger.isDebugEnabled())
        {
            logger.debug("Obtenido el valor: " + propValue + " para la propiedad: " + propertyName);
        }

        return propValue;
    }

    /**
     * M�todo para obtener una propiedad de tipo String.
     *
     * @param propertyName El nombre de la propiedad.
     * @return El valor de la propiedad.
     */
    public static final Integer getInteger(String propertyName)
    {
        Integer intValue = GestorConfiguracion.getInteger(propertyName);

        if (logger.isDebugEnabled())
        {
            logger.debug("Obtenido el valor: " + intValue + " para la propiedad: " + propertyName);
        }

        return intValue;
    }

    /**
     * Obtiene una propiedad por su nombre. �ste metodo se utiliza como puente con la
     * antigua configuraci�n. Ser� eliminado en posteriores versiones de la aplicaci�n.
     *
     * @param propertyName El nombre de la propiedad.
     * @return La propiedad como un String.
     * @deprecated Utilizar los m�todos concretos por tipo expuestos en �sta misma clase.
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
     * Obtiene una propiedad booleana de la configuraci�n. Realiza una conversi�n de la siguiente forma:<br>
     * Devuelve true si la propiedad es el literal 'true', 'verdadero', '1', 'si'<br>
     * Devuelve false en cualquier otro caso.
     * Tener en cuenta que es case sesitive.
     *
     * @param propertyName El nombre de la propiedad a obtener.
     * @return El valor booleano convertido segun la especificaci�n del m�todo.
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

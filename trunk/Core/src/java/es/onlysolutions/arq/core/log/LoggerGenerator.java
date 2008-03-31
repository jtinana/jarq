package es.onlysolutions.arq.core.log;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Clase para la generacion de loggers en base al nombre de la aplicacion actual.
 * Permite obtener loggers distintos para las mismas clases. De esta forma se pueden incluir
 * en el mismo servidor de aplicaciones y utilizando el mismo repositorio de Log4J.
 * El nombre de los logs devueltos se componen de la siguiente forma:<br>
 * <br>
 * <br>
 * LoggerGenerator.getLogger( es.datadviser.myApp.MyClass.class )<br>
 * <br>
 * Si se ha configurado el nombre de aplicacion 'AplicacionEjemplo', la anterior ejecucion registraria un logger con el siguiente nombre:<br><br>
 * <b>AplicacionEjemplo.es.datadviser.myApp.MyClass</b><br><br>
 * De esta forma no habria colisiones si dicha clase estuviera en dos aplicaciones dentro del mismo servidor de aplicaciones,
 * compartiendo el mismo repositorio de Log4J.
 */
public class LoggerGenerator
{
    private LoggerGenerator()
    {
        // constructor privado para evitar instanciaci�n
    }

    /**
     * Obtiene un Logger a partir de la clase pasada como parametro.<br>
     *
     * @param clazz La clase a partir de la cual se creara el Logger
     * @return El Logger creado junto con el nombre de la aplicacion.
     */
    public static Log getLogger(Class clazz)
    {
        return LogFactory.getLog(clazz.getName());
    }
}

package es.onlysolutions.arq.core.mbean;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerRepository;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Implementacion del MBean para el manejo de los logger y appender de log4j.
 * Este MBean solo sirve para manejar logger generados mediante la implementacion de Log4J.
 */
public class Log4J implements Log4JMBean
{
    /**
     * Filtro inicial para los logs a mostrar.
     */
    private static String FILTRO = "es.onlysolutions";

    /**
     * Obtiene el repositorio de Log4J.
     *
     * @return El LoggerRepository actual.
     */
    private LoggerRepository getRepository()
    {
        return Logger.getRootLogger().getLoggerRepository();
    }


    /**
     * Se utilizara para obtener cuando un Logger contiene o no el filtro establecido.
     *
     * @param log El Loger del que se quiere conocer si cumple el filtro
     * @return true si lo cumple.
     */

    private boolean containsFilter(Logger log)
    {
        String name = log.getName();
        return name.startsWith(FILTRO);
    }

    /**
     * Obtiene la representacion que sera mostrada en la lista de Loggers del cliente al MBean.
     *
     * @param log El Logger del que obtener la representacion en String
     * @return El String con la representacion del Logger
     */

    private String getLoggerAsString(Logger log)
    {
        String name = log.getName();
        Level level = log.getEffectiveLevel();
        return name + " | " + level;
    }


    /**
     * Obtiene la lista de la representaci�n como String de los loggers del repositorio.
     *
     * @return Obtiene la lista de la representaci�n como String de los loggers del repositorio.
     */
    public List<String> getLoggers()
    {
        List<String> loggers = new ArrayList<String>(50);
        try
        {

            LoggerRepository repository = getRepository();

            synchronized (repository)
            {
                Enumeration e = repository.getCurrentLoggers();

                while (e.hasMoreElements())
                {
                    Logger log = (Logger) e.nextElement();
                    if (containsFilter(log))
                    {
                        loggers.add(getLoggerAsString(log));
                    }
                }
            }
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
        return loggers;
    }

    /**
     * Obtiene el filtro actual para realizar el filtro de los loggers.
     *
     * @return El fitro actual.
     */
    public String getFiltroParaLosLoggers()
    {
        return FILTRO;
    }

    /**
     * Establece un nuevo filtro a aplicar al listado de loggers.
     *
     * @param filtro El nuevo filtro a establecer.
     */
    public void setFiltroParaLosLoggers(String filtro)
    {
        FILTRO = filtro;
    }

    /**
     * Establece un nivel de trazas a los loggers de la lista seleccionada en el momento de la ejecucion.
     *
     * @param level El nivel a establecer para la lista de loggers.
     * @return El mensaje de la operacion realizada.
     */
    public String estableceNivelLoggers(String level)
    {
        LoggerRepository repository = getRepository();

        synchronized (repository)
        {
            int loggerCount = 0;
            Enumeration loggers = repository.getCurrentLoggers();
            Level resultLevel = Level.toLevel(level);
            while (loggers.hasMoreElements())
            {
                Logger log = (Logger) loggers.nextElement();
                if (containsFilter(log))
                {
                    log.setLevel(resultLevel);
                    loggerCount++;
                }
            }

            return "Se ha modificado el nivel de '" + loggerCount + "' loggers a nivel '" + resultLevel + '\'';
        }

    }

}

package es.onlysolutions.arq.core.mbean;

import java.util.List;

/**
 * Interfaz que ofrece los metodos publicos del MBean para la configuracion de los logs.
 */
public interface Log4JMBean
{
    /**
     * Obtiene todos los loggers que estan registrados actualmente en el repositorio.
     *
     * @return Un listado con la representacion de cada logger existente.
     */
    public List<String> getLoggers();

    /**
     * Obtiene el filtro actual que se aplica al listado de loggers.
     *
     * @return El filtro actual.
     */
    public String getFiltroParaLosLoggers();

    /**
     * Establece un nuevo filtro para el listado de loggers.
     *
     * @param filtro El nuevo filtro a establecer.
     */
    public void setFiltroParaLosLoggers(String filtro);

    /**
     * Establece un nuevo nivel para los loggers seleccionados en la lista.<br>
     * Si se indica un nivel incorrecto se establece a DEBUG.
     *
     * @param level El nuevo nivel a establecer.
     * @return El mensaje de la operacion realizada.
     * @see org.apache.log4j.Level#toLevel(String)
     */
    public String estableceNivelLoggers(String level);
}

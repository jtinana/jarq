package es.onlysolutions.arq.core.configuration;

import es.onlysolutions.arq.core.mbean.ConfigMBean;

/**
 * Implementacion del MBean para el gestor de configuracion.
 */
public class Config implements ConfigMBean
{
    public String resetCache()
    {
        GestorConfiguracion.resetCache();
        return "Operacion completada";
    }
}

package es.onlysolutions.arq.core.mbean;

/**
 * MBean con las operaciones para el Gestor de Configuracion.
 * <b>NOTA: En caso de configuracion en cluster, estas operaciones deben aplicarse a cada nodo por separado.</b>
 */
public interface ConfigMBean
{
    /**
     * Limpia la cache completamente de propiedades de forma que se vuelvan a leer de nuevo.
     * @return Un String con el mensaje de la operacion.
     */
    public String resetCache();
}

package es.onlysolutions.arq.core.cache;

/**
 * Interfaz de constantes para la configuracion del aspecto de Memcache.
 */
public interface IConstantsMemcache
{
    /**
     * Constante de configuracion que indica el tiempo de expiracion de los objetos puestos en Memcache.
     * Si no se especifica, se considerara el maximo tiempo permitido por el protocolo de Memcache.
     */
    public static final String EXPIRATION_TIME = "memcache.expiration";

    /**
     * Separador para la generacion de la clave de Memcache.
     */
    public static final String KEY_SEPARATOR = "_";

    /**
     * Propiedad bajo la que se declara el timeout para los accesos a la cache.
     * Se debe especificar en segundos y si no se especifica se toma por defecto 5 seg.
     */
    public static final String TIMEOUT = "memcache.timeout";
}

package es.onlysolutions.arq.core.cache.exception;

/**
 * Runtime exception lanzada por el paquete encargado de cachear objetos en Memcache.
 */
public class MemcacheException extends RuntimeException
{
    public MemcacheException(String message)
    {
        super(message);
    }

    public MemcacheException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public MemcacheException(Throwable cause)
    {
        super(cause);
    }
}

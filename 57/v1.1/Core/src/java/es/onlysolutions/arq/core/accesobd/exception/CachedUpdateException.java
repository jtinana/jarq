package es.onlysolutions.arq.core.accesobd.exception;

import java.sql.SQLException;


/**
 * Excepcion lanzada cuando se intenta actualizar un Cached Resultset.<br>
 * Esta implementacion no permite actualizaciones sobre el ResultSet.
 */
public class CachedUpdateException extends SQLException
{

    /**
     * Constructor de la CachedUpdateException.
     */
    public CachedUpdateException()
    {
        super("No es posible actualizar el valor cacheado ya que es de s�lo lectura");
    }
}


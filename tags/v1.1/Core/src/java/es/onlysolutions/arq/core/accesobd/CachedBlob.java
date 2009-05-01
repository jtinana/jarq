package es.onlysolutions.arq.core.accesobd;

import es.onlysolutions.arq.core.accesobd.exception.CachedUpdateException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;

/**
 * Implementacion de Blob que carga el blob en memoria.
 */
class CachedBlob implements Blob
{
    /**
     * Los datos del Blob.
     */
    private byte[] _data;


    /**
     * Constructor para CachedBlob.
     *
     * @param blob
     * @throws SQLException
     */
    CachedBlob(Blob blob) throws SQLException
    {
        super();
        _data = blob.getBytes(1L, (int) blob.length());
    }


    /**
     * Constructor por defecto.
     */
    CachedBlob()
    {
        super();
    }


    /**
     * length.
     *
     * @return La longitud del blob.
     * @throws SQLException
     * @see Blob#length()
     */
    public long length() throws SQLException
    {
        return (long) _data.length;
    }


    /**
     * getBytes.
     *
     * @param pos
     * @param length
     * @return Los bytes que se obtienen.
     * @throws SQLException
     * @see Blob#getBytes(long,int)
     */
    public byte[] getBytes(long pos, int length) throws SQLException
    {
        byte[] bytes = new byte[length];
        System.arraycopy(_data, (int) pos, bytes, 0, length);
        return bytes;
    }


    /**
     * getBinaryStream.
     *
     * @return Devuelve un InputStream al blob.
     * @throws SQLException
     * @see Blob#getBinaryStream()
     */
    public InputStream getBinaryStream() throws SQLException
    {
        return new ByteArrayInputStream(_data);
    }


    /**
     * getBinaryStream.
     *
     * @param pos    the offset to the first byte of the partial value to be retrieved.
     *               The first byte in the Blob is at position 1.
     * @param length the length in bytes of the partial value to be retrieved.
     * @return Devuelve un InputStream al blob.
     * @throws SQLException
     * @see Blob#getBinaryStream()
     */
    public InputStream getBinaryStream(long pos, long length) throws SQLException
    {
        return new ByteArrayInputStream(_data, new Long(pos).intValue(), new Long(length).intValue());
    }


    /**
     * position.
     *
     * @param pattern
     * @param start
     * @return Siempre una excepci�n ya que no est� soportado.
     * @throws SQLException
     * @see Blob#position(byte[],long)
     */
    public long position(byte[] pattern, long start) throws SQLException
    {
        throw new SQLException("Metodo no implementado");
    }


    /**
     * position.
     *
     * @param pattern
     * @param start
     * @return Siempre una excepci�n ya que no est� soportado.
     * @throws SQLException
     * @see Blob#position(Blob,long)
     */
    public long position(Blob pattern, long start) throws SQLException
    {
        throw new SQLException("Metodo no implementado");
    }


    /**
     * setBytes.
     *
     * @param longValue
     * @param byteValue
     * @return Siempre una excepci�n ya que no est� soportado.
     * @throws SQLException
     * @see Blob#setBytes(long,byte[])
     */
    public int setBytes(long longValue, byte[] byteValue) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * setBytes.
     *
     * @param longValue
     * @param byteValue
     * @param intValue
     * @param intAnother
     * @return Siempre una excepci�n ya que no est� soportado.
     * @throws SQLException
     * @see Blob#setBytes(long,byte[],int,int)
     */
    public int setBytes(long longValue, byte[] byteValue, int intValue, int intAnother) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * setBinaryStream.
     *
     * @param longValue
     * @return Siempre una excepci�n ya que no est� soportado.
     * @throws SQLException
     * @see Blob#setBinaryStream(long)
     */
    public OutputStream setBinaryStream(long longValue) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * truncate.
     *
     * @param longValue
     * @throws SQLException
     * @see Blob#truncate(long)
     */
	public void truncate(long longValue) throws SQLException {
		throw new CachedUpdateException();
	}
	
	
	public void free() throws SQLException {
		throw new SQLException("Metodo free no implementado");
	}
}
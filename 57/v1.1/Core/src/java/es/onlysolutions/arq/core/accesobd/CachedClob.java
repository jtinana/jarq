package es.onlysolutions.arq.core.accesobd;

import es.onlysolutions.arq.core.accesobd.exception.CachedUpdateException;

import java.io.*;
import java.sql.Clob;
import java.sql.SQLException;

/**
 * Implementaci�n de java.sql.Clob que carga el clob en memoria
 */
class CachedClob implements Clob
{
    private String _data;


    /**
     * Constructor para CachedClob.
     *
     * @param clob
     * @throws SQLException
     */
    CachedClob(Clob clob) throws SQLException
    {
        super();
        this._data = clob.getSubString(1L, (int) clob.length());
    }


    /**
     * Constructor por defecto.
     */
    CachedClob()
    {
        super();
    }


    /**
     * length.
     *
     * @return La longitud de los datos.
     * @throws SQLException
     * @see Clob#length()
     */
    public long length() throws SQLException
    {
        return (long) _data.length();
    }


    /**
     * getSubString.
     *
     * @param pos
     * @param length
     * @return El substring entre las posiciones indicadas.
     * @throws SQLException
     * @see Clob#getSubString(long,int)
     */
    public String getSubString(long pos, int length) throws SQLException
    {
        return _data.substring((int) pos, length);
    }


    /**
     * getCharacterStream.
     *
     * @return Un Reader conteniendo los datos.
     * @throws SQLException
     * @see Clob#getCharacterStream()
     */
    public Reader getCharacterStream() throws SQLException
    {
        return new StringReader(_data);
    }


    /**
     * getCharacterStream.
     *
     * @param pos    the offset to the first character of the partial value to be retrieved.
     *               The first character in the Clob is at position 1.
     * @param length the length in characters of the partial value to be retrieved.
     * @return Un Reader conteniendo los datos.
     * @throws SQLException
     * @see Clob#getCharacterStream()
     */
    public Reader getCharacterStream(long pos, long length) throws SQLException
    {
        return new StringReader(_data);
    }


    /**
     * getAsciiStream.
     *
     * @return Un InputStream conteniendo los datos.
     * @throws SQLException
     * @see Clob#getAsciiStream()
     */
    public InputStream getAsciiStream() throws SQLException
    {
        return new ByteArrayInputStream(_data.getBytes());
    }


    /**
     * position.
     *
     * @param str
     * @param fromIndex
     * @return La posicion del string indicado comenzando a contar desde la posici�n indicada.
     * @throws SQLException
     * @see Clob#position(String,long)
     */
    public long position(String str, long fromIndex) throws SQLException
    {
        return (long) _data.indexOf(str, (int) fromIndex);
    }


    /**
     * position.
     *
     * @param clob
     * @param fromIndex
     * @return La posicion del string indicado comenzando a contar desde la posici�n indicada.
     * @throws SQLException
     * @see Clob#position(Clob,long)
     */
    public long position(Clob clob, long fromIndex) throws SQLException
    {
        return position(clob.getSubString(1L, (int) clob.length()), fromIndex);
    }


    /**
     * setString.
     *
     * @param longValue
     * @param strValue
     * @return Siempre una excepci�n ya que no est� soportado.
     * @throws SQLException
     * @see Clob#setString(long,String)
     */
    public int setString(long longValue, String strValue) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * setString.
     *
     * @param longValue
     * @param strValue
     * @param intValue
     * @param intAnother
     * @return Siempre una excepci�n ya que no est� soportado.
     * @throws SQLException
     * @see Clob#setString(long,String,int,int)
     */
    public int setString(long longValue, String strValue, int intValue, int intAnother) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * setAsciiStream.
     *
     * @param longValue
     * @return Siempre una excepci�n ya que no est� soportado.
     * @throws SQLException
     * @see Clob#setAsciiStream(long)
     */
    public OutputStream setAsciiStream(long longValue) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * setCharacterStream.
     *
     * @param longValue
     * @return Siempre una excepci�n ya que no est� soportado.
     * @throws SQLException
     * @see Clob#setCharacterStream(long)
     */
    public Writer setCharacterStream(long longValue) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * truncate.
     *
     * @param longValue
     * @throws SQLException
     * @see Clob#truncate(long)
     */
	public void truncate(long longValue) throws SQLException {
		throw new CachedUpdateException();
	}
	
	
	public void free() throws SQLException {
		throw new SQLException("Metodo free no implementado");
	}
}
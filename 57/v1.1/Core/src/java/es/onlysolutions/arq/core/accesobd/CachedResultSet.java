package es.onlysolutions.arq.core.accesobd;

import es.onlysolutions.arq.core.accesobd.exception.CachedUpdateException;
import es.onlysolutions.arq.core.log.LoggerGenerator;
import org.apache.commons.logging.Log;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.sql.Date;
import java.util.*;

/**
 * Implementacion de la interfaz java.sql.ResultSet de forma que almacena los datos en memoria. De
 * esta forma se consigue un ResultSet desconectado de la conexion, y no es necesario un manejo
 * especial por parte del desarrollador. En cada ejecucion de los metodos de consulta de la clase
 * JdbcDao se obtiene un objeto de este tipo.
 */
public final class CachedResultSet implements ResultSet
{
    private static final Log logger = LoggerGenerator.getLogger(CachedResultSet.class);
    /**
     * Nemero de columnas del resultSet.
     */
    private int columnCount = -1;

    /**
     * Array ordenado con los nombres de las columnas.
     */
    private String[] columnNames;
    /**
     * Array ordenado con las clases de las columnas.
     */
    private String[] columnClassNames;
    /**
     * Map con los nombres de las columnas.
     */
    private Map columnNamesMap;
    private int lastAccessColumnIndex = -1;
    private List rows;
    private int currentRowNum;
    private int totalRowCount = -1;
    private int batchRowCount = -1;
    private int resultSetType = -1;
    private int resultSetConcurrency = -1;
    private int resultSetFetchSize = -1;
    private int resultSetFetchDirection = -1;
    private SQLWarning resultSetWarning;
    private ResultSetMetaData _rsMetadata;


    /**
     * Cachea todos los registros del resultSet.
     *
     * @param rs El resultSet a cachear
     * @throws SQLException Si ocurre algen error durante el proceso.
     */
    public CachedResultSet(ResultSet rs) throws SQLException
    {
        this(rs, 0, -1);
    }


    /**
     * Cachea un conjunto de registros de un resultSet.
     *
     * @param rs          El resultSet a cachear
     * @param startRowNum el registro de inicio
     * @param maxRows     nemero meximo de registros a cachear
     * @throws SQLException Si ocurre algen error durante el proceso.
     */
    private CachedResultSet(ResultSet rs, int startRowNum, int maxRows) throws SQLException
    {
        super();
        initMetaData(rs.getMetaData());
        this.resultSetType = ResultSet.TYPE_SCROLL_SENSITIVE;
        this.resultSetConcurrency = ResultSet.CONCUR_READ_ONLY;
        this.resultSetFetchSize = rs.getFetchSize();
        this.resultSetFetchDirection = rs.getFetchDirection();
        this.resultSetWarning = rs.getWarnings();
        this._rsMetadata = new CachedResultSetMetadata(rs.getMetaData());
        rows = new ArrayList(113);
        int rowNum = 0;
        int lastRowNum = startRowNum + maxRows;
        if (startRowNum < 0)
        {
            SQLException e = new SQLException("Invalid Start Row Number " + startRowNum);
            logger.error(e);
            throw e;
        }
        if (maxRows <= 0 && maxRows != -1)
        {
            SQLException e = new SQLException("Invalid Maximum Row Number " + maxRows);
            logger.error(e);
            throw e;
        }
        totalRowCount = 0;
        while (rs.next())
        {
            if (rowNum >= startRowNum)
            {
                if (rowNum < lastRowNum || maxRows == -1)
                {
                    Object[] row = new Object[columnCount + 1];
                    for (int i = 1; i <= columnCount; i++)
                    {
                        row[i] = getResultSetValue(rs, i);
                    }
                    rows.add(row);
                }
            }
            rowNum++;
            totalRowCount++;
        }
        batchRowCount = rows.size();
        if (logger.isDebugEnabled())
        {
            logger.debug("Inicializado CachedResultSet");
        }
    }


    /**
     * Metodo privado para obtener el objeto de una posicion.
     *
     * @param rs          El resultset.
     * @param columnIndex El numero de la columna.
     * @return El objeto de esa posicion del resulset <param>rs</param>.
     * @throws SQLException Si ocurre algen error durante el proceso.
     */
    private Object getResultSetValue(ResultSet rs, int columnIndex) throws SQLException
    {
        Object ret = null;
        Object o = rs.getObject(columnIndex);
        if (logger.isDebugEnabled())
        {
            logger.debug("rs.getObject(" + columnIndex + ") -> " + o);
        }
        if (o instanceof Clob)
        {
            Clob c = rs.getClob(columnIndex);
            if (c != null)
            {
                ret = new CachedClob(c);
            }
        }
        else if (o instanceof Blob)
        {
            Blob b = rs.getBlob(columnIndex);
            if (b != null)
            {
                ret = new CachedBlob(b);
            }
        }
        else
        {
            ret = o;
        }
        if (logger.isDebugEnabled())
        {
            logger.debug("getResultSetValue(ResultSet rs, columnIndex:" + columnIndex + " ) -> " + ret);
        }
        return ret;
    }


    /**
     * Inicializa el metadata de las columnas.
     *
     * @param metadata
     * @throws SQLException Si ocurre algen error durante el proceso.
     */
    private void initMetaData(ResultSetMetaData metadata) throws SQLException
    {
        columnCount = metadata.getColumnCount();
        columnNames = new String[columnCount];
        columnClassNames = new String[columnCount];
        columnNamesMap = new HashMap(columnCount << 1);
        String columnName;
        String columnClassName;
        for (int i = 0; i < columnCount; i++)
        {
            columnName = metadata.getColumnName(i + 1);
            columnClassName = metadata.getColumnClassName(i + 1);
            columnNames[i] = columnName.toUpperCase();
            columnClassNames[i] = columnClassName;
            columnNamesMap.put(columnNames[i], new Integer(i + 1));
        }
    }


    /**
     * Metodo next del ResultSet.
     *
     * @return true si hay mas elementos en el resultset.
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#next()
     */
    public boolean next() throws SQLException
    {
        currentRowNum++;
        return currentRowNum <= this.batchRowCount;
    }


    /**
     * Este metodo <b>no</b> cierra el resultSet asociado, y lanza siempre una excepcion. No
     * realiza ninguna accion, ya que el ResultSet no tiene ninguna conexion asociada.
     *
     * @throws SQLException Siempre, ya que no tiene sentido la ejecucion de este metodo.
     * @see ResultSet#close()
     */
    public void close() throws SQLException
    {
        throw new SQLException("Metodo no necesario");
    }


    /**
     * Metodo wasNull del ResultSet.
     *
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz.
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#wasNull()
     */
    public boolean wasNull() throws SQLException
    {
        Object o = getObject(lastAccessColumnIndex);
        return o == null;
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnIndex
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getString(int)
     */
    public String getString(int columnIndex) throws SQLException
    {
        try
        {
            return (String) getObject(columnIndex);
        }
        catch (ClassCastException ex)
        {
            Exception e1 = new SQLException("Column  " + columnIndex + " is not of type java.lang.String. The correct type is: " + getObject(columnIndex).getClass().getName());
            e1.setStackTrace(ex.getStackTrace());
            throw (SQLException) e1;
        }
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnIndex El indice de la columna.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz..
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getBoolean(int)
     */
    public boolean getBoolean(int columnIndex) throws SQLException
    {
        boolean ret;
        try
        {
            Object o = getObject(columnIndex);
            if (o != null)
            {
                ret = ((Boolean) o).booleanValue();
            }
            else
            {
                ret = false;
            }
        }
        catch (ClassCastException ex)
        {
            Exception e1 = new SQLException("Column  " + columnIndex + " is not of type java.lang.Boolean The correct type is: " + getObject(columnIndex).getClass().getName());
            e1.setStackTrace(ex.getStackTrace());
            throw (SQLException) e1;
        }
        return ret;
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnIndex El indice de la columna.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz.
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getByte(int)
     */
    public byte getByte(int columnIndex) throws SQLException
    {
        byte ret;
        try
        {
            Object o = getObject(columnIndex);
            if (o != null)
            {
                ret = ((Byte) o).byteValue();
            }
            else
            {
                ret = (byte) 0;
            }
        }
        catch (ClassCastException ex)
        {
            Exception e1 = new SQLException("Column  " + columnIndex + " is not of type java.lang.Byte. The correct type is: " + getObject(columnIndex).getClass().getName());
            e1.setStackTrace(ex.getStackTrace());
            throw (SQLException) e1;
        }
        return ret;
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnIndex El endice de la columna.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getShort(int)
     */
    public short getShort(int columnIndex) throws SQLException
    {
        short ret;
        try
        {
            Object o = getObject(columnIndex);
            if (o == null)
            {
                ret = (short) 0;
            }
            else
            {
                if (o instanceof Number)
                {
                    ret = ((Number) o).shortValue();
                }
                else
                {
                    ret = Short.parseShort(o.toString());
                }
            }
        }
        catch (NumberFormatException ex)
        {
            Exception e1 = new SQLException("Column  " + columnIndex + " is not of type java.lang.Short " + "or could not be converted to a java.lang.Integer. The correct type is: " + getObject(columnIndex).getClass().getName());
            e1.setStackTrace(ex.getStackTrace());
            throw (SQLException) e1;
        }
        return ret;
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnIndex El indice de la columna.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz.
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getInt(int)
     */
    public int getInt(int columnIndex) throws SQLException
    {
        int ret;
        try
        {
            Object o = getObject(columnIndex);
            if (o == null)
            {
                ret = 0;
            }
            else
            {
                if (o instanceof Number)
                {
                    ret = ((Number) o).intValue();
                }
                else
                {
                    ret = Integer.parseInt(o.toString());
                }
            }
        }
        catch (NumberFormatException ex)
        {
            Exception e1 = new SQLException("Column  " + columnIndex + " is not of type java.lang.Integer " + "or could not be converted to a java.lang.Integer. The correct type is: " + getObject(columnIndex).getClass().getName());
            e1.setStackTrace(ex.getStackTrace());
            throw (SQLException) e1;
        }
        return ret;
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnIndex El indice de la columna.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getLong(int)
     */
    public long getLong(int columnIndex) throws SQLException
    {
        long ret;
        try
        {
            Object o = getObject(columnIndex);
            if (o == null)
            {
                ret = 0L;
            }
            else
            {
                if (o instanceof Number)
                {
                    ret = ((Number) o).longValue();
                }
                else
                {
                    ret = Long.parseLong(o.toString());
                }
            }
        }
        catch (NumberFormatException ex)
        {
            Exception e1 = new SQLException("Column  " + columnIndex + " is not of type java.lang.Long " + "or could not be converted to a java.lang.Long. The correct type is: " + getObject(columnIndex).getClass().getName());
            e1.setStackTrace(ex.getStackTrace());
            throw (SQLException) e1;
        }
        return ret;
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnIndex El indice de la columna.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getFloat(int)
     */
    public float getFloat(int columnIndex) throws SQLException
    {
        float ret;
        try
        {
            Object o = getObject(columnIndex);
            if (o == null)
            {
                ret = 0.0F;
            }
            else
            {
                if (o instanceof Number)
                {
                    ret = ((Number) o).floatValue();
                }
                else
                {
                    ret = Float.parseFloat(o.toString());
                }
            }
        }
        catch (NumberFormatException ex)
        {
            Exception e1 = new SQLException("Column  " + columnIndex + " is not of type java.lang.Float " + "or could not be converted to a java.lang.Float. The correct type is: " + getObject(columnIndex).getClass().getName());
            e1.setStackTrace(ex.getStackTrace());
            throw (SQLException) e1;
        }
        return ret;
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnIndex El indice de la columna.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getDouble(int)
     */
    public double getDouble(int columnIndex) throws SQLException
    {
        double ret;
        try
        {
            Object o = getObject(columnIndex);
            if (o == null)
            {
                ret = 0.0;
            }
            else
            {
                if (o instanceof Number)
                {
                    ret = ((Number) o).doubleValue();
                }
                else
                {
                    ret = Double.parseDouble(o.toString());
                }
            }
        }
        catch (NumberFormatException ex)
        {
            Exception e1 = new SQLException("Column  " + columnIndex + " is not of type java.lang.Double " + "or could not be converted to a java.lang.Double. The correct type is: " + getObject(columnIndex).getClass().getName());
            e1.setStackTrace(ex.getStackTrace());
            throw (SQLException) e1;
        }
        return ret;
    }


    /**
     * No implementado.
     *
     * @param columnIndex
     * @param scale
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getBigDecimal(int,int)
     */
    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException
    {
        methodNotImplementedEx();
        return null;
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnIndex El indice de la columna.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getBytes(int)
     */
    public byte[] getBytes(int columnIndex) throws SQLException
    {
        try
        {
            return (byte[]) getObject(columnIndex);
        }
        catch (ClassCastException ex)
        {
            Exception e1 = new SQLException("Column  " + columnIndex + " is not of type byte []. The correct type is: " + getObject(columnIndex).getClass().getName());
            e1.setStackTrace(ex.getStackTrace());
            throw (SQLException) e1;
        }
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnIndex El indice de la columna.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getDate(int)
     */
    public Date getDate(int columnIndex) throws SQLException
    {
        try
        {
            return (Date) getObject(columnIndex);
        }
        catch (ClassCastException ex)
        {
            Exception e1 = new SQLException("Column  " + columnIndex + " is not of type java.conexion.Date. The correct type is: " + getObject(columnIndex).getClass().getName());
            e1.setStackTrace(ex.getStackTrace());
            throw (SQLException) e1;
        }
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnIndex El indice de la columna.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getTime(int)
     */
    public Time getTime(int columnIndex) throws SQLException
    {
        try
        {
            return (Time) getObject(columnIndex);
        }
        catch (ClassCastException ex)
        {
            Exception e1 = new SQLException("Column  " + columnIndex + " is not of type java.conexion.Time. The correct type is: " + getObject(columnIndex).getClass().getName());
            e1.setStackTrace(ex.getStackTrace());
            throw (SQLException) e1;
        }
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnIndex El indice de la columna.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getTimestamp(int)
     */
    public Timestamp getTimestamp(int columnIndex) throws SQLException
    {
        try
        {
            return (Timestamp) getObject(columnIndex);
        }
        catch (ClassCastException ex)
        {
            Exception e1 = new SQLException("Column  " + columnIndex + " is not of type java.conexion.Timestamp. The correct type is: " + getObject(columnIndex).getClass().getName());
            e1.setStackTrace(ex.getStackTrace());
            throw (SQLException) e1;
        }
    }


    /**
     * No implementado.
     *
     * @param columnIndex
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getAsciiStream(int)
     */
    public InputStream getAsciiStream(int columnIndex) throws SQLException
    {
        methodNotImplementedEx();
        return null;
    }


    /**
     * No implementado.
     *
     * @param columnIndex
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getUnicodeStream(int)
     */
    public InputStream getUnicodeStream(int columnIndex) throws SQLException
    {
        methodNotImplementedEx();
        return null;
    }


    /**
     * No implementado.
     *
     * @param columnIndex
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getBinaryStream(int)
     */
    public InputStream getBinaryStream(int columnIndex) throws SQLException
    {
        methodNotImplementedEx();
        return null;
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnName El nombre de la columna.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getString(String)
     */
    public String getString(String columnName) throws SQLException
    {
        return getString(findColumn(columnName));
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnName El nombre de la columna.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getBoolean(String)
     */
    public boolean getBoolean(String columnName) throws SQLException
    {
        return getBoolean(findColumn(columnName));
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnName El nombre de la columna.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getByte(String)
     */
    public byte getByte(String columnName) throws SQLException
    {
        return getByte(findColumn(columnName));
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnName El nombre de la columna.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getShort(String)
     */
    public short getShort(String columnName) throws SQLException
    {
        return getShort(findColumn(columnName));
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnName El nombre de la columna.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getInt(String)
     */
    public int getInt(String columnName) throws SQLException
    {
        return getInt(findColumn(columnName));
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnName El nombre de la columna.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getLong(String)
     */
    public long getLong(String columnName) throws SQLException
    {
        return getLong(findColumn(columnName));
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnName El nombre de la columna.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getFloat(String)
     */
    public float getFloat(String columnName) throws SQLException
    {
        return getFloat(findColumn(columnName));
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnName El nombre de la columna.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getDouble(String)
     */
    public double getDouble(String columnName) throws SQLException
    {
        return getDouble(findColumn(columnName));
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnName El nombre de la columna.
     * @param scale      El nemero de degitos a la derecha del punto decimal.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getBigDecimal(String,int)
     * @deprecated
     */
    public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException
    {
        return getBigDecimal(findColumn(columnName), scale);
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnName El nombre de la columna.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getBytes(String)
     */
    public byte[] getBytes(String columnName) throws SQLException
    {
        return getBytes(findColumn(columnName));
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnName El nombre de la columna.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getDate(String)
     */
    public Date getDate(String columnName) throws SQLException
    {
        return getDate(findColumn(columnName));
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnName El nombre de la columna.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getTime(String)
     */
    public Time getTime(String columnName) throws SQLException
    {
        return getTime(findColumn(columnName));
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnName El nombre de la columna.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getTimestamp(String)
     */
    public Timestamp getTimestamp(String columnName) throws SQLException
    {
        return getTimestamp(findColumn(columnName));
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnName El nombre de la columna.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getAsciiStream(String)
     */
    public InputStream getAsciiStream(String columnName) throws SQLException
    {
        return getAsciiStream(findColumn(columnName));
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnName El nombre de la columna.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getUnicodeStream(String)
     * @deprecated
     */
    public InputStream getUnicodeStream(String columnName) throws SQLException
    {
        return getUnicodeStream(findColumn(columnName));
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnName El nombre de la columna.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getBinaryStream(String)
     */
    public InputStream getBinaryStream(String columnName) throws SQLException
    {
        return getBinaryStream(findColumn(columnName));
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getWarnings()
     */
    public SQLWarning getWarnings() throws SQLException
    {
        return resultSetWarning;
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#clearWarnings()
     */
    public void clearWarnings() throws SQLException
    {
        resultSetWarning = null;
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz.
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getCursorName()
     */
    public String getCursorName() throws SQLException
    {
        methodNotImplementedEx();
        return null;
    }


    /**
     * No implementado.
     *
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getMetaData()
     */
    public ResultSetMetaData getMetaData() throws SQLException
    {
        return _rsMetadata;
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnIndex El indice de la columna.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getObject(int)
     */
    public Object getObject(int columnIndex) throws SQLException
    {
        if (isBeforeFirst() || isAfterLast())
        {
            throw new SQLException("Row " + currentRowNum + " not found in result set");
        }
        Object[] rowData = (Object[]) rows.get(currentRowNum - 1);
        // Set last access column index for WasNull method
        lastAccessColumnIndex = columnIndex;
        if (columnIndex > rowData.length)
        {
            throw new SQLException("Column Index " + columnIndex + " not found in result set");
        }
        return rowData[columnIndex];
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnName El nombre de la columna.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getObject(String)
     */
    public Object getObject(String columnName) throws SQLException
    {
        return getObject(findColumn(columnName));
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnName El nombre de la columna.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#findColumn(String)
     */
    public int findColumn(String columnName) throws SQLException
    {
        Integer i = (Integer) columnNamesMap.get(columnName.toUpperCase());
        if (i == null)
        {
            throw new SQLException("Column  " + columnName + " not found in result set");
        }
        return i.intValue();
    }


    /**
     * No implementado.
     *
     * @param columnIndex
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getCharacterStream(int)
     */
    public Reader getCharacterStream(int columnIndex) throws SQLException
    {
        methodNotImplementedEx();
        return null;
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnName El nombre de la columna.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getCharacterStream(String)
     */
    public Reader getCharacterStream(String columnName) throws SQLException
    {
        return getCharacterStream(findColumn(columnName));
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnIndex El indice de la columna.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getBigDecimal(int)
     */
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException
    {
        try
        {
            return (BigDecimal) getObject(columnIndex);
        }
        catch (ClassCastException ex)
        {
            Exception e1 = new SQLException("Column  " + columnIndex + " is not of type java.math.BigDecimal. " + "The correct type is: " + getObject(columnIndex).getClass().getName());
            e1.setStackTrace(ex.getStackTrace());
            throw (SQLException) e1;
        }
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnName El nombre de la columna.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getBigDecimal(String)
     */
    public BigDecimal getBigDecimal(String columnName) throws SQLException
    {
        return getBigDecimal(findColumn(columnName));
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#isBeforeFirst()
     */
    public boolean isBeforeFirst() throws SQLException
    {
        return currentRowNum < 1;
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#isAfterLast()
     */
    public boolean isAfterLast() throws SQLException
    {
        return currentRowNum > this.batchRowCount;
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#isFirst()
     */
    public boolean isFirst() throws SQLException
    {
        return currentRowNum == 0;
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#isLast()
     */
    public boolean isLast() throws SQLException
    {
        return currentRowNum == this.batchRowCount;
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#beforeFirst()
     */
    public void beforeFirst() throws SQLException
    {
        currentRowNum = -1;
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#afterLast()
     */
    public void afterLast() throws SQLException
    {
        currentRowNum = this.batchRowCount + 1;
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#first()
     */
    public boolean first() throws SQLException
    {
        currentRowNum = 0;
        return true;
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#last()
     */
    public boolean last() throws SQLException
    {
        currentRowNum = this.batchRowCount;
        return true;
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getRow()
     */
    public int getRow() throws SQLException
    {
        return currentRowNum;
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param row El indice de la columna.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#absolute(int)
     */
    public boolean absolute(int row) throws SQLException
    {
        currentRowNum = row;
        return true;
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param nRows El numero de rows a aeadir.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#relative(int)
     */
    public boolean relative(int nRows) throws SQLException
    {
        currentRowNum += nRows;
        return true;
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#previous()
     */
    public boolean previous() throws SQLException
    {
        currentRowNum--;
        return true;
    }


    /**
     * No implementado.
     *
     * @param columnIndex
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#setFetchDirection(int)
     */
    public void setFetchDirection(int columnIndex) throws SQLException
    {
        methodNotImplementedEx();
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getFetchDirection()
     */
    public int getFetchDirection() throws SQLException
    {
        return resultSetFetchDirection;
    }


    /**
     * No implementado.
     *
     * @param columnIndex
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#setFetchSize(int)
     */
    public void setFetchSize(int columnIndex) throws SQLException
    {
        methodNotImplementedEx();
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getFetchSize()
     */
    public int getFetchSize() throws SQLException
    {
        return resultSetFetchSize;
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getType()
     */
    public int getType() throws SQLException
    {
        return resultSetType;
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getConcurrency()
     */
    public int getConcurrency() throws SQLException
    {
        return resultSetConcurrency;
    }


    /**
     * No implementado.
     *
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#rowUpdated()
     */
    public boolean rowUpdated() throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#rowInserted()
     */
    public boolean rowInserted() throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#rowDeleted()
     */
    public boolean rowDeleted() throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @param columnIndex
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#updateNull(int)
     */
    public void updateNull(int columnIndex) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @param columnIndex
     * @param x
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#updateBoolean(int,boolean)
     */
    public void updateBoolean(int columnIndex, boolean x) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @param columnIndex
     * @param x
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#updateByte(int,byte)
     */
    public void updateByte(int columnIndex, byte x) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @param columnIndex
     * @param x
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#updateShort(int,short)
     */
    public void updateShort(int columnIndex, short x) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @param columnIndex
     * @param x
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#updateInt(int,int)
     */
    public void updateInt(int columnIndex, int x) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @param columnIndex
     * @param x
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#updateLong(int,long)
     */
    public void updateLong(int columnIndex, long x) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @param columnIndex
     * @param x
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#updateFloat(int,float)
     */
    public void updateFloat(int columnIndex, float x) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @param columnIndex
     * @param x
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#updateDouble(int,double)
     */
    public void updateDouble(int columnIndex, double x) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @param columnIndex
     * @param x
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#updateBigDecimal(int,BigDecimal)
     */
    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @param columnIndex
     * @param x
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#updateString(int,String)
     */
    public void updateString(int columnIndex, String x) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @param columnIndex
     * @param x
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#updateBytes(int,byte[])
     */
    public void updateBytes(int columnIndex, byte[] x) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @param columnIndex
     * @param x
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#updateDate(int,Date)
     */
    public void updateDate(int columnIndex, Date x) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @param columnIndex
     * @param x
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#updateTime(int,Time)
     */
    public void updateTime(int columnIndex, Time x) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @param columnIndex
     * @param x
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#updateTimestamp(int,Timestamp)
     */
    public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @param columnIndex
     * @param x
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#updateAsciiStream(int,InputStream,int)
     */
    public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException
    {
        throw new CachedUpdateException();
    }

    public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateCharacterStream(int columnIndex, Reader x) throws SQLException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateClob(int columnIndex, Reader reader) throws SQLException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateClob(String columnLabel, Reader reader) throws SQLException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateNClob(int columnIndex, Reader reader) throws SQLException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateNClob(String columnLabel, Reader reader) throws SQLException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    /**
     * No implementado.
     *
     * @param columnIndex
     * @param x
     * @param length
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#updateAsciiStream(int,InputStream,int)
     */
    public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @param columnIndex
     * @param x
     * @param length
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#updateBinaryStream(int,InputStream,int)
     */
    public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @param columnIndex
     * @param x
     * @param length
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#updateCharacterStream(int,Reader,int)
     */
    public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @param columnIndex
     * @param x
     * @param scale
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#updateObject(int,Object,int)
     */
    public void updateObject(int columnIndex, Object x, int scale) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @param columnIndex
     * @param x
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#updateObject(int,Object)
     */
    public void updateObject(int columnIndex, Object x) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @param columnName
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#updateNull(String)
     */
    public void updateNull(String columnName) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @param columnName
     * @param x
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#updateBoolean(String,boolean)
     */
    public void updateBoolean(String columnName, boolean x) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @param columnName
     * @param x
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#updateByte(String,byte)
     */
    public void updateByte(String columnName, byte x) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @param columnName
     * @param x
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#updateShort(String,short)
     */
    public void updateShort(String columnName, short x) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @param columnName
     * @param x
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#updateInt(String,int)
     */
    public void updateInt(String columnName, int x) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @param columnName
     * @param x
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#updateLong(String,long)
     */
    public void updateLong(String columnName, long x) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @param columnName
     * @param x
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#updateFloat(String,float)
     */
    public void updateFloat(String columnName, float x) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @param columnName
     * @param x
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#updateDouble(String,double)
     */
    public void updateDouble(String columnName, double x) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @param columnName
     * @param x
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#updateBigDecimal(String,BigDecimal)
     */
    public void updateBigDecimal(String columnName, BigDecimal x) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @param columnName
     * @param x
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#updateString(String,String)
     */
    public void updateString(String columnName, String x) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @param columnName
     * @param x
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#updateBytes(String,byte[])
     */
    public void updateBytes(String columnName, byte[] x) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @param columnName
     * @param x
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#updateDate(String,Date)
     */
    public void updateDate(String columnName, Date x) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @param columnName
     * @param x
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#updateTime(String,Time)
     */
    public void updateTime(String columnName, Time x) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @param columnName
     * @param x
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#updateTimestamp(String,Timestamp)
     */
    public void updateTimestamp(String columnName, Timestamp x) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @param columnName
     * @param x
     * @param length
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#updateAsciiStream(String,InputStream,int)
     */
    public void updateAsciiStream(String columnName, InputStream x, int length) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @param columnName
     * @param x
     * @param length
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#updateBinaryStream(String,InputStream,int)
     */
    public void updateBinaryStream(String columnName, InputStream x, int length) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @param columnName
     * @param x
     * @param length
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#updateCharacterStream(String,Reader,int)
     */
    public void updateCharacterStream(String columnName, Reader x, int length) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @param columnName
     * @param x
     * @param scale
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#updateObject(String,Object,int)
     */
    public void updateObject(String columnName, Object x, int scale) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @param columnName
     * @param x
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#updateObject(String,Object)
     */
    public void updateObject(String columnName, Object x) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#insertRow()
     */
    public void insertRow() throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#updateRow()
     */
    public void updateRow() throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#deleteRow()
     */
    public void deleteRow() throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#refreshRow()
     */
    public void refreshRow() throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#cancelRowUpdates()
     */
    public void cancelRowUpdates() throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#moveToInsertRow()
     */
    public void moveToInsertRow() throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#moveToCurrentRow()
     */
    public void moveToCurrentRow() throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz El objeto
     *         Statement
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getStatement()
     */
    public Statement getStatement() throws SQLException
    {
        throw new SQLException("Metodo no implementado");
    }


    /**
     * No implementado.
     *
     * @param columnIndex
     * @param map
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz El objeto a
     *         obtener
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getObject(int,Map)
     */
    public Object getObject(int columnIndex, Map map) throws SQLException
    {
        methodNotImplementedEx();
        return null;
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnIndex El indice de la columna.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz El objeto
     *         Ref
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getRef(int)
     */
    public Ref getRef(int columnIndex) throws SQLException
    {
        try
        {
            return (Ref) getObject(columnIndex);
        }
        catch (ClassCastException ex)
        {
            Exception e1 = new SQLException("Column  " + columnIndex + " is not of type java.conexion.Ref. The correct type is: " + getObject(columnIndex).getClass().getName());
            e1.setStackTrace(ex.getStackTrace());
            throw (SQLException) e1;
        }
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnIndex El indice de la columna.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz El objeto
     *         java.sql.Blob
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getBlob(int)
     */
    public Blob getBlob(int columnIndex) throws SQLException
    {
        try
        {
            return (Blob) getObject(columnIndex);
        }
        catch (ClassCastException ex)
        {
            Exception e1 = new SQLException("Column  " + columnIndex + " is not of type java.conexion.Blob. The correct type is: " + getObject(columnIndex).getClass().getName());
            e1.setStackTrace(ex.getStackTrace());
            throw (SQLException) e1;
        }
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnIndex El indice de la columna.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz El objeto
     *         java.sql.Clob
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getClob(int)
     */
    public Clob getClob(int columnIndex) throws SQLException
    {
        try
        {
            return (Clob) getObject(columnIndex);
        }
        catch (ClassCastException ex)
        {
            Exception e1 = new SQLException("Column  " + columnIndex + " is not of type java.conexion.Clob. The correct type is: " + getObject(columnIndex).getClass().getName());
            e1.setStackTrace(ex.getStackTrace());
            throw (SQLException) e1;
        }
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnIndex El indice de la columna.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz El objeto
     *         Array
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getArray(int)
     */
    public Array getArray(int columnIndex) throws SQLException
    {
        try
        {
            return (Array) getObject(columnIndex);
        }
        catch (ClassCastException ex)
        {
            Exception e1 = new SQLException("Column  " + columnIndex + " is not of type java.conexion.Array. The correct type is: " + getObject(columnIndex).getClass().getName());
            e1.setStackTrace(ex.getStackTrace());
            throw (SQLException) e1;
        }
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz Devuelve los
     *         rows del ResultSet.
     */
    public List getRows()
    {
        return rows;
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnName El nombre de la columna.
     * @param map        El mapa con los mappings de nombres de clases con los tipos SQL.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz.
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getObject(String,Map)
     */
    public Object getObject(String columnName, Map map) throws SQLException
    {
        return getObject(findColumn(columnName), map);
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnName El nombre de la columna.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getRef(String)
     */
    public Ref getRef(String columnName) throws SQLException
    {
        return getRef(findColumn(columnName));
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnName El nombre de la columna.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getBlob(String)
     */
    public Blob getBlob(String columnName) throws SQLException
    {
        return getBlob(findColumn(columnName));
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnName El nombre de la columna.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz El objeto
     *         Clob
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getClob(String)
     */
    public Clob getClob(String columnName) throws SQLException
    {
        return getClob(findColumn(columnName));
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnName El nombre de la columna.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz El objeto
     *         Array
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getArray(String)
     */
    public Array getArray(String columnName) throws SQLException
    {
        return getArray(findColumn(columnName));
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnIndex El indice de la columna.
     * @param cal         El calendario para coonstruir la fecha.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz El objeto
     *         java.sql.Date
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getDate(int,Calendar)
     */
    public Date getDate(int columnIndex, Calendar cal) throws SQLException
    {
        Date d = getDate(columnIndex);
        if (d != null)
        {
            cal.setTime(d);
            d = new Date(cal.getTimeInMillis());
        }
        return d;
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnName El nombre de la columna.
     * @param cal        El calendario a partir del cual construir la fecha.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz El objeto
     *         java.sql.Date
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getDate(String,Calendar)
     */
    public Date getDate(String columnName, Calendar cal) throws SQLException
    {
        return getDate(findColumn(columnName), cal);
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnIndex El indice de la columna.
     * @param cal         El calendario a partir del cual construir la fecha.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz El objeto
     *         java.sql.Time
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getTime(int,Calendar)
     */
    public Time getTime(int columnIndex, Calendar cal) throws SQLException
    {
        Time t = getTime(columnIndex);
        if (t != null)
        {
            cal.setTime(t);
            t = new Time(cal.getTimeInMillis());
        }
        return t;
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnName El nombre de la columna.
     * @param cal        El calendario a partir del cual construir la fecha.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz El objeto
     *         java.sql.Time
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getTime(String,Calendar)
     */
    public Time getTime(String columnName, Calendar cal) throws SQLException
    {
        return getTime(findColumn(columnName), cal);
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnIndex El indice de la columna.
     * @param cal         El calendario a utilizar para constuir la fecha.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz El objeto
     *         java.sql.Timestamp
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getTimestamp(int,Calendar)
     */
    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException
    {
        throw new SQLException("Metodo no implementado");
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnName El nombre de la columna.
     * @param cal        El calendario a utilizar para construir la fecha.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz El objeto
     *         java.sql.Timestamp
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getTimestamp(String,Calendar)
     */
    public Timestamp getTimestamp(String columnName, Calendar cal) throws SQLException
    {
        return getTimestamp(findColumn(columnName), cal);
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnIndex El indice de la columna.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz El objeto
     *         URL
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getURL(int)
     */
    public URL getURL(int columnIndex) throws SQLException
    {
        try
        {
            return (URL) getObject(columnIndex);
        }
        catch (ClassCastException ex)
        {
            Exception e1 = new SQLException("Column  " + columnIndex + " is not of type java.net.URL. The correct type is: " + getObject(columnIndex).getClass().getName());
            e1.setStackTrace(ex.getStackTrace());
            throw (SQLException) e1;
        }
    }


    /**
     * Metodo que cumple con el interfaz ResultSet.
     *
     * @param columnName El nombre de la columna.
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz El objeto
     *         URL
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#getURL(String)
     */
    public URL getURL(String columnName) throws SQLException
    {
        return getURL(findColumn(columnName));
    }


    /**
     * No implementado.
     *
     * @param columnIndex
     * @param x
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#updateRef(int,Ref)
     */
    public void updateRef(int columnIndex, Ref x) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @param columnName
     * @param x
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#updateRef(String,Ref)
     */
    public void updateRef(String columnName, Ref x) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @param columnIndex
     * @param x
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#updateBlob(int,Blob)
     */
    public void updateBlob(int columnIndex, Blob x) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @param columnName
     * @param x
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#updateBlob(String,Blob)
     */
    public void updateBlob(String columnName, Blob x) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @param columnIndex
     * @param inputStream
     * @param length
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#updateBlob(String,Blob)
     */
    public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException
    {
        throw new CachedUpdateException();
    }

    public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateClob(int columnIndex, Reader reader, long length) throws SQLException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateClob(String columnLabel, Reader reader, long length) throws SQLException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    /**
     * No implementado.
     *
     * @param columnIndex
     * @param x
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#updateClob(int,Clob)
     */
    public void updateClob(int columnIndex, Clob x) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @param columnName
     * @param x
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#updateClob(String,Clob)
     */
    public void updateClob(String columnName, Clob x) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @param columnIndex
     * @param x
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#updateArray(int,Array)
     */
    public void updateArray(int columnIndex, Array x) throws SQLException
    {
        throw new CachedUpdateException();
    }


    /**
     * No implementado.
     *
     * @param columnName
     * @param x
     * @throws SQLException Si ocurre algen error durante el proceso.
     * @see ResultSet#updateArray(String,Array)
     */
    public void updateArray(String columnName, Array x) throws SQLException
    {
        throw new CachedUpdateException();
    }

    public RowId getRowId(int columnIndex) throws SQLException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public RowId getRowId(String columnLabel) throws SQLException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateRowId(int columnIndex, RowId x) throws SQLException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateRowId(String columnLabel, RowId x) throws SQLException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getHoldability() throws SQLException
    {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isClosed() throws SQLException
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateNString(int columnIndex, String nString) throws SQLException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateNString(String columnLabel, String nString) throws SQLException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateNClob(int columnIndex, NClob nClob) throws SQLException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateNClob(String columnLabel, NClob nClob) throws SQLException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public NClob getNClob(int columnIndex) throws SQLException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public NClob getNClob(String columnLabel) throws SQLException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public SQLXML getSQLXML(int columnIndex) throws SQLException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public SQLXML getSQLXML(String columnLabel) throws SQLException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getNString(int columnIndex) throws SQLException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getNString(String columnLabel) throws SQLException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Reader getNCharacterStream(int columnIndex) throws SQLException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Reader getNCharacterStream(String columnLabel) throws SQLException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    /**
     * Always throws a SQLException indicating that a method is No implementado.
     *
     * @throws SQLException Si ocurre algen error durante el proceso.
     */
    private void methodNotImplementedEx() throws SQLException
    {
        throw new SQLException("Metodo no implementado.");
    }


    /**
     * Obtiene el nombre de la clase de la columna.
     *
     * @param columnIndex
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz String
     */
    private String getColumnClassName(int columnIndex)
    {
        return columnClassNames[columnIndex - 1];
    }


    /**
     * Obtiene el nombre de la clase de la columna.
     *
     * @param columnName
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz String
     * @throws SQLException Si ocurre algen error durante el proceso.
     */
    private String getColumnClassName(String columnName) throws SQLException
    {
        return getColumnClassName(findColumn(columnName));
    }


    /**
     * Devuelve el nemero de registros del resultSet.
     *
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz int
     */
    public int getTotalRowCount()
    {
        return totalRowCount;
    }


    /**
     * Devuelve el nemero de registros cacheados del resultSet.
     *
     * @return El resultado de la ejecucien del metodo segen la definicien del interfaz int
     */
    public int getBatchRowCount()
    {
        return batchRowCount;
    }

//	public int getHoldability() throws SQLException {

//		return 0;
//	}
//
//
//	public Reader getNCharacterStream(int arg0) throws SQLException {
//
//		return null;
//	}
//
//
//	public Reader getNCharacterStream(String arg0) throws SQLException {
//
//		return null;
//	}
//
//
//	public NClob getNClob(int arg0) throws SQLException {
//
//		return null;
//	}
//
//
//	public NClob getNClob(String arg0) throws SQLException {
//
//		return null;
//	}
//
//
//	public String getNString(int arg0) throws SQLException {
//
//		return null;
//	}
//
//
//	public String getNString(String arg0) throws SQLException {
//
//		return null;
//	}
//
//
//	public RowId getRowId(int arg0) throws SQLException {
//
//		return null;
//	}
//
//
//	public RowId getRowId(String arg0) throws SQLException {
//
//		return null;
//	}
//
//
//	public SQLXML getSQLXML(int arg0) throws SQLException {
//
//		return null;
//	}
//
//
//	public SQLXML getSQLXML(String arg0) throws SQLException {
//
//		return null;
//	}
//
//
//	public boolean isClosed() throws SQLException {
//
//		return false;
//	}
//
//
//	public void updateAsciiStream(String arg0, InputStream arg1) throws SQLException {
//
//
//	}
//
//
//	public void updateAsciiStream(int arg0, InputStream arg1, long arg2) throws SQLException {
//
//
//	}
//
//
//	public void updateAsciiStream(String arg0, InputStream arg1, long arg2) throws SQLException {
//
//
//	}
//
//
//	public void updateBinaryStream(int arg0, InputStream arg1) throws SQLException {
//
//
//	}
//
//
//	public void updateBinaryStream(String arg0, InputStream arg1) throws SQLException {
//
//
//	}
//
//
//	public void updateBinaryStream(int arg0, InputStream arg1, long arg2) throws SQLException {
//
//
//	}
//
//
//	public void updateBinaryStream(String arg0, InputStream arg1, long arg2) throws SQLException {
//
//
//	}
//
//
//	public void updateBlob(int arg0, InputStream arg1) throws SQLException {
//
//
//	}
//
//
//	public void updateBlob(String arg0, InputStream arg1) throws SQLException {
//
//
//	}
//
//
//	public void updateBlob(String arg0, InputStream arg1, long arg2) throws SQLException {
//
//
//	}
//
//
//	public void updateCharacterStream(int arg0, Reader arg1) throws SQLException {
//
//
//	}
//
//
//	public void updateCharacterStream(String arg0, Reader arg1) throws SQLException {
//
//
//	}
//
//
//	public void updateCharacterStream(int arg0, Reader arg1, long arg2) throws SQLException {
//
//
//	}
//
//
//	public void updateCharacterStream(String arg0, Reader arg1, long arg2) throws SQLException {
//
//
//	}
//
//
//	public void updateClob(int arg0, Reader arg1) throws SQLException {
//
//
//	}
//
//
//	public void updateClob(String arg0, Reader arg1) throws SQLException {
//
//
//	}
//
//
//	public void updateClob(int arg0, Reader arg1, long arg2) throws SQLException {
//
//
//	}
//
//
//	public void updateClob(String arg0, Reader arg1, long arg2) throws SQLException {
//
//
//	}
//
//
//	public void updateNCharacterStream(int arg0, Reader arg1) throws SQLException {
//
//
//	}
//
//
//	public void updateNCharacterStream(String arg0, Reader arg1) throws SQLException {
//
//
//	}
//
//
//	public void updateNCharacterStream(int arg0, Reader arg1, long arg2) throws SQLException {
//
//
//	}
//
//
//	public void updateNCharacterStream(String arg0, Reader arg1, long arg2) throws SQLException {
//
//
//	}
//
//
//	public void updateNClob(int arg0, NClob arg1) throws SQLException {
//
//
//	}
//
//
//	public void updateNClob(String arg0, NClob arg1) throws SQLException {
//
//
//	}
//
//
//	public void updateNClob(int arg0, Reader arg1) throws SQLException {
//
//
//	}
//
//
//	public void updateNClob(String arg0, Reader arg1) throws SQLException {
//
//
//	}
//
//
//	public void updateNClob(int arg0, Reader arg1, long arg2) throws SQLException {
//
//
//	}
//
//
//	public void updateNClob(String arg0, Reader arg1, long arg2) throws SQLException {
//
//
//	}
//
//
//	public void updateNString(int arg0, String arg1) throws SQLException {
//
//
//	}
//
//
//	public void updateNString(String arg0, String arg1) throws SQLException {
//
//
//	}
//
//
//	public void updateRowId(int arg0, RowId arg1) throws SQLException {
//
//
//	}
//
//
//	public void updateRowId(String arg0, RowId arg1) throws SQLException {
//
//
//	}
//
//
//	public void updateSQLXML(int arg0, SQLXML arg1) throws SQLException {
//
//
//	}
//
//
//	public void updateSQLXML(String arg0, SQLXML arg1) throws SQLException {
//
//
//	}
//
//
//	public boolean isWrapperFor(Class<?> arg0) throws SQLException {
//
//		return false;
//	}
//
//
//	public <T> T unwrap(Class<T> arg0) throws SQLException {
//		
//		return null;
//	}

    public <T> T unwrap(Class<T> iface) throws SQLException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}

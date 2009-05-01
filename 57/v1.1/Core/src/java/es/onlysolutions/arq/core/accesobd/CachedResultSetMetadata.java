package es.onlysolutions.arq.core.accesobd;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Esta clase es una implementaci�n especifica del interfaz java.sql.ResultSetMetaData. La devuelte
 * el metodo getMetadata() de la clase CachedResultSet, y contiene todos los metadatos del
 * ResultSet, pero sin ninguna conexion abierta a la base de datos.<br>
 * A la hora de trabajar con este ResultSetMetaData, hay que tener en cuenta que al tratarse de
 * datos cacheados, seria posible que hubieran sufrido cierta modificacion, dependiendo del entorno
 * en que nos encontremos.
 *
 * @see ResultSetMetaData
 */
public class CachedResultSetMetadata implements ResultSetMetaData
{

    private String[] _tableNames;

    private String[] _schemaNames;

    private String[] _columnTypeNames;

    private String[] _columnNames;

    private String[] _columnLabels;

    private String[] _columnClassNames;

    private String[] _catalogNames;

    private boolean[] _isWritableColumns;

    private boolean[] _isSignedColumns;

    private boolean[] _isSearchableColumns;

    private boolean[] _isReadOnlyColumns;

    private boolean[] _isDefinitelyWritableColumns;

    private boolean[] _isCurrencyColumns;

    private boolean[] _isCaseSensitiveColumns;

    private boolean[] _isAutoIncrementColumns;

    private int[] _isNullableColumns;

    private int[] _columnScales;

    private int[] _columnPrecision;

    private int[] _columnTypes;

    private int[] _columnDisplaySizes;

    private int _columnCount;


    /**
     * Constructor de la clase, recibe un ResultSetMetadata como parametro del que obtendra los
     * datos y los cacheara.
     *
     * @param rs El ResultSetMetaData del que obtener los metadatos.
     * @throws SQLException Si ocurre algun problema durante la creacion del CachedResultSetMetadata
     */

    public CachedResultSetMetadata(ResultSetMetaData rs) throws SQLException
    {
        // Rellenar datos del rs
        _columnCount = rs.getColumnCount();

        _tableNames = new String[_columnCount];
        _schemaNames = new String[_columnCount];
        _columnTypeNames = new String[_columnCount];
        _columnNames = new String[_columnCount];
        _columnLabels = new String[_columnCount];
        _columnClassNames = new String[_columnCount];
        _catalogNames = new String[_columnCount];
        _isWritableColumns = new boolean[_columnCount];
        _isSignedColumns = new boolean[_columnCount];
        _isSearchableColumns = new boolean[_columnCount];
        _isReadOnlyColumns = new boolean[_columnCount];
        _isDefinitelyWritableColumns = new boolean[_columnCount];
        _isCurrencyColumns = new boolean[_columnCount];
        _isCaseSensitiveColumns = new boolean[_columnCount];
        _isAutoIncrementColumns = new boolean[_columnCount];
        _isNullableColumns = new int[_columnCount];
        _columnScales = new int[_columnCount];
        _columnPrecision = new int[_columnCount];
        _columnTypes = new int[_columnCount];
        _columnDisplaySizes = new int[_columnCount];

        for (int columnIndex = 1; columnIndex <= _columnCount; columnIndex++)
        {
            _tableNames[columnIndex - 1] = rs.getTableName(columnIndex);
            _schemaNames[columnIndex - 1] = rs.getSchemaName(columnIndex);
            _columnTypeNames[columnIndex - 1] = rs.getColumnTypeName(columnIndex);
            _columnNames[columnIndex - 1] = rs.getColumnName(columnIndex);
            _columnLabels[columnIndex - 1] = rs.getColumnLabel(columnIndex);
            _columnClassNames[columnIndex - 1] = rs.getColumnClassName(columnIndex);
            _catalogNames[columnIndex - 1] = rs.getCatalogName(columnIndex);
            _isWritableColumns[columnIndex - 1] = rs.isWritable(columnIndex);
            _isSignedColumns[columnIndex - 1] = rs.isSigned(columnIndex);
            _isSearchableColumns[columnIndex - 1] = rs.isSearchable(columnIndex);
            _isReadOnlyColumns[columnIndex - 1] = rs.isReadOnly(columnIndex);
            _isDefinitelyWritableColumns[columnIndex - 1] = rs.isDefinitelyWritable(columnIndex);
            _isCurrencyColumns[columnIndex - 1] = rs.isCurrency(columnIndex);
            _isCaseSensitiveColumns[columnIndex - 1] = rs.isCaseSensitive(columnIndex);
            _isAutoIncrementColumns[columnIndex - 1] = rs.isAutoIncrement(columnIndex);
            _isNullableColumns[columnIndex - 1] = rs.isNullable(columnIndex);
            _columnScales[columnIndex - 1] = rs.getScale(columnIndex);
            _columnPrecision[columnIndex - 1] = rs.getPrecision(columnIndex);
            _columnTypes[columnIndex - 1] = rs.getColumnType(columnIndex);
            _columnDisplaySizes[columnIndex - 1] = rs.getColumnDisplaySize(columnIndex);
        }

    }


    public int getColumnCount() throws SQLException
    {
        return _columnCount;
    }


    public int getColumnDisplaySize(int column) throws SQLException
    {
        return _columnDisplaySizes[column - 1];
    }


    public int getColumnType(int column) throws SQLException
    {
        return _columnTypes[column - 1];
    }


    public int getPrecision(int column) throws SQLException
    {
        return _columnPrecision[column - 1];
    }


    public int getScale(int column) throws SQLException
    {
        return _columnScales[column - 1];
    }


    public int isNullable(int column) throws SQLException
    {
        return _isNullableColumns[column - 1];
    }


    public boolean isAutoIncrement(int column) throws SQLException
    {
        return _isAutoIncrementColumns[column - 1];
    }


    public boolean isCaseSensitive(int column) throws SQLException
    {
        return _isCaseSensitiveColumns[column - 1];
    }


    public boolean isCurrency(int column) throws SQLException
    {
        return _isCurrencyColumns[column - 1];
    }


    public boolean isDefinitelyWritable(int column) throws SQLException
    {
        return _isDefinitelyWritableColumns[column - 1];
    }


    public boolean isReadOnly(int column) throws SQLException
    {
        return _isReadOnlyColumns[column - 1];
    }


    public boolean isSearchable(int column) throws SQLException
    {
        return _isSearchableColumns[column - 1];
    }


    public boolean isSigned(int column) throws SQLException
    {
        return _isSignedColumns[column - 1];
    }


    public boolean isWritable(int column) throws SQLException
    {
        return _isWritableColumns[column - 1];
    }


    public String getCatalogName(int column) throws SQLException
    {
        return _catalogNames[column - 1];
    }


    public String getColumnClassName(int column) throws SQLException
    {
        return _columnClassNames[column - 1];
    }


    public String getColumnLabel(int column) throws SQLException
    {
        return _columnLabels[column - 1];
    }


    public String getColumnName(int column) throws SQLException
    {
        return _columnNames[column - 1];
    }


    public String getColumnTypeName(int column) throws SQLException
    {
        return _columnTypeNames[column - 1];
    }


    public String getSchemaName(int column) throws SQLException
    {
        return _schemaNames[column - 1];
    }


    public String getTableName(int column) throws SQLException
    {
        return _tableNames[column - 1];
    }


    public boolean isWrapperFor(Class<?> arg0) throws SQLException
    {
        // TODO Auto-generated method stub
        return false;
    }


    public <T> T unwrap(Class<T> arg0) throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
	}
}

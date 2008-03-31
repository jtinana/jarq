package es.onlysolutions.arq.core.accesobd;


import es.onlysolutions.arq.core.accesobd.exception.CachedException;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Esta clase se utiliza como un PreparedStatement cacheado en memoria. Permite el manejo de los datos de un statement
 * preparado sin<br> necesidad de abrir todavia la conexion
 * <p>NOTA:</p> Esta clase no es en si un Statement. El nombre es para representar su funcionalidad, ya que emula un objeto PreparedStatement.
 */

public final class CachedPreparedStatement
{

    /**
     * Lista de elementos que contiene el CachedPreparedStatement.<br>
     * IMPORTANTE: La lista comienza desde la posicion 0, pero el Statement comienza en la 1.
     */
    private AutoAddingList _elements;

    /**
     * Mapa para guargar los sqlTypes de los parametros nulos.
     */
    private Map<Integer, Integer> _sqlTypesForNullElements;

    /**
     * El SQL que contiene el PreparedStatement.
     */
    private String _sql;

    /**
     * Constructor sin parametros. Inicializa la lista interna a una longitud inicial de 10.
     *
     * @param sql El SQL que contiene el CachedPreparedStatement.
     */

    public CachedPreparedStatement(String sql)
    {
        _elements = new AutoAddingList(10);
        _sql = sql;
        _sqlTypesForNullElements = new HashMap<Integer, Integer>(10);
    }

    /**
     * Constructor friendly para instanciarlo adecuadamente en el filtro.<br>
     * No se debe tener acceso desde fuera del paquete al constructor sin par�metros.
     */
    CachedPreparedStatement()
    {
        _elements = new AutoAddingList(10);
        _sqlTypesForNullElements = new HashMap<Integer, Integer>(10);
    }


    /**
     * �ste m�todo rellena el CachedPreparedStatement con los parametros que contenga el PreparedStatement.<br> Este
     * m�todo es de utilidad a la hora del manejo interno del componente, no debe utilizarse por el desarrollador,<br>
     * en su lugar se deben utilizar los metodos setter apropiados.
     *
     * @param pt El PreparedStatement a rellenar con los parametros que contenga.
     * @throws CachedException Si ocurre alg�n error durante el proceso.
     */

    public final void setParameters(PreparedStatement pt) throws CachedException
    {
        for (int index = 0; index < _elements.size(); index++)
        {
            Object value = _elements.get(index);

            try
            {
                if (value == null)
                {
                    pt.setNull(index + 1, _sqlTypesForNullElements.get(index + 1).intValue());
                }
                else
                {
                    pt.setObject(index + 1, value);
                }
            }
            catch (SQLException e)
            {
                throw new CachedException("Error al tratar de insertar el elemento n�mero " + (index + 1), e);
            }
        }
    }

    /**
     * Introduce un objeto Date en la posicion indicada del Statement.
     *
     * @param index la posicion donde sera introducido
     * @param value el objeto Date a insertar.
     * @throws SQLException Si ocurre alg�n error durante el proceso.
     */

    public void setDate(int index, Date value) throws SQLException
    {
        setObject(index, value);
    }

    /**
     * Introduce un objeto Calendar en la posicion indicada del Statement.
     *
     * @param index la posicion donde sera introducido
     * @param value el objeto Calendar a insertar.
     * @throws SQLException Si ocurre alg�n error durante el proceso.
     */


    public void setDate(int index, Calendar value) throws SQLException
    {
        setObject(index, new java.sql.Date(value.getTime().getTime()));
    }

    /**
     * Introduce un boolean en la posicion indicada del Statement.
     *
     * @param index la posicion donde sera introducido
     * @param value el boolean a insertar.
     * @throws SQLException Si ocurre alg�n error durante el proceso.
     */


    public void setBoolean(int index, boolean value) throws SQLException
    {
        setObject(index, Boolean.valueOf(value));
    }

    /**
     * Este metodo no esta soportado. Lanza siempre una SQLException.
     *
     * @param paramIndex la posicion donde insertar.
     * @param sqlType    El tipo de objeto.
     * @param typeName   El nombre del tipo.
     * @throws SQLException Si ocurre alg�n error durante el proceso.
     */

    public void setNull(int paramIndex, int sqlType, String typeName) throws SQLException
    {
        throw new SQLException("Method not supported.ParamIndex: " + paramIndex + "; SqlType: " + sqlType + "; TypeName: " + typeName);
    }

    /**
     * Establece un valor a null en el parametro indicado por el  �ndice.
     *
     * @param paramIndex la posicion donde insertar
     * @param sqlType    El tipo de objeto
     * @throws SQLException si ocurre alg�n error durante la ejecuci�n del m�todo.
     */
    public void setNull(int paramIndex, int sqlType) throws SQLException
    {
        setObject(paramIndex, null);
        _sqlTypesForNullElements.put(Integer.valueOf(paramIndex), Integer.valueOf(sqlType));
    }

    /**
     * Inserta un objeto Long.
     *
     * @param index, la posicion donde insertar
     * @param value, el objeto Long a insertar
     * @throws SQLException Si ocurre alg�n error durante el proceso.
     */

    public void setLong(int index, Long value) throws SQLException
    {
        setObject(index, value);
    }

    /**
     * Inserta un valor long en el statement.
     *
     * @param index posicion donde se insertara el valor
     * @param value el valor long
     * @throws SQLException Si ocurre alg�n error durante el proceso.
     */

    public void setLong(int index, long value) throws SQLException
    {
        setLong(index, new Long(value));
    }

    /**
     * Inserta un objeto Integer.
     *
     * @param index la posicion donde insertar
     * @param value el objeto Integer a insertar
     * @throws SQLException Si ocurre alg�n error durante el proceso.
     */

    public void setInt(int index, Integer value) throws SQLException
    {
        setObject(index, value);
    }

    /**
     * Inserta un valor int.
     *
     * @param index la posicion a insertar
     * @param value el valor int a insertar
     * @throws SQLException Si ocurre alg�n error durante el proceso.
     */


    public void setInt(int index, int value) throws SQLException
    {
        setInt(index, new Integer(value));
    }


    /**
     * Limpia todos los parametros del CachedPreparedStatement.
     *
     * @throws SQLException Si ocurre alg�n error durante el proceso.
     */

    public void clearParameters() throws SQLException
    {
        _elements.clear();
        _sqlTypesForNullElements.clear();
    }

    /**
     * Inserta un objeto Double.
     *
     * @param index la posicion donde insertar
     * @param value el objeto a insertar
     * @throws SQLException Si ocurre alg�n error durante el proceso.
     */

    public void setDouble(int index, Double value) throws SQLException
    {
        setObject(index, value);
    }

    /**
     * Inserta un valor double.
     *
     * @param index la posicion a insertar
     * @param value el valor double
     * @throws SQLException Si ocurre alg�n error durante el proceso.
     */

    public void setDouble(int index, double value) throws SQLException
    {
        setDouble(index, new Double(value));
    }

    /**
     * Inserta un objeto de tipo String.
     *
     * @param index la posicion a insertar
     * @param value el objeto String
     * @throws SQLException Si ocurre alg�n error durante el proceso.
     */


    public void setString(int index, String value) throws SQLException
    {
        setObject(index, value);
    }

    /**
     * Inserta un objeto de tipo BigDecimal.
     *
     * @param index la posicion a insertar
     * @param value el objeto BigDecimal
     * @throws SQLException Si ocurre alg�n error durante el proceso.
     */


    public void setBigDecimal(int index, BigDecimal value) throws SQLException

    {
        setObject(index, value);
    }

    /**
     * Establece el objeto indicado en la posicion pasada como par�metro.
     *
     * @param index La posici�n donde establecer el objeto.
     * @param value El objeto a establecer.
     * @throws SQLException Si ocurre alg�n error durante el proceso.
     */
    void setObject(int index, Object value) throws SQLException
    {
        _elements.set(index - 1, value);
    }

    /**
     * Inserta un objeto de tipo URL.
     *
     * @param index la posicion a insertar.
     * @param value el objeto URL.
     * @throws SQLException Si ocurre alg�n error durante el proceso.
     */


    public void setURL(int index, URL value) throws SQLException
    {
        setObject(index, value);
    }

    /**
     * Inserta un objeto de tipo Array.
     *
     * @param index la posicion a insertar.
     * @param value el objeto Array.
     * @throws SQLException Si ocurre alg�n error durante el proceso.
     */

    public void setArray(int index, Array value) throws SQLException
    {
        setObject(index, value);
    }

    /**
     * Inserta un objeto de tipo Blob.
     *
     * @param index la posicion a insertar.
     * @param value el objeto Blob.
     * @throws SQLException Si ocurre alg�n error durante el proceso.
     */


    public void setBlob(int index, Blob value) throws SQLException
    {
        setObject(index, value);
    }

    /**
     * Inserta un objeto de tipo Clob.
     *
     * @param index la posicion a insertar.
     * @param value el objeto Clob.
     * @throws SQLException Si ocurre alg�n error durante el proceso.
     */

    public void setClob(int index, Clob value) throws SQLException
    {
        setObject(index, value);
    }

    /**
     * Inserta un objeto de tipo Ref.
     *
     * @param index la posicion a insertar.
     * @param value el objeto Ref.
     * @throws SQLException Si ocurre alg�n error durante el proceso.
     */


    public void setRef(int index, Ref value) throws SQLException
    {
        setObject(index, value);
    }

    /**
     * Inserta un objeto de tipo Time.
     *
     * @param index la posicion a insertar.
     * @param value el objeto Time.
     * @throws SQLException Si ocurre alg�n error durante el proceso.
     */

    public void setTime(int index, Time value) throws SQLException
    {
        setObject(index, value);
    }

    /**
     * Inserta un objeto de tipo Timestamp.
     *
     * @param index la posicion a insertar.
     * @param value el objeto Timestamp.
     * @throws SQLException Si ocurre alg�n error durante el proceso.
     */

    public void setTimestamp(int index, Timestamp value) throws SQLException
    {
        setObject(index, value);
    }


    /**
     * �ste metodo comprueba que los par�metros estan introducidos consecutivamente de forma correcta.<br> Dado que en
     * cada llamada al metodo se realiza una comprobacion de todos los parametros, su uso deberia limitarse a
     * desarrollo,<br> desactivandose una vez finalizado.
     *
     * @throws CachedException si esta mal formada la lista de parametros.
     */

    public void isConsistent() throws CachedException
    {
        if (_elements.contains(null))
        {
            throw new CachedException("El CachedPreparedStatement no es consistente. Los elementos deben ser adyacentes.");
        }
    }

    /**
     * Imprime todos los valores del CachedPrearedStatement.
     *
     * @return Un String con la representacion.
     */

    public String toString()
    {
        return _elements.toString();
    }

    /**
     * Devuelve el numero de elementos que contiene el CachedPreparedStatement.
     *
     * @return un int con el numero de elementos contenidos.
     */

    public int getNumberOfElements()
    {
        return _elements.size();
    }

    /**
     * Este metodo se utiliza a nivel interno para mostrar los elementos del Statement en los MBeans.<br>
     * No deberia ser accesible por el desarrollador.
     *
     * @param index La posicion de donde sacar el objeto.
     * @return El objeto que se encuentra en la posicion index.
     */

    Object getObject(int index)
    {
        return _elements.get(index - 1);
    }

    /**
     * Devuelve el SQL del CachedPreparedStatement.<br>
     * �ste metodo es friendly ya que no deber�a acceder el desarrollador al contenido una vez creado el objeto.
     *
     * @return El String con el SQL.
     */
    public String getSql()
    {
        return _sql;
    }

    /**
     * Setter para el SQL. Se deja p�blico ya que dado que un CachedPreparedStatement se puede reutilizar, <br>
     * es muy �til para paginar el poder sustituir el SQL sin modificar los parametros que contiene.
     *
     * @param sql El SQL a establecer. <b>Debe contener los mismos par�metros que el SQL anterior.</b>
     */
    public void setSql(String sql)
    {
        _sql = sql;
    }

}

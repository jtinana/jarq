package es.onlysolutions.arq.core.accesobd;


import es.onlysolutions.arq.core.accesobd.exception.FilterException;
import es.onlysolutions.arq.core.log.LoggerGenerator;
import org.apache.commons.logging.Log;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Clase padre para todos los filtros de datos.<br>
 * Un filtro tan solo sirve para consultas sobre la base de datos.
 * Provee de m�todos addParameter sobrecargados para a�adir desde las clases hijas los parametros adecuados.<br>
 * En la clase hija se deben implementar los m�todos adecuados para saber que nombre de par�metro debe tener el<br>
 * parametro SQL a�adido.<br>
 * <b>Ejemplo de filtro:</b><br><br>
 * public class EjemploFiltro extends FiltroDatos<br>
 * {<br>
 * &nbsp;&nbsp;protected String getSelectFromClause()<br>
 * &nbsp;&nbsp;{<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return "SELECT NOMBRE, EDAD FROM TABLA_EJEMPLO";<br>
 * &nbsp;&nbsp;}<br>
 * &nbsp;&nbsp;.<br>
 * &nbsp;&nbsp;.<br>
 * &nbsp;&nbsp;.<br>
 * resto de implementaci�n de m�todos abstractos . . .<br>
 * <br>
 * &nbsp;&nbsp;public void filtrarPorNombre( String nombre )<br>
 * &nbsp;&nbsp;{<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;addParameter(" NOMBRE like '%" + nombre + "%'");<br>
 * &nbsp;&nbsp;}<br>
 * <br>
 * &nbsp;&nbsp;public void filtrarEdad( int edad )<br>
 * &nbsp;&nbsp;{<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;addParameter( " EDAD = ? ", edad );<br>
 * &nbsp;&nbsp;}<br>
 * }<br>
 * <br>
 * <b>Ejemplo de ejecuci�n de ese filtro:</b><br>
 * <br>
 * public class EjemploDao extends AbstractJdbcDao<br>
 * {<br>
 * &nbsp;&nbsp;public ResultSet executeQuery( EjemploFiltro filtro )<br>
 * &nbsp;&nbsp;{<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;CachedPreparedStatement cpt = filtro.getCachedPreparedStatement();<br>
 * &nbsp;&nbsp;&nbsp&nbsp;&nbsp;&nbsp;return executeQuery( cpt );<br>
 * &nbsp;&nbsp;}<br>
 * }<br>
 * <br>
 * El CachedPreparedStatement obtenido del filtro ya lleva dentro el SQL adecuado junto con los par�metros correctamente formados.
 *
 * @see es.onlysolutions.arq.core.accesobd.CachedPreparedStatement
 */
public abstract class FiltroDatos
{
    /**
     * Constructor sin par�metros. Para las consultas con condiciones fijas, es decir, cualquier condicion que no deba ser a�adida mediante<br>
     * un m�todo del filtro se a�adir� aqui.
     */
    public FiltroDatos()
    {
        //Contructor vacio.
    }

    /**
     * El logger de la clase.
     */
    private static final Log logger = LoggerGenerator.getLogger(FiltroDatos.class);

    /**
     * Lista con los datos a introducir en la clausula Where.
     */
    private final List<String> listaDatosWhere = new ArrayList<String>(10);

    /**
     * PreparedStatement con los par�metros a ejecutar en la query.
     */
    private CachedPreparedStatement cpt;


    /**
     * La numeroPagina final de la lista.
     */
    private int numeroPagina = -1;

    /**
     * M�todo para inicializar el CachedPreparedStatement antes de usarlo, ya que si no se necesita se queda a nulo.
     */
    private void initCpt()
    {
        if (cpt == null)
        {
            cpt = new CachedPreparedStatement();
        }
    }

    /**
     * Devuelve el objeto Where que contiene el Sql de toda la clausula Where y el CachedPreparedStament con sus par�metros.
     *
     * @return Objecto WhereObject con la clausula where completa.
     * @see WhereObject
     */
    private WhereObject getWhere()
    {

        WhereObject whereObj = new WhereObject();
        StringBuffer sqlBuffer = new StringBuffer(200);

        if (listaDatosWhere.size() > 0)
        {
            sqlBuffer.append(" WHERE ");

            for (int index = 0; index < listaDatosWhere.size(); index++)
            {
                String strCondition = listaDatosWhere.get(index);
                if (strCondition != null)
                {
                    sqlBuffer.append(strCondition);
                    if ((index + 1) < listaDatosWhere.size())
                    {
                        sqlBuffer.append(" AND ");
                    }
                }
            }
            if (logger.isDebugEnabled())
            {
                logger.debug("A�adimos WHERE: " + sqlBuffer.toString());
            }

        }

        //A�dimos solo la condicion de paginacion si se proporciona alguna.
        String pagingSql = getPagingSql();
        if (pagingSql != null && pagingSql.length() > 0)
        {
            if (listaDatosWhere.size() > 0) //Ya hemos a�adido antes algun parametro
            {
                sqlBuffer.append(pagingSql);
            }
            else //No se ha a�adido ni el Where
            {
                sqlBuffer.append(pagingSql);
            }
            if (logger.isDebugEnabled())
            {
                logger.debug("A�adimos PAGINACION: " + sqlBuffer.toString());
            }

        }


        if (logger.isDebugEnabled())
        {
            logger.debug("Obtenemos WHERE: ");
            logger.debug(whereObj);
        }

        //Introducimos los datos en el objeto WhereObject.
        String sqlWhere = sqlBuffer.toString();
        if (sqlWhere.length() > 0)
        {
            whereObj.setSqlWhere(sqlBuffer.toString());
            whereObj.setCachedPreparedStatement(cpt);
        }

        return whereObj;
    }

    /**
     * Devuelve el objeto Where que contiene el Sql de toda la clausula Where y el CachedPreparedStament con sus par�metros.
     *
     * @return Objecto WhereObject con la clausula where completa.
     * @see WhereObject
     */
    private WhereObject getWhereForCount()
    {

        WhereObject whereObj = new WhereObject();
        StringBuffer sqlBuffer = new StringBuffer(200);

        if (listaDatosWhere.size() > 0)
        {
            sqlBuffer.append(" WHERE ");

            for (int index = 0; index < listaDatosWhere.size(); index++)
            {
                String strCondition = listaDatosWhere.get(index);
                if (strCondition != null)
                {
                    sqlBuffer.append(strCondition);
                    if ((index + 1) < listaDatosWhere.size())
                    {
                        sqlBuffer.append(" AND ");
                    }
                }
            }
            if (logger.isDebugEnabled())
            {
                logger.debug("A�adimos WHERE: " + sqlBuffer.toString());
            }

        }

        if (logger.isDebugEnabled())
        {
            logger.debug("Obtenemos WHERE: ");
            logger.debug(whereObj);
        }

        //Introducimos los datos en el objeto WhereObject.
        String sqlWhere = sqlBuffer.toString();
        if (sqlWhere.length() > 0)
        {
            whereObj.setSqlWhere(sqlBuffer.toString());
            whereObj.setCachedPreparedStatement(cpt);
        }

        return whereObj;
    }

    /**
     * Devuelve el SQL completo con un SELECT COUNT(*), junto con la clausual where ya preparada y el CachedPreparedStatement
     *
     * @return Un objeto SQL conteniendo el c�digo Sql a ejecutar y CachedPreparedStament con los par�metros.
     * @see SQL
     */
    public CachedPreparedStatement getSqlForCount()
    {
        SQL sqlObj = new SQL();


        String selectFromClause = getSelectCountClause();

        if (selectFromClause == null || selectFromClause.length() < 1)
        {
            throw new FilterException("Se debe indicar una clausula SELECT y FROM. Implemente adecuadamente el m�todo: protected abstract String getSelectFromClause(); ");
        }

        SQL sqlForCount = buildCountWhereClause(selectFromClause, sqlObj);

        CachedPreparedStatement rVal = sqlForCount.getCachedPreparedStatement();

        if (rVal == null)
        {
            rVal = new CachedPreparedStatement();
        }

        //Establecemos el SQL del CachedPreparedStatement.        
        rVal.setSql(sqlForCount.getSql());

        return rVal;
    }


    /**
     * Construye el where para el listado (con paginacion).
     *
     * @param selectFromClause El select
     * @param sqlObj           El objeto SQL.
     * @return El objeto SQL.
     */
    private SQL buildWhereClause(String selectFromClause, SQL sqlObj)
    {
        StringBuffer sqlBuffer = new StringBuffer(200);
        sqlBuffer.append(selectFromClause);

        WhereObject whereObj = getWhere();
        if (whereObj.getSqlWhere() != null)
        {
            sqlBuffer.append(whereObj.getSqlWhere());
        }

        String tailClauses = getTailClauses();

        if (tailClauses != null && tailClauses.length() > 0)
        {
            sqlBuffer.append(tailClauses);
        }

        sqlObj.setCachedPreparedStatement(whereObj.getCachedPreparedStatement());
        sqlObj.setSql(sqlBuffer.toString());

        if (logger.isDebugEnabled())
        {
            logger.debug("SQLFILTRO:" + sqlObj.getSql());
        }

        return sqlObj;
    }

    /**
     * Construye el where para el count (sin paginacion y sin tail clauses).
     *
     * @param selectFromClause El select
     * @param sqlObj           El objeto SQL.
     * @return El objeto SQL.
     */
    private SQL buildCountWhereClause(String selectFromClause, SQL sqlObj)
    {
        StringBuffer sqlBuffer = new StringBuffer(200);
        sqlBuffer.append(selectFromClause);

        WhereObject whereObj = getWhereForCount();
        if (whereObj.getSqlWhere() != null)
        {
            sqlBuffer.append(whereObj.getSqlWhere());
        }

        sqlObj.setCachedPreparedStatement(whereObj.getCachedPreparedStatement());
        sqlObj.setSql(sqlBuffer.toString());

        if (logger.isDebugEnabled())
        {
            logger.debug("SQLFILTRO:" + sqlObj.getSql());
        }

        return sqlObj;
    }

    /**
     * Devuelve el CachedPreparedStatement completo con la query a ejecutar y los par�metros adecuados si son necesarios.
     *
     * @return Un objeto SQL conteniendo el c�digo Sql a ejecutar y CachedPreparedStament con los par�metros.
     * @see SQL
     */
    public CachedPreparedStatement getSql()
    {
        SQL sqlObj = new SQL();


        String selectFromClause = getSelectFromClause();

        if (selectFromClause == null || selectFromClause.length() < 1)
        {
            throw new FilterException("Se debe indicar una clausula SELECT y FROM. Implemente adecuadamente el m�todo: protected abstract String getSelectFromClause(); ");
        }

        SQL sql = buildWhereClause(selectFromClause, sqlObj);

        CachedPreparedStatement rVal = sql.getCachedPreparedStatement();

        if (rVal == null)
        {
            rVal = new CachedPreparedStatement();
        }

        //Establecemos la query a ejecutar.
        rVal.setSql(sql.getSql());

        return rVal;
    }

    /** ***************************************************** **/
    /** ************ M�todos para a�adir par�metros ********* **/
    /** ***************************************************** **/

    /**
     * A�ade un prametro a la clausula Where sin valor.<br>
     * �sto es debido a que en ocasiones si se quiere usar un like no es posible utilizar un CachedPreparedStatement
     *
     * @param sqlValue La nueva condicion a a�adir a la clausula Where.
     */
    protected void addParameter(String sqlValue)
    {
        listaDatosWhere.add(sqlValue);
    }

    /**
     * A�ade un parametro de tipo Long.
     *
     * @param sqlValue  La nueva condicion a a�adir a la clausula Where.
     * @param longValue El valor de tipo Long
     * @throws FilterException Si ocurre algun problema durante la adici�n del par�metro.
     */
    protected void addParameter(String sqlValue, Long longValue)
    {
        initCpt();
        try
        {
            listaDatosWhere.add(sqlValue);
            cpt.setLong(cpt.getNumberOfElements() + 1, longValue);
        }
        catch (SQLException e)
        {
            throw new FilterException("Error al tratar de a�adir el valor: " + longValue, e);
        }
    }


    /**
     * A�ade un par�metro de tipo Integer
     *
     * @param sqlValue La nueva condicion a a�adir a la clausula Where.
     * @param intValue El valor de tipo Integer
     * @throws FilterException Si ocurre algun problema durante la adici�n del par�metro.
     */
    protected void addParameter(String sqlValue, Integer intValue)
    {
        initCpt();
        try
        {
            listaDatosWhere.add(sqlValue);
            cpt.setInt(cpt.getNumberOfElements() + 1, intValue);
        }
        catch (SQLException e)
        {
            throw new FilterException("Error al tratar de a�adir el valor: " + intValue, e);
        }
    }

    /**
     * A�ade un parametro de tipo String.
     *
     * @param sqlValue La nueva condicion a a�adir a la clausula Where.
     * @param strValue El valor de tipo String.
     * @throws FilterException Si ocurre algun problema durante la adici�n del par�metro.
     */
    protected void addParameter(String sqlValue, String strValue)
    {
        initCpt();
        try
        {
            listaDatosWhere.add(sqlValue);
            cpt.setString(cpt.getNumberOfElements() + 1, strValue);
        }
        catch (SQLException e)
        {
            throw new FilterException("Error al tratar de a�adir el valor: " + strValue, e);
        }
    }

    /**
     * A�ade un parametro de tipo String.
     *
     * @param sqlValue   La nueva condicion a a�adir a la clausula Where.
     * @param bigDecimal El valor de tipo String.
     * @throws FilterException Si ocurre algun problema durante la adici�n del par�metro.
     */
    protected void addParameter(String sqlValue, BigDecimal bigDecimal)
    {
        initCpt();
        try
        {
            listaDatosWhere.add(sqlValue);
            cpt.setBigDecimal(cpt.getNumberOfElements() + 1, bigDecimal);
        }
        catch (SQLException e)
        {
            throw new FilterException("Error al tratar de a�adir el valor: " + bigDecimal, e);
        }
    }

    /**
     * A�ade un parametro de tipo Boolean.
     *
     * @param sqlValue     La nueva condicion a a�adir a la clausula Where.
     * @param booleanValue El valor de tipo Boolean.
     * @throws FilterException Si ocurre algun problema durante la adici�n del par�metro.
     */
    protected void addParameter(String sqlValue, Boolean booleanValue)
    {
        initCpt();
        try
        {
            listaDatosWhere.add(sqlValue);
            cpt.setBoolean(cpt.getNumberOfElements() + 1, booleanValue);
        }
        catch (SQLException e)
        {
            throw new FilterException("Error al tratar de a�adir el valor: " + booleanValue, e);
        }
    }

    /**
     * A�ade un parametro de tipo Array.
     *
     * @param sqlValue La nueva condicion a a�adir a la clausula Where.
     * @param array    El valor de tipo Array.
     * @throws FilterException Si ocurre algun problema durante la adici�n del par�metro.
     */
    protected void addParameter(String sqlValue, Array array)
    {
        initCpt();
        try
        {
            listaDatosWhere.add(sqlValue);
            cpt.setArray(cpt.getNumberOfElements() + 1, array);
        }
        catch (SQLException e)
        {
            throw new FilterException("Error al tratar de a�adir el valor: " + array, e);
        }
    }

    /**
     * A�ade un parametro de tipo Blob.
     *
     * @param sqlValue La nueva condicion a a�adir a la clausula Where.
     * @param blob     El valor de tipo Blob.
     * @throws FilterException Si ocurre algun problema durante la adici�n del par�metro.
     */
    protected void addParameter(String sqlValue, Blob blob)
    {
        initCpt();
        try
        {
            listaDatosWhere.add(sqlValue);
            cpt.setBlob(cpt.getNumberOfElements() + 1, blob);
        }
        catch (SQLException e)
        {
            throw new FilterException("Error al tratar de a�adir el valor: " + blob, e);
        }
    }

    /**
     * A�ade un parametro de tipo Calendar.
     *
     * @param sqlValue La nueva condicion a a�adir a la clausula Where.
     * @param cal      El valor de tipo Calendar.
     * @throws FilterException Si ocurre algun problema durante la adici�n del par�metro.
     */
    protected void addParameter(String sqlValue, Calendar cal)
    {
        initCpt();
        try
        {
            listaDatosWhere.add(sqlValue);
            cpt.setDate(cpt.getNumberOfElements() + 1, cal);
        }
        catch (SQLException e)
        {
            throw new FilterException("Error al tratar de a�adir el valor: " + cal, e);
        }
    }

    /**
     * A�ade un parametro de tipo java.util.Date.
     *
     * @param sqlValue La nueva condicion a a�adir a la clausula Where.
     * @param date     El valor de tipo java.util.Date.
     * @throws FilterException Si ocurre algun problema durante la adici�n del par�metro.
     */
    protected void addParameter(String sqlValue, java.util.Date date)
    {
        initCpt();
        try
        {
            listaDatosWhere.add(sqlValue);
            cpt.setDate(cpt.getNumberOfElements() + 1, date);
        }
        catch (SQLException e)
        {
            throw new FilterException("Error al tratar de a�adir el valor: " + date, e);
        }
    }

    /**
     * A�ade un parametro de tipo Timestamp.
     *
     * @param sqlValue  La nueva condicion a a�adir a la clausula Where.
     * @param timestamp El valor de tipo Timestamp.
     * @throws FilterException Si ocurre algun problema durante la adici�n del par�metro.
     */
    protected void addParameter(String sqlValue, Timestamp timestamp)
    {
        initCpt();
        try
        {
            listaDatosWhere.add(sqlValue);
            cpt.setTimestamp(cpt.getNumberOfElements() + 1, timestamp);
        }
        catch (SQLException e)
        {
            throw new FilterException("Error al tratar de a�adir el valor: " + timestamp, e);
        }
    }

    /**
     * A�ade un parametro de tipo Double.
     *
     * @param sqlValue    La nueva condicion a a�adir a la clausula Where.
     * @param doubleValue El valor de tipo Double.
     * @throws FilterException Si ocurre algun problema durante la adici�n del par�metro.
     */
    protected void addParameter(String sqlValue, Double doubleValue)
    {
        initCpt();
        try
        {
            listaDatosWhere.add(sqlValue);
            cpt.setDouble(cpt.getNumberOfElements() + 1, doubleValue);
        }
        catch (SQLException e)
        {
            throw new FilterException("Error al tratar de a�adir el valor: " + doubleValue, e);
        }
    }

    /* ***************************************************************** */
    /* * M�todos para a�adir valores al PreparedStatement directamente * */
    /* ***************************************************************** */


    /**
     * A�ade el valor indicado como par�metro al PreparedStatement.<br>
     * �ste m�todo se utilizar� para a�adir par�metros a WHERE que no sean el principal.
     *
     * @param strValue El String a a�adir como par�metro.
     */
    protected void addPreparedStatementValue(String strValue)
    {
        initCpt();
        try
        {
            cpt.setString(cpt.getNumberOfElements() + 1, strValue);
        }
        catch (SQLException e)
        {
            throw new FilterException("Error al tratar de a�adir el parametro: " + strValue, e);
        }
    }

    /**
     * A�ade el valor indicado como par�metro al PreparedStatement.<br>
     * �ste m�todo se utilizar� para a�adir par�metros a WHERE que no sean el principal.
     *
     * @param doubleValue El Double a a�adir como par�metro.
     */
    protected void addPreparedStatementValue(Double doubleValue)
    {
        initCpt();
        try
        {
            cpt.setDouble(cpt.getNumberOfElements() + 1, doubleValue);
        }
        catch (SQLException e)
        {
            throw new FilterException("Error al tratar de a�adir el parametro: " + doubleValue, e);
        }
    }

    /**
     * A�ade el valor indicado como par�metro al PreparedStatement.<br>
     * �ste m�todo se utilizar� para a�adir par�metros a WHERE que no sean el principal.
     *
     * @param intValue El Double a a�adir como par�metro.
     */
    protected void addPreparedStatementValue(Integer intValue)
    {
        initCpt();
        try
        {
            cpt.setDouble(cpt.getNumberOfElements() + 1, intValue);
        }
        catch (SQLException e)
        {
            throw new FilterException("Error al tratar de a�adir el parametro: " + intValue, e);
        }
    }

    /**
     * A�ade el valor indicado como par�metro al PreparedStatement.<br>
     * �ste m�todo se utilizar� para a�adir par�metros a WHERE que no sean el principal.
     *
     * @param calValue El Double a a�adir como par�metro.
     */
    protected void addPreparedStatementValue(Calendar calValue)
    {
        initCpt();
        try
        {
            cpt.setDate(cpt.getNumberOfElements() + 1, calValue);
        }
        catch (SQLException e)
        {
            throw new FilterException("Error al tratar de a�adir el parametro: " + calValue, e);
        }
    }

    /**
     * A�ade el valor indicado como par�metro al PreparedStatement.<br>
     * �ste m�todo se utilizar� para a�adir par�metros a WHERE que no sean el principal.
     *
     * @param timestampValue El Double a a�adir como par�metro.
     */
    protected void addPreparedStatementValue(Timestamp timestampValue)
    {
        initCpt();
        try
        {
            cpt.setDate(cpt.getNumberOfElements() + 1, timestampValue);
        }
        catch (SQLException e)
        {
            throw new FilterException("Error al tratar de a�adir el parametro: " + timestampValue, e);
        }
    }

    /**
     * A�ade el valor indicado como par�metro al PreparedStatement.<br>
     * �ste m�todo se utilizar� para a�adir par�metros a WHERE que no sean el principal.
     *
     * @param dateValue El Double a a�adir como par�metro.
     */
    protected void addPreparedStatementValue(java.util.Date dateValue)
    {
        initCpt();
        try
        {
            cpt.setDate(cpt.getNumberOfElements() + 1, dateValue);
        }
        catch (SQLException e)
        {
            throw new FilterException("Error al tratar de a�adir el parametro: " + dateValue, e);
        }
    }

    /**
     * A�ade el valor indicado como par�metro al PreparedStatement.<br>
     * �ste m�todo se utilizar� para a�adir par�metros a WHERE que no sean el principal.
     *
     * @param booleanValue El Double a a�adir como par�metro.
     */
    protected void addPreparedStatementValue(Boolean booleanValue)
    {
        initCpt();
        try
        {
            cpt.setBoolean(cpt.getNumberOfElements() + 1, booleanValue);
        }
        catch (SQLException e)
        {
            throw new FilterException("Error al tratar de a�adir el parametro: " + booleanValue, e);
        }
    }

    /**
     * A�ade el valor indicado como par�metro al PreparedStatement.<br>
     * �ste m�todo se utilizar� para a�adir par�metros a WHERE que no sean el principal.
     *
     * @param bigDecimal El Double a a�adir como par�metro.
     */
    protected void addPreparedStatementValue(BigDecimal bigDecimal)
    {
        initCpt();
        try
        {
            cpt.setBigDecimal(cpt.getNumberOfElements() + 1, bigDecimal);
        }
        catch (SQLException e)
        {
            throw new FilterException("Error al tratar de a�adir el parametro: " + bigDecimal, e);
        }
    }

    /**
     * A�ade el valor indicado como par�metro al PreparedStatement.<br>
     * �ste m�todo se utilizar� para a�adir par�metros a WHERE que no sean el principal.
     *
     * @param longValue El Double a a�adir como par�metro.
     */
    protected void addPreparedStatementValue(Long longValue)
    {
        initCpt();
        try
        {
            cpt.setLong(cpt.getNumberOfElements() + 1, longValue);
        }
        catch (SQLException e)
        {
            throw new FilterException("Error al tratar de a�adir el parametro: " + longValue, e);
        }
    }

    /* *************************************************************** */
    /* *************************************************************** */
    /* *************************************************************** */


    /**
     * Devuelve la numeroPagina ACTUAL de la lista.<br>
     *
     * @return El numero de numeroPagina o -1 si no se indico ninguna.
     */
    public int getNumeroPagina()
    {
        return numeroPagina;
    }

    /**
     * Establece la numeroPagina inicial.<br>
     * Si no se quiere establecer ninguna �ste valor tomara -1.
     *
     * @param pagina El numero de la numeroPagina. -1 si no se desea ninguno.
     */
    public void setNumeroPagina(int pagina)
    {
        this.numeroPagina = pagina;
    }

    /** ************************************************* **/
    /** ************** M�todos abstractos *************** **/
    /** ************************************************* **/

    /**
     * M�todo que se debe implementar para obtener la clausula SELECT y FROM.<br>
     * Cada filtro deber� conocer las tablas a las que accede su Query y formar de forma adecuada �ste parametro.
     *
     * @return El SQL de la clausula SELECT y FROM.
     */
    protected abstract String getSelectFromClause();

    /**
     * M�todo que debe ser implementando para devolver el c�digo SQL que se quiera a�adir tras la clausula WHERE.<br>
     * Por ejemplo clausulas ORDER BY, GROUP BY, HAVING, etc
     *
     * @return Un String con el SQL del resto de clausulas. Cadena vacia o null indican que no se a�ade nada.
     */
    protected abstract String getTailClauses();

    /**
     * M�todo que debe devolver la condicion de paginaci�n a introducir en la consulta.<br>
     * No se debe indicar palabras clave como AND, WHERE o cualquier otra que no sea propia de la condicion de paginaci�n.
     *
     * @return Un String con el SQL para la paginaci�n. Si se devuelve null o cadena vac�a no se incluir� condicion alguna.
     */
    protected abstract String getPagingSql();

    /**
     * M�todo que se debe implementar para obtener la clausula SELECT y FROM para realizar la cuenta.<br>
     * Cada filtro deber� conocer las tablas a las que accede su Query y formar de forma adecuada �ste parametro.
     *
     * @return El SQL de la clausula SELECT y FROM.
     */
    protected abstract String getSelectCountClause();
}

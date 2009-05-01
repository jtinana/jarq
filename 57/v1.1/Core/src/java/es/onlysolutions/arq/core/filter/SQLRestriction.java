package es.onlysolutions.arq.core.filter;

import java.util.ArrayList;
import java.util.List;

/**
 * Una restriccion para la clausula WHERE de un filtro SQL.
 * La clausula SQL que se indique debe llevar el operador AND u OR en cada caso.
 */
public class SQLRestriction extends AbstractRestriction
{
    /**
     * Sentencia SQL a a�adir a la restriccion.
     */
    private String SQL;


    /**
     * Lista de parametros necesarios para ejecutar el SQL de esta restriccion.
     * Puede ser una lista vacia, si la condicion no tiene parametros.
     */
    private List<Object> params = new ArrayList<Object>(3);


    /**
     * Getter for property 'params'.
     *
     * @return Value for property 'params'.
     * @see #params
     */
    public List<Object> getParams()
    {
        return params;
    }

    /**
     * Constructor sin parametros.
     */
    public SQLRestriction()
    {
        super();
    }

    /**
     * Constructor con el SQL a introducir para esta restriccion.
     *
     * @param sql El SQL de la restriccion.
     */
    public SQLRestriction(String sql)
    {
        String sqlUpper = sql.toUpperCase();
        checkSqlRestriction(sqlUpper);
        this.SQL = sqlUpper;
    }

    /**
     * Comprueba que sea una restriccion sql valida.
     *
     * @param sql El SQL a validar.
     */
    private void checkSqlRestriction(String sql)
    {
        String sqlTrim = sql.trim();
        if (!sqlTrim.startsWith("AND") && !sqlTrim.startsWith("OR"))
        {
            throw new IllegalArgumentException("La restriccion SQL: " + sql + " debe comenzar por AND u OR");
        }
    }

    /**
     * Constructor que indica el SQL si se trata de una condicion estatica.
     *
     * @param sql     El SQL.
     * @param aStatic Indica si es estatica o no esta condicion. Por defecto se indica siempre true.
     */
    public SQLRestriction(String sql, boolean aStatic)
    {
        String sqlUpper = sql.toUpperCase();
        checkSqlRestriction(sqlUpper);
        this.SQL = sqlUpper;
        setStatic(aStatic);
    }


    /**
     * Inserta un parametro para la una condicion del SQL.
     * Deben insertarse en el mismo orden que en el que aparezcan en la restriccion.
     *
     * @param param El parametro a insertar.
     */
    public void addParam(Object param)
    {
        this.params.add(param);
    }

    /**
     * Establece el SQL a a�adir como restriccion.
     *
     * @param sqlRestriction El SQL a insertar.
     */
    public void setSQL(String sqlRestriction)
    {
        this.SQL = sqlRestriction;
    }

    /**
     * Obtiene el SQL de esta restriccion.
     *
     * @return El SQL interno de la restriccion.
     */
    public String getSQL()
    {
        return SQL;
    }

    /**
     * Metodo para propositos de depuracion.
     *
     * @return Un String con la representacion de la restriccion.
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder(20 * params.size());
        sb.append(SQL);
        sb.append(" -> ");
        sb.append('[');
        for (int index = 0; index < params.size(); index++)
        {
            Object param = params.get(index);
            sb.append(param);
            if ((index + 1) < params.size())
            {
                sb.append(',');
            }
        }
        sb.append(']');

        return sb.toString();
    }
}

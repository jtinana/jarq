package es.onlysolutions.arq.core.filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SQLQuery;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * Filtro para obtener los listados a partir de una consulta SQL.
 * La paginacion sigue
 */
public abstract class SQLFilter extends AbstractFilter
{
    /**
     * El logger de la clase.
     */
    private static final Log logger = LogFactory.getLog(SQLFilter.class);

    /**
     * Constructor con la SQL como parametro.
     * El SQL debe contener la clausula SELECT junto con los atributos que se desean obtener.
     * Se devuelve una lista de mapas con cada nombre de atributo como clave, y su valor como contenido.
     *
     * @param sqlClause La clausua SQL junto con el from y sin indicar WHERE.
     */
    public SQLFilter(String sqlClause)
    {
        Assert.hasLength(sqlClause, "Debe indicar una clausula SELECT no vacia");
        this.sqlClause = sqlClause.toUpperCase();
    }

    /**
     * Array de restricciones a�adidas al filtro.
     */
    private List<SQLRestriction> restrictions = new ArrayList<SQLRestriction>(7);

    /**
     * Restricciones que se aplicaran a todos los SQL. Utiles para indicar condiciones en el WHERE que se deben ejecutar siempre.
     * Incluso para contar el numero total de elementos.
     */
    private List<SQLRestriction> staticRestrictions = new ArrayList<SQLRestriction>(3);

    /**
     * Introduce una restriccion al filtro.
     *
     * @param sqlRestriction El SQLRestriction.
     */
    public void addRestriction(SQLRestriction sqlRestriction)
    {
        if (sqlRestriction.isStatic())
        {
            this.staticRestrictions.add(sqlRestriction);
        }
        else
        {
            this.restrictions.add(sqlRestriction);
        }
    }

    /**
     * Clausula SQL a la que se iran a�adiendo las restricciones.
     */
    private String sqlClause;


    /**
     * Setter for property 'sqlClause'.
     *
     * @param sqlClause Value to set for property 'sqlClause'.
     * @see #sqlClause
     */
    public void setSqlClause(String sqlClause)
    {
        this.sqlClause = sqlClause;
    }

    /**
     * Getter for property 'restrictions'.
     *
     * @return Value for property 'restrictions'.
     * @see #restrictions
     */
    public List<SQLRestriction> getRestrictions()
    {
        return restrictions;
    }

    /**
     * Procesa todas las condiciones introducidas en el filtro y devuelve el SQL ya procesado para ejecutar y obtener los
     * resultados.
     *
     * @return El SQL a ejecutar.
     */
    public String processSql()
    {
        return generateSql(false);
    }

    /**
     * Devuelve el SQL con un select count para obtener el numero de elementos que se van a obtener, sin aplicar las restricciones.
     *
     * @return El SQL preparado para realizar la cuenta sin las restricciones.
     */
    public String processCountSql()
    {
        return generateSql(true);
    }

    /**
     * General el SQL necesario para obtener los resultados o para contar, aplicando las restricciones de forma adecuada.
     *
     * @param isForCount Indica si es para contar.
     * @return El SQL para realizar el count.
     */
    private String generateSql(boolean isForCount)
    {
        StringBuilder sb = new StringBuilder((restrictions.size() * 20) + this.sqlClause.length());

        sb.append(this.sqlClause);

        /**
         * A�adimos las condiciones estaticas.
         */

        boolean whereAdded = false;

        if (this.staticRestrictions.size() > 0)
        {
            sb.append(" WHERE 1=1 ");
            whereAdded = true;

            for (int index = 0; index < staticRestrictions.size(); index++)
            {
                SQLRestriction sqlRestriction = staticRestrictions.get(index);
                sb.append(sqlRestriction.getSQL());
                sb.append(' ');
            }
        }

        if (!isForCount)
        {
            if (this.restrictions.size() > 0)
            {
                if (!whereAdded)
                {
                    sb.append(" WHERE 1=1 ");
                }

                for (int index = 0; index < this.restrictions.size(); index++)
                {
                    SQLRestriction sqlRestriction = this.restrictions.get(index);
                    sb.append(sqlRestriction.getSQL());
                    sb.append(' ');
                }
            }

        }

        if (isForCount)
        {
            /**
             * Sustituimos los atributos del select por un select count(*).
             */
            int lastIndexOfFrom = sb.lastIndexOf("FROM");
            if (lastIndexOfFrom < 1)
            {
                throw new IllegalArgumentException("La clausula SELECT indicada no contiene un FROM. " + this.sqlClause);
            }
            sb.replace(0, lastIndexOfFrom, getSqlForCount());
        }

        if (!isForCount) //Solo se a�aden si no se cuenta
        {
            if (this.orders.size() > 0)
            {
                sb.append(" ORDER BY ");
                for (int indexOrder = 0; indexOrder < orders.size(); indexOrder++)
                {
                    Order order = orders.get(indexOrder);
                    sb.append(parseOrder(order));
                    if ((indexOrder + 1) < orders.size())
                    {
                        sb.append(',');
                    }
                }
            }
        }

        return sb.toString();
    }

    /**
     * Metodo que devuelve la clausula que se introducira para contar.
     * Se puede sobreescribir para modificar el select count a realizar, debido a clausulas distinct o a otras razones.
     *
     * @return El SQL con el COUNT para obtener el numero de elementos.
     */
    protected String getSqlForCount()
    {
        return "SELECT COUNT(*) ";
    }

    /**
     * Establece en la query indicada los parametros necesarios para realizar el count.
     *
     * @param countQuery La query para contar.
     */
    public void setParametersForCount(SQLQuery countQuery)
    {
        this.setParams(countQuery, true);
    }

    /**
     * Establece los parametros necesarios para realizar la query de los resultados con las restricciones.
     *
     * @param query La query donde establecer las restricciones.
     */
    public void setParametersForSelect(SQLQuery query)
    {
        this.setParams(query, false);
    }

    /**
     * Establece los parametros en funcion de si es para contar o para obtener una pagina de resultados.
     *
     * @param isForCount Flag indicando si es para realizar un count o no.
     * @param query      La query donde a�adir las restricciones.
     */
    private void setParams(SQLQuery query, boolean isForCount)
    {
        int paramPosition = 0;
        for (int index = 0; index < staticRestrictions.size(); index++)
        {
            SQLRestriction restriction = staticRestrictions.get(index);
            for (int indexParam = 0; indexParam < restriction.getParams().size(); indexParam++)
            {
                query.setParameter(paramPosition, restriction.getParams().get(indexParam));

                if (logger.isDebugEnabled())
                {
                    logger.debug("Se introduce la restriccion: " + restriction);
                }

                paramPosition++;
            }
        }

        /**
         * A�adimos las restricciones si no es para realizar la cuenta total.
         */
        if (!isForCount)
        {
            for (int index = 0; index < restrictions.size(); index++)
            {
                SQLRestriction restriction = restrictions.get(index);
                for (int indexParam = 0; indexParam < restriction.getParams().size(); indexParam++)
                {
                    query.setParameter(paramPosition, restriction.getParams().get(indexParam));
                    paramPosition++;
                }
            }
        }
    }

    /**
     * Obtiene la representacion en String del orden indicado. Al ser un filtro SQL, se ignora cualquier path indicado en el orden.
     *
     * @param order El orden a parsear.
     * @return La representacion en SQL del orden.
     */
    protected String parseOrder(Order order)
    {
        String result;
        if (Order.DESC.equals(order.getOrderType()))
        {
            result = order.getField() + " DESC ";
        }
        else
        {
            result = order.getField() + " ASC ";
        }

        return result;
    }
}

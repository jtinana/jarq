package es.onlysolutions.arq.core.filter;

import org.hibernate.Query;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase abstracta para los filtros basados en HQL.
 */
public class HQLFilter extends AbstractFilter
{
    /**
     * HQL interno inicial, al que se le iran concatenando las condiciones.
     */
    private StringBuilder innerHQL;

    /**
     * Restricciones que se aplicaran a todas las querys.
     */
    private List<HQLRestriction> staticRestrictions = new ArrayList<HQLRestriction>(3);


    /**
     * Restricciones para obtener el listado.
     */
    private List<HQLRestriction> restrictions = new ArrayList<HQLRestriction>(3);

    /**
     * Constructor unico de la clase, indicando el HQL inicial desde el que se concatenaran el resto de condiciones.
     *
     * @param initialHql El HQL inicial
     */
    public HQLFilter(String initialHql)
    {
        Assert.hasLength(initialHql, "Debe indicarse una sentencia HQL no vacia");
        this.innerHQL = new StringBuilder(initialHql);
    }

    /**
     * A�ade una restriccion.
     *
     * @param hqlRestriction La restriccion a a�adir.
     */
    public void addRestriction(HQLRestriction hqlRestriction)
    {
        Assert.notNull(hqlRestriction, "No se pueden introducir restricciones a null");
        if (hqlRestriction.isStatic())
        {
            this.staticRestrictions.add(hqlRestriction);
        }
        else
        {
            this.restrictions.add(hqlRestriction);
        }
    }

    /**
     * Procesa los parametros internos y construye el HQL necesario para realizar la cuenta del total de elementos.
     *
     * @return El HQL para realizar la cuenta.
     */
    public String processCountHql()
    {
        return this.generateHql(true);
    }

    /**
     * Procesa los parametros necesarios para construir el HQL que obtendra la lista paginada.
     *
     * @return El HQL para la lista.
     */
    public String processHql()
    {
        return this.generateHql(false);
    }

    /**
     * Establece en la Query los parametros necesarios para realizar la cuenta del total de elementos.
     *
     * @param queryForCount La query que se utilizara para contar.
     */
    public void setParametersForCount(Query queryForCount)
    {

    }

    /**
     * Establece en la Query los parametros necesarios para obtener el listado de elementos.
     *
     * @param queryList La query a ejecutar para obtener la lista paginada.
     */
    public void setParametersForList(Query queryList)
    {

    }

    /**
     * General el SQL necesario para obtener los resultados o para contar, aplicando las restricciones de forma adecuada.
     *
     * @param isForCount Indica si es para contar.
     * @return El SQL para realizar el count.
     */
    private String generateHql(boolean isForCount)
    {
        StringBuilder sb = new StringBuilder((restrictions.size() * 20) + this.innerHQL.length());

        sb.append(this.innerHQL);

        /**
         * A�adimos las condiciones estaticas.
         */

        boolean whereAdded = false;

        if (this.staticRestrictions.size() > 0)
        {
            sb.append(" WHERE 1=1 AND ");
            whereAdded = true;

            for (int index = 0; index < staticRestrictions.size(); index++)
            {
                HQLRestriction hqlRestriction = staticRestrictions.get(index);
                sb.append(hqlRestriction.getHqlCondition());
                sb.append(' ');
            }
        }

        if (!isForCount)
        {
            if (this.restrictions.size() > 0)
            {
                if (!whereAdded)
                {
                    sb.append(" WHERE 1=1 AND ");
                }

                for (int index = 0; index < this.restrictions.size(); index++)
                {
                    HQLRestriction hqlRestriction = this.restrictions.get(index);
                    sb.append(hqlRestriction.getHqlCondition());
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
            if (lastIndexOfFrom < 0) //Probamos en minuscula
            {
                lastIndexOfFrom = sb.lastIndexOf("from");
            }


            if (lastIndexOfFrom < 0)
            {
                throw new IllegalArgumentException("La clausula SELECT indicada no contiene un FROM/from ->  " + this.innerHQL);
            }
            sb.replace(0, lastIndexOfFrom, "SELECT COUNT(*) ");
        }
        else
        {
            //A�adimos los order.

            if (this.orders.size() > 0)
            {
                sb.append(" order by ");

                for (int index = 0; index < orders.size(); index++)
                {
                    Order order = orders.get(index);
                    sb.append(' ');
                    sb.append(order.getField());
                    if (order.getOrderType().equals(Order.ASC))
                    {
                        sb.append(" asc");
                    }
                    else if (order.getOrderType().equals(Order.DESC))
                    {
                        sb.append(" desc");
                    }
                    else
                    {
                        throw new IllegalArgumentException("Se ha indicado un codigo de orden inexistente.");
                    }

                    if ((index + 1) < orders.size())
                    {
                        sb.append(',');
                    }
                }
            }


        }

        return sb.toString();
    }

    /**
     * A�ade un orden ascendente.
     *
     * @param attributeName El nombre del attributo a ordenar. Debe incluir los alias si es necesario.
     */
    protected void addAscOrder(String attributeName)
    {
        Order order = new Order(attributeName, Order.ASC);
        addOrder(order);
    }

    /**
     * A�ade un orden de forma descendente.
     *
     * @param attributeName El nombre del atributo a ordenar. Debe incluir los alias si es necesario.
     */
    protected void addDescOrder(String attributeName)
    {
        Order order = new Order(attributeName, Order.DESC);
        addOrder(order);
    }
}

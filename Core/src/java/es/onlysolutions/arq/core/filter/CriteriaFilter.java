package es.onlysolutions.arq.core.filter;

import org.hibernate.criterion.Criterion;

import java.util.HashMap;
import java.util.Map;

/**
 * Filtro para a�adir restricciones a un listado standar de la arquitectura realizado mediante una entidad Hibernate.
 */
public abstract class CriteriaFilter extends AbstractFilter
{
    /**
     * Mapa con las restricciones a a�adir y sus alias path si corresponden.
     */
    private Map<Criterion, String> restrictionList = new HashMap<Criterion, String>(7);

    /**
     * A�ade al filtro una restriccion.
     *
     * @param criterion La restriccion a a�adir. Si se indica un null, no se a�ade restriccion alguna.
     */
    protected void addRestriction(Criterion criterion)
    {
        if (criterion != null)
        {
            this.restrictionList.put(criterion, null);
        }
    }

    /**
     * A�ade al filtro una restriccion.
     *
     * @param criterion La restriccion a a�adir. Si se indica un null, no se a�ade restriccion alguna.
     * @param aliasPath Alias path con el que crear el subcriteria.
     * @see org.hibernate.Criteria#createCriteria(String)
     */
    protected void addRestriction(Criterion criterion, String aliasPath)
    {
        if (criterion != null)
        {
            this.restrictionList.put(criterion, aliasPath);
        }
    }

    /**
     * Obtiene la lista de Restricciones.
     *
     * @return La lista de Criterion.
     */
    public Map<Criterion, String> getRestrictionList()
    {
        return this.restrictionList;
    }

    /**
     * A�ade un Order al filtro con el atributo indicado de forma ascendente.
     *
     * @param attributeName el nombre del atributo por el que ordenar.
     */
    protected void addAscOrder(String attributeName)
    {
        this.addAscOrder(attributeName, null);
    }

    /**
     * A�ade un Order al filtro con el atributo indicado de forma ascendente.
     *
     * @param attributeName el nombre del atributo por el que ordenar.
     * @param path          El path al alias de la subentidad a la que a�adir el orden.
     */
    protected void addAscOrder(String attributeName, String path)
    {
        if (attributeName != null && attributeName.length() > 0)
        {
            Order orderToAdd = new Order();
            orderToAdd.setField(attributeName);
            orderToAdd.setPath(path);
            orderToAdd.setOrderType(Order.ASC);
            addOrder(orderToAdd);
        }
    }

    /**
     * A�ade un orden descendente por el atributo indicado.
     *
     * @param attributeName El nombre del atributo por el que ordenar.
     */
    protected void addDescOrder(String attributeName)
    {
        this.addDescOrder(attributeName, null);
    }

    /**
     * A�ade un orden descendente por el atributo indicado.
     *
     * @param attributeName El nombre del atributo por el que ordenar.
     * @param path          El alias a la propiedad de la subentidad.
     */
    protected void addDescOrder(String attributeName, String path)
    {
        if (attributeName != null && attributeName.length() > 0)
        {
            Order orderToAdd = new Order();
            orderToAdd.setField(attributeName);
            orderToAdd.setPath(path);
            orderToAdd.setOrderType(Order.DESC);
            addOrder(orderToAdd);
        }
    }

}

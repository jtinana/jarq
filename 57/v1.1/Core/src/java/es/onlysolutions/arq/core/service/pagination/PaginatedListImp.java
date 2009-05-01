package es.onlysolutions.arq.core.service.pagination;

import es.onlysolutions.arq.core.accesobd.IConstanstAccesoDatos;
import es.onlysolutions.arq.core.configuration.Configuracion;
import org.displaytag.pagination.PaginatedList;
import org.displaytag.properties.SortOrderEnum;

import java.util.List;

public class PaginatedListImp implements PaginatedList
{
    private List list;
    private int pageNumber = 1;
    private int fullListSize;
    private Integer objectsPerPage;

    /**
     * Devuelve el tama�o total de la lista.
     *
     * @return El tama�o total de toda la lista.
     */
    public int getFullListSize()
    {
        return fullListSize;
    }

    /**
     * Devuelve el criterio para odenar externamente la lista.
     *
     * @return Devuelve el criterio para odenar externamente toda la lista.
     */
    public String getSortCriterion()
    {
        return null;
    }

    /**
     * Devuelve el SortOrderEnum con el que se indicar� la direcci�n de �rden de la lista.
     *
     * @return El objeto SortOrderEnum para ordenar la lista.
     */
    public SortOrderEnum getSortDirection()
    {
        return null;
    }

    /**
     * Devuelve el ID para localizar la lista en caso de que sea Cacheada en memoria.
     *
     * @return El ID con el que obtener la lista de memoria o null si no se cachea.
     */
    public String getSearchId()
    {
        return null;
    }

    /**
     * Establece el tama�o total de la lista.
     *
     * @param fullListSize El tama�o total de la lista a establecer.
     */
    public void setFullListSize(int fullListSize)
    {
        this.fullListSize = fullListSize;
    }

    /**
     * Devuelve la lista parcial actual.
     *
     * @return La lista parcial.
     */
    public List getList()
    {
        return list;
    }

    /**
     * Establece la lista parcial a mostrar.
     *
     * @param list La lista a establecer para este momento.
     */
    public void setList(List list)
    {
        this.list = list;
    }

    /**
     * Devuelve el numero de elementos por p�gina.<br>
     * Este metodo devuelve el tama�o de pagina definido en la configuracion si no se ha establecido ninguno previamente.
     *
     * @return El n�mero de elementos por p�gina.
     */
    public int getObjectsPerPage()
    {
        if (objectsPerPage == null)
        {
            objectsPerPage = Configuracion.getInteger(IConstanstAccesoDatos.PARAM_PAGESIZE);
        }
        return objectsPerPage;
    }

    /**
     * Establece el tama�o de p�gina.
     *
     * @param objectsPerPage El tama�o de p�gina.
     */
    public void setObjectsPerPage(int objectsPerPage)
    {
        this.objectsPerPage = objectsPerPage;
    }

    /**
     * Devuelve el n�mero de p�gina actual.
     *
     * @return El n�mero de p�gina actual.
     */
    public int getPageNumber()
    {
        return pageNumber;
    }

    /**
     * Establece el n�mero de p�gina actual.
     *
     * @param pageNumber El n�mero de p�gina a mostrar.
     */
    public void setPageNumber(int pageNumber)
    {
        this.pageNumber = pageNumber;
    }

    /**
     * Devuelve el numero total de p�ginas.
     *
     * @return El n�mero total de p�ginas.
     */
    public int getTotalPages()
    {
        double aux = Math.ceil(new Double(this.fullListSize).doubleValue() / new Double(this.objectsPerPage).doubleValue());

        return new Double(aux).intValue();
    }


    @Override
    public String toString()
    {
        return getClass().getName() + '(' + fullListSize + ')';
    }
}

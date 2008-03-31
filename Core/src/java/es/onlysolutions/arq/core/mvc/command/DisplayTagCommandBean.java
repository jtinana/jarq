package es.onlysolutions.arq.core.mvc.command;

/**
 * Command bean con los atributos, getter y setters adecuados para manejar el display tag.
 */
public class DisplayTagCommandBean extends AbstractCommandBean
{
    /**
     * Nombre del par�metro que se va a ordenar.
     */
    public String sort;

    /**
     * Nombre de la direccion de ordenacion (asc, desc).
     */
    public String dir;

    /**
     * Nombre del par�metro para indicar el n� de pag.
     */
    public int page = 1;

    /**
     * Nombre del par�metro para indicar el search id.
     */
    public String searchid;


    /**
     * Getter for property 'sort'.
     *
     * @return Value for property 'sort'.
     * @see #sort
     */
    public String getSort()
    {
        return sort;
    }

    /**
     * Setter for property 'sort'.
     *
     * @param sort Value to set for property 'sort'.
     * @see #sort
     */
    public void setSort(String sort)
    {
        this.sort = sort;
    }

    /**
     * Getter for property 'dir'.
     *
     * @return Value for property 'dir'.
     * @see #dir
     */
    public String getDir()
    {
        return dir;
    }

    /**
     * Setter for property 'dir'.
     *
     * @param dir Value to set for property 'dir'.
     * @see #dir
     */
    public void setDir(String dir)
    {
        this.dir = dir;
    }

    /**
     * Getter for property 'page'.
     *
     * @return Value for property 'page'.
     * @see #page
     */
    public int getPage()
    {
        return page;
    }

    /**
     * Setter for property 'page'.
     *
     * @param page Value to set for property 'page'.
     * @see #page
     */
    public void setPage(int page)
    {
        this.page = page;
    }

    /**
     * Getter for property 'searchid'.
     *
     * @return Value for property 'searchid'.
     * @see #searchid
     */
    public String getSearchid()
    {
        return searchid;
    }

    /**
     * Setter for property 'searchid'.
     *
     * @param searchid Value to set for property 'searchid'.
     * @see #searchid
     */
    public void setSearchid(String searchid)
    {
        this.searchid = searchid;
    }
}

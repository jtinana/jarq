package es.onlysolutions.arq.core.filter;

/**
 * Clase interna para almacenar los order que se hayan indicado y en el mismo orden.
 */
public class Order
{
    /**
     * Constante para indicar un Orde ascendente.
     */
    public static final Integer ASC = 1;
    /**
     * Constante para indicar un Orde descendente.
     */
    public static final Integer DESC = 2;

    private String field;
    private Integer orderType;
    private String path;


    /**
     * Getter for property 'path'.
     *
     * @return Value for property 'path'.
     * @see #path
     */
    public String getPath()
    {
        return path;
    }

    /**
     * Setter for property 'path'.
     *
     * @param path Value to set for property 'path'.
     * @see #path
     */
    public void setPath(String path)
    {
        this.path = path;
    }

    /**
     * Constructor sin parametros.
     */
    public Order()
    {
        super();
    }

    /**
     * Constructr con los parametros directamente.
     *
     * @param field     El nombre del campo a ordenar.
     * @param orderType El tipo de orden.
     * @see #ASC
     * @see #DESC
     */
    public Order(String field, Integer orderType)
    {
        this.field = field;
        this.orderType = orderType;
    }


    /**
     * Getter for property 'field'.
     *
     * @return Value for property 'field'.
     * @see #field
     */
    public String getField()
    {
        return field;
    }

    /**
     * Setter for property 'field'.
     *
     * @param field Value to set for property 'field'.
     * @see #field
     */
    public void setField(String field)
    {
        this.field = field;
    }

    /**
     * Getter for property 'orderType'.
     *
     * @return Value for property 'orderType'.
     * @see #orderType
     */
    public Integer getOrderType()
    {
        return orderType;
    }

    /**
     * Setter for property 'orderType'.
     *
     * @param orderType Value to set for property 'orderType'.
     * @see #orderType
     */
    public void setOrderType(Integer orderType)
    {
        this.orderType = orderType;
    }
}

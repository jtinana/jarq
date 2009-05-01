package es.onlysolutions.arq.core.filter;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase abstracta padre de todos los tipos de filtro.
 */
public abstract class AbstractFilter
{
    /**
     * Listado ordenado de los ordenes segun se tengan que ir a�adiendo.
     */
    protected List<Order> orders = new ArrayList<Order>(3);

    /**
     * Comprueba si un objeto pasado como parametro es valido para ser a�adido como filtro.
     * Comprueba el tipo de la clase y que no sea nulo o vacio. <br>
     * Deberia comprobarse mediante este metodo cada campo antes de a�adirlo como restriccion, de la siguiente forma:<br>
     * <br>
     * if( isEmptyOrWhiteSpace(objValue) )<br>
     * {<br>
     * &nbsp;&nbsp;&nbsp;addRestriction( Restrictions.eq("denominacion", objValue) )
     * }<br>
     *
     * @param objValue El objeto que se quiere a�adir como restriccion.
     * @return true si se puede a�adir, false en caso contrario.
     */
    protected boolean isEmptyOrWhiteSpace(Object objValue)
    {
        boolean result = false;

        if (objValue != null)
        {
            if (objValue instanceof String)
            {
                String strObjValue = (String) objValue;
                result = strObjValue.length() > 0;
            }
            else //En cualquier otro caso si no es nulo lo damos por bueno.
            {
                result = true;
            }
        }

        return result;
    }

    /**
     * Permite a�adir un orden ascendente a este filtro.
     * El orden en el que se a�aden los campos es el orden que se aplicara al dicho orden.
     *
     * @param order El nombre del atributo o campo al que a�adir el orden ascendente.
     */
    protected void addOrder(Order order)
    {
        this.orders.add(order);
    }


    /**
     * Getter for property 'orders'.
     *
     * @return Value for property 'orders'.
     * @see #orders
     */
    public List<Order> getOrders()
    {
        return orders;
    }

    /**
     * Setter for property 'orders'.
     *
     * @param orders Value to set for property 'orders'.
     * @see #orders
     */
    public void setOrders(List<Order> orders)
    {
        this.orders = orders;
    }

    /**
     * Sustituye el caracter * por el comodin de SQL (%).
     *
     * @param param El parametro a sustituir.
     * @return El String ya parseado.
     */
    protected String replaceJoker(String param)
    {
        return String.valueOf(param);
    }

    /**
     * Metodo para utilizar en filtros cuando se debe obtener un valor que represente un ID de una entidad.
     * Si dicho valor es negativo o 0 se devuelve un null. De esta forma se evitan valores de control de AJAX.
     *
     * @param param El parametro a chequear.
     * @return El valor si es positivo o null en cualquier otro caso.
     */
    protected Integer adjustFilterValue(Integer param)
    {
        Integer result = null;
        if (param != null)
        {
            if (param > 0)
            {
                result = param;
            }
        }

        return result;
    }
}

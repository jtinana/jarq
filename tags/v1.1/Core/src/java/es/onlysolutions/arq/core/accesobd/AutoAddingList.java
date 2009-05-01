package es.onlysolutions.arq.core.accesobd;

/**
 * Esta clase representa una lista que optimiza el rendimiento de memoria.
 */

public class AutoAddingList
{

    private Object[] _elements;
    private int _actualSize;

    /**
     * Constructor con tama�o inicial.
     *
     * @param initialSize Establece el tama�o inicial de la lista.
     */

    public AutoAddingList(int initialSize)
    {
        _elements = new Object[initialSize];
    }

    /**
     * Establece el objeto <param>value</param>  en la posicion indicada por <param>index</param><br>
     * Si a este metodo se le pasa un index que no es superior al tama�o de la lista, esta se redimensiona automaticamente.
     *
     * @param index La posicion donde insertar
     * @param value El objeto a insertar
     */

    public void set(int index, Object value)
    {
        if (index >= _elements.length)
        {
            int newSize = _elements.length << 1;//Doblamos la capacidad
            if (index >= newSize)
            {
                newSize = index + 1;
            }
            Object[] newElements = new Object[newSize];
            System.arraycopy(_elements, 0, newElements, 0, _elements.length);
            _elements = newElements;
        }

        _elements[index] = value;

        if (_actualSize <= index)
        {
            _actualSize = index + 1;
        }
    }

    /**
     * Devuelve el objeto que se encuentra en la posicion pasado como argumento.
     *
     * @param index la posicion de la que obtener el objeto.
     * @return El objeto devuelto.
     * @throws ArrayIndexOutOfBoundsException Si se le pasa un index incorrecto.
     */

    public Object get(int index)
    {
        checkRange(index);
        return _elements[index];
    }

    /**
     * Este metodo comprueba si seguimos dentro del rango de la lista.
     *
     * @param index La posici�n a chequear.
     */

    private void checkRange(int index)
    {
        if (index >= _actualSize || index < 0)
        {
            throw new ArrayIndexOutOfBoundsException("Index: " + index + "; Size: " + _elements.length);
        }
    }

    /**
     * Devuelve el numero de elementos de la lista.
     *
     * @return el numero de objetos que contiene esta lista.
     */

    public int size()
    {
        return _actualSize;
    }

    /**
     * Devuelve el tama�o del array de objetos que contiene esta clase.<br>
     * Este tama�o es el numero maximo de objetos que se pueden insertar sin redimensionar el array.
     *
     * @return el tama�o actual del array de objetos que contiene esta clase
     */

    public int capacity()
    {
        return _elements.length;
    }

    /**
     * Inicializa la lista y limpia su contenido.
     */

    public void clear()
    {
        _elements = new Object[10];
    }

    /**
     * Comprueba si un elemento esta dentro de la lista.
     *
     * @param o El objeto a comprobar si esta contenido.
     * @return true si esta en la lista.
     */

    public boolean contains(Object o)
    {
        boolean notFind = false;
        int index = 0;

        while (!notFind && index < _actualSize)
        {
            if (_elements[index] == null)
            {
                notFind = o == null;
            }
            else
            {
                notFind = _elements[index].equals(o);
            }
            index++;
        }

        return notFind;
    }

    /**
     * Obtiene una representacion en String del objeto.<br>
     * No se incluyen los datos concretos del objeto, tan solo el tama�o, para mejorar el rendimiento.
     *
     * @return Un String representando el objeto.
     */

    public String toString()
    {
        return "ActualSize: " + _actualSize;
    }
}

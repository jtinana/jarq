package es.onlysolutions.arq.core.mvc.utils;

import java.io.Serializable;

/**
 * Bean para los combos en Spring. Contiene un atributo 'key' y un 'value' que contendran la clave y valor respectivamente
 * para utilizarlos en los options.
 */
public class Option implements Serializable
{
    private Object key;
    private Object value;


    /**
     * Contructor sin parametros.
     */
    public Option()
    {
        super();
    }

    /**
     * Constructor con ambos valores.
     *
     * @param key   La clave del option.
     * @param value El valor de la clave.
     */
    public Option(Serializable key, Object value)
    {
        this.key = key;
        this.value = value;
    }

    /**
     * Getter for property 'key'.
     *
     * @return Value for property 'key'.
     * @see #key
     */
    public Object getKey()
    {
        return key;
    }

    /**
     * Setter for property 'key'.
     *
     * @param key Value to set for property 'key'.
     * @see #key
     */
    public void setKey(Serializable key)
    {
        this.key = key;
    }

    /**
     * Getter for property 'value'.
     *
     * @return Value for property 'value'.
     * @see #value
     */
    public Object getValue()
    {
        return value;
    }

    /**
     * Setter for property 'value'.
     *
     * @param value Value to set for property 'value'.
     * @see #value
     */
    public void setValue(Object value)
    {
        this.value = value;
    }


    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        Option option = (Option) o;

        if (key != null ? !key.equals(option.key) : option.key != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        return (key != null ? key.hashCode() : 0);
    }

    public String toString()
    {
        return "Option{" + "key=" + key + ", value=" + value + '}';
    }

    /**
     * Setter para evitar conflictos con la palabra reservada 'key' en SQL.
     *
     * @param keyValue El valor a establecer como clave.
     */
    public void setClave(Serializable keyValue)
    {
        this.key = keyValue;
    }

    public String getAsString()
    {
        return String.valueOf(this.value);
    }
}

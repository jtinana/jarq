package es.onlysolutions.arq.core.mvc.validator;

import org.springframework.util.Assert;

/**
 * Validacion de uno de los campos con sus condiciones.
 */
class Validation implements Comparable
{
    private String attribute;
    private Integer maxLenght;
    private Boolean required = Boolean.FALSE;
    private String fieldAlias;


    /**
     * Getter for property 'fieldAlias'.
     *
     * @return Value for property 'fieldAlias'.
     * @see #fieldAlias
     */
    public String getFieldAlias()
    {
        return fieldAlias;
    }

    /**
     * Setter for property 'fieldAlias'.
     *
     * @param fieldAlias Value to set for property 'fieldAlias'.
     * @see #fieldAlias
     */
    public void setFieldAlias(String fieldAlias)
    {
        this.fieldAlias = fieldAlias;
    }

    /**
     * Getter for property 'attribute'.
     *
     * @return Value for property 'attribute'.
     * @see #attribute
     */
    public String getAttribute()
    {
        return attribute;
    }

    /**
     * Setter for property 'attribute'.
     *
     * @param attribute Value to set for property 'attribute'.
     * @see #attribute
     */
    public void setAttribute(String attribute)
    {
        this.attribute = attribute;
    }

    /**
     * Getter for property 'maxLenght'.
     *
     * @return Value for property 'maxLenght'.
     * @see #maxLenght
     */
    public Integer getMaxLenght()
    {
        return maxLenght;
    }

    /**
     * Setter for property 'maxLenght'.
     *
     * @param maxLenght Value to set for property 'maxLenght'.
     * @see #maxLenght
     */
    public void setMaxLenght(Integer maxLenght)
    {
        this.maxLenght = maxLenght;
    }

    /**
     * Getter for property 'required'.
     *
     * @return Value for property 'required'.
     * @see #required
     */
    public Boolean getRequired()
    {
        return required;
    }

    /**
     * Setter for property 'required'.
     *
     * @param required Value to set for property 'required'.
     * @see #required
     */
    public void setRequired(Boolean required)
    {
        this.required = required;
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

        Validation that = (Validation) o;

        if (attribute != null ? !attribute.equals(that.attribute) : that.attribute != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        return (attribute != null ? attribute.hashCode() : 0);
    }

    public int compareTo(Object object)
    {
        Assert.isInstanceOf(Validation.class, object, "Solo se permiten compara objetos del tipo " + Validation.class.getName());

        return this.attribute.compareTo(((Validation) object).getAttribute());
    }
}

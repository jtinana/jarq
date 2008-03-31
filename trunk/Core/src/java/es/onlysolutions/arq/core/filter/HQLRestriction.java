package es.onlysolutions.arq.core.filter;

/**
 * Restriccion para un filtro HQL.
 */
public class HQLRestriction extends AbstractRestriction
{
    /**
     * Condicion de esta restriccion.
     */
    private String hqlCondition;

    /**
     * Lista de parametros para esta condicion.
     */
    private Object param;

    /**
     * El alias para sustituir el parametro.
     */
    private String alias;

    /**
     * Getter for property 'hqlCondition'.
     *
     * @return Value for property 'hqlCondition'.
     * @see #hqlCondition
     */
    public String getHqlCondition()
    {
        return hqlCondition;
    }

    /**
     * Setter for property 'hqlCondition'.
     *
     * @param hqlCondition Value to set for property 'hqlCondition'.
     * @see #hqlCondition
     */
    public void setHqlCondition(String hqlCondition)
    {
        this.hqlCondition = hqlCondition;
    }


    /**
     * Getter for property 'param'.
     *
     * @return Value for property 'param'.
     * @see #param
     */
    public Object getParam()
    {
        return param;
    }

    /**
     * Setter for property 'param'.
     *
     * @param param Value to set for property 'param'.
     * @see #param
     */
    public void setParam(Object param)
    {
        this.param = param;
    }


    /**
     * Getter for property 'alias'.
     *
     * @return Value for property 'alias'.
     * @see #alias
     */
    public String getAlias()
    {
        return alias;
    }

    /**
     * Setter for property 'alias'.
     *
     * @param alias Value to set for property 'alias'.
     * @see #alias
     */
    public void setAlias(String alias)
    {
        this.alias = alias;
    }


}

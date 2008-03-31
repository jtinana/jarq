package es.onlysolutions.arq.core.filter;

/**
 * Clase padre para todas las restricciones.
 */
public abstract class AbstractRestriction
{
    /**
     * flag que indica si la condicion es estatica.
     */
    private boolean isStatic = true;

    /**
     * Getter for property 'static'.
     *
     * @return Value for property 'static'.
     */
    public boolean isStatic()
    {
        return isStatic;
    }

    /**
     * Getter for property 'static'.
     *
     * @return Value for property 'static'.
     */
    public boolean getStatic()
    {
        return isStatic;
    }

    /**
     * Setter for property 'static'.
     *
     * @param aStatic Value to set for property 'static'.
     */
    public void setStatic(boolean aStatic)
    {
        isStatic = aStatic;
    }

}

package es.onlysolutions.arq.core.accesobd;

/**
 * Bean que contiene el SQL y el cached prepared Statement con los parametros necesarios para ejecutarla.<br>
 * El CachedPreparedStatement puede ser nulo si el SQL no tiene par�metros.
 */

class SQL
{
    private String sql;
    private CachedPreparedStatement cachedPreparedStatement;

    /**
     * Devuelve el SQL del objeto.
     *
     * @return El SQL que contiene el objeto.
     */
    public String getSql()
    {
        return sql;
    }

    /**
     * Establece el SQL del objeto.
     *
     * @param sql El sql a establecer.
     */
    void setSql(String sql)
    {
        this.sql = sql;
    }

    /**
     * Obtiene el CachedPreparedStatement con los par�metros.
     *
     * @return El CachedPreparedStatement con los par�metros.
     */
    public CachedPreparedStatement getCachedPreparedStatement()
    {
        return cachedPreparedStatement;
    }

    /**
     * Establece el CachedPreparedStatement con los par�metros de la query.
     *
     * @param cachedPreparedStatement El objeto CachedPreparedStatement a establecer.
     */
    void setCachedPreparedStatement(CachedPreparedStatement cachedPreparedStatement)
    {
        this.cachedPreparedStatement = cachedPreparedStatement;
    }
}

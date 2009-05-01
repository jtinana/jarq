package es.onlysolutions.arq.core.accesobd;

/**
 * Objeto que contiene la clausula Where de la consulta y su CachedPreparedStatement<br>
 * con los par�metros.
 */
class WhereObject
{
    private CachedPreparedStatement cachedPreparedStatement;

    private String sqlWhere;


    public CachedPreparedStatement getCachedPreparedStatement()
    {
        return cachedPreparedStatement;
    }

    public void setCachedPreparedStatement(CachedPreparedStatement cachedPreparedStatement)
    {
        this.cachedPreparedStatement = cachedPreparedStatement;
    }

    public String getSqlWhere()
    {
        return sqlWhere;
    }

    public void setSqlWhere(String sqlWhere)
    {
        this.sqlWhere = sqlWhere;
    }


    public String toString()
    {
        if (cachedPreparedStatement != null)
        {
            return "WhereObject{" + "sqlWhere='" + sqlWhere + '\'' + " ParamCount=" + cachedPreparedStatement.getNumberOfElements() + '}';
        }
        else
        {
            return "WhereObject{" + "sqlWhere='" + sqlWhere + '\'' + " ParamCount=0 }";
        }
    }
}

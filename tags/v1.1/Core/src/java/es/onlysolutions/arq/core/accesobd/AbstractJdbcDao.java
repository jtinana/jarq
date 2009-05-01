package es.onlysolutions.arq.core.accesobd;

import es.onlysolutions.arq.core.accesobd.exception.CachedException;
import es.onlysolutions.arq.core.accesobd.exception.DaoException;
import es.onlysolutions.arq.core.log.LoggerGenerator;
import org.apache.commons.logging.Log;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.*;

/**
 * Clase abstracta de la que heredan todas las clases DAO que quieran acceder a BD mediante JDBC.<br>
 * Propciorna los getter y setter adecuados para su configuracion mediante Spring.<br>
 * En la configuraci�n de Spring, dl datasource debe tener como nombre siempre <b>datasource</b>.
 */
public class AbstractJdbcDao
{
    /**
     * El logger de la clase.
     */
    protected final Log logger = LoggerGenerator.getLogger(this.getClass());

    /**
     * El datasource a establecer por la configuracion de Spring.
     */
    private DataSource datasource;

    /**
     * Establece el DataSource.<br>
     * Es llamado automaticamente por el IoC de Spring.<br>
     * <b>Nunca debe ser llamado desde el propio codigo.</b>
     *
     * @param datasource El datasource a establecer.
     */
    public void setDatasource(DataSource datasource)
    {
        this.datasource = datasource;
    }

    /**
     * Ejecuta la query pasada como parametro con los argumentos del PreparedStatement.
     *
     * @param cpt El CachedPreparedStatement con los argumentos a ejecutar.
     * @return El ResultSet
     * @throws DaoException Si ocurre algun error durante la ejecucion del metodo.
     */
    protected ResultSet executeQuery(CachedPreparedStatement cpt) throws DaoException
    {
        Connection con = null;
        ResultSet rs = null;
        ResultSet csr = null;
        PreparedStatement preparedSt = null;
        String sql = cpt.getSql();

        try
        {
            con = getConnection();
            preparedSt = con.prepareStatement(sql);
            cpt.setParameters(preparedSt);
            rs = preparedSt.executeQuery();
            csr = new CachedResultSet(rs);
        }
        catch (CachedException e)
        {
            logger.error("Error al tratar de copiar los parametros al CachedResultSet o al obtener la conexion", e);
            throw e;
        }
        catch (SQLException e)
        {
            logger.error("Error al tratar de ejecutar la query: " + sql, e);
            throw new DaoException("Error al tratar de ejecutar la query: " + sql, e);
        }
        catch (Throwable t)
        {
            logger.error("Error inesperado al tratar de ejecutar la query: " + sql, t);
            throw new DaoException("Error inesperado al tratar de ejecutar la query: ", t);
        }
        finally
        {
            closeResultSet(rs);
            closeStatement(preparedSt);
            closeConnection(con);
        }

        return csr;

    }

    /**
     * Obtiene la conexi�n para ejecutar la query.
     *
     * @return El objeto Connection ya abierto.
     * @throws SQLException Si ocurre algun error al obtener la conexion.
     * @throws DaoException Si ocurre algun error al obtener la conexion.
     */
    private Connection getConnection() throws SQLException, DaoException
    {
        Connection con = datasource.getConnection();
        con.setAutoCommit(true);
        return con;
    }

    /**
     * Cierra un resultset silenciosamente.
     *
     * @param rs El ResultSet a cerrar.
     */
    private void closeResultSet(ResultSet rs)
    {
        if (rs != null)
        {
            try
            {
                rs.close();
            }
            catch (SQLException ex)
            {
                logger.error("No es posible cerrar el resultSet: " + rs, ex);
            }
            catch (RuntimeException t)
            {
                logger.error("Excepcion inesperada al cerrar el ResultSet: " + rs, t);
            }
        }
        else
        {
            logger.warn("Se trataba de cerrar un ResultSet nulo.");
        }
    }

    /**
     * Cierra un statement.
     *
     * @param stmt statement a cerrar
     */
    private void closeStatement(Statement stmt)
    {
        if (stmt != null)
        {
            try
            {
                stmt.close();
            }
            catch (SQLException ex)
            {
                logger.error("No es posible cerrar el statement: " + stmt, ex);
            }
            catch (RuntimeException t)
            {
                logger.error("Excepcion inesperada. No es posible cerrar el statement: " + stmt, t);
            }
        }
        else
        {
            logger.warn("Se trataba de cerrar un Statement nulo.");
        }
    }

    /**
     * Cierra una conexi�n.
     *
     * @param conn connection a cerrar
     */
    private void closeConnection(Connection conn)
    {
        try
        {
            if (conn != null)
            {
                conn.close();
            }
            else
            {
                logger.warn("Se trataba de cerrar una conexion nula");
            }
        }
        catch (SQLException ex)
        {
            logger.error("No es posible cerrar la Connection: " + conn, ex);
        }
        catch (RuntimeException t)
        {
            logger.error("Excepcion inesperada. No es posible cerrar la conexion: " + conn, t);
        }
    }

    /**
     * Obtiene una nueva instancia de un JdbcTemplate.
     *
     * @return La nueva instancia del JdbcTemplate.
     */
    protected JdbcTemplate getJdbcTemplate()
    {
        return new JdbcTemplate(datasource);
    }

}

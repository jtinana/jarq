package es.onlysolutions.arq.core.accesobd;

import org.springframework.jdbc.object.StoredProcedure;
import org.springframework.util.Assert;

import java.util.Iterator;
import java.util.Map;

/**
 * Clase abstracta para los procedimientos almacenados que se necesiten ejecutar.
 * Deben ser ejecutados desde un JdbcDao, ya que en el constructor se le debe pasar
 * el JdbcTemplate del que se deseen obtener las conexiones.
 */
public abstract class AbstractStoredProcedure extends StoredProcedure
{
    /**
     * Ejecuta el procedimiento almacenado con los parametros pasados como parametro.
     * Comprueba adicionalmente que bajo la clave indicada se devuelva un resultado no nulo.
     *
     * @param params    El mapa de parametros a ejecutar. No puede ser null.
     * @param resultKey La clave bajo la que comprobar que existen resultados. No pueder ser vacia.
     * @return El objeto resultado que esta bajo la clave resultKey.
     */
    protected Object executeWithResult(Map params, String resultKey)
    {
        Assert.hasLength(resultKey, "Se debe indicar una resultKey no vacia");
        Assert.notNull(params, "No se puede indicar un mapa nulo");

        Map resultado = super.execute(params);
        Object finalResult = resultado.get(resultKey);

        if (finalResult == null)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("La ejecucion del procedimiento ");
            sb.append(getSql());
            sb.append(" devolvio null");
            sb.append('\n');
            sb.append("Parametros del procedimiento:");
            sb.append('\n');
            Iterator it = params.entrySet().iterator();
            while (it.hasNext())
            {
                Map.Entry entry = (Map.Entry) it.next();
                sb.append(entry.getKey());
                sb.append(" -> ");
                sb.append(entry.getValue());
                sb.append('\n');
            }
            throw new IllegalStateException(sb.toString());
        }

        return finalResult;
    }

}

package es.onlysolutions.arq.core.cache;

import es.onlysolutions.arq.core.cache.exception.MemcacheException;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.MemcachedClient;
import org.springframework.util.Assert;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

/**
 * Wrapper sobre el objeto net.spy.memcached.MemcachedClient para introducirlo como bean en el IoC.
 * Esta clase no implementa el patron Singleton, por lo que de no incluirse como tal en el IoC se debera controlar
 * de forma externa.
 */
public class MemcacheClientWrapper
{
    private MemcachedClient memcachedClient;

    /**
     * Constructor de la clase sin parametros para cumplir con el standar de JavaBean.
     */
    public MemcacheClientWrapper()
    {
        super();
    }

    /**
     * Establece la lista de servidores de Memcache. En cada ejecucion se instancia de nuevo un cliente de Memcache,
     * por lo que se sustituyen los servidores indicados anteriormente.
     * @param addresses La lista de servidores indicados de forma host:port.
     */
    public void setServers( List<String> addresses )
    {
        if( addresses != null && addresses.size() > 0 )
        {
            String strAddresses = "";
            for (String address : addresses)
            {
                strAddresses += address + " ";
            }
            List<InetSocketAddress> inetSocketAddressList = AddrUtil.getAddresses( strAddresses );

            try
            {
                this.memcachedClient = new MemcachedClient( inetSocketAddressList );
            }
            catch (IOException e)
            {
                throw new MemcacheException("Error al tratar de instanciar el objeto " +
                        "net.spy.memcached.MemcachedClient con los servidores: " + strAddresses, e);
            }
        }
    }

    /**
     * Devuelve el cliente de Memcache instanciado y correctamente configurado.
     * Si no estuviera correctamente instancia se obtiene una excepcion.
     * @return El cliente de Memcache instanciado y correctamente configurado.
     * @see net.spy.memcached.MemcachedClient  
     */
    public MemcachedClient client()
    {
        Assert.notNull( this.memcachedClient, "No se han indicado servidores de Memcache. Utilize la propiedad \"servers\"  en la configuracion del bean" );
        return this.memcachedClient;
    }


}

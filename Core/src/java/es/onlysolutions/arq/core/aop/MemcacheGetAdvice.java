package es.onlysolutions.arq.core.aop;

import es.onlysolutions.arq.core.aop.helper.MethodHelper;
import es.onlysolutions.arq.core.cache.IConstantsMemcache;
import es.onlysolutions.arq.core.cache.MemcacheClientWrapper;
import es.onlysolutions.arq.core.cache.exception.MemcacheException;
import es.onlysolutions.arq.core.configuration.Configuracion;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Aspecto para cachear llamadas a los metodos en Memcache.
 */
public class MemcacheGetAdvice implements Ordered, MethodInterceptor
{
    /**
     * El logger de la clase.
     */
    private static final Log logger = LogFactory.getLog(MemcacheGetAdvice.class);

    private MemcacheClientWrapper memcacheClient;
    

    public void setMemcacheClient(MemcacheClientWrapper memcacheClient)
    {
        this.memcacheClient = memcacheClient;
    }

    /**
     * Devuelve siempre el valor Ordered.LOWEST_PRECEDENCE.
     * @return Devuelve siempre el valor Ordered.LOWEST_PRECEDENCE.
     */
    public int getOrder()
    {
        return Ordered.LOWEST_PRECEDENCE;
    }

    /**
     * Invocacion del metodo original. Si se encuentra en Memcache se devuelve el valor cacheado,
     * en otro caso, se ejecuta el metodo, se cachea, y se devuelve el valor.
     * @param methodInvocation El metodo que se esta invocando.
     * @return El resultado del metodo.
     * @throws Throwable Si ocurre algo durante la ejecucion del metodo.
     */
    public Object invoke(MethodInvocation methodInvocation) throws Throwable
    {
        Object result = getFromCache(methodInvocation);

        if( result == null )//No esta en cache
        {
            result = methodInvocation.proceed();
            putInCache( result, methodInvocation );

            if( logger.isDebugEnabled() )
            {
                logger.debug("El metodo " + MethodHelper.toReadableString(methodInvocation.getMethod()) + " no estaba cacheado. Se cachea su resultado");
            }
            
        }

        return result;
    }

    /**
     * Pone en cache el objeto pasado como parametro a menos que sea null.
     * @param objectToPutInCache El objeto a poner en Memcache.
     * @param methodInvocation El metodo invocandose en ese momento.
     */
    private void putInCache(Object objectToPutInCache, MethodInvocation methodInvocation)
    {
        this.putInCache( objectToPutInCache, MethodHelper.getKeyFromMethod(methodInvocation.getMethod(), methodInvocation.getArguments()) );

        /**
         * Actualizamos las listas de metodos para cada argumento, de forma que el aspecto de actualizacion
         * pueda localizar mas tarde los metodos a limpiar en cache cuando se actualice algun parametro.
         */
        addCacheArgumentsList( methodInvocation.getMethod(), methodInvocation.getArguments() );

    }

    /**
     * Pone en cache el objeto indicado bajo la clave indicada.
     * @param objectToPutInCache El objeto a poner en cache.
     * @param key La clave bajo la que almacenarla.
     * @see net.spy.memcached.MemcachedClient#add(String, int, Object)
     */
    private void putInCache(Object objectToPutInCache, String key)
    {
        if( objectToPutInCache != null )
        {
            Future<Boolean> futureResult = null;
            try
            {
                Integer expiration = Configuracion.getInteger(IConstantsMemcache.EXPIRATION_TIME);
                if( expiration == null )
                {
                    expiration = 60 * 60 * 24 * 30;
                }

                futureResult = this.memcacheClient.client().add(
                        key,
                        expiration,
                        objectToPutInCache);

                Integer timeOut = Configuracion.getInteger(IConstantsMemcache.TIMEOUT);

                if( !futureResult.get(timeOut, TimeUnit.SECONDS ) )
                {
                    String msg = "No se ha podido insertar en cache el objeto " +
                            MethodHelper.getKeyFromArgument(objectToPutInCache) + " para el metodo " +
                            key;
                    MemcacheException m = new MemcacheException(msg);
                    logger.error(msg, m);
                    throw m;
                }

            }
            catch ( InterruptedException e )
            {
                if( futureResult != null )
                {
                    futureResult.cancel(false);
                }
                logger.error("Error al tratar de poner en cache el objeto " + MethodHelper.getKeyFromArgument(objectToPutInCache));
                throw new MemcacheException("Error al tratar de poner en cache el objeto " + MethodHelper.getKeyFromArgument(objectToPutInCache), e);
            }
            catch (ExecutionException e)
            {
                if( futureResult != null )
                {
                    futureResult.cancel(false);
                }
                logger.error("Error al tratar de poner en cache el objeto " + MethodHelper.getKeyFromArgument(objectToPutInCache));
                throw new MemcacheException("Error al tratar de poner en cache el objeto " + MethodHelper.getKeyFromArgument(objectToPutInCache), e);
            }
            catch (TimeoutException e)
            {
                if( futureResult != null )
                {
                    futureResult.cancel(false);
                }
                logger.error("Error al tratar de poner en cache el objeto " + MethodHelper.getKeyFromArgument(objectToPutInCache));
                throw new MemcacheException("Error al tratar de poner en cache el objeto " + MethodHelper.getKeyFromArgument(objectToPutInCache), e);
            }
        }
    }

    /**
     * Actualiza la lista de metodos cacheados para cada parametro que ejecuta el metodo, o la crea de no existir ya.
     * @param method El metodo que se esta ejecutando.
     * @param arguments El array de argumentos.
     */
    private void addCacheArgumentsList(Method method, Object[] arguments)
    {
        if( arguments != null )
        {
            for( Object arg : arguments )
            {
                String argKey = MethodHelper.getKeyFromArgument(arg);

                Object keyValue = getFromCache( argKey );

                //El nuevo valor a insertar.
                String newKeyValueToAdd = MethodHelper.getKeyFromMethod( method, arguments );

                if( keyValue != null )
                {
                    Assert.isInstanceOf( List.class, keyValue, "Error de almacenamiento en cache. Ha habido una colision en la clave " +
                            argKey + " y no se ha obtenido un objeto de cache del tipo esperado (" + List.class.getName() + ")" );

                    List<String> argList = (List<String>) keyValue;//La lista de metodos a refrescar ya en cache.
                    argList.add( newKeyValueToAdd );
                    putInCache( argList, argKey );
                }
                else //No existia nada, asi que se inserta un nuevo objeto.
                {
                    List<String> argList = new ArrayList<String>(3);
                    argList.add( newKeyValueToAdd );
                    putInCache( argList, argKey );
                }
            }
        }
    }

    /**
     * Obtiene de Memcache el resultado del metodo almacenado en cache, o null si no lo esta.<br/>
     * El TIMEOUT de la peticion GET se controla mediante la propiedad <b>memcache.timeout</b> definida en la configuracion.
     * @param methodInvocation El metodo a invocar.
     * @return El resultado almacenado en Memcache, o null si no estaba almacenado o ocurre algun error al obtener el valor.
     */
    private Object getFromCache(MethodInvocation methodInvocation)
    {
        return getFromCache( MethodHelper.getKeyFromMethod(methodInvocation.getMethod(), methodInvocation.getArguments()) );
    }

    /**
     * Obtiene de Memcache el objeto almacenado bajo la clave 'key'.
     * @param key La clave de la que obtener el valor.
     * @return El valor o null si no existe actualmente nada cacheado.
     */
    private Object getFromCache( String key )
    {
        Object myObj = null;
        Future<Object> future = memcacheClient.client().asyncGet( key );

        Integer timeOut = Configuracion.getInteger(IConstantsMemcache.TIMEOUT);
        if( timeOut == null )
        {
            timeOut = 5;
        }

        try
        {
            myObj = future.get(timeOut, TimeUnit.SECONDS);

            if( logger.isDebugEnabled() )
            {
                if( myObj != null )
                {
                    logger.debug("Obtenemos de cache: " + myObj + " para la clave " + key);
                }
                else
                {
                    logger.debug("En cache no habia nada para la key " + key);
                }
            }

        }
        catch (TimeoutException e)
        {
            future.cancel(false);
            logger.error("El TimeOut ha sido excedido, se interrumpe la ejecucion y devolvemos null",e);
        }
        catch (ExecutionException e)
        {
            future.cancel(false);
            logger.error("Error durante la ejecucion, se interrumpe la ejecucion y devolvemos null",e);
        }
        catch (InterruptedException e)
        {
            future.cancel(false);
            logger.error("Error de interrupcion del Thread, se interrumpe la ejecucion y devolvemos null",e);
        }

        return myObj;
    }


}

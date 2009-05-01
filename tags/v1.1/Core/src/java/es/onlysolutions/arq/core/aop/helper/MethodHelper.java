package es.onlysolutions.arq.core.aop.helper;

import es.onlysolutions.arq.core.cache.IConstantsMemcache;

import java.lang.reflect.Method;

/**
 * Helper con metodos para manejar objetos Method y ayuda en la API de reflejtion.
 */
public final class MethodHelper
{
    /**
     * Devuelve un String legible para localizar el metodo pasasdo como parametro de forma sencilla.
     * @param m El metodo del que obtener su representacion en String.
     * @return Un String legible para localizar el metodo pasasdo como parametro de forma sencilla.
     */
    public static String toReadableString( Method m )
    {
        return m.getClass().getName() + "-" + m.getName();
    }

    /**
     * Devuelve la clave para almacenar en Memcache sin colisiones este metodo.
     * Esta clave tiene en cuenta la clase que lo invoca, el nombre del metodo y los parametros que se le estan pasando.
     * @param method El metodo del que obtener la clave.
     * @param arguments La lista de argumentos que se le pasa a este metodo.
     * @return La clave como un String.
     */
    public static String getKeyFromMethod(Method method, Object[] arguments)
    {
        String className = method.getDeclaringClass().getName();
        String methodName = method.getName();

        StringBuilder sb = new StringBuilder( className.length() + methodName.length() + 13 );

        sb.append( className );
        sb.append( IConstantsMemcache.KEY_SEPARATOR );
        sb.append( methodName );

        if( arguments != null && arguments.length > 0 )
        {
            for (Object argument : arguments)
            {
                sb.append(IConstantsMemcache.KEY_SEPARATOR);
                sb.append( getKeyFromArgument( argument ) );
            }
        }

        return sb.toString();
    }

    /**
     * Obtiene la clave a partir de un objeto pasado como parametro.
     * Se comprueban distintas isntancias de la clase para obtener el mejor hash de dicha clase.
     * @param argument El argumento del que obtener la clave.
     * @return La clave unica.
     */
    public static String getKeyFromArgument(Object argument)
    {
        String result = null;

        if( argument != null)
        {
            result += argument.getClass().getName() + IConstantsMemcache.KEY_SEPARATOR + argument.hashCode();
        }

        return result;
    }
}

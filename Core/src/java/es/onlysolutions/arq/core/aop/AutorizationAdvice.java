package es.onlysolutions.arq.core.aop;

import es.onlysolutions.arq.core.auth.IUserSettings;
import es.onlysolutions.arq.core.auth.UserManager;
import es.onlysolutions.arq.core.auth.exception.AutorisationException;
import es.onlysolutions.arq.core.log.LoggerGenerator;
import org.apache.commons.logging.Log;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.core.Ordered;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Aspecto para comprobar si el usuario logueado actualmente en la session tiene el permiso para la ejecucion
 * del metodo del facade.
 * En caso de que un metodo no este definido en la configuracion se permite siempre su ejecucion.
 * <b>Debe ejecutarse siempre previamente a cualquier inicio de advice de transaccionalidad</b>
 */
public class AutorizationAdvice implements Ordered, MethodBeforeAdvice
{
    /**
     * El logger de la clase.
     */
    private static final Log logger = LoggerGenerator.getLogger(AutorizationAdvice.class);

    /**
     * Properties con las relaciones de metodo y permiso de cada facade.
     */
    private Properties permissions = new Properties();

    /**
     * Constructor del Aspecto. Lee de la configuracion la relacione de permisos y metodos de facade.
     *
     * @param permissionsFile El nombre del fichero a buscar en el classpath. Si se desea buscar desde la raiz se debe indicar un caracter '/' al comienzo del nombre.
     */
    public AutorizationAdvice(String permissionsFile)
    {
        readConfiguration(permissionsFile);
    }

    /**
     * Metodo que lee la configuraci�n de los permisos y prepara la clase para cachearlos.
     *
     * @param permissionsFile El nombre del fichero a buscar en el classpath. Si se desea buscar desde la raiz se debe indicar un caracter '/' al comienzo del nombre.
     */
    private void readConfiguration(String permissionsFile)
    {
        InputStream in = getClass().getResourceAsStream(permissionsFile);

        if (in == null)
        {
            throw new IllegalArgumentException("No se ha podido encontrar el fichero: " + permissionsFile + " en el classPath");
        }

        try
        {
            permissions.load(in);
        }
        catch (IOException e)
        {
            throw new BeanInitializationException("Error al tratar de cargar la configuracion del fichero: " + permissionsFile);
        }

        /**
         * Procedemos a validar si existen clases indicadas para el permiso que no existan en el classpath.
         */
        int correctPermissions = 0;
        int errorPermissions = 0;
        Iterator itProps = permissions.entrySet().iterator();
        while (itProps.hasNext())
        {
            Map.Entry entry = (Map.Entry) itProps.next();

            String permissionEntry = (String) entry.getKey();
            if (checkPermissionEntry(permissionEntry))
            {
                logger.info(entry.getKey() + " -> " + entry.getValue());
                correctPermissions++;
            }
            else
            {
                errorPermissions++;
            }
        }

        logger.info("Leidos " + correctPermissions + " permisos CORRECTOS de la configuracion");
        logger.info("Leidos " + errorPermissions + " permisos ERRONEOS de la configuracion");

    }

    /**
     * Comprueba que la entrada indicada sea una entrada de permiso correcta.
     *
     * @param permissionEntry La entrada de permiso siguiente la nomenclatura [nombreClase]@[nombreMetodo]
     * @return true si es correcto, false en caso contrario. En caso de devolver false, loguea a nivel ERROR.
     */
    private boolean checkPermissionEntry(String permissionEntry)
    {
        boolean result = false;
        if (isConsistentEntry(permissionEntry))
        {
            int indexOfArroba = permissionEntry.indexOf('@');
            String className = permissionEntry.substring(0, indexOfArroba);
            String methodName = permissionEntry.substring(indexOfArroba + 1, permissionEntry.length());

            Object objInstance = null;
            try
            {
                Class clazz = Class.forName(className);
                objInstance = clazz.newInstance();
            }
            catch (ClassNotFoundException e)
            {
                logger.error("La clase indicada en la entrada '" + permissionEntry + "' no existe", e);
            }
            catch (IllegalAccessException e)
            {
                logger.error("Acceso ilegal al tratar de instanciar la clase indicada en la entrada: " + permissionEntry, e);
            }
            catch (InstantiationException e)
            {
                logger.error("No se ha podido instanciar la clase de la entrada: " + permissionEntry, e);
            }

            if (objInstance != null)
            {
                //Chequeamos que contenga el metodo indicada en la entrada.
                Method[] methods = objInstance.getClass().getMethods();
                for (int index = 0; index < methods.length && !result; index++)
                {
                    Method method = methods[index];
                    if (method.getName().equals(methodName))
                    {
                        result = true;
                    }
                }

                if (!result) //No existe tal metodo
                {
                    logger.error("\n\nEl metodo indicado en la entrada '" + permissionEntry + "' no existe en la clase\n");
                }

            }
        }
        else
        {
            logger.error("\n\n La entrada '" + permissionEntry + "' no corresponde con una entrada valida de permiso\n");
        }

        return result;
    }

    /**
     * comprueba que este bien formada sintacticamente la entrada para el permiso.
     *
     * @param permissionEntry La entrada a comprobar.
     * @return true si esta bien formada.
     */
    private boolean isConsistentEntry(String permissionEntry)
    {
        return permissionEntry.indexOf('@') != -1;
    }

    /**
     * Establecemos que este aspecto tenga la mayor preferencia para no comenzar la transaccion antes de verificar si se debe autorizar su ejecucion.
     *
     * @return El orden de este Aspecto, en este caso el mayor posible.
     */
    public int getOrder()
    {
        return HIGHEST_PRECEDENCE;
    }

    /**
     * Ejecucion previa al metodo. Se comprueba si se tiene autorizacion para su ejecucion.
     *
     * @param method El nombre del metodo que se va a ejecutar.
     * @param args   Los argumentos del metodo.
     * @param target El objeto cuyo metodo se va ejecutar.
     * @throws Throwable Cualquier excepcion que ocurra durante la ejecucion del metodo.
     */
    public void before(Method method, Object[] args, Object target) throws Throwable
    {
        String className = target.getClass().getName();
        String methodName = method.getName();
        String fullMethodName = className + '@' + methodName;
        String requiredPermission = permissions.getProperty(fullMethodName);

        if (logger.isDebugEnabled())
        {
            logger.debug("Autorizando " + fullMethodName + " -> " + requiredPermission);
        }

        if (requiredPermission != null)
        {
            IUserSettings credenciales = UserManager.instance().getUser();
            if (credenciales == null)
            {
                AutorisationException e = new AutorisationException("No hay un usuario asociado a la peticion");
                logger.error(e);
                throw e;
            }

            if (!credenciales.hasPermission(requiredPermission))
            {
                AutorisationException e = new AutorisationException("El usuario no tiene autorizacion para ejecutar la operacion '" + requiredPermission + "'");
                logger.error(e);
                throw e;
            }
            else
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Autorizada la ejecucion del metodo: " + methodName);
                }
            }
        }
    }
}

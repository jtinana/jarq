package es.onlysolutions.arq.core.service.utils;

import es.onlysolutions.arq.core.accesobd.IEntityId;
import es.onlysolutions.arq.core.accesobd.exception.DaoException;
import es.onlysolutions.arq.core.log.LoggerGenerator;
import es.onlysolutions.arq.core.service.exception.EntityException;
import es.onlysolutions.arq.core.service.exception.ValidationException;
import org.apache.commons.logging.Log;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.util.Assert;
import org.springframework.validation.ObjectError;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Clase Utils para manejo de entidades.
 */
public final class EntityUtils
{
    /**
     * El logger de la clase.
     */
    private static final Log logger = LoggerGenerator.getLogger(EntityUtils.class);

    /**
     * Este metodo sustituye aquellos atributos de la entidad que son
     * cadenas de caracteres vacias por null. Realiza la consulta de
     * aquellos atributos cuyo metodo get devuelve un String y asigna el
     * valor nulo a traves del metodo set.
     *
     * @param obj La Entidad sobre la que se modificaran los valores a null.
     */
    public static void sustituirValoresVacios(Object obj)
    {
        if (obj != null)
        {
            Class clazz = obj.getClass();
            Method[] metodos = clazz.getMethods();
            Method metodoSet;
            String valor;
            String nombre = null;
            Object[] param = {null};
            try
            {
                for (int i = 0; i < metodos.length; i++)
                {
                    if (metodos[i].getName().startsWith("get"))
                    {
                        nombre = metodos[i].getName().substring(metodos[i].getName().indexOf("get") + 3);
                        if ((metodos[i].getReturnType() == String.class))
                        {
                            valor = (String) metodos[i].invoke(obj);
                            if ("".equals(valor))
                            {
                                metodoSet = obtenerMetodoSet(metodos, nombre);
                                if (metodoSet != null)
                                {
                                    metodoSet.invoke(obj, param);
                                }
                            }
                        }
                    }
                }
            }
            catch (IllegalAccessException e)
            {
                InvalidPropertyException invalidPropertyException = new InvalidPropertyException(clazz, nombre, "Error al tratar de acceder al getter de la propiedad", e);
                logger.error(invalidPropertyException);
                throw invalidPropertyException;
            }
            catch (InvocationTargetException e)
            {
                InvalidPropertyException invalidPropertyException = new InvalidPropertyException(clazz, nombre, "Error al tratar de invocar al getter de la propiedad", e);
                logger.error(invalidPropertyException);
                throw invalidPropertyException;
            }
        }
    }

    /**
     * Este metodo obtiene el metodo set asociado al nombre de un atributo.
     *
     * @param metodos Array de metodos, donde se puede encontrar el set.
     * @param nombre  Nombre del atributo.
     * @return El metodo para asignar valor a dicho atributo.
     */
    private static Method obtenerMetodoSet(Method[] metodos, String nombre)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Vamos a buscar el setter para el atributo: " + nombre + " en la lista de metodos: " + metodos);
        }

        int i = 0;
        Method metodo = null;
        boolean continuar = true;
        String trozoMetodoSetter = firtsLetterToUpperCase(nombre);
        while (i < metodos.length && continuar)
        {
            if ((metodos[i].getName().indexOf(trozoMetodoSetter) >= 0) && (metodos[i].getName().startsWith("set")))
            {
                metodo = metodos[i];
                continuar = false;
            }
            i++;
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("Encontrado metodo: " + metodo);
        }
        return metodo;
    }

    /**
     * convierte la primera letra en mayuscula.
     *
     * @param nombre El nombre a convertir la primera letra en mayusculas.
     * @return El String convertido.
     */
    private static String firtsLetterToUpperCase(String nombre)
    {
        char[] nombreArray = nombre.toCharArray();
        nombreArray[0] = Character.toString(nombreArray[0]).toUpperCase().toCharArray()[0];
        return new String(nombreArray);
    }

    /**
     * Realiza una copia del HashMap pasado como parametro. No se realiza comprobacion alguna de los tipos de las
     * clases contenidas en el mapa. Devuelve instancias de HashMaps, por lo tanto se debe llamar al metodo adecuado
     * en funcion del tipo de mapa que se quiera devolver.
     * No se realiza clone de los elementos que contenga el Mapa original.
     * Si se pasa como parametro un null se devuelve un null.
     *
     * @param mapToClone El mapa a clonar.
     * @return Una nueva instancia exacta a la pasada como parametro.
     */
    public static Map cloneHashMap(Map mapToClone)
    {
        HashMap result = null;
        if (mapToClone != null)
        {
            Iterator it = mapToClone.entrySet().iterator();
            result = new HashMap(mapToClone.size());

            while (it.hasNext())
            {
                Map.Entry entry = (Map.Entry) it.next();
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    /**
     * Realiza una copia del ArrayList pasado como parametro.
     * Se invoca al metodo clon de cada uno de los elementos, por lo que deben implementar el interfaz Cloneable.
     * Se devuelven instancias de ArrayList, en caso de pasar como argumento un null, se devuelve un null.
     *
     * @param listToClone La lista a clonar.
     * @return La copia exacta de la lista.
     */
    public static List cloneArrayList(List listToClone)
    {
        List result = null;

        if (listToClone != null)
        {
            result = new ArrayList(listToClone.size());
            for (int index = 0; index < listToClone.size(); index++)
            {
                Object element = listToClone.get(index);
                if (element != null)
                {
                    try
                    {
                        Method method = element.getClass().getMethod("clone", null);
                        result.add(method.invoke(element, null));
                    }
                    catch (NoSuchMethodException e)
                    {
                        throw new IllegalArgumentException("Los objetos del tipo " + element.getClass().getName() + " no tienen implementado el metodo clone sin argumentos", e);
                    }
                    catch (IllegalAccessException e)
                    {
                        throw new IllegalArgumentException("Acceso ilegal en la clase " + element.getClass().getName() + " al metodo clone sin argumentos", e);
                    }
                    catch (InvocationTargetException e)
                    {
                        throw new IllegalArgumentException("Error al invocar el metodo clone en la clase " + element.getClass().getName(), e);

                    }
                }
            }
        }
        return result;
    }

    /**
     * Comprueba si el metodo indicado como parametro es un getter, y ademas devuelve un objeto de tipo IEntityId.
     *
     * @param metodo El metodo a comprobar.
     * @return true si es un getter y devuelve una entidad.
     */
    public static Boolean isEntityGetter(Method metodo)
    {
        Boolean result = Boolean.FALSE;
        Assert.notNull(metodo, "Debe indicarse un metodo no nulo");
        if (metodo.getName().startsWith("get"))
        {
            //Comprobamos que sea una sublcase del Interfaz IEntityId
            if (IEntityId.class.isAssignableFrom(metodo.getReturnType()))
            {
                result = Boolean.TRUE;
            }
        }

        return result;
    }

    /**
     * Dado el metodo, que obligatoriamente debe ser un getter, devuelve el metodo setter que le corresponde.
     *
     * @param method  El metodo getter del que obtener el setter.
     * @param entidad La entidad de la que obtener el setter equivalente.
     * @return El metodo setter correspondiente.
     * @throws es.onlysolutions.arq.core.service.exception.ValidationException
     *          Si no existe el setter para el getter.
     */
    public static Method obtenerSetterDesdeGetter(Method method, IEntityId entidad)
    {
        Method result;
        String nombreAttSet = EntityUtils.obtenerNombreAtributoDeGetter(method);
        Method setter = obtenerMetodoSet(entidad.getClass().getMethods(), nombreAttSet);
        Assert.notNull(setter, "No se ha podido encontrar el setter correspondiente al atributo: " + nombreAttSet);
        try
        {
            result = entidad.getClass().getMethod(setter.getName(), method.getReturnType());
        }
        catch (NoSuchMethodException e)
        {
            ObjectError error = new ObjectError("aplicacion", new String[]{"IEntityId.setterNoExisteParaGetter"}, new String[]{method.getName()}, "El metodo getter {0} no tiene un setter equivalente -> " + e.toString());
            throw new ValidationException(error);
        }

        return result;
    }

    /**
     * Este metodo recibe un metodo que cumpla con la especificacion de getter de los JavaBeans y devuelve el nombre del atributo que devuelve.
     *
     * @param method El metodo del que obtener el atributo.
     * @return El nombre del atributo correspondiente al metodo indicado.
     */
    public static String obtenerNombreAtributoDeGetter(Method method)
    {
        String methodName = method.getName();
        //getNombreMetodo -> substring(4, size)
        char[] attName = methodName.substring(3, methodName.length()).toCharArray();
        attName[0] = Character.valueOf(attName[0]).toString().toLowerCase().toCharArray()[0];
        return new String(attName);
    }

    /**
     * Obtiene el ID de una entidad que implementa el IEntityId. En caso de que el metodo getIdName devuelva null o cadena vacia,
     * este metodo devuelve null.
     *
     * @param entidad La entidad que implementa el IEntityId. Si es null se devuelve un null.
     * @return La clave primaria de esta entidad.
     */
    public static Serializable getId(IEntityId entidad)
    {
        try
        {
            Serializable result = null;
            if (entidad != null && entidad.getIdName() != null && entidad.getIdName().length() > 0)
            {
                BeanInfo beanInfo = Introspector.getBeanInfo(entidad.getClass());
                PropertyDescriptor[] properties = beanInfo.getPropertyDescriptors();

                boolean notFound = true;
                for (int index = 0; index < properties.length && notFound; index++)
                {
                    PropertyDescriptor property = properties[index];
                    if (property.getName().equals(entidad.getIdName()))
                    {
                        notFound = false;
                        Object objResult = property.getReadMethod().invoke(entidad);
                        if (objResult != null)
                        {
                            if (!(objResult instanceof Serializable))
                            {
                                throw new EntityException("La propiedad '" + property.getName() +
                                        "' debe implementar el interfaz " +
                                        Serializable.class.getName() +
                                        " en la clase " + entidad.getClass().getName() + " -> PropertyClass: " + objResult.getClass().getName());
                            }
                            else
                            {
                                result = (Serializable) objResult;
                            }
                        }
                    }
                }

                if (notFound)
                {
                    throw new EntityException("La propiedad obtenida por el metodo getIdName(): " + entidad.getIdName() +
                            " no esta definida en la clase: " + entidad.getClass().getName());
                }
            }
            return result;
        }
        catch (IntrospectionException e)
        {
            logger.error(e);
            throw new EntityException("Introspection Exception", e);
        }
        catch (IllegalAccessException e)
        {
            logger.error(e);
            throw new EntityException("IllegalAccess Exception", e);
        }
        catch (InvocationTargetException e)
        {
            logger.error(e);
            throw new EntityException("InvocationTarget Exception", e);
        }

    }

    /**
     * Comprueba si va a ser neceario reemplazar algun valor nulo de una entidad por su entidad vacia.
     *
     * @param entidad La entidad a comprobar.
     * @return true si es neceario.
     */
    public static boolean needToReplaceNullEntities(IEntityId entidad)
    {
        boolean notFound = true;
        if (entidad != null)
        {
            try
            {
                BeanInfo beanInfo = Introspector.getBeanInfo(entidad.getClass());
                PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();

                for (int index = 0; index < propertyDescriptors.length && notFound; index++)
                {
                    PropertyDescriptor propertyDescriptor = propertyDescriptors[index];
                    if (IEntityId.class.isAssignableFrom(propertyDescriptor.getPropertyType()))
                    {
                        notFound = false;
                    }
                }
            }
            catch (IntrospectionException e)
            {
                logger.error(e);
                throw new DaoException("Error durante la introspeccion", e);
            }
        }
        return !notFound;
    }

    /**
     * Reemplaza cualquier ocurrencia de un atributo que este a null, y sea un IEntityId, por una instancia vacia de la clase.
     * De esta forma se pueden mapear directamente cualquier entidad directamente sin preocuparse por NullPointerExceptions.
     *
     * @param entityIdResult La entidad donde sustituir los atributos.
     */
    public static void replaceNullWithEmptyEntities(IEntityId entityIdResult)
    {
        if (entityIdResult != null)
        {
            try
            {
                BeanInfo beanInfo = Introspector.getBeanInfo(entityIdResult.getClass());
                PropertyDescriptor[] propertyDescriptor = beanInfo.getPropertyDescriptors();

                for (int index = 0; index < propertyDescriptor.length; index++)
                {
                    PropertyDescriptor descriptor = propertyDescriptor[index];
                    loadEmptyEntity(entityIdResult, descriptor);
                }
            }
            catch (IntrospectionException e)
            {
                logger.error("Error al tratar de obtener las propiedades del bean " + entityIdResult.getClass().getName(), e);
                throw new DaoException("Error al tratar de obtener las propiedades del bean " + entityIdResult.getClass().getName(), e);
            }
            catch (InvocationTargetException e)
            {
                logger.error("Error de invocacion sobre el metodo del bean " + entityIdResult.getClass().getName(), e);
                throw new DaoException("Error de invocacion sobre el metodo del bean " + entityIdResult.getClass().getName(), e);
            }
            catch (InstantiationException e)
            {
                logger.error("Error de instanciacion de clase", e);
                throw new DaoException("Error de instanciacion de clase", e);
            }
            catch (IllegalAccessException e)
            {
                logger.error("Acceso ilegal a la clase: " + entityIdResult.getClass().getName(), e);
                throw new DaoException("Acceso ilegal a la clase: " + entityIdResult.getClass().getName(), e);
            }
            if (logger.isDebugEnabled())
            {
                logger.debug("Cargadas entidades nulas como vacias para la clase " + entityIdResult.getClass().getName());
            }
        }
    }

    /**
     * Comprueba si la propiedad dada es del tipo IEntityId y si es nula la carga vacia.
     *
     * @param entityId   La entidad a comprobar.
     * @param descriptor El PropertyDescriptor  de la propiedad.
     * @throws InvocationTargetException Si ocurre un error de invocacion.
     * @throws IllegalAccessException    Si ocurre un error de acceso.
     * @throws InstantiationException    Si ocurre un error durante la instanciacion.
     */
    public static void loadEmptyEntity(IEntityId entityId, PropertyDescriptor descriptor) throws InvocationTargetException, IllegalAccessException, InstantiationException
    {
        if (IEntityId.class.isAssignableFrom(descriptor.getPropertyType()))
        {
            Method method = descriptor.getReadMethod();
            Object methodInvocationResult = method.invoke(entityId, null);

            if (methodInvocationResult == null)//Tenemos que cargar una entidad vacia.
            {
                Object newInstance = descriptor.getPropertyType().newInstance(); //Instanciamos una clase nueva.
                descriptor.getWriteMethod().invoke(entityId, newInstance); //La establecemos en la propiedad.
            }
        }
    }


}

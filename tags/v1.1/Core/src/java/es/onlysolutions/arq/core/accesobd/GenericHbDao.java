package es.onlysolutions.arq.core.accesobd;

import es.onlysolutions.arq.core.accesobd.exception.DaoException;
import es.onlysolutions.arq.core.configuration.Configuracion;
import es.onlysolutions.arq.core.filter.CriteriaFilter;
import es.onlysolutions.arq.core.filter.HQLFilter;
import es.onlysolutions.arq.core.filter.SQLFilter;
import es.onlysolutions.arq.core.service.exception.ValidationException;
import es.onlysolutions.arq.core.service.pagination.PaginatedListImp;
import es.onlysolutions.arq.core.service.utils.EntityUtils;
import org.displaytag.pagination.PaginatedList;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.criterion.*;
import org.hibernate.transform.AliasToEntityMapResultTransformer;
import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;
import org.springframework.validation.ObjectError;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;


/**
 * Dao con las operaciones genéricas definidas por la arquitectura.
 */
public class GenericHbDao extends AbstractHibernateDao
{
    //El HQL a ejecutar en caso de ser indicado como propiedad.
    private String hql;

    /**
     * Caracter para incluir en las expresiones like como comodin.
     */
    private static final char LIKE_EXPRESION_CHARACTER = '%';

    public String getHql()
    {
        return hql;
    }


    /**
     * Setter for property 'hql'.
     *
     * @param hql Value to set for property 'hql'.
     * @see #hql
     */
    public void setHql(String hql)
    {
        this.hql = hql;
    }


    /**
     * Realiza la carga de una entidad. Debe tener en cuenta que en caso de realizar la carga de una entidad, que contenga
     * subentidades que puedan ser nulas, dichas entidades seran devueltas vacias, pero ya instanciadas. De esta forma
     * se pueden realizar mapeos directamente contra ellas sin comprobaciones previas.
     * <b><i>Todas las entidades devueltas de esta forma no estan asociadas con la Session de Hibernate.</i></b>
     *
     * @param entidad La entidad de la que realizar la carga. Obtendra la primary key de esta entidad.
     * @return La entidad cargada de la base de datos.
     */
    public IEntityId cargar(IEntityId entidad)
    {
        IEntityId result = null;

        if (entidad != null)
        {
            Serializable id = EntityUtils.getId(entidad);
            if (id == null)
            {
                result = entidad;
            }
            else
            {
                IEntityId entityIdResult = (IEntityId) this.load(entidad.getClass(), id);

                if (EntityUtils.needToReplaceNullEntities(entidad))
                {
                    IEntityId detachedEntity = copyInstanceNewEntity(entityIdResult);
                    EntityUtils.replaceNullWithEmptyEntities(detachedEntity);
                    result = detachedEntity;
                }
                else
                {
                    result = entityIdResult;
                }
            }

        }
        return result;
    }

    /**
     * Crea una copia identica de la misma entidad con sus valores originales.
     * Al ser una copia realiza fuera del contexto de Hibernate no esta asociada a la sesion y puede ser manipulada.
     *
     * @param entidad La entidad a copiar.
     * @return La nueva entidad copiada.
     */
    private IEntityId copyInstanceNewEntity(IEntityId entidad)
    {
        IEntityId newInstance;
        try
        {
            newInstance = entidad.getClass().newInstance();
            BeanUtils.copyProperties(entidad, newInstance);
            return newInstance;
        }
        catch (InstantiationException e)
        {
            logger.error(e);
            throw new DaoException(e);
        }
        catch (IllegalAccessException e)
        {
            logger.error(e);
            throw new DaoException(e);
        }
    }


    /**
     * Salva en la base de datos la entidad pasada como parametro. Se realiza un save o un update en
     * funcion de lo que sea necesario.
     *
     * @param entidad La entidad a salvar.
     * @return La entidad persistida.
     */
    public IEntityId guardar(IEntityId entidad)
    {
        Serializable id = EntityUtils.getId(entidad);
        Serializable resultId;
        IEntityId resultObj;

        loadChildrenEntities(entidad);

        if (id == null)
        {
            // Se debe proceder a un save, ya que no tenemos el id de la entidad.
            resultId = save(entidad);
            resultObj = (IEntityId) load(entidad.getClass(), resultId);
        }
        else
        {
            // Es posible que se trate de un save, ya que en algun formulario se introduce la pk
            // directamente.
            saveOrUpdate(entidad);
            resultObj = (IEntityId) load(entidad.getClass(), id);
        }
        return resultObj;
    }

    /**
     * Recorre los metodos de la entidad indicada, y realiza loads sobre aquellas subentidades que tengan cargado el id.
     * Si una entidad tiene como id un null sustituye el objeto completo por un valor null.
     *
     * @param entidad La entidad de la que cargar las entidades hijas.
     */
    private void loadChildrenEntities(IEntityId entidad)
    {
        Assert.notNull(entidad, "No se puede cargar las subentidades de una entidad nula");
        try
        {
            Class entityClass = entidad.getClass();

            Method[] methods = entityClass.getMethods();

            for (int index = 0; index < methods.length; index++)
            {
                Method method = methods[index];
                if (EntityUtils.isEntityGetter(method))
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Cargamos la entidad que devuelve el metodo: " + method);
                    }
                    IEntityId subEntity = (IEntityId) method.invoke(entidad);

                    if (logger.isDebugEnabled())
                    {
                        logger.debug("La ejecucion del metodo " + method.getName() + " devolvio -> " + subEntity);
                    }

                    //Si el metodo devuelve una entidad la procesamos y la cargamos.
                    if (subEntity != null)
                    {
                        IEntityId loadedEntity = null;
                        Serializable subEntityId = EntityUtils.getId(subEntity);
                        if (subEntityId != null)
                        {
                            loadedEntity = (IEntityId) load(method.getReturnType(), subEntityId);
                        }

                        //Ejecutamos el setter y le ponemos la entidad cargada, que sera null si no contenia un id.
                        Method setterMethod = EntityUtils.obtenerSetterDesdeGetter(method, entidad);
                        setterMethod.invoke(entidad, loadedEntity);

                        if (logger.isDebugEnabled())
                        {
                            logger.debug("Ejecutado metodo " + setterMethod.getName() + " con el parametro " + loadedEntity);
                        }
                    }

                }
            }
        }
        catch (IllegalAccessException e)
        {
            ObjectError error = new ObjectError("aplicacion", new String[]{"IEntityId.illegalAccessException"}, new String[]{entidad.getClass().getName()}, "Ha ocurrido un acceso ilegal en la clase {0}" + e.toString());
            throw new ValidationException(error);
        }
        catch (InvocationTargetException e)
        {
            ObjectError error = new ObjectError("aplicacion", new String[]{"IEntityId.invocationTargetException"}, new String[]{entidad.getClass().getName()}, "Ha ocurrido un InvocationTargetException en la clase {0}" + e.toString());
            throw new ValidationException(error);
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("Cargadas las entidades hijas de la Entidad: " + entidad);
        }
    }


    /**
     * Elimina de la base de datos la entidad indicada.
     *
     * @param entidad La entidad a borrar.
     */
    public void borrar(IEntityId entidad)
    {
        this.delete(entidad);
    }


    /**
     * Obtiene una lista de entidades en funcion de los criterios.
     *
     * @param entityClazz La clase de la entidad de la que obtener el listado.
     * @param criterios   Los criterios a aplicar.
     * @param numPagina   El numero de pagina a obtener.
     * @param tamPagina   El tamaño de pagina a aplicar.
     * @param sort        Nombre de la propiedad por la que ordenar.
     * @param dir         Direccion para la ordenacion.
     * @return La lista paginada.
     */
    public PaginatedList list(Class entityClazz, Object criterios, Integer numPagina, Integer tamPagina, String sort, String dir)
    {

        PaginatedList result;

        if (logger.isDebugEnabled())
        {
            logger.debug("Entidad: " + entityClazz.getName() + ";Criterios: " + criterios + "; NumPagina: " + numPagina + "; TamPagina: " + tamPagina + "; Sort: " + sort + "; Dir: " + dir);
        }

        Assert.notNull(entityClazz, "Se debe indicar una clase de entidad Hibernate valida como parametro del matodo 'list'");
        Criteria countCriteria = getCriteria(entityClazz);
        Criteria resultCriteria = getCriteria(entityClazz);

        if (criterios != null)
        {
            /**
             * Tratamos el criteria en funcion del tipo de filtro que sea.
             */
            if (criterios instanceof CriteriaFilter)
            {
                result = applyCriteriaFilter(criterios, resultCriteria, countCriteria, numPagina, tamPagina);
            }
            else if (criterios instanceof SQLFilter)
            {
                SQLFilter filter = (SQLFilter) criterios;
                result = applySqlFilter(filter, numPagina, tamPagina);
            }
            else if (criterios instanceof HQLFilter)
            {
                HQLFilter hqlFilter = (HQLFilter) criterios;
                result = applyHqlFilter(hqlFilter, numPagina, tamPagina);
            }
            else //Es un objeto normal.
            {
                //Solo añadimos orden a los resultados.
                result = applyStandardFilter(resultCriteria, sort, dir, countCriteria, criterios, entityClazz, numPagina, tamPagina);
            }

        }
        else
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("No se realiza filtrado por criterios para la entidad: " + entityClazz.getName());
            }
            result = getListFromCriteria(countCriteria, resultCriteria, numPagina, tamPagina);
        }

        return result;

    }

    /**
     * Inicializa el numero de pagina a 1. Se debe hacer en cuanto se añada alguna condicion de filtrado,
     * ya que de lo contrario, se perderan paginas si la condicion de filtro devuelve menos paginas que en la
     * que se realizo dicho filtrado.
     *
     * @param paginatedList El PaginatedList a inicializar.
     */
    private void initPageNumber(PaginatedList paginatedList)
    {
        if (paginatedList instanceof PaginatedListImp)
        {
            PaginatedListImp paginatedListImp = (PaginatedListImp) paginatedList;
            paginatedListImp.setPageNumber(1);
        }
        else
        {
            throw new IllegalArgumentException("La arquitectura tan solo soporta actualmente PaginatedListImp.");
        }
    }

    /**
     * Aplica un filtro estandar, es decir, un bean normal del que obtener las propiedades de forma normal.
     *
     * @return La lista con los filtros aplicados.
     */
    private PaginatedList applyStandardFilter(Criteria resultCriteria, String sort, String dir, Criteria countCriteria, Object criterios, Class entityClazz, Integer numPagina, Integer tamPagina)
    {
        PaginatedList result;
        setOrderInCriteria(resultCriteria, sort, dir);

        countCriteria.add(Example.create(criterios).ignoreCase().enableLike(MatchMode.ANYWHERE));
        resultCriteria.add(Example.create(criterios).ignoreCase().enableLike(MatchMode.ANYWHERE));
        addEntityCriterias(entityClazz, criterios, resultCriteria, countCriteria);
        result = getListFromCriteria(countCriteria, resultCriteria, numPagina, tamPagina);
        return result;
    }

    private PaginatedList applyCriteriaFilter(Object criterios, Criteria resultCriteria, Criteria countCriteria, Integer numPagina, Integer tamPagina)
    {
        CriteriaFilter criteriaFilter = (CriteriaFilter) criterios;

        /**
         * El los siguiente mapas, se guardaran los paths añadidos y los criterias creados mediante este path.
         * Al añadir cualquier path, se comprobara si ya esta creado previamente en funcion de su tipo de restriccion
         * para añadirle mas condiciones al mismo criteria.
         * Hibernate no permite dos llamadas al createCriteria con el mismo path, ya que en realidad serian dos JOIN.
         */
        Map<String, Criteria> paths = new HashMap<String, Criteria>(13);
        Map<String, Criteria> countPaths = new HashMap<String, Criteria>(13);

        //Añadimos los ordenes tan solo al criteria de resultados, ya que no influye para el count.
        List<es.onlysolutions.arq.core.filter.Order> ordenes = criteriaFilter.getOrders();
        for (int indexOrder = 0; indexOrder < ordenes.size(); indexOrder++)
        {
            es.onlysolutions.arq.core.filter.Order order = ordenes.get(indexOrder);
            String path = order.getPath();
            if (path != null && path.length() > 0)
            {
                if (es.onlysolutions.arq.core.filter.Order.ASC.equals(order.getOrderType()))
                {
                    addToCachedCriteria(paths, path, resultCriteria, Order.asc(order.getField()));
                }
                else
                {
                    addToCachedCriteria(paths, path, resultCriteria, Order.desc(order.getField()));
                }
            }
            else
            {
                if (es.onlysolutions.arq.core.filter.Order.ASC.equals(order.getOrderType()))
                {
                    resultCriteria.addOrder(Order.asc(order.getField()));
                }
                else
                {
                    resultCriteria.addOrder(Order.desc(order.getField()));
                }
            }


            if (logger.isDebugEnabled())
            {
                logger.debug("Añadido Order: " + order);
            }
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("Añadidos " + ordenes.size() + " ordenes al Criteria");
        }

        //Añadimos las restricciones.
        Map<Criterion, String> restricciones = criteriaFilter.getRestrictionList();

        Iterator<Map.Entry<Criterion, String>> itRestrictions = restricciones.entrySet().iterator();
        while (itRestrictions.hasNext())
        {
            Map.Entry<Criterion, String> entry = itRestrictions.next();
            Criterion criterion = entry.getKey();
            String aliasPath = entry.getValue();

            if (aliasPath != null && aliasPath.length() > 0)
            {
                addToCachedCriteria(paths, aliasPath, resultCriteria, criterion);
                addToCachedCriteria(countPaths, aliasPath, countCriteria, criterion);
            }
            else
            {
                resultCriteria.add(criterion);
                countCriteria.add(criterion);
            }

            if (logger.isDebugEnabled())
            {
                logger.debug("Añadida restriccion: " + criterion);
            }
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("Añadidas " + restricciones.size() + " restricciones al Criteria");
        }

        return getListFromCriteria(countCriteria, resultCriteria, numPagina, tamPagina);
    }

    /**
     * Obtiene un objeto Criteria en funcion del path indicado. Si ha sido creado previamente se obtiene la referencia
     * anterior, y si no se crea un objeto nuevo.
     *
     * @param paths     El mapa de path y criterias añadidos hasta el momento.
     * @param aliasPath El path del que comprobar si se han añadido Criteria previamente.
     * @param criteria  El objeto Criteria al que asociarle el path indicado.
     * @param param     El objeto que se desea añadir al Criteria. Se detecta la instancia de la que se trata y se añade de forma correcta.
     *                  si no se pasa como parametro un Order o un Criterion se lanza una excepcion.
     * @return El objeto Criteria o la referencia al Criteria ya guardado.
     */
    private Criteria addToCachedCriteria(Map<String, Criteria> paths, String aliasPath, Criteria criteria, Object param)
    {
        Criteria result;
        if (paths.containsKey(aliasPath))
        {
            result = paths.get(aliasPath);
        }
        else
        {
            result = criteria.createCriteria(aliasPath);
        }

        if (param instanceof Order)
        {
            Order order = (Order) param;
            result.addOrder(order);
        }
        else if (param instanceof Criterion)
        {
            Criterion criterion = (Criterion) param;
            result.add(criterion);
        }
        else
        {
            throw new IllegalArgumentException("El ");
        }
        paths.put(aliasPath, result);//Actualizamos el mapa.

        return result;
    }


    /**
     * Aplica el filtro SQL.
     *
     * @param sqlFilter El filtro SQL a aplicar.
     * @param numPagina El numero de pagina.
     * @param tamPagina El tamaño de pagina.
     * @return La lista paginada resultante.
     */
    private PaginatedList applySqlFilter(SQLFilter sqlFilter, Integer numPagina, Integer tamPagina)
    {
        SQLQuery countQuery = getSQLQuery(sqlFilter.processCountSql());
        sqlFilter.setParametersForCount(countQuery);
        Number count = (Number) countQuery.uniqueResult();

        if (count == null)
        {
            count = Integer.valueOf(0);
        }

        SQLQuery query = getSQLQuery(sqlFilter.processSql());
        query.setResultTransformer(new AliasToEntityMapResultTransformer());
        int firstResult = (numPagina.intValue() - 1) * tamPagina.intValue();
        if (firstResult > 0)
        {
            query.setFirstResult(firstResult);
        }

        query.setMaxResults(tamPagina);
        sqlFilter.setParametersForSelect(query);

        List results = query.list();
        PaginatedListImp paginatedList = new PaginatedListImp();
        paginatedList.setPageNumber(numPagina);
        paginatedList.setObjectsPerPage(tamPagina);
        paginatedList.setList(results);
        paginatedList.setFullListSize(count.intValue());
        return paginatedList;
    }

    /**
     * Aplica el filtro de HQL.
     *
     * @param hqlFilter El filtro a aplicar.
     * @param numPagina El numero de pagina a obtener.
     * @param tamPagina El tamaño de pagina.
     * @return La lista paginada resultante.
     */
    private PaginatedList applyHqlFilter(HQLFilter hqlFilter, Integer numPagina, Integer tamPagina)
    {
        Query queryForCount = getQuery(hqlFilter.processCountHql());
        hqlFilter.setParametersForCount(queryForCount);
        Number count = (Number) queryForCount.uniqueResult();

        if (count == null)
        {
            count = Integer.valueOf(0);
        }

        Query queryList = getQuery(hqlFilter.processHql());
        int firstResult = (numPagina.intValue() - 1) * tamPagina.intValue();
        if (firstResult > 0)
        {
            queryList.setFirstResult(firstResult);
        }

        queryList.setMaxResults(tamPagina);
        hqlFilter.setParametersForList(queryList);

        List results = queryList.list();
        PaginatedListImp paginatedList = new PaginatedListImp();
        paginatedList.setPageNumber(numPagina);
        paginatedList.setObjectsPerPage(tamPagina);
        paginatedList.setList(results);
        paginatedList.setFullListSize(count.intValue());
        return paginatedList;
    }

    /**
     * Añade los criterios adecuados para poder filtrar por los subcriterios indicados en el filtro (objeto criterios).
     * Este metodo recorre los metodos de la entidad y de aquellos que sean getters y devuelvan un IEntityId, los añade
     * a los criteria para realizar el filtrado.
     * Para poder realizar el filtrado, <b>es imprescindible que los getter que devuelven los IEntityId de la entidad
     * sean exactamente iguales que los del objeto criterios.</b>
     *
     * @param entityClazz    La clase de la entidad de la que se quiere filtrar.
     * @param criterios      El objeto que contiene los criterios de busqueda.
     * @param resultCriteria El objeto criteria del que se obtendran los resultados.
     * @param countCriteria  El objeto criteria del que se obtendran el numero de elementos.
     */
    protected void addEntityCriterias(Class entityClazz, Object criterios, Criteria resultCriteria, Criteria countCriteria)
    {
        try
        {
            Object instance = entityClazz.newInstance();
            Assert.isInstanceOf(IEntityId.class, instance, "La entidad debe implementar el interfaz " + IEntityId.class.getName());

            IEntityId entityInstance = (IEntityId) instance;

            Method[] methods = entityInstance.getClass().getMethods();

            for (int index = 0; index < methods.length; index++)
            {
                Method method = methods[index];
                /**
                 * Si el tipo de devuelve el metodo es del tipo IEntityId hay que mirar si viene una entidad asi en los
                 * criterios y filtrar por su id.
                 */
                if (isGetterOfEntityIdReturnType(method))
                {
                    Method criteriaMethod = obtenerCriteriaMethod(method, criterios);
                    if (criteriaMethod != null)
                    {
                        /**
                         * En este punto tenemos el metodo del objeto criterios que devuelve nuestro valor a filtrar, y sabemos
                         * el nombre del metodo de la entidad en donde debemos establecer el filtro.
                         */
                        Object result = criteriaMethod.invoke(criterios);
                        if (result != null)
                        {
                            Assert.isInstanceOf(IEntityId.class, result, "El objeto que devuelve el metodo: " + criteriaMethod.getName() + " no implementa el interfaz " + IEntityId.class.getName());

                            IEntityId entityIdResult = (IEntityId) result;

                            /**
                             * Se comprueba que en la entidad haya un ID no nulo para filtrar, si no se omite.
                             */
                            Serializable iDEntityIdResult = EntityUtils.getId(entityIdResult);
                            if (iDEntityIdResult != null)
                            {
                                //En el criterios se ha filtrado por una entidad, añadimos un criteria a esa entidad con su id.
                                String entityNameAtt = EntityUtils.obtenerNombreAtributoDeGetter(method);

                                resultCriteria.createCriteria(entityNameAtt);
                                resultCriteria.add(Expression.eq(entityNameAtt + '.' + entityIdResult.getIdName(), iDEntityIdResult));

                                countCriteria.createCriteria(entityNameAtt);
                                countCriteria.add(Expression.eq(entityNameAtt + '.' + entityIdResult.getIdName(), iDEntityIdResult));
                            }
                        }
                    }
                }
                else if (isGetterOfEntityId(method, entityInstance))
                {
                    String attName = EntityUtils.obtenerNombreAtributoDeGetter(method);
                    Method criteriaMethod = obtenerCriteriaMethod(method, criterios);
                    Object result = criteriaMethod.invoke(criterios);

                    if (result != null)
                    {
                        /**
                         * Si el resultado es de tipo String podemos ejecutar un like con %.
                         */
                        if (result instanceof String)
                        {
                            resultCriteria.add(Expression.like(attName, LIKE_EXPRESION_CHARACTER + result.toString() + LIKE_EXPRESION_CHARACTER));
                            countCriteria.add(Expression.like(attName, LIKE_EXPRESION_CHARACTER + result.toString() + LIKE_EXPRESION_CHARACTER));
                        }
                        else //No se puede establecer un like debido a problemas a tratar de convertir al tipo de la entidad.
                        {
                            resultCriteria.add(Expression.eq(attName, result));
                            countCriteria.add(Expression.eq(attName, result));
                        }
                    }
                }
            }

        }
        catch (InstantiationException e)
        {
            logger.error(e);
            throw new DaoException("Error al tratar de instanciar la clase: " + entityClazz.getName(), e);
        }
        catch (IllegalAccessException e)
        {
            logger.error(e);
            throw new DaoException("Acceso ilegal al tratar de instanciar la clase: " + entityClazz.getName(), e);
        }
        catch (InvocationTargetException e)
        {
            logger.error(e);
            throw new DaoException("Error al tratar de invocar el metodo de la clase: " + entityClazz.getName(), e);
        }
    }

    /**
     * Devuelve true si en metodo es un getter de un atributo ID de la entidad.
     *
     * @param method         El metodo a comprobar.
     * @param entityInstance La clase de la que comprobar si es metodo que devuelve el ID.
     * @return true si es un getter de un atributo de ID.
     */
    private boolean isGetterOfEntityId(Method method, IEntityId entityInstance)
    {
        boolean result;
        if (method.getName().startsWith("get"))
        {
            String attName = entityInstance.getIdName();
            String attIdName = EntityUtils.obtenerNombreAtributoDeGetter(method);

            result = attName.equals(attIdName);
        }
        else
        {
            result = false;
        }
        return result;
    }


    /**
     * Devuelve true si el metodo pasado como parametro es un getter que ademas devuelve un objeto que implemente el interfaz
     * IEntityId.
     *
     * @param method El metodo a comprobar.
     * @return true si es un getter que devuelve un IEntityId, false en caso contrario.
     */
    private boolean isGetterOfEntityIdReturnType(Method method)
    {
        Class returnType = method.getReturnType();

        boolean isEntityId = implementInterface(returnType);

        if (isEntityId)
        {
            isEntityId = method.getName().startsWith("get");
        }
        return isEntityId;
    }

    /**
     * Este metodo devuelve true si la clase pasada como parametro implementa el interfaz IEntityId.
     *
     * @param clazz La clase a comprobar si implementa el interfaz.
     * @return true si lo implementa, false en caso contrario.
     */
    private boolean implementInterface(Class clazz)
    {
        Class[] interfaces = clazz.getInterfaces();

        for (int index = 0; index < interfaces.length; index++)
        {
            Class anInterface = interfaces[index];
            if (anInterface == IEntityId.class)
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Obtiene el metodo pasado como argumento del objeto criterios, o devuelve null si no lo encuentra.
     *
     * @param method    El metodo a encontrar.
     * @param criterios El objeto criterios donde encontrar el metodo.
     * @return El metodo del objeto criterios, o null si no se encuentra.
     */
    private Method obtenerCriteriaMethod(Method method, Object criterios)
    {
        Method criteriaMethod = null;
        try
        {
            criteriaMethod = criterios.getClass().getMethod(method.getName(), method.getParameterTypes());

        }
        catch (NoSuchMethodException e)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("El objeto utilizado como criterios no tiene el metodo de entidad: " + method.getName());
            }
        }
        return criteriaMethod;
    }

    /**
     * Establece un orden indicado sobre el objeto Criteria.
     *
     * @param criteria El criteria sobre el que establecer el órden.
     * @param sort     El campo a ordenar. Si es nulo no se añade órden al criteria.
     * @param dir      La dirección del orden. Si se indica 'desc' se pone descendiente, ascendente en cualquier otro caso.
     */
    protected void setOrderInCriteria(Criteria criteria, String sort, String dir)
    {
        Order order = obtainOrder(sort, dir);
        if (order != null)
        {
            criteria.addOrder(order);
        }
    }

    /**
     * Devuelve el objeto Order a partir de los parametros de orden.
     *
     * @param sort Campo por el que obtener el orden. Si éste parametro es nulo se devuelve null como order.
     * @param dir  Direccion de la ordenacion. El valor 'desc' indicará descenciente, y cualquier otro valor ascendente.
     * @return El objeto Order o null en caso de indicar sort a null.
     */
    private Order obtainOrder(String sort, String dir)
    {
        Order order = null;
        if (sort != null && sort.length() > 0)
        {
            if (dir != null && dir.length() > 0)
            {
                if ("desc".equalsIgnoreCase(dir))
                {
                    order = Order.desc(sort);
                }
                else
                { // En cualquier otro caso se ordena por asc
                    order = Order.asc(sort);
                }
            }
            else
            { // Si no se indica dirección se toma asc por defecto.
                order = Order.asc(sort);
            }
        }
        return order;
    }


    /**
     * Recibe como parametros los objetos criteria para contar y obtener la lista y los parametros de la lista.
     * Al no indicarse orden no se establece orden sobre los criterios de busqueda.
     * Este metodo es llamado al aplicar ciertos filtros que modifiquen previamente los objectos Criteria.
     *
     * @param countCriteria   El criteria necesario para contar.
     * @param resultCriteria  El criteria necesario para obtener el resultado.
     * @param pageNumber      El número de pagina a mostrar.
     * @param defaultPageSize El tamaño de pagina a utilizar. Si se indica a null, o negativo se lee de la configuración.
     * @return La lista paginada en base a la cuenta de filas y con la pagina actual.
     */
    private PaginatedList getListFromCriteria(Criteria countCriteria, Criteria resultCriteria, Integer pageNumber, Integer defaultPageSize)
    {
        Integer pageSize;

        if (defaultPageSize == null || defaultPageSize.intValue() < 1)
        {
            pageSize = Configuracion.getInteger(IConstanstAccesoDatos.PARAM_PAGESIZE);
        }
        else
        {
            pageSize = defaultPageSize;
        }

        PaginatedListImp paginatedList = new PaginatedListImp();
        countCriteria.setProjection(Projections.rowCount());

        Integer totalRows = getRowsFromUniqueResult(countCriteria);


        Assert.notNull(totalRows, "Se ha tratado de listar una clase que no correspondía con una entidad de Hibernate");
        paginatedList.setFullListSize(totalRows.intValue());

        paginatedList.setObjectsPerPage(pageSize.intValue());

        List lista = new ArrayList(pageSize.intValue());
        if (totalRows.intValue() == 0)
        {
            paginatedList.setList(lista);
            paginatedList.setPageNumber(1);
            return paginatedList;
        }

        int firstResult = (pageNumber.intValue() - 1) * pageSize.intValue();
        if (firstResult > 0)
        {
            resultCriteria.setFirstResult(firstResult);
        }
        else
        {
            resultCriteria.setFirstResult(0);
        }

        resultCriteria.setMaxResults(pageSize.intValue());

        if (pageNumber.intValue() > paginatedList.getTotalPages())
        {
            pageNumber = new Integer(paginatedList.getTotalPages());
        }

        paginatedList.setFullListSize(totalRows);
        paginatedList.setObjectsPerPage(pageSize.intValue());
        paginatedList.setPageNumber(pageNumber);

        lista = resultCriteria.list();

        if (logger.isDebugEnabled())
        {
            logger.debug("Obtenida una lista total de entidades de tamaño " + lista.size());
        }

        paginatedList.setList(lista);

        return paginatedList;
    }

    /**
     * Obtiene el numero total de elementos a partir de un countCriteria. Esto se debe realizar debido a posibles
     * problemas con el tipo devuelto por el metodo uniqueResult de la clase Criteria.
     *
     * @param countCriteria El criteria ya preparado para contar.
     * @return El numero de Rows, o se lanza una excepcion en caso de no poder obtenerlo.
     * @see org.hibernate.Criteria#uniqueResult()
     */
    private Integer getRowsFromUniqueResult(Criteria countCriteria)
    {
        Integer totalRows;
        Object uniqueResult = countCriteria.uniqueResult();
        Assert.notNull(uniqueResult, "La ejecucion del metodo 'countCriteria.uniqueResult()' ha devuelto null");

        if (uniqueResult instanceof Integer)
        {
            totalRows = (Integer) uniqueResult;
        }
        else if (uniqueResult instanceof String)
        {
            try
            {
                totalRows = Integer.valueOf((String) uniqueResult);
            }
            catch (NumberFormatException e)
            {
                logger.error(e);
                throw new DaoException("No se ha podido obtener el numero total de registros, " + "debido a que UniqueResult no ha devuelto un Objeto convertible a Integer", e);
            }
        }
        else if (uniqueResult instanceof Number)
        {
            totalRows = ((Number) uniqueResult).intValue();
        }
        else //No sabemos que puede venir, y no es ni numero ni String. Se trata de convertir el toString del objeto.
        {
            String numberToString = uniqueResult.toString();
            try
            {
                totalRows = Integer.valueOf(numberToString);
            }
            catch (NumberFormatException e)
            {
                logger.error(e);
                throw new DaoException("No se ha podido obtener el numero total de registros, " + "debido a que UniqueResult no ha devuelto un Objeto convertible a Integer", e);
            }
        }
        return totalRows;
    }
}
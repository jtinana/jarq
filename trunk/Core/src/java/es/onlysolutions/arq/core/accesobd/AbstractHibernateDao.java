package es.onlysolutions.arq.core.accesobd;

import es.onlysolutions.arq.core.log.LoggerGenerator;
import org.apache.commons.logging.Log;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.util.Assert;

import java.io.Serializable;

/**
 * Clase abstracta de la que heredaran todos los dao que utilizen Hibernate.<br>
 * Proporciona los metodos necesarios para no tener que implementar los getter, setter,<br>
 * y el obtener el template de Hibernate.<br>
 * La session de Hibernate se debe establecer con el nombre <b>sessionFactory</b>.
 */
public abstract class AbstractHibernateDao
{

    /**
     * El logger de la clase.
     */
    protected final Log logger = LoggerGenerator.getLogger(this.getClass());

    /**
     * La session Factory establecida por Spring.
     */
    private SessionFactory sessionFactory;

    /**
     * El getter para la session de Hibernate.<br>
     * Todos los daos comparten la misma session de Hibernate.
     *
     * @return La SessionFactory de Hibernate.
     * @deprecated Ser� eliminado en posteriores versiones de la arquitectura para evitar el acceso directo a la SessionFactory.
     */
    protected SessionFactory getSessionFactory()
    {
        return sessionFactory;
    }

    /**
     * Setter para la SessionFactory de Hibernate.<br>
     * Todos los daos comparten la misma session de Hibernate.
     *
     * @param sessionFactory La session de Hibernate establecida por el IoC container.
     */
    public void setSessionFactory(SessionFactory sessionFactory)
    {
        this.sessionFactory = sessionFactory;
    }

    /**
     * M�todo para obtener el Template.
     *
     * @return El objeto HibernateTemplate ya instanciado.
     */
    private HibernateTemplate getHibernateTemplate()
    {
        HibernateTemplate template = new HibernateTemplate(sessionFactory);

        if (logger.isDebugEnabled())
        {
            logger.debug("Obtenemos el HibernateTemplate: " + template);
        }

        return template;
    }

    /**
     * Persiste el objeto en la base de datos.<br>
     * Es un wraper del m�todo save del HibernateTemplate.
     *
     * @param entity La entidad Hibernate a persistir.
     * @return El objeto serializable representando la nueva PK del objeto insertado.
     * @see HibernateTemplate#save(Object)
     */
    public Serializable save(Object entity)
    {
        if (entity == null)
        {
            throw new IllegalArgumentException("La entidad a persistir no pueder null");
        }
        HibernateTemplate template = getHibernateTemplate();

        if (logger.isDebugEnabled())
        {
            logger.debug("Se va a tratar de realizar un SAVE sobre la entidad: " + entity);
            logger.debug("Entity: " + entity);
        }

        Serializable pk = template.save(entity);

        if (logger.isDebugEnabled())
        {
            logger.debug("OK -> " + pk + " save(" + entity + ')');
        }

        return pk;
    }

    /**
     * Obtiene el objeto de la base de datos de la clase indicada y con la primary key indicada.<br>
     * Es un wraper del m�todo load del objeto HibernateTemplate.
     *
     * @param clazz La clase del objeto a obtener.
     * @param pk    La primary key a obtener.
     * @return La entidad Hibernate obtenida de la base de datos.
     * @see HibernateTemplate#load(Class,Serializable)
     */
    public Object load(Class clazz, Serializable pk)
    {
        if (clazz == null)
        {
            throw new IllegalArgumentException("La clase a cargar no puede ser null");
        }

        if (pk == null)
        {
            throw new IllegalArgumentException("La clave primaria a cargar no puede ser null");
        }

        HibernateTemplate template = getHibernateTemplate();

        if (logger.isDebugEnabled())
        {
            logger.debug("Se va a tratar de realizar un LOAD sobre la entidad: " + clazz.getName() + " con PK: " + pk);
        }

        Object resLoad = template.load(clazz, pk);

        if (logger.isDebugEnabled())
        {
            logger.debug("OK -> " + resLoad + " load( " + clazz.getName() + ',' + pk + " ) ");
        }

        return resLoad;
    }

    /**
     * Obtiene el objeto de la base de datos directamente sin pasar por la cache.<br>
     * Devuelve null si el objeto no existe en la base de datos.<br>
     * �ste m�todo es un wraper del m�todo HibernateTemplate#get
     *
     * @param clazz La clase a obtener.
     * @param ser   La primary key del objeto a obtener.
     * @return El objeto obtenido de la base de datos o null si no se encuentra.
     * @see HibernateTemplate#get(Class,Serializable)
     */

    public Object get(Class clazz, Serializable ser)
    {
        if (clazz == null)
        {
            throw new IllegalArgumentException("El parametro Class no puede ser null");
        }

        if (ser == null)
        {
            throw new IllegalArgumentException("El parametro Serializable no puede ser null");
        }

        HibernateTemplate template = getHibernateTemplate();

        if (logger.isDebugEnabled())
        {
            logger.debug("Se va a tratar de realizar un GET sobre la entidad: " + clazz.getName() + " con PK: " + ser);
        }

        Object obj = template.get(clazz, ser);

        if (logger.isDebugEnabled())
        {
            logger.debug("OK -> " + obj + " get( " + clazz.getName() + ", " + ser + " )");
        }

        return obj;
    }

    /**
     * Realiza una actualizacion de la entidad pasada como par�metro.<br>
     * �ste m�todo es un wraper del m�todo HibernateTemplate@update .
     *
     * @param entity La entidad a persistir.
     * @see HibernateTemplate#update(Object)
     */
    public void update(Object entity)
    {
        if (entity == null)
        {
            throw new IllegalArgumentException("El par�metro Object no puede ser null");
        }

        HibernateTemplate template = getHibernateTemplate();

        if (logger.isDebugEnabled())
        {
            logger.debug("Se va a tratar de realizar un UPDATE sobre la entidad: " + entity.getClass().getName());
        }

        template.update(entity);

        if (logger.isDebugEnabled())
        {
            logger.debug("OK -> update( " + entity + " )");
        }
    }

    /**
     * Elimina la entidad pasada como par�metro de la base de datos.
     * �ste m�todo es un wraper del m�todo HibernateTemplate@delete .
     *
     * @param entity La entidad a suprimir de la BD.
     * @see HibernateTemplate#delete(Object)
     */
    public void delete(Object entity)
    {
        Assert.notNull(entity, "El par�metro Object no puede ser null");
        HibernateTemplate template = getHibernateTemplate();

        if (logger.isDebugEnabled())
        {
            logger.debug("Se va a tratar de realizar un DELETE sobre la entidad: " + entity.getClass().getName());
        }

        template.delete(entity);

        if (logger.isDebugEnabled())
        {
            logger.debug("OK -> delete( " + entity + " )");
        }

    }

    /**
     * Realiza un save o un update en funcion de si existe la entidad previamente o no.
     *
     * @param entity La entidad a persistir.
     * @see HibernateTemplate#saveOrUpdate(Object)
     */
    public void saveOrUpdate(Object entity)
    {
        if (entity == null)
        {
            throw new IllegalArgumentException("El par�metro Object no puede ser null");
        }

        HibernateTemplate template = getHibernateTemplate();

        if (logger.isDebugEnabled())
        {
            logger.debug("Se va a tratar de realizar un saveOrUpdate sobre la entidad: " + entity);
        }

        template.saveOrUpdate(entity);

        if (logger.isDebugEnabled())
        {
            logger.debug("OK -> saveOrUpdate( " + entity + " )");
        }

    }

    /**
     * Obtiene un SQLQuery de la session actual.
     *
     * @param sql El SQL con el que construir la SQLQuery.
     * @return Un SQLQuery de la session actual.
     */
    protected SQLQuery getSQLQuery(String sql)
    {
        return sessionFactory.getCurrentSession().createSQLQuery(sql);
    }

    /**
     * Obtiene un objeto Query para ejecutar el HQL indicado.
     *
     * @param hql El HQL a ejecutar.
     * @return El objeto Query instanciado.
     */
    protected Query getQuery(String hql)
    {
        return sessionFactory.getCurrentSession().createQuery(hql);
    }


    /**
     * Obtiene un objeto Criteria de la session actual asociandolo a la clase de la entidad pasada como par�metro.
     *
     * @param entityClass La clase de la entidad sobre la que crear el Criteria.
     * @return El Criteria instanciado.
     */
    protected Criteria getCriteria(Class entityClass)
    {
        return sessionFactory.getCurrentSession().createCriteria(entityClass);
    }

}

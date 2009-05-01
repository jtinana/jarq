package es.onlysolutions.arq.core.service.facade;

import es.onlysolutions.arq.core.accesobd.GenericHbDao;
import es.onlysolutions.arq.core.accesobd.IConstanstAccesoDatos;
import es.onlysolutions.arq.core.accesobd.IEntityId;
import es.onlysolutions.arq.core.configuration.Configuracion;
import es.onlysolutions.arq.core.log.LoggerGenerator;
import es.onlysolutions.arq.core.service.utils.EntityUtils;
import org.apache.commons.logging.Log;
import org.displaytag.pagination.PaginatedList;
import org.springframework.util.Assert;

import java.io.Serializable;

/**
 * Facade genérico de la arquitectura. Realiza las operaciones genericas definidas por la arquitectura, que son:<br>
 * cargar, borrar, guardar y list entidades.
 * Es una clase abstracta, y debe ser heredada. De ésta forma se obliga a localizar una subclase y se pueden autorizar
 * sus metodos de forma separada.
 */

public abstract class GenericFacade
{
    /**
     * El logger de la clase.
     */
    protected final Log logger = LoggerGenerator.getLogger(this.getClass());

    /**
     * Instancia del generic dao para llamada a los metodos genericos.
     */
    private GenericHbDao daoHb;


    public GenericHbDao getDaoHb()
    {
        return daoHb;
    }

    /**
     * Setter para el generic dao. Lo llamara el IoC container.
     *
     * @param daoHb El dao generico a establecer.
     */
    public void setDaoHb(GenericHbDao daoHb)
    {
        this.daoHb = daoHb;
    }

    /**
     * Devuelve la entidad con los datos rellenos. Obtiene la primary key de la endidad y realiza un load para rellenarla.
     *
     * @param entidad La entidad de la que obtener la primary key.
     * @return La entidad rellena de la base de datos.
     */
    public IEntityId cargar(IEntityId entidad)
    {
        Assert.notNull(this.daoHb, "No se ha configurado un genericHbDao bajo el atributo 'daoHb' para el facade: " + getClass().getName() + ", revise la configuracion del facade.xml");
        IEntityId entityId = this.daoHb.cargar(entidad);

        if (logger.isInfoEnabled())
        {
            logger.info("Cargamos la entidad con ID: " + EntityUtils.getId(entidad) + " -> " + entityId);
        }

        return entityId;
    }

    /**
     * Elimina de la base de datos la entidad indicada.
     *
     * @param entidad La entidad a borrar.
     */
    public void borrar(IEntityId entidad)
    {
        Assert.notNull(this.daoHb, "No se ha configurado un genericHbDao bajo el atributo 'daoHb' para el facade: " + getClass().getName() + ", revise la configuracion del facade.xml");
        this.daoHb.borrar(entidad);

        if (logger.isInfoEnabled())
        {
            logger.info("Entidad : " + entidad + " eliminada. ID: " + EntityUtils.getId(entidad));
        }
    }

    /**
     * Almacena la entidad en la base de datos. Realiza un save sobre dicha entidad.
     *
     * @param entidad La entidad a salvar.
     * @return La nueva primary key asociada a la entidad guardada.
     */
    public IEntityId guardar(IEntityId entidad)
    {
        Assert.notNull(this.daoHb, "No se ha configurado un genericHbDao bajo el atributo 'daoHb' para el facade: " + getClass().getName() + ", revise la configuracion del facade.xml");
        IEntityId entityId = this.daoHb.guardar(entidad);

        if (logger.isInfoEnabled())
        {
            logger.info("Entidad : " + entidad + " salvada. ID: " + EntityUtils.getId(entidad));
        }

        return entityId;
    }

    /**
     * Devuelve una lista paginada en base a los criterios indicados como parametros.
     *
     * @param entityClazz La clase de la entidad del que obtener el listado.
     * @param criterios   El objeto del que obtener los criterios de busqueda, si se pasa un null no se realiza filtro alguno.
     * @param numPagina   El numero de pagina que obtener.
     * @param pageSize    El tamaño de pagina a listar.
     * @param sort        Nombre de la propiedad de la entidad por la que ordenar.
     * @param dir         Direccion por la que ordenar (asc o desc).
     * @return La lista paginada.
     */
    public PaginatedList list(Class entityClazz, Object criterios, Integer numPagina, Integer pageSize, String sort, String dir)
    {
        Assert.notNull(this.daoHb, "No se ha configurado un genericHbDao bajo el atributo 'daoHb' para el facade: " + getClass().getName() + ", revise la configuracion del facade.xml");
        PaginatedList list = this.daoHb.list(entityClazz, criterios, numPagina, pageSize, sort, dir);

        if (logger.isDebugEnabled())
        {
            logger.debug("Obtenemos la lista: " + list);
        }

        return list;
    }

    /**
     * Devuelve una lista paginada en base a los criterios indicados como parametros. No recibe el tamaño de pagina y lo
     * obtiene de la configuracion.
     *
     * @param entityClazz La clase de la entidad a listar.
     * @param criterios   Los criterios por los que filtrar, si se pasa un null no se realiza filtro alguno.
     * @param numPagina   El numero de pagina a listar.
     * @param sort        El nombre del campo de la entidad por el que ordenar.
     * @param dir         La direccion de ordenacion (valores asc o desc)
     * @return La lista paginada resultante.
     */
    public PaginatedList list(Class entityClazz, Object criterios, Integer numPagina, String sort, String dir)
    {
        int pageSize = Configuracion.getInteger(IConstanstAccesoDatos.PARAM_PAGESIZE);
        return this.list(entityClazz, criterios, numPagina, pageSize, sort, dir);
    }

    /**
     * Realiza la exportacion de la lista. El metodo delega el listado en el metodo GenericHbDao#list, pero pidiendo un tamaño de
     * pagina en funcion del tamaño de exportacion definido en la configuracion y pidiendo siempre desde la primera pagina.
     *
     * @param entityClazz La clase entidad a exportar.
     * @param criterios   Los criterios por los que filtrar.
     * @param sort        El campo por el que ordenar.
     * @param dir         La direccion de la ordenacion.
     * @param pageSize    El tamaño de pagina a exportar.
     * @param pageNumber  El numero de pagina a exportar. Deberia ser siempre la primera, pero se permite una peticion concreta.
     * @return La lista a exportar.
     */

    public PaginatedList export(Class entityClazz, Object criterios, Integer pageNumber, Integer pageSize, String sort, String dir)
    {
        Assert.notNull(this.daoHb, "No se ha configurado un genericHbDao bajo el atributo 'daoHb' para el facade: " + getClass().getName() + ", revise la configuracion del facade.xml");
        PaginatedList list = this.daoHb.list(entityClazz, criterios, pageNumber, pageSize, sort, dir);

        if (logger.isInfoEnabled())
        {
            logger.info("Exportamos la lista: " + list);
        }

        return list;

    }

    /**
     * Realiza la exportacion de la lista. El metodo delega el listado en el metodo GenericHbDao#list, pero pidiendo un tamaño de
     * pagina en funcion del tamaño de exportacion definido en la configuracion y pidiendo siempre desde la primera pagina.
     *
     * @param entityClazz La clase entidad a exportar.
     * @param criterios   Los criterios por los que filtrar.
     * @param sort        El campo por el que ordenar.
     * @param dir         La direccion de la ordenacion.
     * @return La lista a exportar.
     */
    public PaginatedList export(Class entityClazz, Object criterios, String sort, String dir)
    {
        //exportacion, tamaño de pagina especial
        int pageSize = Configuracion.getInteger("displayTag.exportSize");

        //Exportamos desde la primera pagina
        return this.export(entityClazz, criterios, 1, pageSize, sort, dir);
    }

    /**
     * Realiza un load de la entidad indicada.
     *
     * @param entityClass La clase de la entidad a cargar.
     * @param pk          La clave principal de la entidad a cargar.
     * @return La entidad cargada de la base de datos.
     * @see es.onlysolutions.arq.core.accesobd.GenericHbDao#load(Class,java.io.Serializable)
     */
    public Object load(Class entityClass, Serializable pk)
    {
        return this.daoHb.load(entityClass, pk);
    }

    /**
     * Realiza un get de la entidad indicada.
     *
     * @param entityClass La clase de la entidad a cargar.
     * @param pk          La clave principal de la entidad a cargar.
     * @return La clave cargada o null si no se encuentra.
     * @see es.onlysolutions.arq.core.accesobd.GenericHbDao#get(Class,java.io.Serializable)
     */
    public Object get(Class entityClass, Serializable pk)
    {
        return this.daoHb.get(entityClass, pk);
    }
}

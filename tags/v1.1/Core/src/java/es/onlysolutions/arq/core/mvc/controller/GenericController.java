package es.onlysolutions.arq.core.mvc.controller;

import es.onlysolutions.arq.core.accesobd.IEntityId;
import es.onlysolutions.arq.core.mvc.ajax.JSONRPCBridgeWrapper;
import es.onlysolutions.arq.core.mvc.command.AbstractCommandBean;
import es.onlysolutions.arq.core.mvc.command.DisplayTagCommandBean;
import es.onlysolutions.arq.core.service.facade.GenericFacade;
import es.onlysolutions.arq.core.service.utils.EntityUtils;
import org.displaytag.pagination.PaginatedList;
import org.springframework.util.Assert;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Enumeration;

/**
 * Controller de la arquitectura para realizar mantenimientos. Implementa varios metodos para
 * guardar, borrar y list que pueden usados de forma generica por cualquier formulario que utilize
 * AbstractCommandBean como Command Bean.
 */
public abstract class GenericController extends GlobalController
{
    /**
     * Facade generico que contienen todos los controller. Evita la declaracion de un atributo en las clases hijas.
     */
    private GenericFacade facade;

    /**
     * Atributo que indica cuando en este controller se chequeara si el metodo de submit es POST para ejecutar el onSubmit.
     */
    private boolean submissionWithPost = false;

    /**
     * View al que redirigir, utilizado para casos de error y tener una tercera opcion.
     */
    private String errorView;

    /**
     * Vista secundaria a la que redirigir.
     */
    private String secondaryView;

    /**
     * Accion para realizar la carga de la entidad.
     */
    public static final String ACTION_CARGAR = "cargar";

    /**
     * Accion para guardar la entidad.
     */
    public static final String ACTION_GUARDAR = "guardar";

    /**
     * Accion para realizar el borrado de la entidad.
     */
    public static final String ACTION_BORRAR = "borrar";

    /**
     * Accion para realizar la modificacion de la entidad.
     */
    public static final String ACTION_MODIFICAR = "modificar";

    /**
     * Valor por defecto para la exportacion del display tag.
     */
    public static final int DEFAULT_EXPORT_SIZE = 5000;

    /**
     * Objeto Bridge para llamadas AJAX utilizando JSON-RPC.
     * Este objeto se introducira en la session bajo el nombre del id del bean en cada peticion a este controller
     */
    private JSONRPCBridgeWrapper jsonBridge;


    /**
     * Getter for property 'jsonBridge'.
     *
     * @return Value for property 'jsonBridge'.
     * @see #jsonBridge
     */
    public JSONRPCBridgeWrapper getJsonBridge()
    {
        return jsonBridge;
    }

    /**
     * Setter for property 'jsonBridge'.
     *
     * @param jsonBridge Value to set for property 'jsonBridge'.
     * @see #jsonBridge
     */
    public void setJsonBridge(JSONRPCBridgeWrapper jsonBridge)
    {
        this.jsonBridge = jsonBridge;
    }

    /**
     * Getter for property 'submissionWithPost'.
     *
     * @return Value for property 'submissionWithPost'.
     * @see #submissionWithPost
     */
    public boolean isSubmissionWithPost()
    {
        return submissionWithPost;
    }


    /**
     * Establece si en este controller se debe chequear si es un POST para ejecutar un onsubmit. Por
     * defecto es 'false', lo que significa que siempre se ejecuta el onSubmit a menos que se
     * indique un true.
     *
     * @param submissionWithPost Value to set for property 'submissionWithPost'.
     * @see #submissionWithPost
     */
    public void setSubmissionWithPost(boolean submissionWithPost)
    {
        this.submissionWithPost = submissionWithPost;
        if (logger.isInfoEnabled())
        {
            if (this.submissionWithPost)
            {
                logger.info("Controller configurado para ejecutar onSubmit solo en peticiones POST.");
            }
            else
            {
                logger.info("Controller configurado para ejecutar en cada peticion el metodo onSubmit");
            }
        }
    }


    /**
     * Getter para la propiedad 'facade'.
     *
     * @return El valor de la propiedad 'facade'.
     */
    public GenericFacade getFacade()
    {
        return facade;
    }


    /**
     * Setter para la propiedad 'facade'.
     *
     * @param facade Establece el valor facade para la propiedad 'facade'.
     */
    public void setFacade(GenericFacade facade)
    {
        this.facade = facade;
    }


    /**
     * Se sobreescribe el metodo para indicar que cada petici�n es un form submission. De esta forma
     * se ejecuta en cada petici�n el metodo onSubmit que implementa la l�gica principal de la
     * clase.
     *
     * @param request El HttpServletRequest.
     * @return true si es un form submision, false en caso contrario.
     */
    @Override
    protected boolean isFormSubmission(HttpServletRequest request)
    {
        boolean result = true;
        if (this.submissionWithPost)
        {
            result = "POST".equals(request.getMethod());
        }
        return result;
    }


    /**
     * Realiza la carga de la entidad indicada en el AbstractCommandBean.
     *
     * @param request  El HttpServletRequest.
     * @param response El HttpServletResponse.
     * @param command  El AbstractCommandBean.
     * @param e        La BindException de esta peticion.
     * @throws Exception Cualquier excepcion que ocurra durante la ejecucion de dicho metodo.
     */
    protected void doCargar(HttpServletRequest request, HttpServletResponse response, AbstractCommandBean command, BindException e) throws Exception
    {
        Assert.notNull(facade, "No se ha indicado un genericFacade bajo el atributo 'facade' para el controller: " + getClass().getName() + ", revise el fichero controller.xml");
        IEntityId entidad = command.getEntidad();
        if (EntityUtils.getId(entidad) != null)
        {
            entidad = facade.cargar(entidad);
            command.setEntidad(entidad);
        }
    }


    /**
     * Persiste la entidad del AbstractCommandBean en la base de datos.
     *
     * @param request  El HttpServletRequest.
     * @param response El HttpServletResponse.
     * @param command  El AbstractCommandBean.
     * @param e        La BindException de esta peticion.
     * @throws Exception Cualquier excepcion que ocurra durante la ejecucion de dicho metodo.
     */
    protected void doGuardar(HttpServletRequest request, HttpServletResponse response, AbstractCommandBean command, BindException e) throws Exception
    {
        Assert.notNull(facade, "No se ha indicado un genericFacade bajo el atributo 'facade' para el controller: " + getClass().getName() + ", revise el fichero controller.xml");
        IEntityId entidad = command.getEntidad();
        entidad = facade.guardar(entidad);
        command.setEntidad(entidad);
    }


    /**
     * Elimina de la base de datos la entidad indicada en el AbstractCommandBean.
     *
     * @param request  El HttpServletRequest.
     * @param response El HttpServletResponse.
     * @param command  El AbstractCommandBean.
     * @param e        La BindException de esta peticion.
     * @throws Exception Cualquier excepcion que ocurra durante la ejecucion de dicho metodo.
     */
    protected void doBorrar(HttpServletRequest request, HttpServletResponse response, AbstractCommandBean command, BindException e) throws Exception
    {
        Assert.notNull(facade, "No se ha indicado un genericFacade bajo el atributo 'facade' para el controller: " + getClass().getName() + ", revise el fichero controller.xml");
        IEntityId entidad = command.getEntidad();
        facade.borrar(entidad);
        command.setEntidad(entidad.getClass().newInstance());
    }


    /**
     * Realiza un listado en base a los criterios indicados en el AbstractCommandBean.
     *
     * @param request       El HttpServletRequest.
     * @param response      El HttpServletResponse.
     * @param command       El AbstractCommandBean.
     * @param bindException La BindException de esta peticion.
     * @throws Exception Cualquier excepcion que ocurra durante la ejecucion de dicho metodo.
     */
    protected void doListar(HttpServletRequest request, HttpServletResponse response, AbstractCommandBean command, BindException bindException) throws Exception
    {
        Assert.notNull(command, "No se permite un AbstractCommandBean a null como parametro, " + "revise la configuracion del controller: " + getClass().getName());
        Assert.notNull(facade, "No se ha indicado un genericFacade bajo el atributo 'facade' para el controller: " + getClass().getName() + ", revise el fichero controller.xml");
        IEntityId entidad = command.getEntidad();
        Assert.notNull(entidad, "El AbstractCommandBean debe tener un atributo 'entidad' no nulo. " + "Verifique el constructor de la clase: " + command.getClass().getName());
        Object criterios = command.getCriteria();
        PaginatedList paginatedList = command.getPaginatedList();
        Assert.notNull(entidad, "El AbstractCommandBean debe tener un atributo 'paginatedList' no nulo. " + "Verifique el constructor de la clase: " + command.getClass().getName());
        // Parametros por si se trata de una peticion desde un DisplayTag
        int page = paginatedList.getPageNumber();
        String sort = null;
        String dir = null;
        boolean isExportAction = false;
        if (command instanceof DisplayTagCommandBean)
        {
            DisplayTagCommandBean displayTagCommandBean = (DisplayTagCommandBean) command;
            page = displayTagCommandBean.getPage();
            sort = displayTagCommandBean.getSort();
            dir = displayTagCommandBean.getDir();
            /**
             * Si se trata de un DisplayTagCommandBean y aparece el parametro de exportacion
             * entonces estamos ante una exportacion, con lo que hay que asignar el tamanyo de pagina
             * al parametro correspondiente
             */
            isExportAction = isExportDisplayTagAction(request);
        }
        if (isExportAction)
        {
            paginatedList = this.facade.export(entidad.getClass(), criterios, sort, dir);
        }
        else
        {
            paginatedList = this.facade.list(entidad.getClass(), criterios, page, sort, dir);
        }
        command.setPaginatedList(paginatedList);
    }


    /**
     * Implementacion del metodo onSubmit de Spring. Nuestra implementacion presupone que el command
     * bean es una subclase de AbstractCommandBean. Obtiene del bean la accion a ejecutar y ejecuta
     * el metodo correspondiente a dicha accion. Finalmente realiza un listado y finaliza delegando
     * el resultado en super.onSubmit.
     *
     * @param request       El HttpServletRequest.
     * @param response      El HttpServletResponse.
     * @param command       El Command Bean.
     * @param bindException El BindException de la peticion.
     * @return El ModelAndView al que redirigir.
     * @throws Exception Si ocurre alguna excepcion durante la ejecucion.
     */
    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command, BindException bindException) throws Exception
    {
        Assert.notNull(command, "No se permite un CommandBean a null como parametro, revise la configuracion del controller: " + getClass().getName());
        if (!(command instanceof AbstractCommandBean))
        {
            throw new IllegalArgumentException("El objeto Command Bean debe heredar de la clase " + AbstractCommandBean.class.getName());
        }
        AbstractCommandBean abstractCommandBean = (AbstractCommandBean) command;
        String accion = abstractCommandBean.getAccion();
        Object entidad = abstractCommandBean.getEntidad();
        // Ponemos los las cadenas vacias a null en el criterio de busqueda
        Object criteria = abstractCommandBean.getCriteria();
        EntityUtils.sustituirValoresVacios(criteria);
        if (ACTION_CARGAR.equals(accion))
        {
            doCargar(request, response, abstractCommandBean, bindException);
        }
        else if (ACTION_GUARDAR.equals(accion) || ACTION_MODIFICAR.equals(accion))
        {
            doGuardar(request, response, abstractCommandBean, bindException);
        }
        else if (ACTION_BORRAR.equals(accion))
        {
            doBorrar(request, response, abstractCommandBean, bindException);
            // Instanciamos de nuevo la entidad para que se limpie el formulario.
            entidad = entidad.getClass().newInstance();
        }
        // Realizamos el listado.
        doListar(request, response, abstractCommandBean, bindException);
        // Redirigimos al success view si el proceso ha ido bien.
        ModelAndView modelAndView = new ModelAndView(getSuccessView(), getCommandName(), abstractCommandBean);
        return modelAndView;
    }


    /**
     * Sobreescribimos el formBackingObject para obligar a implementar el metodo loadPage.
     *
     * @param request El HttpServletRequest de la peticion.
     * @return El Command Bean con el que rellenar la pagina. null si no se desea precargar nada.
     * @throws Exception Si ocurre alguna excepcion durante el proceso.
     */
    @Override
    protected final Object formBackingObject(HttpServletRequest request) throws Exception
    {
        AbstractCommandBean commandBean = loadPage(request);
        Object resultCommand;
        if (commandBean == null)
        { // No se desea precargar nada, devolvemos el bean instanciado.
            if (logger.isDebugEnabled())
            {
                logger.debug("El bean se devolvio a null, se instancia uno nuevo");
            }
            resultCommand = createCommand();
        }
        else
        {
            resultCommand = commandBean;
        }
        if (logger.isDebugEnabled())
        {
            logger.debug("Se devuelve el Command Bean: " + resultCommand);
        }

        //Registramos el Jason Bridge.
        if (getJsonBridge() != null)
        {
            HttpSession session = request.getSession(false);
            JSONRPCBridgeWrapper json_bridge = getJsonBridge();
            Assert.hasLength(json_bridge.getRegisterName(), "No se ha indicado un nombre para el JSONRPCBridgeWrapper");
            session.setAttribute(json_bridge.getRegisterName(), json_bridge.getInnerBridge());
        }


        return resultCommand;
    }


    /**
     * Metodo que devuelve el AbstractCommandBean que se utiliza para la carga de la pagina. En este
     * metodo se debe cargar el commandBean con todas las listas y datos necesarios para cargar la
     * pagina sin errores, delegando el proceso de la peticion en el metodo onSubmit.
     *
     * @param request El HttpServletRequest del que obtener los parametros si son necesarios.
     *                Utilizar para ello la clase de Spring
     *                org.springframework.web.bind.ServletRequestUtils
     * @return El AbstractCommandBean con los datos, o null si no se desea precargar nada.
     * @throws Exception Si ocurre algun error durante el proceso.
     * @see org.springframework.web.bind.ServletRequestUtils
     */
    protected abstract AbstractCommandBean loadPage(HttpServletRequest request) throws Exception;


    /**
     * Metodo para buscar el parametro asociado a la opcion de exportacion. Dicho parametro aparece
     * en la sesion segun la expresion regular "d-XXXXX-e", por lo que se comprueba que el parametro
     * empiece por los caracteres "d-" y finalice por "-e".
     *
     * @param request El HttpServletRequest del que obtener los parametros.
     * @return TRUE si se ha encontrado el parametro de exportacion, FALSE en caso contrario.
     */
    private boolean isExportDisplayTagAction(HttpServletRequest request)
    {
        boolean encontrado = false;
        Enumeration enumer = request.getParameterNames();
        String parametro;
        while ((enumer.hasMoreElements()) && (!encontrado))
        {
            parametro = (String) enumer.nextElement();
            encontrado = (parametro.startsWith("d-")) && (parametro.endsWith("-e"));
        }
        return encontrado;
    }


    /**
     * Obtiene el nombre de la vista de error.
     *
     * @return Un String con el nombre de la vista de error.
     */
    public String getErrorView()
    {
        return errorView;
    }


    /**
     * Establece una vista de error para tener la opcion de configurar una tercera View.
     *
     * @param errorView El error view a establecer.
     */
    public void setErrorView(String errorView)
    {
        this.errorView = errorView;
    }


    /**
     * Vista secundaria a la que redirigir.
     *
     * @return La vista secundaria a la que redirigir.
     */
    public String getSecondaryView()
    {
        return secondaryView;
    }


    /**
     * Establece la vista secundaria a la que redirigir.
     *
     * @param secondaryView La vista secundaria.
     */
    public void setSecondaryView(String secondaryView)
    {
        this.secondaryView = secondaryView;
    }
}
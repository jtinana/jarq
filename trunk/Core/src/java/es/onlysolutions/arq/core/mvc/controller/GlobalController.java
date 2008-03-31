package es.onlysolutions.arq.core.mvc.controller;

import es.onlysolutions.arq.core.auth.IUserSettings;
import es.onlysolutions.arq.core.auth.UserManager;
import es.onlysolutions.arq.core.auth.exception.AutorisationException;
import es.onlysolutions.arq.core.configuration.Configuracion;
import es.onlysolutions.arq.core.log.LoggerGenerator;
import es.onlysolutions.arq.core.mvc.bind.CalendarCustomEditor;
import es.onlysolutions.arq.core.mvc.bind.DateSqlCustomEditor;
import es.onlysolutions.arq.core.mvc.bind.DateUtilCustomEditor;
import es.onlysolutions.arq.core.mvc.bind.TimestampCustomEditor;
import es.onlysolutions.arq.core.mvc.exception.IExceptionConfiguration;
import es.onlysolutions.arq.core.service.exception.ValidationException;
import org.apache.commons.logging.Log;
import org.springframework.util.Assert;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.beans.PropertyEditorSupport;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Controlador que comprueba si el usuario esta logueado en la session y lo introduce en el
 * UserManager.
 * Por defecto introduce un CustomEditor para realizar el binding automatico entre campos del formulario y clases de tipo
 * java.util.Date, java.sql.Date, java.sql.Timestamp y java.util.Calendar.
 * Dichos campos pueden ser sobreescritos mediante sus atributos correspondientes.
 *
 * @see IUserSettings
 * @see org.springframework.web.servlet.mvc.SimpleFormController
 * @see #setCalendarCustomEditor(java.beans.PropertyEditorSupport)
 * @see #setTimestampCustomEditor(java.beans.PropertyEditorSupport)
 * @see #setDateUtilCustomEditor(java.beans.PropertyEditorSupport)
 * @see #setDateSqlCustomEditor(java.beans.PropertyEditorSupport)
 */
public class GlobalController extends SimpleFormController
{
    /**
     * El logger de la clase.
     */
    protected final Log logger = LoggerGenerator.getLogger(getClass());

    /**
     * Codigo de autenticacion que indica que la sesion ha expirado o no estamos logueados.
     */
    public static final int SESSION_EXPIRED = 1;

    /**
     * Codigo de autenticacion que indica que el objeto que hay en session no es del tipo IUserSettings.
     */
    public static final int USER_NOT_VALID = 2;

    /**
     * Codigo de autenticacion que indica que la no existe un usuario en la session.
     */
    public static final int USER_NOT_FOUND = 3;

    /**
     * Nombre del parametro de configuracion que indicara el nombre del Jsp al que redirigir. El
     * nombre obtenido de este parametro se utilizara para construir directamente el ModelAndView.
     */
    public static final String CONF_AUTHENTICATION_PROPERTY = "auth.authentication.modelAndView";

    /**
     * Nombre del parametro de configuracion que indicara el nombre del Jsp al que redirigir. El
     * nombre obtenido de este parametro se utilizara para construir directamente el ModelAndView.
     */
    public static final String CONF_AUTORISATION_PROPERTY = "auth.autorisation.modelAndView";
    /**
     * Nombre del atributo del ModelAndView bajo el que se guarda el codigo de error.
     */
    public static final String VIEW_NAME_AUTHENTICATION_ERROR_CODE = "VIEW_NAME_AUTHENTICATION_ERROR_CODE";

    /**
     * Constante bajo la cual se introduce la URI inicial si ocurre un error de autenticacion.
     * De esta forma se puede montar una redireccion.
     */
    public static final String INITIAL_URL_AUTHENTICATION_ERROR = "INITIAL_URL_AUTHENTICATION_ERROR_" + System.currentTimeMillis();

    /**
     * Clase que implementa el interfaz para el control y manejo de excepciones
     */

    private IExceptionConfiguration ieConfiguration;

    /**
     * Atributo para definir la clase que actuara como CustomEditor para los objetos <b><i>java.util.Date</i></b>.
     */
    private PropertyEditorSupport dateUtilCustomEditor = new DateUtilCustomEditor();

    /**
     * Atributo para definir la clase que actuara como CustomEditor para los objetos <b><i>java.sql.Date</i></b>.
     */
    private PropertyEditorSupport dateSqlCustomEditor = new DateSqlCustomEditor();

    /**
     * Atributo para definir la clase que actuara como CustomEditor para los objetos <b><i>java.util.Calendar</i></b>.
     */
    private PropertyEditorSupport calendarCustomEditor = new CalendarCustomEditor();

    /**
     * Atributo para definir la clase que actuara como CustomEditor para los objetos <b><i>java.sql.Timestamp</i></b>.
     */
    private PropertyEditorSupport timestampCustomEditor = new TimestampCustomEditor();

    /**
     * Atributo para indicar si en este controller se activa la autenticacion. Por defecto es true.
     */
    private boolean authenticationEnabled = true;


    /**
     * Establece la autenticacion de este controller. Si se desactiva no se requerira estar logueado en session para ejecutarlo.
     *
     * @param authenticationEnabled Value to set for property 'authenticationEnabled'.
     * @see #authenticationEnabled
     */
    public void setAuthenticationEnabled(boolean authenticationEnabled)
    {
        this.authenticationEnabled = authenticationEnabled;
        if (logger.isInfoEnabled())
        {
            if (!authenticationEnabled)
            {
                logger.info("** Controller configurado con la Autenticacion desactivada **");
            }
        }
    }

    /**
     * Setter for property 'ieConfiguration'.
     *
     * @param ieConfiguration Value to set for property 'ieConfiguration'.
     */
    public void setIeConfiguration(IExceptionConfiguration ieConfiguration)
    {
        this.ieConfiguration = ieConfiguration;
    }

    /**
     * Obtiene el objeto PropertyEditorSupport que se esta utiliando actualmente para el tipo java.util.Date.
     *
     * @return El objeto PropertyEditorSupport a utilizar.
     */
    public PropertyEditorSupport getDateUtilCustomEditor()
    {
        return dateUtilCustomEditor;
    }

    /**
     * Establece el CustomEditor que se utilizara para los objetos de tipo java.util.Date.
     *
     * @param paramDateUtilCustomEditor El objeto PropertyEditorSupport a utilizar.
     */
    public void setDateUtilCustomEditor(PropertyEditorSupport paramDateUtilCustomEditor)
    {
        this.dateUtilCustomEditor = paramDateUtilCustomEditor;

        if (logger.isInfoEnabled())
        {
            logger.info("Establecido para java.util.Date el PropertyEditor: " + paramDateUtilCustomEditor);
        }
    }

    /**
     * Obtiene el objeto PropertyEditorSupport que se esta utiliando actualmente para el tipo java.sql.Date.
     *
     * @return El objeto PropertyEditorSupport a utilizar.
     */
    public PropertyEditorSupport getDateSqlCustomEditor()
    {
        return dateSqlCustomEditor;
    }

    /**
     * Establece el CustomEditor que se utilizara para los objetos de tipo java.sql.Date.
     *
     * @param paramDateSqlCustomEditor El objeto PropertyEditorSupport a utilizar.
     */
    public void setDateSqlCustomEditor(PropertyEditorSupport paramDateSqlCustomEditor)
    {
        this.dateSqlCustomEditor = paramDateSqlCustomEditor;

        if (logger.isInfoEnabled())
        {
            logger.info("Establecido para java.sql.Date el PropertyEditor: " + paramDateSqlCustomEditor);
        }
    }

    /**
     * Obtiene el objeto PropertyEditorSupport que se esta utiliando actualmente para el tipo java.util.Calendar.
     *
     * @return El objeto PropertyEditorSupport a utilizar.
     */
    public PropertyEditorSupport getCalendarCustomEditor()
    {
        return calendarCustomEditor;
    }

    /**
     * Establece el CustomEditor que se utilizara para los objetos de tipo java.util.Calendar.
     *
     * @param paramCalendarCustomEditor El objeto PropertyEditorSupport a utilizar.
     */
    public void setCalendarCustomEditor(PropertyEditorSupport paramCalendarCustomEditor)
    {
        this.calendarCustomEditor = paramCalendarCustomEditor;

        if (logger.isInfoEnabled())
        {
            logger.info("Establecido para java.util.Calendar el PropertyEditor: " + paramCalendarCustomEditor);
        }
    }

    /**
     * Obtiene el objeto PropertyEditorSupport que se esta utiliando actualmente para el tipo java.sql.Timestamp.
     *
     * @return El objeto PropertyEditorSupport a utilizar.
     */
    public PropertyEditorSupport getTimestampCustomEditor()
    {
        return timestampCustomEditor;
    }

    /**
     * Establece el CustomEditor que se utilizara para los objetos de tipo java.sql.Timestamp.
     *
     * @param paramTimestampCustomEditor El objeto PropertyEditorSupport a utilizar.
     */
    public void setTimestampCustomEditor(PropertyEditorSupport paramTimestampCustomEditor)
    {
        this.timestampCustomEditor = paramTimestampCustomEditor;

        if (logger.isInfoEnabled())
        {
            logger.info("Establecido para java.sql.Timestamp el PropertyEditor: " + paramTimestampCustomEditor);
        }
    }

    /**
     * Se intercepta todas las llamadas al controlador y se comprueba au autenticacion. En caso de
     * ser v�lida se continua con la ejecucion normal, y si no es asi se redirige a una pantalla de
     * error de autorizacion.
     *
     * @param request             El HttpServletRequest de la peticion.
     * @param httpServletResponse El HttpServletResponse de la peticion.
     * @return El ModelAndView al que redirigir.
     * @throws Exception Si ocurre cualquier error durante la ejecucion del metodo.
     */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse httpServletResponse) throws Exception
    {
        ModelAndView result;

        if (authenticationEnabled)
        {
            HttpSession session = request.getSession(false);
            if (session == null)
            {
                result = authenticationModelAndView(SESSION_EXPIRED, request);
            }
            else
            {
                Object objUser = session.getAttribute(IUserSettings.USER_SETTINGS_ATTRIBUTE_NAME);
                if (objUser == null)
                {
                    result = authenticationModelAndView(USER_NOT_FOUND, request);
                }
                else if (!(objUser instanceof IUserSettings))
                {
                    result = authenticationModelAndView(USER_NOT_VALID, request);
                }
                else
                { // El objeto es de tipo IUserSettings y no es nulo.
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Autenticado con �xito.");
                    }
                    try
                    {
                        IUserSettings userSettings = (IUserSettings) objUser;
                        userSettings.setRemoteAddress(request.getRemoteAddr());
                        UserManager.instance().setUser(userSettings);
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("Introducido en el UserManager el Usuario: " + objUser);
                        }
                        result = super.handleRequestInternal(request, httpServletResponse);
                    }
                    catch (AutorisationException e)
                    {
                        logger.error(e);
                        result = autorisationModelAndView();
                    }
                }
            }
        }
        else //Si no hay autenticacion que comprobar, se realiza de forma normal.
        {
            try
            {
                /**
                 * Se ejecuta sin comprobar la autenticacion, pero es posible que debido a los permisos no se tengan
                 * privilegios para ejecutar ciertos Facades. Capturamos dicha excepcion y redirigimos de forma adecuada
                 * para indicar al usuario que a pesar de ser una pagina publica, no tiene privilegios.
                 * Esto es una contradiccion, pero puede pasar debido a que el desarrollador haya cometido el error
                 * de configurar un controller de forma publica, pero no haya creado unos Facades adecuados para ser
                 * ejecutados de forma publica. 
                 */
                result = super.handleRequestInternal(request, httpServletResponse);
            }
            catch (AutorisationException ae)
            {
                logger.error(ae);
                result = autorisationModelAndView();
            }
        }


        if (logger.isDebugEnabled())
        {
            logger.debug("Redirigimos al ModelAndView: " + result);
        }
        return result;
    }


    /**
     * Devuelve el ModelAndView al que redirigir debido a un error de autorizacion.
     *
     * @return El ModelAndView al que redirigir.
     */
    private ModelAndView autorisationModelAndView()
    {
        ModelAndView view = new ModelAndView(Configuracion.getString(CONF_AUTORISATION_PROPERTY));
        return view;
    }


    /**
     * Devuelve el ModelAndView al que redirigir si hay error de autenticaci�n.
     *
     * @param autenticationCodeError El codigo de error para indicar la razon del error de
     *                               autenticacion.
     * @param request                La request donde introducir los parametros de redireccion.
     * @return El ModelAndView de autenticacion.
     */
    private ModelAndView authenticationModelAndView(int autenticationCodeError, HttpServletRequest request)
    {
        ModelAndView modelAndView = new ModelAndView(Configuracion.getString(CONF_AUTHENTICATION_PROPERTY));
        modelAndView.addObject(VIEW_NAME_AUTHENTICATION_ERROR_CODE, new Integer(autenticationCodeError));

        StringBuilder url = new StringBuilder(request.getRequestURI().length() * 2);
        url.append(request.getRequestURI());
        if (request.getQueryString() != null && request.getQueryString().length() > 0)
        {
            url.append('?');
            url.append(request.getQueryString());
        }
        request.setAttribute(INITIAL_URL_AUTHENTICATION_ERROR, url.toString());

        return modelAndView;
    }


    /**
     * Recupera todas las excepciones producidas al llamar al metodo padre y las almacena en la
     * variable bindErrors, para ser mostradas desde la ventana.
     *
     * @param httpServletRequest  El HttpServletRequest de la peticion.
     * @param httpServletResponse El HttpServletResponse de la peticion.
     * @param object              El Bean objeto de la peticion.
     * @param bindException       El BindException de la petici�n.
     * @return El ModelAndView al que redirigir.
     * @throws Exception Si ocurre cualquier error durante la ejecucion del metodo.
     */
    @Override
    protected ModelAndView processFormSubmission(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object object, BindException bindException) throws Exception
    {
        ModelAndView mav;
        try
        {
            mav = super.processFormSubmission(httpServletRequest, httpServletResponse, object, bindException);
        }
        catch (ValidationException ve)
        {
            List<ObjectError> list = ve.getAllErrors();
            for (int i = 0; i < list.size(); i++)
            {
                bindException.addError(list.get(i));
            }
            mav = showForm(httpServletRequest, httpServletResponse, bindException);
        }
        catch (AutorisationException autorisationException)
        {
            throw autorisationException;
        }
        catch (Exception ex)
        {
            List<ObjectError> errores = new ArrayList<ObjectError>(1);
            if (ieConfiguration == null)
            {
                ObjectError error = new ObjectError(this.getClass().getName(), null, null, "Se ha producido un error generico en la aplicacion: " + ex.toString());
                errores.add(error);
            }
            else
            {
                List<ObjectError> handleErrors = null;
                try
                {
                    handleErrors = ieConfiguration.handleException(httpServletRequest, httpServletResponse, object, bindException, ex);
                    Assert.notNull(handleErrors, "La especificacion del interfaz " + IExceptionConfiguration.class.getName() + " obliga a devolver un ObjectError");
                }
                catch (Exception e)
                {
                    logger.error("Error al ejecutar la clase " + ieConfiguration.getClass().getName(), e);
                    ObjectError error = new ObjectError(this.getClass().getName(), null, null, "Error al ejecutar 'handleException' en la clase " + ieConfiguration.getClass().getName() + " -> " + ex.toString());
                    errores.add(error);
                }

                //Se a�aden los errores a la lista final de errores.
                if (handleErrors != null)
                {
                    errores.addAll(handleErrors);
                }
            }
            addErrores(bindException, errores);
            mav = showForm(httpServletRequest, httpServletResponse, bindException);
        }
        return mav;
    }


    /**
     * Este metodo introduce una lista de errores en el bindException.
     *
     * @param bindException El BindException de la petici�n.
     * @param errores       Lista de errores producidos en la petici�n.
     */
    private void addErrores(BindException bindException, List<ObjectError> errores)
    {
        for (int i = 0; i < errores.size(); i++)
        {
            bindException.addError(errores.get(i));
        }
    }

    /**
     * Se realiza la comprobacion de que PropertyEditors han sido registrados para este controller y se inicializa el binder
     * con ellos. En caso de ser sobreescrito este metodo por subclases debe ser llamado desde la clase hija para
     * mantener dicho registro.
     *
     * @param request El HttpServletRequest de la peticion.
     * @param binder  El binder donde registrar los CustomEditors.
     * @throws Exception Si ocurre alguna Exception durante el registro.
     */
    @Override
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception
    {
        if (this.dateSqlCustomEditor != null)
        {
            binder.registerCustomEditor(java.sql.Date.class, dateSqlCustomEditor);
        }

        if (this.dateUtilCustomEditor != null)
        {
            binder.registerCustomEditor(java.util.Date.class, dateUtilCustomEditor);
        }

        if (this.calendarCustomEditor != null)
        {
            binder.registerCustomEditor(Calendar.class, calendarCustomEditor);
        }

        if (this.timestampCustomEditor != null)
        {
            binder.registerCustomEditor(Timestamp.class, timestampCustomEditor);
        }
    }
}
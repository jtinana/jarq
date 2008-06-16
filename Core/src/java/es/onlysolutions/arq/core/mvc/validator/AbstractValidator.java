package es.onlysolutions.arq.core.mvc.validator;

import es.onlysolutions.arq.core.log.LoggerGenerator;
import es.onlysolutions.arq.core.mvc.command.AbstractCommandBean;
import es.onlysolutions.arq.core.mvc.utils.CalendarUtils;
import es.onlysolutions.arq.core.mvc.utils.StringUtils;
import es.onlysolutions.arq.core.mvc.utils.SynchronizedDateFormat;
import es.onlysolutions.arq.core.service.exception.ValidationException;
import org.apache.commons.logging.Log;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.*;


/**
 * Clase para hacer validaciones genericas de campos.
 * Implementa el interfaz <link>org.springframework.validation.Validator</link>
 *
 * @see org.springframework.validation.Validator
 */
public abstract class AbstractValidator implements Validator
{
    // Constantes para validar el content type de los ficheros Excel.
    public final static String EXCEL1 = "application/excel";
    private final static String EXCEL2 = "application/vnd.ms-excel";
    private final static String EXCEL3 = "application/x-excel";
    private final static String EXCEL4 = "application/x-msexcel";

    /**
     * Constante para indicar cuando un ObjectError es un error global.
     */
    public static final String GLOBAL_ERROR = "-" + System.currentTimeMillis() + "-";
    /**
     * Mapa con las validaciones a introducir desde la configuracion.
     */
    private Map<String, Validation> validations = new TreeMap<String, Validation>();

    /**
     * Constante para definir la propiedad de 'attribute'.
     */
    public static final String ATTRIBUTE = "attribute";

    /**
     * Constante para definir la propiedad de 'required'.
     */
    public static final String REQUIRED = "required";

    /**
     * Constante para definir la propiedad de 'maxLenght'.
     */
    public static final String MAX_LENGHT = "maxLenght";

    /**
     * El alias para el campo a validar.
     */
    public static final String FIELD_ALIAS = "fieldAlias";

    /**
     * El logger de la clase.
     */
    private static final Log logger = LoggerGenerator.getLogger(AbstractValidator.class);

    /**
     * DateFormat que se utilizara para todos los parseos de fechas en la validacion.
     * Se debe acceder a este atributo de forma sincronizada, utilizar para ello
     */
    private SynchronizedDateFormat synchronizedDateFormat = new SynchronizedDateFormat("dd/MM/yyyy");

    /**
     * Sobreescribimos la validacion para capturar errores de forma controlada.
     *
     * @param target El objeto command bean.
     * @param errors El objeto errors.
     */
    public final void validate(Object target, Errors errors)
    {
        try
        {
            if (target instanceof AbstractCommandBean)
            {
                validateFromConfiguration((AbstractCommandBean) target, errors);
                validateForm((AbstractCommandBean) target, errors);
            }
            else
            {
                errors.reject(AbstractCommandBean.class.getName(), "El Command Bean debe heredar de la clase: " + AbstractCommandBean.class.getName());
            }
        }
        catch (ValidationException e)
        {
            List<ObjectError> exceptionErrors = e.getAllErrors();
            for (int index = 0; index < exceptionErrors.size(); index++)
            {
                ObjectError objectError = exceptionErrors.get(index);

                if (!GLOBAL_ERROR.equals(objectError.getObjectName()))
                {
                    errors.rejectValue(
                            objectError.getObjectName(),
                            objectError.getCode(),
                            objectError.getArguments(),
                            objectError.getDefaultMessage()
                    );
                }
                else //Es un error general, sin campo asociado
                {
                    errors.reject(
                            objectError.getCode(),
                            objectError.getArguments(),
                            objectError.getDefaultMessage()
                    );
                }
            }
        }
        catch (Exception e)
        {
            logger.error("Error al validar", e);
            errors.reject(this.getClass().getName(), "Error al realizar la validacion: " + e.toString());
        }
    }

    /**
     * Realiza todas las validaciones introducidas en la configuracion.
     *
     * @param abstractCommandBean El command bean a comprobar.
     * @param errors              El objeto errors donde almacenar los errores.
     * @throws java.lang.IllegalAccessException
     *          Si ocurre un acceso ilegal.
     * @throws java.lang.reflect.InvocationTargetException
     *          Si ocurre un error de invocacion de un metodo.
     * @throws java.beans.IntrospectionException
     *          Si ocurre algun error durante la Introspeccion del Bean.
     * @see java.beans.Introspector
     */
    private void validateFromConfiguration(AbstractCommandBean abstractCommandBean, Errors errors) throws IntrospectionException, IllegalAccessException, InvocationTargetException
    {
        BeanInfo beanInfo = Introspector.getBeanInfo(abstractCommandBean.getClass());
        PropertyDescriptor[] propertyDescriptor = beanInfo.getPropertyDescriptors();

        for (int indexProperty = 0; indexProperty < propertyDescriptor.length; indexProperty++)
        {
            PropertyDescriptor descriptor = propertyDescriptor[indexProperty];
            if (this.validations.containsKey(descriptor.getName()))
            {
                //Realizamos las validaciones para esta propiedad.
                validateProperty(errors, this.validations.get(descriptor.getName()), descriptor, abstractCommandBean);
            }
            else if (logger.isDebugEnabled())
            {
                logger.debug("No se ha encontrado validacion para la propiedad -> " + descriptor.getName());
            }
        }
    }

    /**
     * Valida la propiedad indicada e introduce los posibles errores de validaciones.
     *
     * @param errors     El objeto errors donde almacenar dichos errores.
     * @param validation La validacion a realizar.
     * @param descriptor El descriptor de la propiedad a validar.
     * @param command    El command bean.
     * @throws java.lang.IllegalAccessException
     *          Si ocurre un acceso ilegal.
     * @throws java.lang.reflect.InvocationTargetException
     *          Si ocurre un error de invocacion de un metodo.
     */
    private void validateProperty(Errors errors, Validation validation, PropertyDescriptor descriptor,
                                  AbstractCommandBean command) throws IllegalAccessException, InvocationTargetException
    {
        Object propValue = descriptor.getReadMethod().invoke(command);
        String propertyName = descriptor.getName();
        String fieldAlias = validation.getFieldAlias();
        Integer maxLenght = validation.getMaxLenght();

        if (validation.getRequired())
        {
            if (propValue == null || (propValue instanceof String && ((String) propValue).length() == 0))
            {
                errors.rejectValue(propertyName, "required", new String[]{fieldAlias},
                                   "El campo {0} es obligatorio");
            }
        }

        if (maxLenght != null && maxLenght > 0)
        {
            if (propValue instanceof String)
            {
                String strValue = (String) propValue;
                if (strValue.length() > maxLenght)
                {
                    errors.rejectValue(propertyName, "maxLenght", new String[]{fieldAlias},
                                       "El campo {0} excede de su longitud maxima");
                }
            }
        }
    }

    /**
     * @param command El commandBean del que realizar las validaciones.
     * @param errors  El objeto Errors donde guardar los errores de validaciones.
     */
    protected abstract void validateForm(AbstractCommandBean command, Errors errors);


    /**
     * Metodo generico para validar un telefono. Se comprueba que la longitud del telefono sea 9 y
     * que todos los caracteres sean numericos. Si el campo se presenta nulo, se lanza una
     * IllegalArgumentException. Si el valor es nulo, no se hace ninguna validacion.
     *
     * @param errors         La instancia de Errors para registrar errores.
     * @param nombreAtributo El nombre del atributo del commandBean del que se comprueba el error.
     * @param campo          Nombre del campo que se va a comprobar.
     * @param valor          Contenido del campo a validar.
     *                       validacion.
     */
    protected void validarTelefono(Errors errors, String nombreAtributo, String campo, String valor)
    {
        validarTelefono(errors, nombreAtributo, campo, valor, 9, false);
    }


    /**
     * Metodo generico para validar un telefono. Se comprueba que la longitud del telefono sea la
     * especificada y que todos los caracteres sean numericos. Si el campo se presenta nulo, se lanza
     * una IllegalArgumentException. A diferencia del anterior, se puede indicar si el atributo
     * tratado puede ser nulo o no.
     *
     * @param errors         La instancia de Errors para registrar errores.
     * @param nombreAtributo El nombre del atributo del commandBean del que se comprueba el error.
     * @param campo          Nombre del campo que se va a comprobar.
     * @param valor          Contenido del campo a validar.
     * @param longitud       Longitud maxima del campo.
     * @param obligatorio    Indica si el atributo es obligatorio o no.
     */
    protected void validarTelefono(Errors errors, String nombreAtributo, String campo, String valor, int longitud, boolean obligatorio)
    {
        if (campo == null)
        {
            throw new IllegalArgumentException("Debe indicar un nombre de campo");
        }
        isNumberWithoutDecimals(errors, nombreAtributo, campo, valor);
        if (obligatorio && StringUtils.isEmpty(valor))
        {
            errors.rejectValue(nombreAtributo, "campoObligatorio", new String[]{campo}, "El campo {0} es obligatorio");
        }
        if ((StringUtils.isNotEmpty(valor)) && (valor.length() != longitud))
        {
            errors.rejectValue(nombreAtributo, "longitudTelefono", new String[]{campo}, "La longitud del campo {0} no es valida, debe ser 9");
        }
    }

    /**
     * Realiza la validacion de un fichero de tipo MultipartFile validando que sea un fichero Excel
     * valido.<br>
     * Si ocurre un error, se almacena un error con codigo 'excel.extension'
     *
     * @param file   El fichero a validar. Si se pasa null no se realiza accion alguna.
     * @param errors El objeto errors donde almacenar los errores de validacion.
     * @param field  El nombre del atributo bajo el que almacenar los errores de validacion.
     * @return Devuelve true si no se obtiene ningun error de validacion durante la ejecucion de
     *         este metodo.
     */
    protected boolean validarFicheroExcel(MultipartFile file, Errors errors, String field)
    {
        boolean result = true;
        if (file != null)
        {
            Assert.hasLength(field, "Debe indicar el nombre del campo/atributo bajo el que almacenar los errores de validacion");
            if ((!EXCEL1.equals(file.getContentType())) && (!EXCEL2.equals(file.getContentType())) && (!EXCEL3.equals(file.getContentType())) && (!EXCEL4.equals(file.getContentType())))
            {
                errors.rejectValue(field, "excel.extension", new String[]{file.getOriginalFilename(), "Excel"}, "El fichero {0} no es un fichero {1} valido");
                result = false;
            }
        }
        return result;
    }


    /**
     * Metodo generico para comprobar que un valor sea numerico sin decimales, comprobando que todos los
     * caracteres son numeros. Si el campo se presenta nulo, se lanza una IllegalArgumentException.
     * Si el valor es nulo, no se hace ninguna validacion.
     *
     * @param errors         La instancia de Errors para registrar errores.
     * @param nombreAtributo El nombre del atributo del commandBean del que se comprueba el error.
     * @param campo          Nombre del campo que se va a comprobar.
     * @param valor          Contenido del campo a validar.
     */
    private void isNumberWithoutDecimals(Errors errors, String nombreAtributo, String campo, Object valor)
    {
        if (campo == null)
        {
            throw new IllegalArgumentException("Debe indicar un nombre de campo");
        }
        try
        {
            if (valor != null && valor.toString().length() > 0)
            {
                Long.parseLong(valor.toString());
            }
        }
        catch (Exception ex)
        {
            errors.rejectValue(nombreAtributo, "valorNumerico", new String[]{campo}, "El campo {0} debe ser un valor numerico");
        }
    }


    /**
     * Metodo generico para comprobar que un valor sea numerico con decimales, comprobando que todos los
     * caracteres son numeros o el separador de miles o el signo. Si el campo se presenta nulo, se lanza una
     * IllegalArgumentException. Si el valor es nulo, no se hace ninguna validacion.
     *
     * @param errors         La instancia de Errors para registrar errores.
     * @param nombreAtributo El nombre del atributo del commandBean del que se comprueba el error.
     * @param campo          Nombre del campo que se va a comprobar.
     * @param valor          Contenido del campo a validar.
     */
    private void isNumberWithDecimals(Errors errors, String nombreAtributo, String campo, Object valor)
    {
        if (campo == null)
        {
            throw new IllegalArgumentException("Debe indicar un nombre de campo");
        }
        try
        {
            if (valor != null && valor.toString().length() > 0)
            {
                Double.parseDouble(valor.toString());
            }
        }
        catch (Exception ex)
        {
            errors.rejectValue(nombreAtributo, "valorNumerico", new String[]{campo}, "El campo {0} debe ser un valor numerico");
        }
    }


    /**
     * Valida que el campo de fecha tenga un formato v�lido.
     *
     * @param errors     La instancia de Errors para registrar errores.
     * @param fieldName  El nombre del atributo del commandBean del que se comprueba el error.
     * @param fieldAlias Nombre del campo que se va a comprobar.
     * @param valor      El valor del campo fecha a validar. Si es null o cadena vacia no se realiza validacion alguna.
     * @see #validarObjetoFecha(org.springframework.validation.Errors, Object, String, String)
     * @deprecated Utilizar en su lugar el metodo validarObjetoFecha.
     */
    protected void validarFecha(Errors errors, String fieldName, String fieldAlias, String valor)
    {
        validarObjetoFecha(errors, valor, fieldAlias, fieldName);
    }


    /**
     * Valida que el campo de fecha tenga un formato v�lido. Ademas, si es obligatorio,
     * comprueba que el dato exista.
     *
     * @param errors      La instancia de Errors para registrar errores.
     * @param fieldName   El nombre del atributo del commandBean del que se comprueba el error.
     * @param fieldAlias  Nombre del campo que se va a comprobar.
     * @param valor       El valor del campo fecha a validar. Si es null o cadena vacia no se realiza
     *                    validacion alguna.
     * @param obligatorio Indica si el parametro es obligatorio.
     * @see #validarObjetoFecha(org.springframework.validation.Errors, Object, String, String, boolean)
     * @deprecated Utilizar en su lugar el metodo validarObjetoFecha
     */
    protected void validarFecha(Errors errors, String fieldName, String fieldAlias, String valor, boolean obligatorio)
    {
        validarObjetoFecha(errors, valor, fieldName, fieldAlias, obligatorio);
    }


    /**
     * Metodo que comprueba la longitud de un campo de tipo string, y si es
     * obligatorio o no.
     *
     * @param errores        La instancia de Errors para registrar errores.
     * @param nombreAtributo El nombre del atributo del commandBean del que se comprueba el error.
     * @param nombre         Nombre del campo que se va a comprobar.
     * @param valor          Contenido del campo a validar.
     * @param longitud       Longitud maxima del campo.
     * @param obligatorio    Indica si el atributo es obligatorio o no.
     */
    protected void validarString(Errors errores, String nombreAtributo, String nombre, String valor, int longitud, boolean obligatorio)
    {
        if (obligatorio && StringUtils.isEmpty(valor))
        {
            errores.rejectValue(nombreAtributo, "campoObligatorio", new String[]{nombre}, "El campo {0} es obligatorio");
            return;
        }
        if (valor.length() > longitud)
        {
            errores.rejectValue(nombreAtributo, "stringExcedeLongitud", new String[]{nombre}, "El campo {0} excede la longitud maxima");
        }
    }


    /**
     * Este metodo comprueba si el dato recibido es un numero o no, pero sin decimales.
     * Ademas, si es obligatorio, comprueba que el dato exista. Si se produce algun error,
     * lo almacena en la variable errores.
     * El valor puede ser "null" en el caso de la conversion de un valor numerico
     * nulo a String.
     *
     * @param errores        La instancia de Errors para registrar errores.
     * @param nombreAtributo El nombre del atributo del commandBean del que se comprueba el error.
     * @param nombre         Nombre del campo que se va a comprobar.
     * @param valor          Contenido del campo a validar.
     * @param obligatorio    Indica si el atributo es obligatorio o no.
     */
    protected void validarNumero(Errors errores, String nombreAtributo, String nombre, Object valor, boolean obligatorio)
    {
        if (obligatorio)
        {
            if (valor == null)
            {
                errores.rejectValue(nombreAtributo, "campoObligatorio", new String[]{nombre}, "El campo {0} es obligatorio");
            }
        }
        if (valor != null && valor.toString().trim().length() > 0)
        {
            isNumberWithoutDecimals(errores, nombreAtributo, nombre, valor);
        }
    } //validarNumero(Errors errores, String nombreAtributo,...


    /**
     * Este metodo comprueba si el dato recibido es un numero o no, permitiendo decimales.
     * Ademas, si es obligatorio, comprueba que el dato exista. Si se produce algun error,
     * lo almacena en la variable errores.
     * El valor puede ser "null" en el caso de la conversion de un valor numerico
     * nulo a String.
     *
     * @param errores        La instancia de Errors para registrar errores.
     * @param nombreAtributo El nombre del atributo del commandBean del que se comprueba el error.
     * @param nombre         Nombre del campo que se va a comprobar.
     * @param valor          Contenido del campo a validar.
     * @param obligatorio    Indica si el atributo es obligatorio o no.
     */
    protected void validarNumeroDecimal(Errors errores, String nombreAtributo, String nombre, Object valor, boolean obligatorio)
    {
        if (obligatorio)
        {
            if (valor == null)
            {
                errores.rejectValue(nombreAtributo, "campoObligatorio", new String[]{nombre}, "El campo {0} es obligatorio");
            }
        }
        if (valor != null)
        {
            isNumberWithDecimals(errores, nombreAtributo, nombre, valor);
        }
    } //validarNumeroDecimal(Errors errores, String nombreAtributo,...


    /**
     * Este metodo comprueba si el dato recibido es una direccion de email valida
     * o no. Ademas, si es obligatorio, comprueba que el dato exista. Si se produce
     * algun error, lo almacena en la variable errores.
     *
     * @param errores        La instancia de Errors para registrar errores.
     * @param nombreAtributo El nombre del atributo del commandBean del que se comprueba el error.
     * @param nombre         Nombre del campo que se va a comprobar.
     * @param valor          Contenido del campo a validar.
     * @param obligatorio    Indica si el atributo es obligatorio o no.
     * @param longitud       La longitud minima del campo
     */
    protected void validarEmail(Errors errores, String nombreAtributo, String nombre, String valor, int longitud, boolean obligatorio)
    {
        if (obligatorio)
        {
            if (StringUtils.isEmpty(valor))
            {
                errores.rejectValue(nombreAtributo, "campoObligatorio", new String[]{nombre}, "El campo {0} es obligatorio");
            }
        }
        if (StringUtils.isNotEmpty(valor))
        {
            if (valor.length() > longitud)
            {
                errores.rejectValue(nombreAtributo, "stringExcedeLongitud", new String[]{nombre}, "El campo {0} excede la longitud maxima");
            }
            try
            {
                InternetAddress internetAddress = new InternetAddress(valor, true);

                if (logger.isDebugEnabled())
                {
                    logger.debug("Se parsea correctamente el email: " + internetAddress);
                }
            }
            catch (AddressException ae)
            {
                errores.rejectValue(nombreAtributo, "emailNoValido", new String[]{nombre}, "El campo {0} no es un eMail valido");
            }
        }
    }

    /**
     * Metodo generico para validar si una fecha <param>fechaFin</param> es anterior a otra (<param>fechaInicio</param>).
     * Si alguna de las dos fechas se pasa a null no se realiza validacion alguna.
     *
     * @param errors      El objeto Errors donde se almacenan los errores de validacion.
     * @param fechaInicio La fecha de incio contra la que se compara.
     * @param fechaFin    La fecha que se valida si es anterior a la fecha de inicio.
     * @param fieldName   El nombre del campo de bean al que asociar el error.
     * @param errorCode   el codigo de mensaje de error a mostrar.
     * @param fieldAlias  El alias del campo que se mostrar en el mensaje de error.
     */
    protected void validarFechaEsAnterior(Errors errors, Calendar fechaInicio, Calendar fechaFin, String fieldName, String errorCode, String fieldAlias)
    {
        if (fechaInicio != null && fechaFin != null)
        {
            if (fechaFin.before(fechaInicio))
            {
                errors.rejectValue(fieldName, errorCode, new String[]{fieldAlias}, "La fecha {0} es anterior a la Fecha de inicio");
            }
        }
    }

    /**
     * Realiza la validacion de dos fechas pasadas como un String. Debido a que es necesaria una conversion a un Calendar,
     * si algun formato de fecha falla al tratar de convertirse no se realiza validacion alguna.<br>
     * Para realizar la conversion se utiliza el metodo convertoCal de la clase CalendarUtils.<br>
     * Una vez convertidas las fechas, se delega toda ejecucion en el metodo validarFechaEsAnterior
     *
     * @param errors      El objeto Errors donde guardar los errores de validacion.
     * @param fechaInicio La fecha de inicio contra la que se compara.
     * @param fechaFin    La fecha que se valida si es anterior a la fecha de inicio.
     * @param fieldName   El nombre del campo de bean al que asociar el error.
     * @param errorCode   el codigo de mensaje de error a mostrar.
     * @param fieldAlias  El alias del campo que se mostrar en el mensaje de error.
     * @see es.onlysolutions.arq.core.mvc.utils.CalendarUtils#convertToCal(String)
     * @see #validarFechaEsAnterior(org.springframework.validation.Errors,java.util.Calendar,java.util.Calendar,String,String,String)
     */
    protected void validarFechaEsAnterior(Errors errors, String fechaInicio, String fechaFin, String fieldName, String errorCode, String fieldAlias)
    {
        Calendar calFechaInicio = null;
        Calendar calFechaFin = null;

        try
        {
            calFechaInicio = CalendarUtils.convertToCal(fechaInicio);
            calFechaFin = CalendarUtils.convertToCal(fechaFin);
        }
        catch (IllegalArgumentException e)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("No se realiza validacion de comparacion de fechas ya que no se pudo convertir una de ellas.", e);
            }
        }

        validarFechaEsAnterior(errors, calFechaInicio, calFechaFin, fieldName, errorCode, fieldAlias);
    }

    /**
     * Realiza la validacion de una serie de checkBoxes que sean Strings. Se validara que al menos haya un elemento marcado.
     *
     * @param errors     El objeto Errors donde introducir los errores de validacion.
     * @param checkBoxes Los checkBoxes a validar.
     * @param fieldName  El nombre del campo del command bean a validar.
     * @param fieldAlias El nombre del campo que se desea que aparezca en el mensaje de error.
     */
    protected void validarCheckBoxes(Errors errors, String[] checkBoxes, String fieldName, String fieldAlias)
    {
        if (checkBoxes == null || checkBoxes.length == 0)
        {
            errors.rejectValue(fieldName, "checkBoxes.required", new String[]{fieldAlias}, "Debe seleccionar al menos un campo {0}");
        }
    }

    /**
     * Realiza la validacion de una serie de checkBoxes que sean Integer. Se validara que al menos haya un elemento marcado.
     *
     * @param errors     El objeto Errors donde introducir los errores de validacion.
     * @param checkBoxes Los checkBoxes a validar.
     * @param fieldName  El nombre del campo del command bean a validar.
     * @param fieldAlias El nombre del campo que se desea que aparezca en el mensaje de error.
     */
    protected void validarCheckBoxes(Errors errors, Integer[] checkBoxes, String fieldName, String fieldAlias)
    {
        if (checkBoxes == null || checkBoxes.length == 0)
        {
            errors.rejectValue(fieldName, "checkBoxes.required", new String[]{fieldAlias}, "Debe seleccionar al menos un campo {0}");
        }
    }

    /**
     * Realiza la validacion de una serie de checkBoxes indicados como un array de Boolean. Se validara que al menos uno
     * venga marcado (a true).
     *
     * @param errors     El objeto Errors donde introducir los errores de validacion.
     * @param checkBoxes Los checkBoxes a validar.
     * @param fieldName  El nombre del campo del command bean a validar.
     * @param fieldAlias El nombre del campo que se desea que aparezca en el mensaje de error.
     */
    protected void validarCheckBoxes(Errors errors, Boolean[] checkBoxes, String fieldName, String fieldAlias)
    {
        if (checkBoxes != null)
        {
            boolean alMenosUnoMarcado = false;
            for (int index = 0; index < checkBoxes.length; index++)
            {
                Boolean oneCheckBox = checkBoxes[index];
                if (oneCheckBox)
                {
                    alMenosUnoMarcado = true;
                }
            }

            if (!alMenosUnoMarcado)
            {
                errors.rejectValue(fieldName, "checkBoxes.required", new String[]{fieldAlias}, "Debe marcar al menos un campo {0}");
            }
        }
        else
        {
            /**
             * En Spring esto no puede pasar ya que siempre envia un array, pero se introduce por si no se utilizaran
             * los tags de Spring.
             */
            errors.rejectValue(fieldName, "checkBoxes.required", new String[]{fieldAlias}, "Debe marcar al menos un campo {0}");
        }
    }


    /**
     * Comprueba si un campo es nulo o esta vacio. En caso de no estarlo, comprueba si no supera la longitud
     * determinada. En cualquier caso, si se produce un error, lo a�ade a los errores.
     *
     * @param valor       Valor del campo que hay que comprobar.
     * @param campo       campo de la entidad.
     * @param nombre      Nombre del campo.
     * @param length      Longitud maxima del campo.
     * @param errors      Errores de validacion.
     * @param obligatorio Indica si se debe realizar la validacion de si el campo es obligatorio o no.
     * @return true si no existe error alguno al terminar la ejecucion del metodo, false si se ha anyadido algun error durante el mismo.
     */
    protected Boolean rejectIfEmptyOrWhiteEspaceOrLength(Errors errors, String valor, String campo, String nombre, int length, boolean obligatorio)
    {
        Boolean result = Boolean.TRUE;
        int initialErrors = errors.getErrorCount();

        if (obligatorio)
        {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, nombre, "campoObligatorio", new String[]{campo}, "El campo {0} es obligatorio");
        }
        if ((valor != null) && (valor.length() > length))
        {
            errors.rejectValue(nombre, "tamExcesivo", new String[]{campo}, "La longitud del campo {0} excede su limite (" + length + ")");
        }

        if (errors.getErrorCount() > initialErrors)
        {
            result = Boolean.FALSE;
        }


        return result;
    }

    /**
     * Ejecuta el metodo 'rejectIfEmptyOrWhiteEspaceOrLength' pero indicando que el campo es obligatorio.
     *
     * @see #rejectIfEmptyOrWhiteEspaceOrLength(org.springframework.validation.Errors,String,String,String,int,boolean)
     */
    protected Boolean rejectIfEmptyOrWhiteEspaceOrLength(Errors errors, String valor, String campo, String nombre, int length)
    {
        return rejectIfEmptyOrWhiteEspaceOrLength(errors, valor, campo, nombre, length, true);
    }

    /**
     * Realiza la misma validacion que el metodo del mismo nombre de la clase ValidationUtils de spring, pero indicando si ocurre
     * algun error de validacion durante el proceso para facilitar la programacion.
     *
     * @param errors         Errores de validacion.
     * @param fieldName      El nombre del campo del command bean.
     * @param messageKey     La clave del mensaje a buscar en el ResourceBundle.
     * @param messagesValues El array de valores a sustituir en el mensaje.
     * @param defaultMessage Mensaje por defecto si el messageKey no existe.
     * @return true si la validacion es correcta, false si se a�ade algun error de validacion durante el metodo.
     * @see org.springframework.validation.ValidationUtils#rejectIfEmptyOrWhitespace(org.springframework.validation.Errors,String,String,Object[],String)
     */
    protected Boolean rejectIfEmptyOrWhitespace(Errors errors, String fieldName, String messageKey, String[] messagesValues, String defaultMessage)
    {
        Boolean result = Boolean.TRUE;

        int initialErrors = errors.getErrorCount();

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, fieldName, messageKey, messagesValues, defaultMessage);

        if (errors.getErrorCount() > initialErrors)
        {
            result = Boolean.FALSE;
        }

        return result;
    }

    /**
     * Establece la lista de mapas con las validaciones a introducir desde la configuracion.
     * Cada elemento de la lista contendra un mapa, donde se indicaran las siguientes propiedades:
     * attribute -> java.lang.String Indica el nombre del atributo del bean que se desea validar. <b>Es obligatorio</b>.
     * required -> boolean Indica si este atributo es obligatorio y se debe validar como tal. Si no se inclye por defecto es false.
     * maxLenght -> java.lang.Integer Indica la longitud maxima indicada.
     *
     * @param list La lista de mapas con los parametros de validacion.
     */
    public void setValidations(List<Map<String, String>> list)
    {
        if (list != null && list.size() > 0)
        {
            for (int index = 0; index < list.size(); index++)
            {
                Map<String, String> map = list.get(index);
                String attribute = map.get(ATTRIBUTE);
                Assert.hasLength(attribute, "El parametro " + ATTRIBUTE + " es obligatorio");

                String fieldAlias = map.get(FIELD_ALIAS);
                Assert.hasLength(fieldAlias, "El campo " + FIELD_ALIAS + " es obligatorio");

                String objRequired = map.get(REQUIRED);

                Boolean required = Boolean.valueOf(objRequired);

                String objMaxLenght = map.get(MAX_LENGHT);

                Integer maxLenght = null;
                if (objMaxLenght != null && objMaxLenght.length() > 0)
                {
                    maxLenght = Integer.valueOf(objMaxLenght);
                }

                Validation validation = new Validation();
                validation.setAttribute(attribute);
                validation.setMaxLenght(maxLenght);
                validation.setRequired(required);

                this.validations.put(attribute, validation);
            }
        }
    }


    /**
     * Valida un campo y verifica que sea un fecha valida. Se verifica si el campo es un String, java.util.Date, java.sql.Date,
     * java.sql.Timestamp o java.lang.Calendar.
     *
     * @param errors     El objeto Errors donde almacenar los errores de validacion.
     * @param value      El valor a validar. Debe ser del tipo String, en cuyo caso se valida que el formato sea correcto, o un tipo de fecha valido.
     * @param fieldName  El nombre del campo que se esta validando.
     * @param fieldAlias El alias que se quiere dar al campo.
     * @param required   Indica si es obligatorio que el campo exista, y en caso de no existir se introduce un error de validacion.
     * @return true si la validacion es correcta, false en caso de ocurrir algun error de validacion.
     */
    protected boolean validarObjetoFecha(Errors errors, Object value, String fieldName, String fieldAlias, boolean required)
    {
        int numberOfErrors = errors.getGlobalErrorCount();

        if (required)
        {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, fieldName, "campoObligatorio",
                                                      new String[]{fieldAlias}, "El campo {0} es obligatorio");
        }

        if (value != null)
        {
            if (value instanceof String)
            {
                String strValue = (String) value;
                Date parseDate;
                boolean parseSuccesfull;
                try
                {
                    parseDate = synchronizedDateFormat.parseDate(strValue);
                    parseSuccesfull = parseDate != null;
                }
                catch (ParseException e)
                {
                    parseSuccesfull = false;
                }

                if (!parseSuccesfull)
                {
                    errors.rejectValue(fieldName, "fechaNoValida", new String[]{fieldAlias}, "La fecha {0} no tiene el formato correcto (dd/MM/yyyy)");
                }
            }
            else if (
                    value instanceof java.sql.Date ||
                            value instanceof java.util.Date ||
                            value instanceof Timestamp ||
                            value instanceof Calendar
                    )
            {
                /**
                 * Si ya es de este tipo de objeto no se debe comprobar nada, y aunque se podria quechear tan solo que sea
                 * de tipo java.util.Date, que tanto el de sql como el timestamp le heredan, se deja asi para que quede
                 * claro los tipos que en principio se soportan, y por si algun dia se quiere desglosar para otro tipo
                 * de validaciones.
                 */

            }
            else // Es cualquier otro tipo, asi que es un error.
            {
                errors.rejectValue(fieldName, "tipoFechaNoValido",
                                   new String[]{fieldName}, "El campo {0} no es un objeto de fecha valido");
            }
        }

        return numberOfErrors >= errors.getGlobalErrorCount();
    }

    /**
     * Valida un campo y verifica que sea un fecha valida. Se verifica si el campo es un String, java.util.Date, java.sql.Date,
     * java.sql.Timestamp o java.lang.Calendar. No se comprueba que el campo sea obligatorio.
     *
     * @param errors     El objeto Errors donde almacenar los errores de validacion.
     * @param value      El valor a validar. Debe ser del tipo String, en cuyo caso se valida que el formato sea correcto, o un tipo de fecha valido.
     * @param fieldName  El nombre del campo que se esta validando.
     * @param fieldAlias El alias que se quiere dar al campo.
     * @return true si la validacion es correcta, false en caso de ocurrir algun error de validacion.
     */
    protected boolean validarObjetoFecha(Errors errors, Object value, String fieldName, String fieldAlias)
    {
        return this.validarObjetoFecha(errors, value, fieldName, fieldAlias, false);
    }


}
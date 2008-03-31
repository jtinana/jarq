package es.onlysolutions.arq.core.mvc.tag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;
import java.io.IOException;

/**
 * Tag JSP que genera la funcion JavaScript necesaria para validar un campo en funcion de los parametros indicados.
 */
public class JavaScriptValidatorFieldTag extends BodyTagSupport
{
    /**
     * Tipo de validacion a realizar. Valores validos:<br>
     * REQUIRED -> Indica que el campo es necesario que se indique.
     * NOT_REQUIRED -> <b>Valor por defecto</b>. El campo no es necesario. Si no viene no se realizaran comprobaciones adicionales sobre el.
     */
    private String validationType;

    /**
     * Flag que indica si el campo debe ser un numero. Por defecto es false.
     */
    private boolean isNumber = false;

    /**
     * Flag que indica si se debe validar si el campo debe ser un email valido. Por defecto a false.
     */
    private boolean isEmail = false;

    /**
     * Atributo para indicar la longitud maxima que debe tener el campo. Por defecto no se comprueba, y el valor 0 no esta permitido.
     */
    private Integer maxLenght;

    /**
     * El ID del campo a validar mediante JavaScript.
     */
    private String fieldId;

    /**
     * El alias para el campo indicado. se utiliza en los mensajes generados.
     */
    private String fieldAlias;

    /**
     * Indica el formato que debe tener el campo siguiendo el mismo patron que un SimpleDateFormat.
     * El indicar este campo no desabilita ninguno de los demas, por lo tanto se deben tener en cuenta para no
     * forzar a una validacion que siempre dara error.
     */
    private String dateFormat;

    /**
     * Nombre de la funcion JavaScript que comprueba si el campo es requerido.
     */
    private static final String JAVASCRIPT_REQUIRED_FUNCTION = "arq_validateRequiredField";

    /**
     * Nombre de la funcion JavaScript que comprueba si el campo excede de la longitud maxima.
     */
    private static final String JAVASCRIPT_MAX_LENGHT_FUNCTION = "arq_validateLenghField";

    /**
     * Nombre de la funcion JavaScript que comprueba si el campo excede de la longitud maxima.
     */
    private static final String JAVASCRIPT_ISNUMBER_FUNCTION = "arq_isNumberField";

    /**
     * Nombre de la funcion JavaScript para comprobar si el campo es fecha valida.
     */
    private static final String JAVASCRIPT_ISDATE_FUNCTION = "arq_isDate";

    /**
     * Nombre de la funcion JavaScript para comprobar si es un email valido.
     */
    private static final String JAVASCRIPT_ISEMAIL_FUNCTION = "arq_isEmail";

    /**
     * Constante para el valor que debe tener el atributo 'validationType' si se quiere indicar que es obligatorio.
     */
    public static final String VALIDATION_TYPE_REQUIRED = "REQUIRED";

    /**
     * Constante para el valor que debe tener el atributo 'validationType' si se quiere indicar que <b>NO</b> es obligatorio.
     */
    public static final String VALIDATION_TYPE_NOT_REQUIRED = "NOT_REQUIRED";


    /**
     * Indica el formato que debe tener el campo siguiendo el mismo patron que un SimpleDateFormat.
     * El indicar este campo no desabilita ninguno de los demas, por lo tanto se deben tener en cuenta para no
     * forzar a una validacion que siempre dara error.
     *
     * @return Value for property 'dateFormat'.
     * @see #dateFormat
     */
    public String getDateFormat()
    {
        return dateFormat;
    }

    /**
     * Indica el formato que debe tener el campo siguiendo el mismo patron que un SimpleDateFormat.
     * El indicar este campo no desabilita ninguno de los demas, por lo tanto se deben tener en cuenta para no
     * forzar a una validacion que siempre dara error.
     *
     * @param dateFormat Value to set for property 'dateFormat'.
     * @see #dateFormat
     */
    public void setDateFormat(String dateFormat)
    {
        this.dateFormat = dateFormat;
    }

    /**
     * Flag que indica si se debe validar si el campo debe ser un email valido. Por defecto a false.
     *
     * @param flag Indica si debe validarse o no.
     */
    public void setIsEmail(boolean flag)
    {
        this.isEmail = flag;
    }

    /**
     * Flag que indica si se debe validar si el campo debe ser un email valido. Por defecto a false.
     *
     * @return El flag indicando si debe ser validado.
     */
    public boolean getIsEmail()
    {
        return this.isEmail;
    }

    /**
     * Tipo de validacion a realizar. Valores validos:<br>
     * REQUIRED -> Indica que el campo es necesario que se indique.
     * NOT_REQUIRED -> <b>Valor por defecto</b>. El campo no es necesario. Si no viene no se realizaran comprobaciones adicionales sobre el.
     *
     * @return Value for property 'validationType'.
     * @see #validationType
     */
    public String getValidationType()
    {
        return validationType;
    }

    /**
     * Tipo de validacion a realizar. Valores validos:<br>
     * REQUIRED -> Indica que el campo es necesario que se indique.
     * NOT_REQUIRED -> <b>Valor por defecto</b>. El campo no es necesario. Si no viene no se realizaran comprobaciones adicionales sobre el.
     *
     * @param validationType Value to set for property 'validationType'.
     * @see #validationType
     */
    public void setValidationType(String validationType)
    {
        this.validationType = validationType;
    }

    /**
     * Flag que indica si el campo debe ser un numero. Por defecto es false.
     *
     * @return Value for property 'number'.
     * @see #isNumber
     */
    public boolean getIsNumber()
    {
        return isNumber;
    }

    /**
     * Flag que indica si el campo debe ser un numero. Por defecto es false.
     *
     * @param number Value to set for property 'number'.
     * @see #isNumber
     */
    public void setIsNumber(boolean number)
    {
        isNumber = number;
    }

    /**
     * Atributo para indicar la longitud maxima que debe tener el campo. Por defecto no se comprueba, y el valor 0 no esta permitido.
     *
     * @return Value for property 'maxLenght'.
     * @see #maxLenght
     */
    public Integer getMaxLenght()
    {
        return maxLenght;
    }

    /**
     * Atributo para indicar la longitud maxima que debe tener el campo. Por defecto no se comprueba, y el valor 0 no esta permitido.
     *
     * @param maxLenght Value to set for property 'maxLenght'.
     * @see #maxLenght
     */
    public void setMaxLenght(Integer maxLenght)
    {
        this.maxLenght = maxLenght;
    }

    /**
     * El ID del campo a validar mediante JavaScript.
     *
     * @return Value for property 'fieldId'.
     * @see #fieldId
     */
    public String getFieldId()
    {
        return fieldId;
    }

    /**
     * El ID del campo a validar mediante JavaScript.
     *
     * @param fieldId Value to set for property 'fieldId'.
     * @see #fieldId
     */
    public void setFieldId(String fieldId)
    {
        this.fieldId = fieldId;
    }


    /**
     * El alias para el campo indicado. se utiliza en los mensajes generados.
     *
     * @return Value for property 'fieldAlias'.
     * @see #fieldAlias
     */
    public String getFieldAlias()
    {
        return fieldAlias;
    }

    /**
     * El alias para el campo indicado. se utiliza en los mensajes generados.
     *
     * @param fieldAlias Value to set for property 'fieldAlias'.
     * @see #fieldAlias
     */
    public void setFieldAlias(String fieldAlias)
    {
        this.fieldAlias = fieldAlias;
    }

    @Override
    public int doStartTag() throws JspException
    {

        if (VALIDATION_TYPE_REQUIRED.equalsIgnoreCase(this.validationType)) //Si el atributo debe comprobar que esta.
        {
            writeText(JAVASCRIPT_REQUIRED_FUNCTION + "('" + this.fieldId + "', '" + this.fieldAlias + "');");
        }

        if (this.maxLenght != null && this.maxLenght > 0)
        {
            writeText('\n');
            writeText(JAVASCRIPT_MAX_LENGHT_FUNCTION + "('" + this.fieldId + "', '" + this.fieldAlias + "', " + this.maxLenght + ");");
        }

        if (this.isNumber)
        {
            writeText('\n');
            writeText(JAVASCRIPT_ISNUMBER_FUNCTION + "('" + this.fieldId + "', '" + this.fieldAlias + "');");
        }

        if (this.dateFormat != null && this.dateFormat.length() > 0)
        {
            writeText('\n');
            writeText(JAVASCRIPT_ISDATE_FUNCTION + "('" + this.fieldId + "', '" + this.fieldAlias + "', '" + this.dateFormat + "');");
        }

        if (this.isEmail)
        {
            writeText('\n');
            writeText(JAVASCRIPT_ISEMAIL_FUNCTION + "('" + this.fieldId + "', '" + this.fieldAlias + "');");
        }

        return SKIP_BODY;
    }

    /**
     * Escribe en la JSP el texto pasado como parametro.
     *
     * @param textToWrite El texto a escribir.
     * @throws javax.servlet.jsp.JspException Si ocurre algun error durante las escritura.
     */
    private void writeText(Object textToWrite) throws JspException
    {
        try
        {
            JspWriter jspWriter = this.pageContext.getOut();
            jspWriter.print(textToWrite);
        }
        catch (IOException ioException)
        {
            throw new JspException("Error al tratar de escribir el texto: " + textToWrite, ioException);
        }
    }
}

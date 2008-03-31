package es.onlysolutions.arq.core.mvc.tag;

import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.servlet.support.BindStatus;
import org.springframework.web.servlet.tags.BindTag;
import org.springframework.web.servlet.tags.MessageTag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Tag para mostrar el contenido del array de errores. Puede recibir el parametro para obtener los
 * errores y el tipo de formato para escribir dichos errores. Ambos parametros son opcionales,
 * cogiendo valores por defecto.
 */
public class ErrorsTag extends BodyTagSupport
{
    private static final String ALERT = "alert";
    private static final String TABLE = "table";
    // Atributo opcional que define el nombre del command.
    private String commandName;
    // Atributo opcional que indica la forma de presentar los errores
    private String printModel;
    // Atributo opcional que indica la clase jsp de la tabla
    private String classTable;
    // Atributo opcional que indica la clase jsp de la fila
    private String classTr;
    // Atributo opcional que indica la clase jsp de la columna
    private String classTd;
    // Atributo opcional que indica si se visualizan los errores globales o locales
    private String visibility;


    /**
     * Getter for property 'printModel'.
     *
     * @return Value for property 'printModel'.
     */
    public String getPrintModel()
    {
        return printModel;
    }


    /**
     * Setter for property 'printModel'.
     *
     * @param printModel Value to set for property 'printModel'.
     */
    public void setPrintModel(String printModel)
    {
        this.printModel = printModel;
    }


    /**
     * Getter for property 'commandName'.
     *
     * @return Value for property 'commandName'.
     */
    public String getCommandName()
    {
        return commandName;
    }


    /**
     * Getter for property 'classTable'.
     *
     * @return Value for property 'classTable'.
     */
    public String getClassTable()
    {
        return classTable;
    }


    /**
     * Setter for property 'classTable'.
     *
     * @param classTable Value to set for property 'classTable'.
     */
    public void setClassTable(String classTable)
    {
        this.classTable = classTable;
    }


    /**
     * Getter for property 'classTd'.
     *
     * @return Value for property 'classTd'.
     */
    public String getClassTd()
    {
        return classTd;
    }


    /**
     * Setter for property 'classTd'.
     *
     * @param classTd Value to set for property 'classTd'.
     */
    public void setClassTd(String classTd)
    {
        this.classTd = classTd;
    }


    /**
     * Getter for property 'classTr'.
     *
     * @return Value for property 'classTr'.
     */
    public String getClassTr()
    {
        return classTr;
    }


    /**
     * Setter for property 'classTr'.
     *
     * @param classTr Value to set for property 'classTr'.
     */
    public void setClassTr(String classTr)
    {
        this.classTr = classTr;
    }


    /**
     * Getter for property 'visibility'.
     *
     * @return Value for property 'visibility'.
     */
    public String getVisibility()
    {
        return visibility;
    }


    /**
     * Setter for property 'visibility'.
     *
     * @param visibility Value to set for property 'visibility'.
     */
    public void setVisibility(String visibility)
    {
        this.visibility = visibility;
    }


    /**
     * Setter for property 'commandName'.
     *
     * @param permission Value to set for property 'commandName'.
     */
    public void setCommandName(String permission)
    {
        this.commandName = permission;
    }


    /**
     * Evalua el array de errores de la sesion y lo muestra, si hay alguno, segun Deja el body sin
     * procesar en caso contario.
     *
     * @return El codigo de procesamiento del BODY.
     * @throws JspException Si ocurre algun error durante el proceso.
     */
    @Override
    public int doStartTag() throws JspException
    {
        String cName = getCommandName();
        String pModel = getPrintModel();
        BindStatus status;
        BindingResult errors;
        BindTag bindTag = new BindTag();
        if (cName == null)
        {
            cName = "command";
        }
        bindTag.setPageContext(pageContext);
        bindTag.setPath(cName);
        bindTag.doStartTag();
        status = (BindStatus) pageContext.getRequest().getAttribute("status");
        errors = (BindingResult) status.getErrors();
        if ((errors != null) && (errors.getErrorCount() > 0))
        {
            if ((pModel == null) || ((ALERT.equalsIgnoreCase(pModel))))
            {
                escribirErroresEnAlert(errors);
            }
            else if (pModel.equalsIgnoreCase(TABLE))
            {
                escribirErroresEnTable(errors);
            }
        }
        bindTag.doEndTag();
        return SKIP_BODY;
    }


    /**
     * Metodo para escribir en la pagina jsp los errores de validacion producidos en un alert.
     *
     * @param errors Lista de errores producidos.
     * @throws javax.servlet.jsp.JspException Si ocurre algun error durante la escritura del Tag.
     */
    private void escribirErroresEnAlert(BindingResult errors) throws JspException
    {
        JspWriter writer = pageContext.getOut();
        List listErrors;
        Iterator iter;
        ObjectError error;
        MessageTag messageTag;
        try
        {
            writer.print("alert('");
            listErrors = errors.getAllErrors();
            iter = listErrors.iterator();
            while (iter.hasNext())
            {
                error = (ObjectError) iter.next();
                messageTag = new MessageTag();
                messageTag.setPageContext(pageContext);
                messageTag.setCode(error.getCode());
                messageTag.setArguments(error.getArguments());
                messageTag.setText(error.getDefaultMessage());
                messageTag.doStartTag();
                messageTag.doEndTag();
                writer.print("\\n");
            }
            writer.print("');");
        }
        catch (IOException ex)
        {
            throw new JspException("Error durante la generacion del alert", ex);
        }
    }


    /**
     * Metodo para escribir en la pagina jsp los errores de validacion producidos en una tabla HTML.
     *
     * @param errors Lista de errores producidos.
     * @throws javax.servlet.jsp.JspException Si ocurre algun error durante la escritura del Tag.
     */
    private void escribirErroresEnTable(BindingResult errors) throws JspException
    {
        JspWriter writer = pageContext.getOut();
        List listErrors;
        Iterator iter;
        ObjectError error;
        MessageTag messageTag;
        try
        {
            if (getClassTable() == null)
            {
                writer.print("<table>");
            }
            else
            {
                writer.print("<table class='" + getClassTable() + "'>");
            }
            if ("global".equals(getVisibility()))
            {
                listErrors = errors.getGlobalErrors();
            }
            else if ("field".equals(getVisibility()))
            {
                listErrors = errors.getFieldErrors();
            }
            else
            {
                listErrors = errors.getAllErrors();
            }
            iter = listErrors.iterator();
            while (iter.hasNext())
            {
                if (getClassTr() == null)
                {
                    writer.print("<tr>");
                }
                else
                {
                    writer.print("<tr class='" + getClassTr() + "'>");
                }
                if (getClassTd() == null)
                {
                    writer.print("<td>");
                }
                else
                {
                    writer.print("<td class='" + getClassTd() + "'>");
                }
                error = (ObjectError) iter.next();
                messageTag = new MessageTag();
                messageTag.setPageContext(pageContext);
                messageTag.setCode(error.getCode());
                messageTag.setArguments(error.getArguments());
                messageTag.setText(error.getDefaultMessage());
                messageTag.doStartTag();
                messageTag.doEndTag();
                writer.print("</td>");
                writer.print("</tr>");
            }
            writer.print("</table>");
        }
        catch (IOException ioException)
        {
            throw new JspException("Error durante la generacion de la tabla", ioException);
        }
    }
}
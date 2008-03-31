package es.onlysolutions.arq.core.mvc.tag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;
import java.io.IOException;

/**
 * Tag que envolvera el resto de tags de validacion para generar la funcion JavaScript principal de validacion.
 */
public class ValidateFieldsTag extends BodyTagSupport
{
    /**
     * Valor por defecto para la funcion JavaScript.
     */
    public static final String DEFAULT_FUNCTION_NAME = "validateFields";

    /**
     * Indica el nombre de la funcion JavaScript a generar. Si no se indica por defecto se usa DEFAULT_FUNCTION_NAME.
     *
     * @see #DEFAULT_FUNCTION_NAME
     */
    private String functionName;


    /**
     * Indica el nombre de la funcion JavaScript a generar. Si no se indica por defecto se usa DEFAULT_FUNCTION_NAME.
     *
     * @return Value for property 'functionName'.
     * @see #functionName
     */
    public String getFunctionName()
    {
        return functionName;
    }

    /**
     * Indica el nombre de la funcion JavaScript a generar. Si no se indica por defecto se usa DEFAULT_FUNCTION_NAME.
     *
     * @param functionName Value to set for property 'functionName'.
     * @see #functionName
     */
    public void setFunctionName(String functionName)
    {
        this.functionName = functionName;
    }


    @Override
    public int doStartTag() throws JspException
    {
        if (functionName != null && functionName.length() > 0)
        {
            writeText("function " + functionName + "() {");
        }
        else
        {
            writeText("function " + DEFAULT_FUNCTION_NAME + "() {");
        }
        writeText('\n');

        StringBuilder sb = new StringBuilder(200);
        sb.append("for( var i=0; i<arq_error_fields_array.length; i++ )\n");
        sb.append("{\n");
        sb.append("    var element = document.getElementById( arq_error_fields_array[i] );\n");
        sb.append("    var parentNode;\n");
        sb.append("    if( element )\n");
        sb.append("    {\n");
        sb.append("        parentNode = element.parentNode;\n");
        sb.append("        parentNode.removeChild( element );\n");
        sb.append("    }\n");
        sb.append("}");
        sb.append("arq_error_fields_array.splice(0);\n");
        sb.append("\n");
        sb.append("\n");
        sb.append("for( var i=0; i<arq_highLight_fields_array.length; i++ )\n");
        sb.append("{\n");
        sb.append("    var fieldId = arq_highLight_fields_array[i][0];\n");
        sb.append("    var previousCss = arq_highLight_fields_array[i][1];\n");
        sb.append("    document.getElementById( fieldId ).className = previousCss;\n");
        sb.append("}\n");
        sb.append("arq_highLight_fields_array.splice(0);");
        sb.append('\n');
        sb.append("arq_hasErrors = false;");
        writeText(sb.toString());
        return EVAL_BODY_INCLUDE;
    }


    @Override
    public int doEndTag() throws JspException
    {
        writeText('\n');

        StringBuilder sb = new StringBuilder(200);
        sb.append("if( arq_hasErrors )\n");
        sb.append("{\n");
        sb.append("   if( arq_showFieldMessage )\n");
        sb.append("{\n");
        sb.append("   return false;\n");
        sb.append("}\n");
        sb.append("else\n");
        sb.append("{\n");
        sb.append("   alert(arq_validationTextMsj);\n");
        sb.append("}\n");
        sb.append("   return false;\n");
        sb.append("}\n");
        sb.append("else\n");
        sb.append("{\n");
        sb.append("   return true;\n");
        sb.append("}");
        writeText(sb.toString());

        writeText("}");
        return EVAL_PAGE;
    }


    private void writeText(Object texto) throws JspException
    {
        try
        {
            JspWriter jspWriter = pageContext.getOut();
            jspWriter.println(texto);
        }
        catch (IOException ioEx)
        {
            throw new JspException(ioEx);
        }
    }

}

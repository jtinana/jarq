package es.onlysolutions.arq.core.mvc.bind;

import es.onlysolutions.arq.core.mvc.utils.CalendarUtils;

import java.beans.PropertyEditorSupport;

/**
 * Custom editor para permitir el mapeo de Strings contra clases de java.sql.Date.
 * De esta forma se permite el mapeo directamente contra cualquier campo del command bean o de la entidad
 * que contiene.
 */
public class DateUtilCustomEditor extends PropertyEditorSupport
{
    @Override
    public void setAsText(String text) throws IllegalArgumentException
    {
        Object valueToSet = null;
        if (text != null && text.length() > 0)
        {
            valueToSet = CalendarUtils.convertToCal(text).getTime();
        }

        setValue(valueToSet);
    }

    @Override
    public String getAsText()
    {
        String result = null;

        if (getValue() != null)
        {
            result = CalendarUtils.convertToString((java.util.Date) getValue());
        }
        return result;
    }

    @Override
    public String toString()
    {
        return "DateUtilCustomEditor: java.lang.String -> java.util.Date ";
    }


}

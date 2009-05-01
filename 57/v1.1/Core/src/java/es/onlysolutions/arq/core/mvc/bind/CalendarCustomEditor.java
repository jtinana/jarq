package es.onlysolutions.arq.core.mvc.bind;

import es.onlysolutions.arq.core.mvc.utils.CalendarUtils;

import java.beans.PropertyEditorSupport;
import java.util.Calendar;

/**
 * Custom editor para realizar el mapeo de propiedades contra un java.util.Calendar.
 */
public class CalendarCustomEditor extends PropertyEditorSupport
{
    @Override
    public void setAsText(String text) throws IllegalArgumentException
    {
        Object valueToSet = null;
        if (text != null && text.length() > 0)
        {
            valueToSet = CalendarUtils.convertToCal(text);
        }

        setValue(valueToSet);
    }

    @Override
    public String getAsText()
    {
        String result = null;

        if (getValue() != null)
        {
            result = CalendarUtils.convertToString((Calendar) getValue());
        }
        return result;
    }

    @Override
    public String toString()
    {
        return "CalendarCustomEditor: java.lang.String -> java.util.Calendar ";
    }
}

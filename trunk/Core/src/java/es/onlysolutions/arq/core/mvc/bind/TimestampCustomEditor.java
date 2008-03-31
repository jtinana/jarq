package es.onlysolutions.arq.core.mvc.bind;

import es.onlysolutions.arq.core.mvc.utils.CalendarUtils;

import java.beans.PropertyEditorSupport;
import java.sql.Timestamp;

/**
 * Custom editor para permitir el mapeo contra clases del tipo java.sql.Timestamp.
 */
public class TimestampCustomEditor extends PropertyEditorSupport
{
    @Override
    public void setAsText(String text) throws IllegalArgumentException
    {
        Object valueToSet = null;
        if (text != null && text.length() > 0)
        {
            valueToSet = new Timestamp(CalendarUtils.convertToCal(text).getTimeInMillis());
        }

        setValue(valueToSet);
    }

    @Override
    public String getAsText()
    {
        String result = null;

        if (getValue() != null)
        {
            result = CalendarUtils.convertToString((Timestamp) getValue());
        }
        return result;
    }

    @Override
    public String toString()
    {
        return "TimestampCustomEditor: java.lang.String -> java.sql.Timestamp ";
    }


}

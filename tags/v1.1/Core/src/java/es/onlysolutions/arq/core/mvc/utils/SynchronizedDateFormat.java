package es.onlysolutions.arq.core.mvc.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Esta clase es un Wrapper sobre la clase SimpleDateFormat pero con sus metodos sincronizados.
 * Tiene sentido su uso en un entorno en el que varios Threads necesiten realizar operaciones de formateo
 * de fechas.<br>
 * Se utilizara principalmente en la validacion de fechas. Por esta razon por defecto el SimpleDateFormat interno es lenient.
 * Si se desea modificar este comportamiento debera modificarse manualmente.
 */
public class SynchronizedDateFormat
{

    /**
     * Construye un SynchronizedDateFormat utilizando el patron de formateo indicado.
     *
     * @param pattern El pattern a utilizar para su formateo.
     * @see java.text.SimpleDateFormat#SimpleDateFormat(String)
     */
    public SynchronizedDateFormat(String pattern)
    {
        simpleDateFormat = new SimpleDateFormat(pattern);
        simpleDateFormat.setLenient(false);
    }

    /**
     * SimpleDateFormat sobre el cual se sincronizaran las operaciones.
     */
    private SimpleDateFormat simpleDateFormat;

    /**
     * Objeto para realizar el bloqueo.
     */
    private final Object lock = new Object();

    /**
     * Procede al formateo de un objeto Date. <br>
     * La llamada a este metodo es equivalente a SimpleDateFormat#format( date, "dd/MM/yyyy" )
     *
     * @param theDate La fecha a formatear.
     * @return El String formateado.
     * @see java.text.SimpleDateFormat#format(java.util.Date)
     */
    public String format(Date theDate)
    {
        String str;

        synchronized (lock)
        {
            str = simpleDateFormat.format(theDate);
        }

        return str;
    }

    /**
     * Realiza el formateo del calendario.<br>
     * Es equivalente a realizar la llamada SynchronizedDateFormat#format( Calendar#getTime() )
     *
     * @param calendar El calendario a formatear.
     * @return El String formateado.
     */
    public String format(Calendar calendar)
    {
        return this.format(calendar.getTime());
    }

    /**
     * Realiza el parseo de una String a un objeto Date.
     *
     * @param strDate El String del que obtener el objeto Date.
     * @return El objeto Date instanciado.
     * @throws java.text.ParseException Si el String pasado como parametro no es una fecha correcta.
     * @see java.text.SimpleDateFormat#parse(String)
     */
    public Date parseDate(String strDate) throws ParseException
    {
        Date result;
        synchronized (lock)
        {
            result = simpleDateFormat.parse(strDate);
        }
        return result;
    }

    /**
     * Obtiene un objeto Calendar a partir de un String que represente una fecha.
     *
     * @param strDate El String a formatear.
     * @return El objeto Calendar instanciado como un GregorianCalendar.
     * @throws ParseException Si ocurre algun problema durante el parseo del String.
     * @see java.text.SimpleDateFormat#parse(String)
     */
    public Calendar parseCalendar(String strDate) throws ParseException
    {
        Date date = simpleDateFormat.parse(strDate);
        Calendar calDate = new GregorianCalendar();
        calDate.setTime(date);
        return calDate;
    }

    /**
     * Establece si este DateFormat es lenient o no. <b>Este metodo no est� sincronizado</b>
     *
     * @param flag El valor a establecer.
     */
    public void setLenient(boolean flag)
    {
        simpleDateFormat.setLenient(flag);
    }
}

package es.onlysolutions.arq.core.mvc.utils;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * Clase de utilidades para el manejo de calendarios y sus posibles conversiones.
 */
public final class CalendarUtils
{
    /**
     * El date format sincronizado con el que esta clase formatea las fechas.
     */
    private static final SynchronizedDateFormat dateFormat = new SynchronizedDateFormat("dd/MM/yyyy");

    /**
     * Constructor privado para evitar instanciacion.
     */
    private CalendarUtils()
    {
        super();
    }

    /**
     * Devuelve un calendario con el dia de hoy y la hora indicada por el argumento.
     * �ste m�todo es util para obtener calendarios a leer horas de la configuracion.
     *
     * @param strHour El String indicando la hora. Debe cumplir con el formato: HH:MM
     * @return El objeto Calendar instanciado con la hora establecida.
     */
    public static Calendar getCalendarFromHourString(String strHour)
    {
        Calendar startTime = new GregorianCalendar();

        StringTokenizer st = new StringTokenizer(strHour, ":");
        int intHour = Integer.parseInt(st.nextToken());
        int intMin = Integer.parseInt(st.nextToken());

        startTime.set(Calendar.HOUR_OF_DAY, intHour);
        startTime.set(Calendar.MINUTE, intMin);
        return startTime;
    }

    /**
     * Convierte una fecha pasada como String a un GregorianCalendar.
     *
     * @param strFechaCita El string representando la fecha.
     * @return El GregorianCalendar instanciado.
     */
    public static Calendar convertToCal(String strFechaCita)
    {
        Calendar cal;
        try
        {
            StringTokenizer st = new StringTokenizer(strFechaCita, "/");
            int dia = Integer.parseInt(st.nextToken());
            int mes = Integer.parseInt(st.nextToken()) - 1;
            int anio = Integer.parseInt(st.nextToken());
            cal = new GregorianCalendar();
            cal.set(Calendar.DAY_OF_MONTH, dia);
            cal.set(Calendar.MONTH, mes);
            cal.set(Calendar.YEAR, anio);
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("La fecha: " + strFechaCita + " no tiene el formato correcto");
        }
        catch (NoSuchElementException e)
        {
            throw new IllegalArgumentException("La fecha: " + strFechaCita + " no tiene el formato correcto");
        }
        return cal;
    }

    /**
     * Convierte el Calendario indicado a un String con el formato "dd/MM/yyyy".
     *
     * @param calendar El calendario a convertir.
     * @return El String representando la fecha.
     */
    public static String convertToString(Calendar calendar)
    {
        return dateFormat.format(calendar);
    }

    /**
     * Convierte la fecha indicada a un String con el formato "dd/MM/yyyy".
     *
     * @param utilDate El objeto java.util.Date a convertir.
     * @return El String representando la fecha.
     */
    public static String convertToString(java.util.Date utilDate)
    {
        return dateFormat.format(utilDate);
    }

    /**
     * Convierte la fecha indicada a un String con el formato "dd/MM/yyyy".
     *
     * @param sqlDate El objeto java.util.Date a convertir.
     * @return El String representando la fecha.
     */
    public static String convertToString(java.sql.Date sqlDate)
    {
        return dateFormat.format(new java.util.Date(sqlDate.getTime()));
    }


}

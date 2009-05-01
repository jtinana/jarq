package es.onlysolutions.arq.core.mvc.utils;

/**
 * Clase de utilidad con funciones de apoyo para el manejo y tratamiento de
 * cadenas.
 * <p/>
 * Los m�todos son est�ticos (de clase), por no que no es necesario instanciarla
 * para utilizarlos.
 *
 * @author Luis Pab�n (lpabon@datadviser.com)
 */
public class StringUtils
{
    //Constante para cadena vac�a.
    public static final String EMPTY_STRING = "";

    /**
     * Constructor por defecto (privado).
     * Se oculta el constructor por ser una clase de utilidad que s�lo tiene
     * m�todos est�ticos.
     */
    private StringUtils()
    {
    }


    /**
     * Averigua si una cadena no est� vac�a y no es nula.
     *
     * @param string cadena a consultar.
     * @return <code>true</code> si la cadena no es nula y no est� vac�a,
     *         <code>false</code> en caso contrario.
     */
    public static boolean isNotEmpty(String string)
    {
        return (string != null) && (string.length() > 0);
    } //isNotEmpty(String string)


    /**
     * Averigua si una cadena est� vac�a o es nula.
     *
     * @param string cadena a consultar.
     * @return <code>true</code> si la cadena es nula o est� vac�a,
     *         <code>false</code> en caso contrario.
     */
    public static boolean isEmpty(String string)
    {
        return (string == null) || (string.length() == 0);
    } //isEmpty(String string)
}
package es.onlysolutions.arq.core.auth;

import java.io.Serializable;

/**
 * Interfaz para definir objetos usuario. Toda implementacion de objetos usuario deben implementar dicho interfaz.
 */
public interface IUserSettings
{
    /**
     * Constante que define el nombre bajo el que se guarda en sesion el objeto IUserSettings.
     */
    public static final String USER_SETTINGS_ATTRIBUTE_NAME = "USER_SETTINGS_ATTRIBUTE_NAME";

    /**
     * Establece la direccion remota del usuario. Se establecer� en cada peticion.
     * En caso de no conseguir obtenerla, se establecer� un valor null.
     * No deberia ser llamado directamente por el usuario.
     *
     * @param remoteAddress La direccion remota de la ultima peticion del usuario.
     */
    public void setRemoteAddress(String remoteAddress);

    /**
     * Devuelve la direccion remota del usuario.
     * Si no puede obtener o no se desea, es posible devolver un valor null.
     *
     * @return Un String con el host remoto de la peticion o null si no se desea esa informacion.
     */
    public String getRemoteAddress();

    /**
     * Comprobara si el usuario tiene el permiso indicado.
     *
     * @param strPermission El permiso a comprobar si se tiene.
     * @return true si se tiene el permiso indicado, false en otro caso.
     */
    public boolean hasPermission(String strPermission);

    /**
     * Devolver� el login del usuario.
     * <b>Es recomendable que el login de usuario sea �nico</b>.
     *
     * @return El objeto que representa el login de �ste usuario. Nunca debe devolver un valor nulo.
     */
    public Serializable getUserLogin();
}

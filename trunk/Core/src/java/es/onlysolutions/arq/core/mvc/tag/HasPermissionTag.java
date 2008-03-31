package es.onlysolutions.arq.core.mvc.tag;


import es.onlysolutions.arq.core.auth.IUserSettings;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;
import java.util.StringTokenizer;

/**
 * Tag para procesar el contenido en caso de tener el permiso indicado.
 */
public class HasPermissionTag extends BodyTagSupport
{
    /**
     * Token de separacion para los permisos.
     */
    private static final String TOKEN = ",";

    /**
     * Permiso a verificar.
     */
    private String permission;

    /**
     * Atributo opcional que indica si se debe comprobar la existencia del permiso, o la no existencia del permiso
     * para evaluar el BODY del tag.
     */
    private Boolean notHasPermission;

    /**
     * Indica la forma de evaluar los permisos en caso de que se indique mas de uno.
     * Valores validos: AND, OR.
     */
    private String evaluationCondition = "AND";


    /**
     * Getter for property 'evaluationCondition'.
     *
     * @return Value for property 'evaluationCondition'.
     * @see #evaluationCondition
     */
    public String getEvaluationCondition()
    {
        return evaluationCondition;
    }

    /**
     * Setter for property 'evaluationCondition'.
     *
     * @param evaluationCondition Value to set for property 'evaluationCondition'.
     * @see #evaluationCondition
     */
    public void setEvaluationCondition(String evaluationCondition)
    {
        this.evaluationCondition = evaluationCondition;
    }

    /**
     * Getter for property 'notHasPermission'.
     *
     * @return Value for property 'notHasPermission'.
     */
    public Boolean getNotHasPermission()
    {
        return notHasPermission;
    }

    /**
     * Setter for property 'notHasPermission'.
     *
     * @param notHasPermission Value to set for property 'notHasPermission'.
     */
    public void setNotHasPermission(Boolean notHasPermission)
    {
        this.notHasPermission = notHasPermission;
    }

    /**
     * Getter for property 'permission'.
     *
     * @return Value for property 'permission'.
     */
    public String getPermission()
    {
        return permission;
    }

    /**
     * Setter for property 'permission'.
     *
     * @param permission Value to set for property 'permission'.
     */
    public void setPermission(String permission)
    {
        this.permission = permission;
    }

    /**
     * Evalua el permiso del usuario y procesa el BODY si lo tiene. Deja el body sin procesar en caso contario.
     *
     * @return El codigo de procesamiento del BODY.
     * @throws JspException Si ocurre algun error durante el proceso.
     */
    @Override
    public int doStartTag() throws JspException
    {
        int bodyTreatment = SKIP_BODY;

        Object objUserSettings = pageContext.getSession().getAttribute(IUserSettings.USER_SETTINGS_ATTRIBUTE_NAME);

        if (objUserSettings instanceof IUserSettings)
        {
            IUserSettings userSettings = (IUserSettings) objUserSettings;

            //Tratamos en caso de que haya mas de un permiso indicado.
            String permissions = getPermission();
            if (permissions.indexOf(TOKEN) == -1)
            {
                if (validarPermiso(userSettings, permissions))
                {
                    bodyTreatment = EVAL_BODY_INCLUDE;
                }
            }
            else //Contiene el token, luego tenemos mas de un permiso.
            {
                if ("OR".equalsIgnoreCase(getEvaluationCondition()))
                {
                    boolean tieneAlMenosUno = false;
                    StringTokenizer st = new StringTokenizer(permissions, TOKEN);

                    while (st.hasMoreTokens() && !tieneAlMenosUno)
                    {
                        String unPermiso = st.nextToken();
                        tieneAlMenosUno = validarPermiso(userSettings, unPermiso);
                        if (tieneAlMenosUno)
                        {
                            bodyTreatment = EVAL_BODY_INCLUDE;
                        }
                    }
                }
                else if ("AND".equalsIgnoreCase(getEvaluationCondition()))
                {
                    boolean tieneTodos = true;
                    StringTokenizer st = new StringTokenizer(permissions, TOKEN);

                    while (st.hasMoreTokens() && tieneTodos)
                    {
                        String unPermiso = st.nextToken();
                        if (!validarPermiso(userSettings, unPermiso))
                        {
                            tieneTodos = false;
                        }
                    }

                    if (tieneTodos)
                    {
                        bodyTreatment = EVAL_BODY_INCLUDE;
                    }
                }
                else //Se ha indicado un valor distinto de AND u OR.
                {
                    throw new JspException("Se debe indicar un valor OR o AND en el atributo 'evaluationCondition'");
                }
            }

        }

        return bodyTreatment;
    }

    /**
     * Valida si el usuario tiene el permiso indicado.
     *
     * @param userSettings El objeto IUserSettings.
     * @param permissions  El permiso a validar.
     * @return true si tiene el permiso.
     */
    private boolean validarPermiso(IUserSettings userSettings, String permissions)
    {
        boolean result = false;
        if (userSettings.hasPermission(permissions))
        {
            //Si tiene el permiso, y se indica a false la verficiacion de que no tenga permiso
            if (Boolean.FALSE.equals(getNotHasPermission()) || getNotHasPermission() == null)
            {
                result = true;
            }
        }
        else if (Boolean.TRUE.equals(getNotHasPermission()))
        {
            //No se tiene el permiso indicado, pero se indica a true el flag notHasPermission.
            result = true;
        }
        return result;
    }


}

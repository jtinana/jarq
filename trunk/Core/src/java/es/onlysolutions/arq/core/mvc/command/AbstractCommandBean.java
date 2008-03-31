package es.onlysolutions.arq.core.mvc.command;

import es.onlysolutions.arq.core.accesobd.IEntityId;
import es.onlysolutions.arq.core.service.pagination.PaginatedListImp;
import org.displaytag.pagination.PaginatedList;

import java.io.Serializable;

/**
 * Clase abstracta para todos los command bean.<br>
 * Se introducen los parametros comunes a todas las JSP y sus getter y setter.<br>
 * Contiene los siguientes atributos:<br>
 * &nbsp;&nbsp;� <b>PaginatedListImp</b> La lista paginada a mostrar.<br>
 * &nbsp;&nbsp;� <b>entidad</b> La entidad sobre la que se realizar�n los mantenimientos.<br>
 * &nbsp;&nbsp;� <b>criteria</b> El bean con los atributos correspondientes a los campos rellenos para filtrar.<br>
 * &nbsp;&nbsp;� <b>msg</b> El mensaje indicado para mostrar en la p�gina.<br>
 * &nbsp;&nbsp;� <b>accion</b> Contendr� el String adecuado (ver constantes de la clase) para indicar que acci�n ejecutar� el GenericController.<br>
 */
public abstract class AbstractCommandBean implements Serializable
{

    /**
     * Constructor sin par�metros de la clase. Subclases pueden incluir funcionalidad dentro de su constructor, pero cada bean
     * debe tener un constructor sin par�metros para poder ser instanciado por la aquitectura.
     */
    protected AbstractCommandBean()
    {
        setPaginatedList(new PaginatedListImp());
    }

    /**
     * Constantes para definir el accion de list.
     *
     * @see es.onlysolutions.arq.core.mvc.controller.GenericController
     * @deprecated Utilizar en su lugar las constantes del GenericController
     */
    public static final String LISTAR = "ACCION_LISTAR";
    /**
     * Constantes para definir el accion de cargar.
     *
     * @see es.onlysolutions.arq.core.mvc.controller.GenericController
     * @deprecated Utilizar en su lugar las constantes del GenericController
     */
    public static final String CARGAR = "ACCION_CARGAR";
    /**
     * Constantes para definir el accion de guardar.
     *
     * @see es.onlysolutions.arq.core.mvc.controller.GenericController
     * @deprecated Utilizar en su lugar las constantes del GenericController
     */
    public static final String GUARDAR = "ACCION_GUARDAR";
    /**
     * Constantes para definir el accion de borrar.
     *
     * @see es.onlysolutions.arq.core.mvc.controller.GenericController
     * @deprecated Utilizar en su lugar las constantes del GenericController
     */
    public static final String BORRAR = "ACCION_BORRAR";

    /**
     * Atributo para guardar el mensaje al resultado de la accion. Se utilizara para alerts y mensajes personalizados.
     */
    private String msg;

    /**
     * Atributo para indicar la accion a ejecutar.
     */
    private String accion;

    /**
     * La entidad asociada al formulario.
     */
    private IEntityId entidad;

    /**
     * Objeto donde se indicar�n los criterios de b�squeda para los listados.
     * Estos par�metros pueden ser distintos a los de la entidad, y se deber� modificar el dao de la forma adecuada.
     */
    private Object criteria;


    /**
     * Lista paginada para el mantenimiento.
     */
    private PaginatedList paginatedList;


    /**
     * Getter for property 'paginatedList'.
     *
     * @return Value for property 'paginatedList'.
     * @see #paginatedList
     */
    public PaginatedList getPaginatedList()
    {
        return paginatedList;
    }

    /**
     * Setter for property 'paginatedList'.
     *
     * @param paginatedList Value to set for property 'paginatedList'.
     * @see #paginatedList
     */
    public void setPaginatedList(PaginatedList paginatedList)
    {
        this.paginatedList = paginatedList;
    }

    /**
     * Getter for property 'criteria'.
     *
     * @return Value for property 'criteria'.
     * @see #criteria
     */
    public Object getCriteria()
    {
        return criteria;
    }

    /**
     * Setter for property 'criteria'.
     *
     * @param criteria Value to set for property 'criteria'.
     * @see #criteria
     */
    public void setCriteria(Object criteria)
    {
        this.criteria = criteria;
    }

    /**
     * Getter for property 'msg'.
     *
     * @return Value for property 'msg'.
     * @see #msg
     */
    public String getMsg()
    {
        return msg;
    }

    /**
     * Setter for property 'msg'.
     *
     * @param msg Value to set for property 'msg'.
     * @see #msg
     */
    public void setMsg(String msg)
    {
        this.msg = msg;
    }

    /**
     * Getter for property 'accion'.
     *
     * @return Value for property 'accion'.
     * @see #accion
     */
    public String getAccion()
    {
        return accion;
    }

    /**
     * Setter for property 'accion'.
     *
     * @param accion Value to set for property 'accion'.
     * @see #accion
     */
    public void setAccion(String accion)
    {
        this.accion = accion;
    }

    /**
     * Getter for property 'entidad'.
     *
     * @return Value for property 'entidad'.
     * @see #entidad
     */
    public IEntityId getEntidad()
    {
        return entidad;
    }

    /**
     * Setter for property 'entidad'.
     *
     * @param entidad Value to set for property 'entidad'.
     * @see #entidad
     */
    public void setEntidad(IEntityId entidad)
    {
        this.entidad = entidad;
    }
}
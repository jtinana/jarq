package es.onlysolutions.arq.core.mvc.list.decorator;

import es.onlysolutions.arq.core.accesobd.IEntityId;
import es.onlysolutions.arq.core.mvc.command.AbstractCommandBean;
import es.onlysolutions.arq.core.service.utils.EntityUtils;
import org.displaytag.decorator.TableDecorator;
import org.displaytag.pagination.PaginatedList;

import javax.servlet.ServletRequest;
import java.io.Serializable;
import java.util.List;

public class RowIdDecorator extends TableDecorator
{
    /**
     * A�ade el ID al campo id del tr de inicio de fila.
     *
     * @return El id a a�adir al tr.
     */
    @Override
    public String addRowId()
    {
        Object currentObject = getCurrentRowObject();
        String id = null;
        String out = null;

        if (currentObject instanceof IEntityId)
        {
            IEntityId entityId = (IEntityId) currentObject;
            id = String.valueOf(EntityUtils.getId(entityId));
            if (entityId instanceof Number)
            {
                out = id + "\" onclick=\"marcarFilaDisplayTag(" + id + ")";
            }
            else
            {
                out = id + "\" onclick=\"marcarFilaDisplayTag('" + id + "')";
            }
        }

        return out;
    }


    /**
     * Devuelve el stilo adecuado si debe marcarse como se�alado.
     *
     * @return El estilo a aplicar por si esta se�alado.
     */
    @Override
    public String addRowClass()
    {
        ServletRequest request = getPageContext().getRequest();
        Object commandObject = request.getAttribute("command");
        String css = "filaNoSeleccionadaCssDisplayTag";

        if (commandObject == null)
        {
            throw new IllegalArgumentException("No existe ning�n atributo con nombre 'command' en la request");
        }

        if (!(commandObject instanceof AbstractCommandBean))
        {
            throw new IllegalArgumentException("El attributo 'command' de la request no es de la clase " + AbstractCommandBean.class.getName());
        }

        AbstractCommandBean bean = (AbstractCommandBean) commandObject;
        PaginatedList paginatedList = bean.getPaginatedList();
        if (paginatedList == null)
        {
            throw new IllegalArgumentException("No se puede decorar una lista con PaginatedList a nulo");
        }

        List listaParcial = paginatedList.getList();

        //Recorremos la lista para resaltar la fila que sea la indicada por la entidad
        for (int index = 0; index < listaParcial.size(); index++)
        {
            Object entityObject = listaParcial.get(index);

            if (!(entityObject instanceof IEntityId))
            {
                throw new IllegalArgumentException("El PaginatedList debe contener elementos que implementen el interfaz " + IEntityId.class.getName() + " para poder ser decorado");
            }
            IEntityId entityId = (IEntityId) entityObject;

            Serializable id = EntityUtils.getId(entityId);
            Serializable beanId = EntityUtils.getId(bean.getEntidad());
            if (id != null)
            {
                if (id.equals(beanId) && (index == getListIndex()))
                {
                    return "filaSeleccionadaCssDisplayTag";
                }
            }
            else if (id == beanId && index == getListIndex())
            {
                return "filaSeleccionadaCssDisplayTag";
            }


        }
        return css;

    }
}

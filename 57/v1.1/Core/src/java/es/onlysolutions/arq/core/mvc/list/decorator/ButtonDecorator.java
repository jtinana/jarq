package es.onlysolutions.arq.core.mvc.list.decorator;

import org.displaytag.decorator.DisplaytagColumnDecorator;
import org.displaytag.exception.DecoratorException;
import org.displaytag.properties.MediaTypeEnum;

import javax.servlet.jsp.PageContext;

/**
 * Decorator para decorar un campo de columna que sea un c�digo. Genera un bot�n que al pulsarlo ejecuta la
 * funci�n JavaScript marcarFilaDisplayTag( codigo ).
 * El estilo del bot�n generado es: buttonDisplayTagCss
 */
public class ButtonDecorator implements DisplaytagColumnDecorator
{
    /**
     * Implementamos el m�todo decorate para obtener un bot�n que al pulsarlo seleccione el elemento.
     *
     * @param object        El objeto a decorar.
     * @param pageContext   El contexto de la p�gina.
     * @param mediaTypeEnum El tipo de media.
     * @return El objeto decorado.
     * @throws DecoratorException Si ocurre alguna excepci�n durante la decoraci�n.
     */
    public Object decorate(Object object, PageContext pageContext, MediaTypeEnum mediaTypeEnum) throws DecoratorException
    {
        String strObject = object.toString();
        String decorateResult;

        decorateResult = "<input type=\"button\" name=\"btnDisplayTag\" class=\"buttonDisplayTagCss\" value=\"Seleccionar\" " + "onclick=\"marcarFilaDisplayTag(" + strObject + ")\" />";

        return decorateResult;
    }
}

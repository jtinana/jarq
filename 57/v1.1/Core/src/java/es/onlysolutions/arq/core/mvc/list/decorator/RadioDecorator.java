package es.onlysolutions.arq.core.mvc.list.decorator;

import org.displaytag.decorator.DisplaytagColumnDecorator;
import org.displaytag.exception.DecoratorException;
import org.displaytag.properties.MediaTypeEnum;

import javax.servlet.jsp.PageContext;

/**
 * Decorator que genera un Radio Button de nombre 'radioDisplayTag' y con en el evento de onClick se ejecuta la funci�n
 * javascript marcarFilaDisplayTag
 */
public class RadioDecorator implements DisplaytagColumnDecorator
{
    /**
     * Implementaci�n del m�todo #decorate para envolverlo en un link y permitir eventos en el onclick de la fila.
     *
     * @param object        El objeto a decorar.
     * @param pageContext   El contexto de la p�gina.
     * @param mediaTypeEnum Tipo de media actual (html, pdf, etc)
     * @return El objeto decorado
     * @throws DecoratorException Si ocurre cualquier excepci�n durante la decoraci�n.
     */
    public Object decorate(Object object, PageContext pageContext, MediaTypeEnum mediaTypeEnum) throws DecoratorException
    {
        String strObject = object.toString();
        String decorateResult;


        decorateResult = "<input type=\"radio\" name=\"radioDisplayTag\" onclick=\"marcarFilaDisplayTag(" + strObject + ")\" />";

        return decorateResult;

    }
}

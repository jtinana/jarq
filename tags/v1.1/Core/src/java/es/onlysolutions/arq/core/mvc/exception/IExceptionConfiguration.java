package es.onlysolutions.arq.core.mvc.exception;

import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.List;

/**
 * Interfaz para controlar y procesar las excepciones del controller que no son lanzadas desde el
 * validator. Todas las peticiones pasan por aqui, por lo que se hace necesario sincronizar la clase que
 * la implemente.
 */
public interface IExceptionConfiguration extends Serializable
{
    /**
     * Realiza el tratamiento de la excepcion recogida. Debe devolver un valor siempre.
     *
     * @param httpServletRequest  El HttpServletRequest asociado a la peticion.
     * @param httpServletResponse El HttpServletResponse asociado a la peticion.
     * @param object              El command bean mapeado del formulario.
     * @param bindException       El objeto BindException donde almacenar los errores de validacion.
     * @param exception           La excepcion que se desea manejar.
     * @return Una lista de ObjectErrors con los errores producidos al procesar la peticion.
     *         Si devuelve null es porque la implementacion del interfaz IExceptionConfiguration es incorrecta.
     */
    public List<ObjectError> handleException(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object object, BindException bindException, Exception exception);
}
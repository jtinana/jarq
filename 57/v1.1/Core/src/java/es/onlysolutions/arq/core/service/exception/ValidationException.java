package es.onlysolutions.arq.core.service.exception;

import org.springframework.validation.ObjectError;

import java.util.ArrayList;
import java.util.List;

/**
 * Excepcion que contiene los errores de validacion necesarios para mostrar mensajes de error. Esta
 * excepcion sera captura por la aquitectura y se a�adiran todos los errores de validacion
 * automaticamente.
 */
public class ValidationException extends RuntimeException
{
    //Lista de errores.
    private List<ObjectError> errors = new ArrayList<ObjectError>(7);


    /**
     * Constructor sin parametros.
     *
     * @param error El error a a�adir a la excepcion.
     */
    public ValidationException(ObjectError error)
    {
        this.errors.add(error);
    }


    /**
     * Constructor con la lista de errores.
     *
     * @param errorList La lista de ObjectError a a�adir a los errores.
     */
    public ValidationException(List<ObjectError> errorList)
    {
        this.errors.addAll(errorList);
    }


    /**
     * A�ade un error nuevo a la lista de errores.
     *
     * @param error El ObjectError a a a�adir a la lista.
     */
    public void addError(ObjectError error)
    {
        this.errors.add(error);
    }


    /**
     * Devuelve el numero de errores que contiene la excepcion.
     *
     * @return El numero de errores contenidos en la excepcion.
     */
    public int size()
    {
        return this.errors.size();
    }


    /**
     * Limpia la excepcion de errores.
     */
    public void clearErrors()
    {
        this.errors.clear();
    }


    /**
     * Devuelve los errores de validacion que contiene la excepcion.
     *
     * @return La lista de ObjectError.
     */
    public List<ObjectError> getAllErrors()
    {
        return this.errors;
    }
}
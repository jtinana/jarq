package es.onlysolutions.arq.core.accesobd;

import java.io.Serializable;

/**
 * Interfaz para definir las entidades Hibernate de la arquitectura.
 * Provee un metodo para obtener la Primary Key del objeto (ID) y su nombre de atributo.
 */
public interface IEntityId extends Serializable
{


    /**
     * Devuelve el nombre del atributo que es ID en esta entidad.<br>
     * Esto es necesario para el filtrado automatico de las subentidades de cada entidad padre.
     *
     * @return Un String representando el nombre del atributo que es el ID de esta entidad.
     */
    public String getIdName();


}

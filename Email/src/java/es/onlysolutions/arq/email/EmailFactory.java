package es.onlysolutions.arq.email;

import es.onlysolutions.arq.core.configuration.Configuracion;
import es.onlysolutions.arq.core.log.LoggerGenerator;
import es.onlysolutions.arq.email.exception.EmailException;
import org.apache.commons.logging.Log;

/**
 * Factor�a para obtener implementaciones de Email.
 * En funci�n de la implementaci�n el env�o se resolver� mediante una u otra implementaci�n.
 * Consultar el JavaDoc de las distintas implementaciones para resolver las depencias y par�metros de
 * configuraci�n.
 */
public class EmailFactory
{

    /**
     *
     * El logger de la clase.
     */
    private static final Log logger = LoggerGenerator.getLogger( EmailFactory.class );

    /**
     * Constante que define el nombre del par�metro de configuraci�n a leer para obtener la clase a instanciar.
     */
    public static final String IMPLEMENTATION_CLASS_PARAM = "email.implementationClassEmail";

    /**
     * Instancia privada.
     */
    private static final EmailFactory _instance = new EmailFactory();

    /**
     * Constructor privado para evitar instanciaci�n.
     */
    private EmailFactory()
    {
        super();
    }

    /**
     * Crea una nueva instancia del objeto indicado en la configuraci�n.
     * @return La instancia de la subclase de Email indicada en la configuraci�n instanciada.
     */
    private Email createEmailInstance()
    {
        String className = Configuracion.getString( IMPLEMENTATION_CLASS_PARAM );

        Class theInstanceClass;

        Email rVal;

        try
            {
                theInstanceClass = Class.forName( className );
        }
        catch (ClassNotFoundException e)
        {
            logger.error(e);
            throw new EmailException("No se ha podido instancia la clase de la configuracion, compruebe que " +
                    "es un nombre de clase correcto y es accesible", e);
        }

        try
            {
                Object objectReference = theInstanceClass.newInstance();

            if( !(objectReference instanceof Email) )
            {
                throw new EmailException("La clase: " + className + " debe herederar de: " + Email.class.getName());
            }

            rVal = (Email) objectReference;

            if( logger.isInfoEnabled() )
            {
                logger.info("Leida correctamente la instancia de la configuraci�n: " + rVal);
            }
        }
        catch (InstantiationException e)
        {
            logger.error(e) ;
            throw new EmailException("La clase: " + className + " no ha podido ser instanciada", e);
        }
        catch (IllegalAccessException e)
        {
            logger.error(e);
            throw new EmailException("Error al tratar de acceder a la clase: " + className, e);
        }

        return rVal;
    }

    /**
     * M�todo est�tico para obtener la referencia al singleton.
     * @return El objeto en memoria de EmailFactory.
     */
    public static final EmailFactory instance()
    {
        return _instance;
    }

    /**
     * Obtiene la instancia concreta del Email a utilizar.
     * @return La clase Email instanciada.
     */
    public Email getEmail()
    {
        /**
         * Ya hemos leido. Se devuelve una nueva instancia en cada envio de mail para evitar problemas de sincronizacion.
         */
        return createEmailInstance();
    }
}

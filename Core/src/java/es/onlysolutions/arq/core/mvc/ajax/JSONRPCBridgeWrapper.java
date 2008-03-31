package es.onlysolutions.arq.core.mvc.ajax;

import com.metaparadigm.jsonrpc.JSONRPCBridge;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Iterator;
import java.util.Map;

/**
 * Wrapper para adaptar el Jason Bridge a la nomeclatura de JavaBean.
 */
public class JSONRPCBridgeWrapper
{

    /**
     * El logger de la clase.
     */
    private static final Log logger = LogFactory.getLog(JSONRPCBridgeWrapper.class);
    /**
     * JSONRPCBridge interno.
     */
    private JSONRPCBridge innerBridge;

    /**
     * Nombre bajo el que registrar el JSONBridge en la HttpSession.
     */
    private String registerName;

    /**
     * Constructor sin parametros.
     */
    public JSONRPCBridgeWrapper()
    {
        this.innerBridge = new JSONRPCBridge();
    }

    /**
     * Constructor indicando si se deben utilizar lo Serializers por defecto.
     *
     * @param useDefaultSerializers flag de Default Serializers.
     */
    public JSONRPCBridgeWrapper(boolean useDefaultSerializers)
    {
        this.innerBridge = new JSONRPCBridge(useDefaultSerializers);
    }

    /**
     * Obtiene el nombre bajo el que registrar el JSONRPCBridge en la session.
     *
     * @return El nombre bajo el que registrar el JSONRPCBridge en la session.
     */
    public String getRegisterName()
    {
        return registerName;
    }

    /**
     * Establece el nombre bajo el que registrar el JSONBridge en la session.
     *
     * @param registerName El nombre bajo el que registrarlo.
     */
    public void setRegisterName(String registerName)
    {
        this.registerName = registerName;
    }

    /**
     * Obtiene la referencia interna al JSONRPCBridge original.
     *
     * @return El JSONRPCBridge original.
     */
    public JSONRPCBridge getInnerBridge()
    {
        return innerBridge;
    }

    /**
     * Establece los objetos a registrar en este JSONRPCBridge.
     * Se utiliza un setter con un mapa para facilitar la configuracion desde el IoC de Spring.
     *
     * @param map El mapa de String y Objetos a registrar.
     */
    public void setRegisteredObjects(Map<String, Object> map)
    {
        Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();

        while (it.hasNext())
        {
            Map.Entry<String, Object> entry = it.next();
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value != null)
            {
                this.innerBridge.registerObject(key, value);

                if (logger.isInfoEnabled())
                {
                    logger.info(key + " -> " + value.getClass().getName());
                }
            }
            else
            {
                logger.warn("Se ha tratado de registrar bajo la key: " + key + " un valor null");
            }
        }
    }

    /**
     * Registra un nuevo objeto en el JSONRPCBridge de la forma habitual.
     *
     * @param key    La clave bajo la que registrar el objeto.
     * @param object El objeto a registrar.
     */
    public void registerObject(String key, Object object)
    {
        this.innerBridge.registerObject(key, object);
    }
}

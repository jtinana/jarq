package es.onlysolutions.arq.core.thread;


import es.onlysolutions.arq.core.log.LoggerGenerator;
import es.onlysolutions.arq.core.thread.exception.RegisterThreadException;
import org.apache.commons.logging.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Manager de Threads que permite registrar y desregitrar Threads asi como mantener su estado.
 */
public class InternalTaskManager
{
    /**
     * El logger de la clase.
     */
    private static final Log logger = LoggerGenerator.getLogger(InternalTaskManager.class);

    /**
     * Mapa de los InternalTask registrados en el InternalTaskManager.
     */
    private Map<String, InternalTask> threads = new HashMap<String, InternalTask>(10);

    /**
     * Instancia del singleton.
     */
    private static final InternalTaskManager instance = new InternalTaskManager();

    /**
     * M�todo para localizar la instancia del singleton.
     *
     * @return La �nica instancia en memoria del InternalTaskManager
     */
    public static InternalTaskManager instance()
    {
        return instance;
    }

    /**
     * Registra un Thread bajo el nombre indicado. Esta operacion no arranca el Thread.
     * Debe tenerse en cuenta que se registrara en base al nombre, por lo que es Case Sensitive.
     *
     * @param taskName El nombre del Thread bajo el que registrarse. Deber� ser �nico.
     * @param thread   El objeto Thread instanciado.
     */
    public void registerTask(String taskName, InternalTask thread)
    {
        checkNotExistThread(taskName);
        this.threads.put(taskName, thread);

        if (logger.isDebugEnabled())
        {
            logger.debug("Registrado la tarea: " + taskName);
        }
    }


    /**
     * Desregistra el Thread bajo ese nombre. En caso de tratarse de un Thread arrancado se lanza una RegisterThreadException.
     *
     * @param taskName El nombre del Thread a eliminar.
     */
    public void unregisterTask(String taskName)
    {
        checkExistThread(taskName);
        this.threads.remove(taskName);
    }

    /**
     * Arranca el Thread asociado con el nombre indicado.
     *
     * @param taskName El nombre del Thread a arrancar.
     * @see Thread#start()
     */
    public void startTask(String taskName)
    {
        checkExistThread(taskName);

        InternalTask task = this.threads.get(taskName);
        task.start();

        if (logger.isInfoEnabled())
        {
            logger.info("La tarea de nombre: " + taskName + " asociada al Thread: " + task.getName() + " ha sido arrancada");
        }

        this.threads.put(taskName, task);
    }


    /**
     * Detiene la tarea asociada al nombre indicado. Establece una se�al de parada en la tarea indicada.
     *
     * @param taskName El nombre del Thread a parar.
     */
    public void stopTask(String taskName)
    {
        checkExistThread(taskName);
        InternalTask thread = this.threads.get(taskName);

        if (thread.isAlive())
        {
            thread.interruptThread();

            if (logger.isInfoEnabled())
            {
                logger.info("Sobre el thread " + thread.getName() + " se ha establecido la se�al de parada");
            }

            this.threads.put(taskName, thread);
        }
    }

    /**
     * Comprueba si la tarea indicada est� arrancada.
     *
     * @param taskName El nombre del Thread a comprobar.
     * @return true si est� arrancado.
     * @see Thread#isAlive()
     */
    public boolean isStarted(String taskName)
    {
        checkExistThread(taskName);
        Thread thread = this.threads.get(taskName);

        return thread.isAlive();
    }

    /**
     * Comprueba si la tarea indicada esta parado.
     *
     * @param taskName El nombre del Thread a comprobar.
     * @return true si est� parado.
     * @see #isStarted(String)
     */
    public boolean isStopped(String taskName)
    {
        return !isStarted(taskName);
    }

    /**
     * Comprueba que el nombre del Thread no exista actualmente en el mapa de threads, y lanza una excepcion
     * en caso de que exista uno con el mismo nombre.
     *
     * @param taskName El nombre del Thread a comprobar.
     */
    private void checkNotExistThread(String taskName)
    {
        if (this.threads.containsKey(taskName))
        {
            throw new RegisterThreadException("El nombre de Thread: " + taskName + " ya esta registrado");
        }
    }

    /**
     * Comprueba que el nombre del Thread exista.
     *
     * @param taskName El nombre del thread a comprobar.
     */
    private void checkExistThread(String taskName)
    {
        if (!this.threads.containsKey(taskName))
        {
            throw new RegisterThreadException("El nombre de Thread: " + taskName + " NO esta registrado");
        }
    }

    /**
     * Establece una se�al de parada en todas las tareas registradas en el InternalTaskManager.
     */
    public void stopAllTaks()
    {
        if (logger.isInfoEnabled())
        {
            logger.info("Deteniendo todas las tareas . . .");
        }

        Iterator<Map.Entry<String, InternalTask>> itTaks = this.threads.entrySet().iterator();

        while (itTaks.hasNext())
        {
            Map.Entry<String, InternalTask> entry = itTaks.next();
            String taskName = entry.getKey();
            InternalTask task = entry.getValue();

            task.interruptThread();

            if (logger.isInfoEnabled())
            {
                logger.info("Establecida se�al de parada para la Tarea: " + taskName);
            }
        }

        if (logger.isInfoEnabled())
        {
            logger.info("Detenidas todas las tareas");
        }
    }
}

package es.onlysolutions.arq.core.thread;

import es.onlysolutions.arq.core.configuration.Configuracion;
import es.onlysolutions.arq.core.log.LoggerGenerator;
import es.onlysolutions.arq.core.thread.exception.InternalTaskException;
import org.apache.commons.logging.Log;

/**
 * Clase abstracta que maneja el ThreadManajer como Threads. Todos los Thread que se desee que sean
 * controlados desde el InternalTaskManager deben heredar de �sta clase.
 * Esta clase proporciona la logica necesaria para permitir la parada de los threads, de ahi el cambio en la nomenclatura.
 * Todas las tareas deberan introducir como condici�n de parada del Thread el m�todo #notInterrupted, adem�s de las que se consideren necearias.
 *
 * @see #notInterrupted()
 * @see Thread
 */
public abstract class InternalTask extends Thread
{

    /**
     * El logger de la clase.
     */
    private static final Log logger = LoggerGenerator.getLogger(InternalTask.class);

    /**
     * Variable booleana que indicar� cuando sobre el Thread ha sido establecida la se�al de parada.
     */
    private boolean isInterrupted = false;

    /**
     * <b>�ste metodo debe ser invocado como parte de la condic�n de parada del Thread.</b>
     * Cuando sobre el Thread manager se introduzca una se�al de parada, �ste metodo devolver� en su siguiente invocaci�n false,
     * y el Thread deber� terminar su ejecuci�n actual y terminar.
     *
     * @return true si el Thread debe continuar con su ejecuci�n actual, false en caso de haber sido establecida una se�al de parada sobre �l.
     */
    public boolean notInterrupted()
    {
        return !isInterrupted;
    }

    /**
     * Establece sobre el Thread actual una se�al de parada. El Thread terminara su ejecucion actual y terminara.
     */
    public void interruptThread()
    {
        this.isInterrupted = true;
    }

    /**
     * Elimina la se�al de parada del Thread actual. �sto no garantiza que la parada sea interrumpida,
     * y depender� del estado actual del Thread.
     */
    public void cancelInterruptSignal()
    {
        this.isInterrupted = false;
    }


    /**
     * If this thread was constructed using a separate
     * <code>Runnable</code> run object, then that
     * <code>Runnable</code> object's <code>run</code> method is called;
     * otherwise, this method does nothing and returns.
     * <p/>
     * Subclasses of <code>Thread</code> should override this method.
     *
     * @see Thread#start()
     * @see Thread#stop()
     * @see Thread#Thread(ThreadGroup,
     *      Runnable,String)
     * @see Runnable#run()
     */
    @Override
    public final void run()
    {
        int numeroFallos = 0;
        int maxNumFallos = Configuracion.getInteger("internalTask.maxNumFallos");
        while (notInterrupted())
        {
            try
            {
                executeTask();
            }
            catch (InternalTaskException e)
            {
                logger.error("Se ha producido un error controlado durante la ejecucion de la tarea", e);
            }
            catch (Throwable e)
            {
                logger.error("Se ha producido un error interno durante la tarea " + getClass().getName(), e);
                numeroFallos++;
            }

            if (numeroFallos > maxNumFallos)
            {
                logger.warn("La tarea " + getClass().getName() + " ha fallado mas de " + maxNumFallos + " veces. Se establece una se�al de parada en la tarea");
                interruptThread();
            }
        }
    }

    /**
     * Accion que ejecutara �sta tarea. �ste metodo no debe controlar la parada ni bloqueo de la tarea, dicha tarea
     * se delega en el ThreadManager.<br>
     * <b>No es recomendable el uso de bucles en �ste metodo, pueden llevar a un estado sin fin de la tarea.</b><br>
     * Ejemplo: <br>
     * public class MyTask extends InternalTask<br>
     * {<br>
     * &nbsp;&nbsp;&nbsp;protected void executeTask()<br>
     * &nbsp;&nbsp;&nbsp;{<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;if( miCondition == true )<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;//Acciones de la tarea<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;// ...<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}<br>
     * &nbsp;&nbsp;&nbsp;}<br>
     * }<br>
     */
    protected abstract void executeTask();

    /**
     * Realiza un sleep de la tarea durante los minutos indicados en la configuracion bajo la propiedad: 'aeatlasTask.sleepTime'
     */
    protected void sleepTask()
    {
        Integer sleepTime = Configuracion.getInteger("aeatlasTask.sleepTime");
        try
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("La tarea se duerme durante " + sleepTime + " minutos");
            }
            sleep(getMillisFromMinutes(sleepTime));
        }
        catch (InterruptedException e)
        {
            logger.error("Se interrumpe la tarea: " + getClass().getName(), e);
            interrupt();
        }
    }

    /**
     * Obtiene los milisegundos correspondiente a los minutos pasados como parametros.
     *
     * @param minutes El tiempo a convertir a milisegundos.
     * @return Los milisegundos equivalentes.
     */
    private long getMillisFromMinutes(Integer minutes)
    {
        long seconds = minutes.intValue() * 60;
        long millis = seconds * 1000;
        return millis;
    }
}

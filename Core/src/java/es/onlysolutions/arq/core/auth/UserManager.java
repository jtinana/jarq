package es.onlysolutions.arq.core.auth;

/**
 * Manager para los usuarios. Almacena en un ThreadLocal el objeto usuario para tener acceso a �l desde capas
 * inferiores de la aplicacion en cada peticion.
 */
public class UserManager
{
    /**
     * El ThreadLocal donde se almacenaran los usuarios por Thread.
     */
    private ThreadLocal<IUserSettings> currentUser = new ThreadLocal<IUserSettings>();

    /**
     * La unica instancia de la clase.
     */
    private static final UserManager instance = new UserManager();

    /**
     * Constructor privado para evitar instanciacion.
     */
    private UserManager()
    {
        super();
    }

    /**
     * M�todo est�tico que devuelve la �nica instancia de la clase.
     *
     * @return El objeto UserManager en memoria.
     */
    public static UserManager instance()
    {
        return instance;
    }

    /**
     * Guarda en el Thread actual el objeto usuario.
     *
     * @param user El objeto Usuario a almacenar.
     */
    public void setUser(IUserSettings user)
    {
        this.currentUser.set(user);
    }

    /**
     * Obtiene la referencia al Usuario del Thread actual.
     *
     * @return El objeto IUserSettings del thread actual.
     */
    public IUserSettings getUser()
    {
        return this.currentUser.get();
    }
}

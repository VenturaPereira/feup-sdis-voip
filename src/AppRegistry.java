import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class AppRegistry {

    /**
     * 
     */
    public static PhoneInterface connect_to_registry(String proxy_addr, String username) {
        try {
            Registry registry = LocateRegistry.getRegistry(proxy_addr);
            return (PhoneInterface) registry.lookup(username);
        }
        catch (RemoteException | NotBoundException e ){
            e.printStackTrace();
        }
        return null;
    }
    

}
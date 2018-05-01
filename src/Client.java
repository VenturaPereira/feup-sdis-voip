import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {

    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry(args[1]);
            PhoneInterface stub = (PhoneInterface) registry.lookup(args[0]);
    
            stub.send_register_request();
        }
        catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }

    }
}
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.IOException;


public interface PhoneInterface extends Remote {
    void send_register_request() throws RemoteException;
    void send_invite_request(String username) throws RemoteException, IOException;
}
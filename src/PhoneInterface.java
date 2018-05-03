import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PhoneInterface extends Remote {
    void send_register_request() throws RemoteException;
    void send_invite_request() throws RemoteException;
}
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.IOException;
import java.net.UnknownHostException;

public interface PhoneInterface extends Remote {
    void send_register_request() throws IOException, RemoteException, UnknownHostException;
    void send_invite_request(String username) throws RemoteException, IOException;
    void reject_received_call(String username) throws RemoteException, IOException;
}
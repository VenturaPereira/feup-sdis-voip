import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {

    public static void main(String[] args) {

            String command = args[2];

        try {
            Registry registry = LocateRegistry.getRegistry(args[1]);
            PhoneInterface stub = (PhoneInterface) registry.lookup(args[0]);


            switch(command){

                case "boot":

                    stub.send_register_request();

                    break;

                case "call":

                    try{    
                        stub.send_invite_request(args[3]);
                    }catch(Exception e){
                        System.err.println("App exception: " + e.toString());
				        e.printStackTrace();
                    }
                    break;

                default:
                    break;


            }
    
            
            

        }
        catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }





    }
}
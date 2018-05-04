import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.lang.ArrayIndexOutOfBoundsException;

public class Phone implements PhoneInterface {

    private String host_name, username;
    private int port;
    private DatagramSocket socket;

    /**
     * Phone constructor.
     */
    public Phone(String username, String host_name, int port) throws SocketException {
        this.username = username;
        this.host_name = host_name;
        this.port = port;

        this.socket = new DatagramSocket();
    }

    public void bind_to_registry() {        
        try {
            PhoneInterface stub = (PhoneInterface) UnicastRemoteObject.exportObject(this, 0);
            System.out.println("Phone booted!");
            LocateRegistry.getRegistry().bind(this.username, stub);
        }
        catch (RemoteException | AlreadyBoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 
     */
    @Override
    public void send_register_request() {
        try{
            System.out.println("About to send request");
       this.send("REGISTER");
        }catch(Exception e){
             System.err.println("App exception: " + e.toString());
				        e.printStackTrace();
        }
    }

    /**
     * 
     */
    @Override
    public void send_invite_request(String username) throws IOException {
        InetAddress address = InetAddress.getByName(this.host_name);
        String message = String.format("INVITE %s", username);
        DatagramPacket packet = Message.build_packet(message,address, this.port);
        this.socket.send(packet);
    }


    /**
     * Sends message to socket.
     */
    public void send(String type) throws IOException {
        InetAddress addr = InetAddress.getByName(this.host_name); 
        DatagramPacket packet = null;
        
        switch (type) {
            case "REGISTER":
                System.out.println("Requestingssdsad");
                String message = String.format("REGISTER %s %s", this.username, InetAddress.getLocalHost().toString());
                packet = Message.build_packet(message, addr, this.port);
                break;
    
        
                default:
                break;
        }
        this.socket.send(packet);
        System.out.println("sent  " + packet.getData());
    }
    
    public void listen() {        
        while (true) {
            System.out.println("hi");
            DatagramPacket packet = new DatagramPacket(new byte[512], 512);

            try {
                this.socket.receive(packet);
                System.out.println(new String(packet.getData()));
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void main(String[] args) throws IOException {
        Phone phone = new Phone(args[0], args[1], Integer.parseInt(args[2]));
        
        try {
            phone.bind_to_registry();
         //  phone.display_connection_info();
        }
        catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("\nInvalid arguments!\nUsage: \"java Phone <username> <proxy_hostname> <proxy_port>\"\n");
        }
         System.out.println("exiting...");
         phone.listen();
        
    }
}
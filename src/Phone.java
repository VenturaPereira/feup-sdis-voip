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

    /**
     * 
     */
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
        try {
            this.send(Message.Type.REGISTER);
        }
        catch (Exception e) {
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
    public void send(Message.Type type) throws IOException {
        InetAddress addr = InetAddress.getByName(this.host_name);
        DatagramPacket packet = null;
        
        switch (type) {
            case REGISTER:
                String message = String.format("REGISTER %s %s %d", this.username, InetAddress.getLocalHost().toString(), this.port);
                packet = Message.build_packet(message, addr, this.port);
                break;

            case RINGING:
                packet = Message.build_packet("180 RINGING", addr, port);
                break;
    
            default:
                break;
        }
        this.socket.send(packet);
    }

    /**
     * Handles received packets on the phone.
     * @param message   The datagram packet received from either other phone/server.
     */
    public void message_monitor(DatagramPacket message) throws IOException {
        switch (Message.get_type(message.getData())) {
            
            case SINVITE:
                String callee_info = Message.get_callee_ip(message.getData());
                this.send(Message.Type.INVITE);

            case INVITE:
                this.send(Message.Type.RINGING);
                System.out.println("Incoming call!");
                break;


            case RINGING:
                System.out.println("📞  Ringing...");
                break;

            case OK:
                System.out.println("Call accepted!");
        
            default:
                break;
        }
    }
    
    public void listen() {        
        while (true) {
            DatagramPacket packet = new DatagramPacket(new byte[512], 512);

            try {
                this.socket.receive(packet);
                System.out.println(new String(packet.getData()));
                this.message_monitor(packet);
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
        }
        catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("\nInvalid arguments!\nUsage: \"java Phone <username> <proxy_hostname> <proxy_port>\"\n");
        }
        
        phone.listen();   
    }
}
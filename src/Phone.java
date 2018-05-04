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

    private String username;
    private int proxy_port;
    private InetAddress proxy_addr;
    private DatagramSocket socket;

    /**
     * Phone constructor.
     */
    public Phone(String username, String proxy_host_name, int proxy_port) throws SocketException, UnknownHostException {
        this.username = username;
        this.proxy_addr = InetAddress.getByName(proxy_host_name);
        this.proxy_port = proxy_port;

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
     * A RMI-called method.
     * TODO: Lacking documentation.
     */
    @Override
    public void send_register_request() throws IOException, UnknownHostException {
        String message = String.format("REGISTER %s %s %d", this.username, InetAddress.getLocalHost(), this.socket.getLocalPort());
        this.send(message, this.proxy_addr, this.proxy_port);
    }

    /**
     * A RMI-called method.
     * When a user wishes to call other user, a INVITE request is sent to the server.
     * The server should reply with the callee's stored IP and port.
     * @param username  Callee's username.
     */
    @Override
    public void send_invite_request(String username) throws IOException {
        String message = String.format("INVITE %s", username);
        this.send(message, this.proxy_addr, this.proxy_port);
    }

    /**
     * Sends message to socket.
     */
    public void send(String message, InetAddress addr, int port) throws IOException {
        DatagramPacket packet = Message.build_packet(message, addr, port);
        this.socket.send(packet);
    }

    /**
     * Handles received packets on the phone.
     * @param message   The datagram packet received from either other phone/server.
     */
    public void message_monitor(DatagramPacket message) throws IOException {
        switch (Message.get_type(message.getData())) {
            
            case SINVITE:
                //String callee_info = Message.get_callee_ip(message.getData());
                //this.send(Message.Type.INVITE);
                System.out.println("got invite");
                break;

            case INVITE:
                //this.send(Message.Type.RINGING);
                System.out.println("Incoming call!");
                break;

            case RINGING:
                System.out.println("📞  Ringing...");
                break;

            case OK:
                System.out.println("Call accepted!");
                break;
        
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
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Optional;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.lang.ArrayIndexOutOfBoundsException;

public class Phone implements PhoneInterface {

    private final int SPEAKER_INDEX = 1;

    private String username;
    private int proxy_port;
    private InetAddress proxy_addr;
    private DatagramSocket socket;

    private ArrayList<DatagramPacket> incoming_calls;

    /**
     * Phone constructor.
     */
    public Phone(String username, String proxy_host_name, int proxy_port) throws SocketException, UnknownHostException {
        this.username = username;
        this.proxy_addr = InetAddress.getByName(proxy_host_name);
        this.proxy_port = proxy_port;
        this.incoming_calls = new ArrayList<DatagramPacket>();

        this.socket = new DatagramSocket();
    }

    /**
     * Binds to RMI registry an object of this class, which implements PhoneInterface.
     * It may then be accessible by the application terminal.
     */
    public void bind_to_registry() {        
        try {
            PhoneInterface stub = (PhoneInterface) UnicastRemoteObject.exportObject(this, 0);
            LocateRegistry.getRegistry().bind(this.username, stub);
            System.out.format("Welcome, %s!\nIn order to use VoIP functions, please REGISTER your phone.\n\n", this.username);
        }
        catch (RemoteException | AlreadyBoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * A RMI-called method.
     * By booting the phone, the phone's provided username is stored along with its IP address and port,
     * on a somehow basic implementation of a DNS server - a ProxyServer object.
     */
    @Override
    public void send_register_request() throws IOException, UnknownHostException {
        String message = String.format("REGISTER %s %s %d", this.username, InetAddress.getLocalHost().getHostAddress(), this.socket.getLocalPort());
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
     * A RMI-called method.
     * Rejects a provided user's call.
     * TODO: Elaborate on documentation.
     */
    @Override
    public void reject_received_call(String username) throws IOException {
        Optional<DatagramPacket> match = this.incoming_calls.stream().filter(x -> Message.get_info(x.getData(), SPEAKER_INDEX).equals(username)).findFirst();
        
        if (match.isPresent()) {
            String message = String.format("REJECT %s", this.username);
            this.send(message, match.get().getAddress(), match.get().getPort());

            this.incoming_calls.remove(match.get());    // Rejects incoming call.
            System.out.format("\n⚠️  You have rejected an incoming call from '%s'.\n\n", username);
        } 
        else {
            System.out.println("no incoming call");
        }
    }

    /** PEER COMMUNICATION METHODS */

    /**
     * Sends message to socket.
     * @param message   The message to be transmitted to the socket.
     * @param addr      The InetAddress (basically an IP address) to forward the packet.
     * @param port      The port on the provided IP where the message should land.
     */
    public void send(String message, InetAddress addr, int port) throws IOException {
        DatagramPacket packet = Message.build_packet(message, addr, port);
        this.socket.send(packet);
        System.out.format("📢  [SENT] '%s' sent to %s, %d\n", message, addr.getHostAddress(), port);
    }

    /**
     * Handles received packets on the phone.
     * @param message   The datagram packet received from either other phone/server.
     */
    public void message_monitor(DatagramPacket message) throws IOException {
        
        switch (Message.get_type(message.getData())) {

            case UNAUTHORIZED:
                System.out.println("\n⚠️  Proxy rejected REGISTER request.\n   This is likely due to lack of password hashing.\n");
                break;

            case REJECT:
                System.out.format("\n⚠️  Your call was rejected. Looks like '%s' is busy.\n\n", Message.get_info(message.getData(), SPEAKER_INDEX));
                break;

            case SOK:
                System.out.println("\n✅  Proxy successfully stored this phone's InetAddress!\n   You may now establish voice calls.\n");
                break;

            case SINVITE:
                String[] callee_info = Message.get_callee_info(message.getData());
                this.send("INVITE " + this.username, InetAddress.getByName(callee_info[1]), Integer.parseInt(callee_info[2].trim()));
                break;

            case INVITE:
                String caller = Message.get_info(message.getData(), SPEAKER_INDEX);
                this.send("RINGING " + this.username, message.getAddress(), message.getPort());
                
                this.incoming_calls.add(message); // Adds an incoming call.
                System.out.format("\n📞  Incoming call from '%s'!\n\n", caller);
                break;

            case RINGING:
                String callee = Message.get_info(message.getData(), SPEAKER_INDEX);
                System.out.format("\n📞  Ringing '%s'...\n\n", callee);
                break;

            case OK:
                System.out.println("Call accepted!");
                break;
        
            default:
                break;
        }
    }
    
    /**
     * An endless loop which continuously listens for received packets on the socket.
     * A received packet is then transferred to a monitor function which handles it according to its type.
     */
    public void listen() {        
        while (true) {
            DatagramPacket packet = new DatagramPacket(new byte[512], 512);

            try {
                this.socket.receive(packet);
                System.out.format("📩  [READ] '%s' from %s, %d\n", new String(packet.getData()).trim(), packet.getAddress(), packet.getPort());
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
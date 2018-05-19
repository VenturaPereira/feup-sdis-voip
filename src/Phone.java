import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Optional;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.lang.ArrayIndexOutOfBoundsException;

public class Phone implements Runnable {

    private final int SPEAKER_INDEX = 1;

    private String username;
    private InetAddress proxy_addr;
    private DatagramSocket socket;

    private int in_device, out_device;    

    private ArrayList<DatagramPacket> incoming_calls, ongoing_calls;

    /**
     * Phone constructor.
     */
    public Phone(String username, String proxy_host_name, int in_device, int out_device) throws SocketException, UnknownHostException {
        this.username = username;
        this.proxy_addr = InetAddress.getByName(proxy_host_name);
        this.in_device = in_device;
        this.out_device = out_device;
        
        this.incoming_calls = new ArrayList<DatagramPacket>();
        this.ongoing_calls = new ArrayList<DatagramPacket>();

        this.socket = new DatagramSocket();
        new Thread(this).start();
    }

    /**
     * By booting the phone, the phone's provided username is stored along with its IP address and port,
     * on a somehow basic implementation of a DNS server - a ProxyServer object.
     */
    public void send_register_request() throws IOException, UnknownHostException {
        String message = String.format("REGISTER %s %s %d", this.username, InetAddress.getLocalHost().getHostAddress(), this.socket.getLocalPort());
        this.send(message, this.proxy_addr, Macros.PROXY_PORT);
    }

    /**
     * When a user wishes to call other user, a INVITE request is sent to the server.
     * The server should reply with the callee's stored IP and port.
     * @param username  Callee's username.
     */
    public void send_invite_request(String username) throws IOException {
        String message = String.format("INVITE %s", username);
        this.send(message, this.proxy_addr, Macros.PROXY_PORT);
    }

    /**
     * 
     */
    public void send_contact_list_request() throws IOException {
        String message = String.format("CONTACTS");
        this.send(message, this.proxy_addr, Macros.PROXY_PORT);
    }

    /**
     * TODO: Write documentation.
     */
    public void accept_received_call(String username) throws IOException {
        Optional<DatagramPacket> match = this.incoming_calls.stream().filter(x -> Message.get_info(x.getData(), SPEAKER_INDEX).equals(username)).findFirst();
        
        if (match.isPresent()) {
            String message = String.format("ACCEPT %s", this.username);
            this.send(message, match.get().getAddress(), match.get().getPort());

            this.ongoing_calls.add(match.get());        // Adds new ongoing call.
            this.incoming_calls.remove(match.get());    // Eliminates incoming call.

            System.out.format("\n✅  Accepted incoming call from '%s'. Connecting...\n\n", username);
        }
        else {
            System.out.format("\n⚠️  User '%s' isn't calling you!\n\n", username);
        }
    }

    /**
     * Rejects a provided user's call.
     * TODO: Elaborate on documentation.
     */
    public void reject_received_call(String username) throws IOException {
        Optional<DatagramPacket> match = this.incoming_calls.stream().filter(x -> Message.get_info(x.getData(), SPEAKER_INDEX).equals(username)).findFirst();
        
        if (match.isPresent()) {
            String message = String.format("REJECT %s", this.username);
            this.send(message, match.get().getAddress(), match.get().getPort());

            this.incoming_calls.remove(match.get());    // Rejects incoming call.
            System.out.format("\n⚠️  You have rejected an incoming call from '%s'.\n\n", username);
        } 
        else {
            System.out.println("\n⚠️  No incoming call to reject!\n\n");
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

            // Triggered when the user attempts to register on the proxy without credentials or with faulty hashing.
            case UNAUTHORIZED:
                System.out.println("\n⚠️  Proxy rejected REGISTER request.\n   This is likely due to lack of password hashing.\n");
                break;

            // Triggered when the callee accepts your ongoing call.
            case ACCEPT:
                System.out.format("\n✅  Your call has been accepted! Connecting to '%s'...\n\n", Message.get_info(message.getData(), SPEAKER_INDEX));
                Thread caller_mic = new Thread(new Voice(in_device, message.getAddress()));
                Thread caller_speakers = new Thread(new ReceiveTest(out_device));
                caller_mic.start();
                caller_speakers.start();
                break;

            // Triggered when the user's ongoing phone call attempt has been rejected by the callee.
            case REJECT:
                System.out.format("\n⚠️  Your call was rejected. Looks like '%s' is busy.\n\n", Message.get_info(message.getData(), SPEAKER_INDEX));
                break;

            case UNREGISTERED:
                System.out.println("\n⚠️  Proxy unregistered this phone's IP address!\n   In order to use VoIP functions, please REGISTER your phone.\n");
                break;
            
            case SOK:
                System.out.println("\n✅  Proxy successfully stored this phone's IP address!\n   You may now establish voice calls.\n");
                break;

            case SINVITE:
                String[] callee_info = Message.get_callee_info(message.getData());
                this.send("INVITE " + this.username, InetAddress.getByName(callee_info[1]), Integer.parseInt(callee_info[2].trim()));
                break;

            case SCONTACTS:
                System.out.println(message.getData());
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
                Thread callee_mic = new Thread(new Voice(in_device, message.getAddress()));
                Thread callee_speakers = new Thread(new ReceiveTest(out_device));
                callee_mic.start();
                callee_speakers.start();
                break;
        
            default:
                break;
        }
    }
    
    /**
     * An endless loop which continuously listens for received packets on the socket.
     * A received packet is then transferred to a monitor function which handles it according to its type.
     */
    @Override
    public void run() {  
        System.out.println("running thread");      
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

}
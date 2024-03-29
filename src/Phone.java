import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Optional;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URL;
import java.lang.ArrayIndexOutOfBoundsException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Phone implements Runnable {

    private final int SPEAKER_INDEX = 1;

    private String username;
    private InetAddress proxy_addr;
    private DatagramSocket socket;

    private int in_device, out_device;

    private ArrayList<DatagramPacket> incoming_calls, ongoing_calls;
    private static ExecutorService exec;

    private PrivateCallMicrophone curr_mic;
    private PrivateCallSpeakers curr_speakers;

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
       // new Thread(this).start();
        exec = Executors.newFixedThreadPool(1000);
        exec.execute(this);

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
     * Attempts to create a new lobby by requesting it to the server.
     * @param lobby_name    The name for the lobby to be created.
     */
    public void send_lobby_register_request(String lobby_name) throws IOException {
        String message = String.format("LOBBYREGISTER %s", lobby_name);
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
    public void join_lobby_request(String lobby_name) throws IOException {
        String message = String.format("LOBBYJOIN %s", lobby_name);
        this.send(message, this.proxy_addr, Macros.PROXY_PORT);
    }

    /**
     *
     */
    public void send_contact_list_request() throws IOException {
        this.send("CONTACTS", this.proxy_addr, Macros.PROXY_PORT);
    }

    /**
     *
     */
    public void send_lobby_list_request() throws IOException {
        this.send("LOBBIES", this.proxy_addr, Macros.PROXY_PORT);
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
     * 
     */
    public void adjust_volume(int device_type, float value) {
        if (value < 0 || value > 100) return;

        if (device_type == 0) this.curr_mic.set_volume(value); 
        else this.curr_speakers.set_volume(value);
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


    public void thread_iniciator(int mode, int in_device, int out_device, InetAddress addr, int port){
            if(mode == 1) {
             //   Thread call_mic = new Thread(new PrivateCallMicrophone(in_device, addr));
               // Thread call_speakers = new Thread(new PrivateCallSpeakers(out_device));
              //  call_mic.start();
                //call_speakers.start();
                this.curr_mic = new PrivateCallMicrophone(in_device, addr);
                exec.execute(this.curr_mic);

                this.curr_speakers = new PrivateCallSpeakers(out_device);
                exec.execute(this.curr_speakers);

            }else if(mode == 0){
                exec.execute(new LobbyMicrophone(in_device,addr,port));
                exec.execute(new LobbySpeakers(out_device,port,addr));
            }

    }

    /**
     * Handles received packets on the phone.
     * @param message   The datagram packet received from either other phone/server.
     */
    public void message_monitor(DatagramPacket message) throws IOException {

        switch (Message.get_type(message.getData())) {

            // Triggered when the callee accepts your ongoing call.
            case ACCEPT:
                System.out.format("\n✅  Your call has been accepted! Connecting to '%s'...\n\n", Message.get_info(message.getData(), SPEAKER_INDEX));
                this.thread_iniciator(1, in_device, out_device, message.getAddress(),0);
                this.send("OK", message.getAddress(), message.getPort());
                break;

            // Triggered when the user's ongoing phone call attempt has been rejected by the callee.
            case REJECT:
                System.out.format("\n⚠️  Your call was rejected. Looks like '%s' is busy.\n\n", Message.get_info(message.getData(), SPEAKER_INDEX));
                break;

            case UNREGISTERED:
                System.out.println("\n⚠️  Proxy unregistered this phone's IP address!\n   In order to use VoIP functions, please REGISTER your phone.\n");
                break;

            case SLOBBIES:
                break;

            case SLOBBY:
                String[] lobby_info = Message.get_callee_info(message.getData());
                System.out.println("addr: " + lobby_info[1]);
                System.out.println("port: " + lobby_info[2]);
                this.thread_iniciator(0, in_device, out_device, InetAddress.getByName(lobby_info[1]), Integer.parseInt(lobby_info[2]));
                break;

            case SLOBBYOK:
                System.out.println("Lobby created.");
                break;

            case SOK:
                System.out.println("\n✅  Proxy successfully stored this phone's IP address!\n   You may now establish voice calls.\n");
                break;

            case SINVITE:
                String[] callee_info = Message.get_callee_info(message.getData());
                System.out.println("in sinvite: " + InetAddress.getByName(callee_info[1]));
                this.send("INVITE " + this.username, InetAddress.getByName(callee_info[1]), Integer.parseInt(callee_info[2].trim()));
                break;

            case SCONTACTS:
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

                this.thread_iniciator(1, in_device, out_device, message.getAddress(),0);
                break;

            default:
                break;
        }
    }

    /**
     * 
     */
    public float get_mic_volume() {
        return curr_mic.get_volume();
    }

    /**
     * 
     */
    public float get_speakers_volume() {
        return curr_speakers.get_volume();
    }
    
    /**
     * An endless loop which continuously listens for received packets on the socket.
     * A received packet is then transferred to a monitor function which handles it according to its type.
     */
    @Override
    public void run() {
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

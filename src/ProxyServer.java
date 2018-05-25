import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.util.HashMap;
import java.io.IOException;
import java.util.*;
import java.io.*;

public class ProxyServer {

    private DatagramSocket socket;
    private HashMap<String, String> contacts;
    private HashMap<String, String> lobbies;

    /**
     * ProxyServer entry point (main).
     */
    public static void main(String[] args) throws IOException {
        ProxyServer proxy = new ProxyServer();
        proxy.listen();
    }

    /**
     * ProxyServer constructor.
     * @param port      Port to open UDP server on.
     */
    public ProxyServer() throws SocketException {
        this.socket = new DatagramSocket(Macros.PROXY_PORT);
        this.contacts = new HashMap<String, String>();
        this.lobbies = new HashMap<String, String>();
    }
    
    /**
     * Attempts to store contact as provided by a user's REGISTER request.
     * Server replies with 200 OK code when successful and 403 FORBIDDEN if the table has a <username> entry.
     * If the client does not provide a password correctly hashed, it shall reply 401 UNAUTHORIZED.
     * @param request   A user's REGISTER request.   
     */
    public void store_contact(DatagramPacket request) throws IOException {
        HashMap<String, String> args = Message.parse_register(request.getData());

        if (this.contacts.containsKey(args.get("username"))) {
            this.contacts.remove(args.get("username"));
            this.send("UNREGISTERED 200", request.getAddress(), request.getPort());
        }
        else {         
            this.contacts.put(args.get("username"), (args.get("ip") + " " + args.get("port")).trim());
            this.send("SOK 200", request.getAddress(), request.getPort());
        }
    }

    /**
     * 
     */
    public void store_lobby(DatagramPacket request) throws IOException {
        String lobby_name = Message.get_info(request.getData(), 1);
        
        if (this.lobbies.containsKey(lobby_name)) {
            System.out.println("Occupied!");
        } 
        else {
            this.lobbies.put(lobby_name, String.format("%s %d", Macros.LOBBY_IP, Macros.LOBBY_PORT));
            this.send("SOK 200", request.getAddress(), request.getPort());
        }
    }

    /**
     * Sends a reply to the caller containing the callee's IP.
     * Server replies with 404 NOT FOUND code when username cannot be matched on the registry.
     * @param request   The datagram packet received from the client.
     */
    public void get_contact_ip(DatagramPacket request) throws IOException {
        String username = Message.parse_invite(request.getData());
     
        if (this.contacts.containsKey(username)) {
            this.send("SINVITE " + this.contacts.get(username), request.getAddress(), request.getPort());
        }
        else {
            this.send("NOT_FOUND 404", request.getAddress(), request.getPort());
        }
    }

    /**
     * 
     */
    public void get_lobby_ip(DatagramPacket request) throws IOException {
        String lobby_name = Message.get_info(request.getData(), 1);

        if (this.lobbies.containsKey(lobby_name)) {
            this.send("SLOBBY " + this.lobbies.get(lobby_name), request.getAddress(), request.getPort());
        }
    }

    /**
     * 
     */
    public void get_contact_list(DatagramPacket request) throws IOException {
        String contacts = "";

        for (String key : this.contacts.keySet())
            contacts += key + " ";

        this.send("SCONTACTS " + contacts.trim(), request.getAddress(), request.getPort());
    }

    /**
     * 
     */
    public void get_lobby_list(DatagramPacket request) throws IOException {
        String lobbies = "";

        for (String key : this.lobbies.keySet())
            lobbies += key + " ";

        this.send("SLOBBIES " + lobbies.trim(), request.getAddress(), request.getPort());
    }

    /**
     * Monitors requests and sends them to the respective handle methods.
     * @param request   The datagram packet received from the client.
     */
    public void request_monitor(DatagramPacket request) throws IOException {
        switch (Message.get_type(request.getData())) {
            case REGISTER:
                this.store_contact(request);
                break;

            case LOBBIES:
                this.get_lobby_list(request); break;

            case LOBBYJOIN:
                this.get_lobby_ip(request); break;

            case LOBBYREGISTER:
                this.store_lobby(request); break;

            case INVITE:
                this.get_contact_ip(request);
                break;

            case CONTACTS:
                this.get_contact_list(request);
        
            default:
                break;
        }
    }
    
    /**
     * Replies a provided message to the client via their IP address and port.
     * @param message   Message to reply to client
     * @param addr      The client's IP address, extracted from received packet.
     * @param port      The client's port, extracted from received packet.
     */
    public void send(String message, InetAddress addr, int port) throws IOException {
        DatagramPacket packet = Message.build_packet(message, addr, port);
        this.socket.send(packet);
        System.out.format("📢  [SENT] '%s' sent to %s, %d\n", message, addr.getHostAddress(), port);
    }

    /**
     * Passively listens to socket for new client requests.
     */
    public void listen() throws IOException {
        System.out.format("📡  Enabled UDP server on port %d...\n\n", this.socket.getLocalPort());

        while (true) {
            DatagramPacket packet = new DatagramPacket(new byte[512], 512);
            this.socket.receive(packet);
           
            System.out.format("📩  [READ] '%s' from %s, %d\n", new String(packet.getData()).trim(), packet.getAddress(), packet.getPort());
            this.request_monitor(packet);
        }
    }

}
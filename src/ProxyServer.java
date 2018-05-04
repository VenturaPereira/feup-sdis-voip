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

    /**
     * ProxyServer entry point (main).
     */
    public static void main(String[] args) throws IOException {
        ProxyServer proxy = new ProxyServer(Integer.parseInt(args[0]));
        proxy.listen();
    }

    /**
     * ProxyServer constructor.
     * @param port      Port to open UDP server on.
     */
    public ProxyServer(int port) throws SocketException {
        this.socket = new DatagramSocket(port);
        this.contacts = new HashMap<String, String>();
    }
    
    /**
     * Attempts to store contact as provided by a user's REGISTER request.
     * Server replies with 200 OK code when successful and 403 FORBIDDEN if the table has a <username> entry.
     * @param request   A user's REGISTER request.   
     */
    public void store_contact(DatagramPacket request) throws IOException {
        HashMap<String, String> args = Message.parse_register(request.getData());

        if (this.contacts.containsKey(args.get("username"))) {
            this.reply("FORBIDDEN 403", request.getAddress(), request.getPort());
        } 
        else {
            this.contacts.put(args.get("username"), (args.get("ip") + "/" + args.get("port")).trim());
            this.reply("SOK 200", request.getAddress(), request.getPort());
        }
    }

    /**
     * Sends a reply to the caller containing the callee's IP.
     * Server replies with 404 NOT FOUND code when username cannot be matched on the registry.
     * @param request   The datagram packet received from the client.
     */
    public void get_contact_ip(DatagramPacket request) throws IOException{
        String username = Message.parse_invite(request.getData()).trim();
     
        if (this.contacts.containsKey(username)) {
            this.reply("SINVITE " + this.contacts.get(username), request.getAddress(), request.getPort());
        }
        else {
            this.reply("NOT_FOUND 404", request.getAddress(), request.getPort());
        }
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

            case INVITE:
                this.get_contact_ip(request);
                break;
        
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
    public void reply(String message, InetAddress addr, int port) throws IOException {
        DatagramPacket packet = Message.build_packet(message, addr, port);
        this.socket.send(packet);
        System.out.format("📢  [REPLY] '%s' sent to %s, %d\n\n", message, addr.getHostAddress(), port);
    }

    /**
     * Passively listens to socket for new client requests.
     */
    public void listen() throws IOException {
        System.out.format("📡  Enabled UDP server on port %d...\n\n", this.socket.getLocalPort());

        while (true) {
            DatagramPacket packet = new DatagramPacket(new byte[512], 512);
            this.socket.receive(packet);
           
            System.out.format("📩  [REQUEST] %s\n", new String(packet.getData()).trim());
            this.request_monitor(packet);
        }
    }

}
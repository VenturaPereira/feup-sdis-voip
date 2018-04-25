import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.util.HashMap;
import java.io.IOException;

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
     * Replies a provided message to the client via their IP address and port.
     * @param message   Message to reply to client
     * @param addr      The client's IP address, extracted from received packet.
     * @param port      The client's port, extracted from received packet.
     */
    public void reply(String message, InetAddress addr, int port) throws IOException {
        DatagramPacket packet = Message.build_packet(message, addr, port);
        this.socket.send(packet);
        System.out.format("📢  [REPLY] '%s' sent to %s, %d", message, addr.getHostAddress(), port);
    }

    /**
     * Passively listens to socket for new client requests.
     */
    public void listen() throws IOException {
        System.out.format("📡  Enabled UDP server on port %d...\n\n", this.socket.getLocalPort());

        while (true) {
            DatagramPacket packet = new DatagramPacket(new byte[512], 512);
            this.socket.receive(packet);
            System.out.println(String.format("📩  [REQUEST] %s", new String(packet.getData())));
            this.reply("200 OK", packet.getAddress(), packet.getPort());
        }
    }

}
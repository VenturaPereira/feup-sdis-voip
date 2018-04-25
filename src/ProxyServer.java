import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.util.HashMap;
import java.io.IOException;

public class ProxyServer {

    private DatagramSocket socket;
    private HashMap<String, String> contacts;

    public static void main(String[] args) throws IOException {
        ProxyServer proxy = new ProxyServer(Integer.parseInt(args[0]));

        proxy.listen();
    }

    public ProxyServer(int port) throws SocketException {
        this.socket = new DatagramSocket(port);
        this.contacts = new HashMap<String, String>();
    }

    public boolean store_contact(String username, String ip) {
        if (contacts.containsKey(username)) {
            System.out.format("%s already registered. Sending 403 FORBIDDEN.\n", username);
            return false;
        }
        this.contacts.put(username, ip);
        System.out.format("Added contact entry with username %s and IP %s!\n", username, ip);
        return true;
    }

    public void listen() throws IOException {

        while (true) {
            DatagramPacket packet = new DatagramPacket(new byte[512], 512);
            this.socket.receive(packet);

            HashMap<String, String> tokens = Message.parse_register(packet.getData());
            store_contact(tokens.get("username"), tokens.get("ip"));
        }
    }

}
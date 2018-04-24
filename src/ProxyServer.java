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

        proxy.read_socket();
    }

    public ProxyServer(int port) throws SocketException {
        this.socket = new DatagramSocket(port);
    }

    public void read_socket() throws IOException {
        
        while (true) {
            DatagramPacket packet = new DatagramPacket(new byte[512], 512);
            this.socket.receive(packet);

            System.out.format("%s\n", new String(packet.getData()));
        }
    }

}
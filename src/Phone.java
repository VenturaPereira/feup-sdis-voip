import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.UnknownHostException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;

public class Phone {

    private String host_name;
    private int port;
    private DatagramSocket socket;

    public static void main(String[] args) throws IOException {
        Phone phone = new Phone(args[0], Integer.parseInt(args[1]));

        phone.display_connection_info();
        phone.connect_to_proxy();
    }

    public Phone(String host_name, int port) throws SocketException {
        this.host_name = host_name;
        this.port = port;
        this.socket = new DatagramSocket();
    }

    public void connect_to_proxy() throws IOException {
        InetAddress addr = InetAddress.getByName(this.host_name);

        byte[] handshake_msg = new String("Hello").getBytes();

        DatagramPacket handshake = new DatagramPacket(handshake_msg, handshake_msg.length, addr, this.port);
        this.socket.send(handshake);
    }
    
    public void display_connection_info() {
        System.out.println("\nSetting up proxy server...");
        System.out.format("• Proxy hostname: %s\n", this.host_name);
        System.out.format("• Proxy port: %d\n\n", this.port);
        System.out.println("Connecting to proxy server...\n");
    }

}
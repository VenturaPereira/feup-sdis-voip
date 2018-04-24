import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.UnknownHostException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.lang.ArrayIndexOutOfBoundsException;

public class Phone {

    private String host_name, username;
    private int port;
    private DatagramSocket socket;

    public static void main(String[] args) throws IOException {
        try {
            Phone phone = new Phone(args[0], args[1], Integer.parseInt(args[2]));
            phone.display_connection_info();
            phone.connect_to_proxy();
        }
        catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("\nInvalid arguments!\nUsage: \"java Phone <username> <hostname> <port>\"\n");
        }
    }

    public Phone(String username, String host_name, int port) throws SocketException {
        this.username = username;
        this.host_name = host_name;
        this.port = port;
        this.socket = new DatagramSocket();
    }

    public void connect_to_proxy() throws IOException {
        InetAddress addr = InetAddress.getByName(this.host_name);

        byte[] msg = Message.build_register(this.username, InetAddress.getLocalHost().toString());

        DatagramPacket handshake = new DatagramPacket(msg, msg.length, addr, this.port);
        this.socket.send(handshake);
    }
    
    public void display_connection_info() {
        System.out.format("\nWelcome, %s!\n", this.username);

        System.out.println("\nSetting up proxy server...");
        System.out.format("• Proxy hostname: %s\n", this.host_name);
        System.out.format("• Proxy port: %d\n\n", this.port);
        System.out.println("Attempting to register on proxy...\n");
    }

}
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.UnknownHostException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.lang.ArrayIndexOutOfBoundsException;

public class Phone implements Runnable {

    private String host_name, username;
    private int port;
    private DatagramSocket socket;

    /**
     * Phone constructor.
     */
    public Phone(String username, String host_name, int port) throws SocketException {
        this.username = username;
        this.host_name = host_name;
        this.port = port;
        this.socket = new DatagramSocket();
    }

    /**
     * Sends message to socket.
     */
    public void send(String type) throws IOException {
        InetAddress addr = InetAddress.getByName(this.host_name); 
        DatagramPacket packet = null;
        
        switch (type) {
            case "REGISTER":
                String message = String.format("REGISTER %s %s", this.username, InetAddress.getLocalHost().toString());
                packet = Message.build_packet(message, addr, this.port);
                break;
        
                default:
                break;
        }
        this.socket.send(packet);
    }
    
    public void display_connection_info() {
        System.out.format("\nWelcome, %s!\n", this.username);
        
        System.out.println("\nSetting up proxy server...");
        System.out.format("• Proxy hostname: %s\n", this.host_name);
        System.out.format("• Proxy port: %d\n\n", this.port);
        System.out.println("Attempting to register on proxy...\n");
    }
    
    @Override
    public void run() {        
        while (true) {
            DatagramPacket packet = new DatagramPacket(new byte[512], 512);

            try {
                this.socket.receive(packet);
                System.out.println(new String(packet.getData()));
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void main(String[] args) throws IOException {
        try {
            Phone phone = new Phone(args[0], args[1], Integer.parseInt(args[2]));
            Thread thread = new Thread(phone); // Starts reading from socket.
            thread.start();

            phone.display_connection_info();
            phone.send("REGISTER");
        }
        catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("\nInvalid arguments!\nUsage: \"java Phone <username> <proxy_hostname> <proxy_port>\"\n");
        }
    }
}
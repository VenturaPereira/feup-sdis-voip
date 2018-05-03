import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;

public class Message {

    public static byte[] build_register(String username, String ip) {
        return String.format("REGISTER %s %s", username, ip).getBytes();
    }

    public static byte[] build_invite_server(String username){
        return String.format("INVITE %s", username).getBytes();
    }

    public static String get_type(byte[] request) {
        return new String(request).split(" ")[0];
    }
    
    public static HashMap<String, String> parse_register(byte[] request) {
        HashMap<String, String> result = new HashMap<String, String>();
        String[] tokens = new String(request).split(" ");

        result.put("username", tokens[1]);
        result.put("ip", tokens[2]);
        
        return result;
    }

    public static DatagramPacket build_packet(String message, InetAddress addr, int port) {
        byte[] msg = new String(message).getBytes();
        return new DatagramPacket(msg, msg.length, addr, port);
    }

    /*
    public static void send_packet(DatagramSocket socket, String message, InetAddress addr, int port) {
        DatagramPacket packet = new DatagramPacket(message, message.length, addr, port);
        socket.send(packet);
    }
    */

}
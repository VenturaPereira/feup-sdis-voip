import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;

public class Message {

    /**
     * Message type enum.
     */
    public enum Type {
        REGISTER("REGISTER"), INVITE("INVITE"), RINGING("RINGING"), SINVITE("SINVITE"),
        SOK("SOK"), OK("OK"), ACK("ACK"), UNREGISTERED("UNREGISTERED"), NOT_FOUND("NOT_FOUND"),
        ACCEPT("ACCEPT"), REJECT("REJECT"), SCONTACTS("SCONTACTS"),
        CONTACTS("CONTACTS");

        public final String value; 

        private Type(String value) {
            this.value = value;
        }
    }

    /**
     * Returns the message type of received packet.
     * @param request   The received packet.
     */
    public static Message.Type get_type(byte[] request) {
        return Message.Type.valueOf(new String(request).trim().split(" ")[0]);
    }

    /**
     * Returns a specific parameter encoded on the packet.
     * @param message   The message to split.
     * @param index     Specific index of parameter to extract.
     */
    public static String get_info(byte[] message, int index) {
        return new String(message).trim().split(" ")[index];
    }

    /**
     * Returns an array of parameters encoded on the packet.
     * @param message   The message to split.
     */
    public static String[] get_callee_info(byte[] message) {
        return new String(message).trim().split(" ");
    }
    
    public static HashMap<String, String> parse_register(byte[] request) {
        HashMap<String, String> result = new HashMap<String, String>();
        String[] tokens = new String(request).split(" ");

        result.put("username", tokens[1]);
        result.put("ip", tokens[2]);
        result.put("port", tokens[3]);
        
        return result;
    }

    public static String parse_invite(byte[] request) {
        String[] tokens = new String(request).split(" ");
        return tokens[1].trim();
    }

    public static DatagramPacket build_packet(String message, InetAddress addr, int port) {
        byte[] msg = new String(message).trim().getBytes();
        return new DatagramPacket(msg, msg.length, addr, port);
    }

}
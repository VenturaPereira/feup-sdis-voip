import java.util.HashMap;

public class Message {

    public static byte[] build_register(String username, String ip) {
        return String.format("REGISTER %s %s", username, ip).getBytes();
    }

    public static HashMap<String, String> parse_register(byte[] request) {
        HashMap<String, String> result = new HashMap<String, String>();
        String[] tokens = new String(request).split(" ");

        result.put("username", tokens[1]);
        result.put("ip", tokens[2]);

        return result;
    }

}
public class Message {

    public static byte[] build_register(String username, String ip) {
        return String.format("REGISTER %s %s", username, ip).getBytes();
    }

}
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.Scanner;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public class Application {

    private Properties properties;
    private Phone phone;

    public Application() {}

    /**
     * 
     */
    public enum Type {
        CALL("CALL");
        
        public final String value; 
        private Type(String value) { this.value = value; }
    }

    public void command_monitor() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            switch (Type.valueOf(reader.readLine())) {
                case CALL:
                    this.phone.send_contact_list_request();
            }
        }

    }

    /**
     * 
     */
    public void run_first_time_setup() throws IOException, LineUnavailableException {
        this.properties = new Properties();
        Voice voice = new Voice();

        Scanner scanner = new Scanner(System.in);

        System.out.format("Proxy IPv4: ");
        properties.setProperty("PROXY_IPV4", scanner.nextLine());

        System.out.format("Username: ");
        properties.setProperty("USERNAME", scanner.nextLine());
        
        voice.display_devices(TargetDataLine.class);
        System.out.format("Input device: ");
        properties.setProperty("DEFAULT_INPUT_DEVICE", scanner.nextLine());
        
        voice.display_devices(SourceDataLine.class);
        System.out.format("Output device: ");
        properties.setProperty("DEFAULT_OUTPUT_DEVICE", scanner.nextLine());

        scanner.close(); // Prevent resource leaks.

        // Store information on 'app.properties' file.
        properties.store(new FileWriter("app.properties"), null);
    }

    /**
     * 
     */
    public void initialize_phone() throws SocketException, IOException, UnknownHostException {
        System.out.println(this.properties);
        this.phone = new Phone(this.properties.getProperty("USERNAME"), this.properties.getProperty("PROXY_IPV4"),
        Integer.parseInt(this.properties.getProperty("DEFAULT_INPUT_DEVICE")), Integer.parseInt(this.properties.getProperty("DEFAULT_OUTPUT_DEVICE")));
    
        this.phone.send_register_request();
    }

    /**
     * 
     */
    public Properties load_properties() throws IOException, LineUnavailableException {
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream("app.properties")); // Attempt to load properties.
            this.properties = properties;
        }
        catch (FileNotFoundException e) {
            run_first_time_setup();
            load_properties();  // After running first time setup, attempt to load properties.
        }
        return null;
    }

    /**
     * 
     */
    public static void main(String[] args) {
        System.out.format("\nWelcome to FEUP VoIP!\n");
        
        Application app = new Application();
        
        try {
            app.load_properties();
            app.initialize_phone();

            app.command_monitor();
        } catch (Exception e) {}
        


    }

}
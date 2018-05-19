import java.awt.TrayIcon.MessageType;
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

    private Phone phone;
    private Properties properties;

    /**
     * Default application constructor.
     */
    public Application() {}

    /**
     * 
     */
    public void command_monitor() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            switch (reader.readLine()) {
                case "CALL":
                    this.phone.send_contact_list_request();
                    this.phone.send_invite_request(reader.readLine());
                    break;

                case "ACCEPT":
                    this.phone.accept_received_call(reader.readLine());
                    break;

                case "REJECT":
                    this.phone.reject_received_call(reader.readLine());
                    break;
            }
        }

    }

    /**
     * 
     */
    public void run_first_time_setup() throws IOException, LineUnavailableException {
        this.properties = new Properties();
        Scanner scanner = new Scanner(System.in);

        System.out.format("Proxy IPv4: ");
        properties.setProperty("PROXY_IPV4", scanner.nextLine());

        System.out.format("Username: ");
        properties.setProperty("USERNAME", scanner.nextLine());
        
        Voice.display_devices(TargetDataLine.class);
        System.out.format("Input device: ");
        properties.setProperty("DEFAULT_INPUT_DEVICE", scanner.nextLine());
        
        Voice.display_devices(SourceDataLine.class);
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
        this.phone = new Phone(this.properties.getProperty("USERNAME"), this.properties.getProperty("PROXY_IPV4"),
        Integer.parseInt(this.properties.getProperty("DEFAULT_INPUT_DEVICE")), Integer.parseInt(this.properties.getProperty("DEFAULT_OUTPUT_DEVICE")));
        
        WinNotification.alert("Phone booted!", "You may now attempt to connect to proxy server.", MessageType.INFO);
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
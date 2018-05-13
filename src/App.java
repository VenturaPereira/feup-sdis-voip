import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Properties;
import java.util.Scanner;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public class App {

    private PhoneInterface phone;
    private Properties properties;

    public App() {
    }

    public enum Type {
        BOOT("BOOT");
        public final String value; 
        private Type(String value) { this.value = value; }
    }

    public String get_property(String property) {
        return this.properties.getProperty(property);
    }

    public void set_phone_interface(PhoneInterface phone) {
        this.phone = phone;
    }



    public void run_first_time_setup() throws IOException, LineUnavailableException {
        System.out.println("\nNo configuration file found! Running first time setup...\n");
        Properties properties = new Properties();
        Voice voice = new Voice();

        Scanner scanner = new Scanner(System.in);

        System.out.format("Proxy address:\n> ");
        properties.setProperty("PROXY_ADDR", scanner.next().trim());
        
        System.out.format("\nUsername:\n> ");
        properties.setProperty("USERNAME", scanner.next().trim());

        System.out.format("\nInput device:\n");
        voice.display_devices(TargetDataLine.class);
        properties.setProperty("DEFAULT_INPUT_DEVICE", Integer.toString(scanner.nextInt()));

        System.out.format("\nOutput device:\n");
        voice.display_devices(SourceDataLine.class);
        properties.setProperty("DEFAULT_OUTPUT_DEVICE", Integer.toString(scanner.nextInt()));

        scanner.close(); // Prevent resource leaks.

        // Store information on 'app.properties' file.
        properties.store(new FileWriter("app.properties"), null);
    }

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


    public void request_handler() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            switch (Type.valueOf(scanner.next())) {
                
                // Attempt to connect to registry and fetch the PhoneInterface. 
                case BOOT:
                    this.phone = AppRegistry.connect_to_registry(this.get_property("PROXY_ADDR"), this.get_property("USERNAME"));
                    break;

                default:
                    System.out.format("Command not found!\n");
                    break;
            }

        }

    }


    public static void main(String[] args) {
        App app = new App();
        
        try {
            app.load_properties();
        } 
        catch (IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
        
        System.out.format("\nWelcome to FEUP VoIP!\n");

        app.request_handler();
    }

}

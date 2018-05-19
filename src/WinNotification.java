import java.awt.AWTException;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;

public class WinNotification {

    public static void alert(String title, String description, MessageType type) {
        if (!SystemTray.isSupported()) return;

        SystemTray tray = SystemTray.getSystemTray();

        Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
        TrayIcon tray_icon = new TrayIcon(image, "Tray Demo");

        tray_icon.setImageAutoSize(true);
        tray_icon.setToolTip("asdf");
        
        try {
            tray.add(tray_icon);
        } catch (AWTException e) {}

        tray_icon.displayMessage(title, description, type);
    }

}
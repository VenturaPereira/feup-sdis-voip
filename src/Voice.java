import java.io.ByteArrayOutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public class Voice {
    
    TargetDataLine line;
    AudioFormat format;

    public Voice() {
    }

    public void setup_target_data_line() {
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, this.format);   // Format is an AudioFormat object.

        if (!AudioSystem.isLineSupported(info)) {
            // TODO: Handle error.
        }

        try {
            this.line = (TargetDataLine) AudioSystem.getLine(info);
            this.line.open(format);
        } 
        catch (LineUnavailableException exception) {
            exception.printStackTrace();
        }

    }

    public void record_audio() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] data = new byte[this.line.getBufferSize() / 5];

        this.line.start();  // Begin audio capture.

        int num_read = this.line.read(data, 0, data.length);
        out.write(data, 0, num_read);
    }


    public static void main(String[] args) {
    }

}
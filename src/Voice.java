import java.io.ByteArrayOutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

public class Voice {
    
    private static final float SAMPLE_RATE = 8000.0f;
    private static final int CHUNK_SIZE = 1024, SAMPLE_SIZE = 16, CHANNEL_MONO = 1, CHANNEL_STEREO = 2;

    private TargetDataLine microphone;
    private AudioFormat format;

    public Voice() {
        this.format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE, CHANNEL_MONO, true, true);
    }

    public void open_mic_line() throws LineUnavailableException {
        this.microphone = AudioSystem.getTargetDataLine(this.format);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, this.format);
        
        this.microphone = (TargetDataLine) AudioSystem.getLine(info);
        this.microphone.open();
    }

    public void rec_mic_line() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] data = new byte[this.microphone.getBufferSize() / 5];

        this.microphone.start();  // Begin audio capture.
    }

    public void select_input_device() {
        Mixer.Info[] mixer_info = AudioSystem.getMixerInfo();

        for (Mixer.Info info : mixer_info) {
            System.out.println(info.getName());
        }
    }


    public static void main(String[] args) {
        Voice voice = new Voice();
        
        try {
            voice.select_input_device();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

}
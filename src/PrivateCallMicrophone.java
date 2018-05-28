import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.Port.Info;

public class PrivateCallMicrophone implements Runnable {

    private static final float SAMPLE_RATE = 44100.0f;
    private static final int CHUNK_SIZE = 1024, SAMPLE_SIZE = 16, CHANNEL_MONO = 1, CHANNEL_STEREO = 2;

    private TargetDataLine microphone;
    private SourceDataLine speakers;
    private AudioFormat format;
    private InetAddress addr;
    private DatagramPacket dgp;
    private int in_device;


    /**
     * Voice class constructor.
     */
    public PrivateCallMicrophone(int in_device, InetAddress addr) {
        this.addr=addr;
        this.in_device = in_device;
        this.format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE, CHANNEL_STEREO, true, true);
    }

    /**
     *
     */
    public void open_lines(Mixer mic_mixer) throws LineUnavailableException {
        DataLine.Info out_info = new DataLine.Info(TargetDataLine.class, this.format);
        this.microphone = (TargetDataLine) mic_mixer.getLine(out_info);
        this.microphone.open(this.format);
    }

    /**
     *
     */
    public void rec_mic_line() throws IOException {
       try{

        byte[] data = new byte[this.microphone.getBufferSize() / 5];
        DatagramSocket socket = new DatagramSocket();
        this.microphone.start();  // Begin audio capture.

        while (true) {
            int bytes_read = this.microphone.read(data, 0, 4096);


            dgp = new DatagramPacket(data, data.length,this.addr, Macros.COMS_PORT);
            socket.send(dgp);

        }


        }catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();

        } catch (IOException e2) {

        }
    }

    /**
     *
     */
    public float get_volume() {
        FloatControl volume_control = (FloatControl) microphone.getControl(FloatControl.Type.MASTER_GAIN);
        return volume_control.getValue();
    }

    /**
     *
     */
    public void set_volume(float value) {
        FloatControl volume_control = (FloatControl) microphone.getControl(FloatControl.Type.MASTER_GAIN);
        float volume = (float)(Math.log(value / 100) / Math.log(10.0) * 20.0);
        volume_control.setValue(volume);
        System.out.format("Volume set to %f/100!\n", value);
    }

    /**
     *
     */
    public static void display_devices(Class<?> line_type) throws LineUnavailableException {
        Mixer.Info[] mixer_info = AudioSystem.getMixerInfo();

        for (int i = 0; i < mixer_info.length; i++) {
            Mixer mixer = AudioSystem.getMixer(mixer_info[i]);
            Line.Info[] line_infos;

            if (line_type.equals(TargetDataLine.class)) line_infos = mixer.getTargetLineInfo();
            else line_infos = mixer.getSourceLineInfo();

            if (line_infos.length >= 1 && line_infos[0].getLineClass().equals(line_type))
                System.out.format("(%d)\t%s\n", i+1, mixer_info[i].getName());
        }
    }





    /**
     *
     */
    public Mixer select_mic_device() throws LineUnavailableException {
        Mixer.Info[] mixer_info = AudioSystem.getMixerInfo();
        return AudioSystem.getMixer(mixer_info[this.in_device]);
    }



    public void run() {

        try {

            Mixer mic_mixer = this.select_mic_device();
           // Mixer speaker_mixer = voice.select_speaker_device();

            this.open_lines(mic_mixer);
            this.rec_mic_line();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

}

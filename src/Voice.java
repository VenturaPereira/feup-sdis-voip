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
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.Port.Info;

public class Voice {
    
    private static final float SAMPLE_RATE = 8000.0f;
    private static final int CHUNK_SIZE = 1024, SAMPLE_SIZE = 16, CHANNEL_MONO = 1, CHANNEL_STEREO = 2;

    private TargetDataLine microphone;
    private SourceDataLine speakers;
    private AudioFormat format;
    private InetAddress addr;
    private DatagramPacket dgp;

    /**
     * Voice class constructor.
     */
    public Voice() {
        this.format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE, CHANNEL_STEREO, true, true);
    }

    /**
     *
     */
    public void open_lines(Mixer mic_mixer) throws LineUnavailableException {
        DataLine.Info out_info = new DataLine.Info(TargetDataLine.class, this.format);
        this.microphone = (TargetDataLine) mic_mixer.getLine(out_info);
        this.microphone.open(this.format);

    /*    DataLine.Info in_info = new DataLine.Info(SourceDataLine.class, this.format);
        this.speakers = (SourceDataLine) speakers_mixer.getLine(in_info);
        this.speakers.open(this.format);*/
    }
    
    /**
     * 
     */
    public void rec_mic_line() throws IOException {
       try{
        ByteArrayOutputStream out = new ByteArrayOutputStream();
      
        byte[] data = new byte[this.microphone.getBufferSize() / 5];
        addr = InetAddress.getByName("127.0.0.1");
        DatagramSocket socket = new DatagramSocket();
        this.microphone.start();  // Begin audio capture.
        //this.speakers.start();

        while (true) {
            int bytes_read = this.microphone.read(data, 0, 1024);

            //out.write(data);
            dgp = new DatagramPacket(data, data.length,addr,9000);
            socket.send(dgp);
           // this.speakers.write(data, 0, bytes_read);
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
    public void display_devices(Class<?> line_type) throws LineUnavailableException {
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
        return AudioSystem.getMixer(mixer_info[3]);
    }

    public Mixer select_speaker_device() throws LineUnavailableException {
        Mixer.Info[] mixer_info = AudioSystem.getMixerInfo();
        return AudioSystem.getMixer(mixer_info[0]);
    }

    public static void main(String[] args) {
        Voice voice = new Voice();
        
        try {
            voice.display_devices(SourceDataLine.class);
            voice.display_devices(TargetDataLine.class);
            Mixer mic_mixer = voice.select_mic_device();
            Mixer speaker_mixer = voice.select_speaker_device();

            voice.open_lines(mic_mixer);
            voice.rec_mic_line();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

}

import java.io.ByteArrayInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.Mixer;



import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;

import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.Port.Info;


import javax.sound.sampled.LineUnavailableException;



public class PrivateCallSpeakers implements Runnable{

    AudioInputStream audioInputStream;
    static AudioInputStream ais;
    static AudioFormat format;
    static boolean status = true;
    static float sampleRate = 8000.0f;
    private int out_device;

    static DataLine.Info dataLineInfo;
    static SourceDataLine sourceDataLine;


    public PrivateCallSpeakers(int out_device){
        this.out_device = out_device;
        this.format = new AudioFormat(sampleRate, 16, 2, true, true);
    }

    /**
     * 
     */
    public float get_volume() {
        FloatControl volume_control = (FloatControl) sourceDataLine.getControl(FloatControl.Type.MASTER_GAIN); 
        return volume_control.getValue();
    }

    /**
     * 
     */
    public void set_volume(float value) {
        FloatControl volume_control = (FloatControl) sourceDataLine.getControl(FloatControl.Type.MASTER_GAIN);
        float volume = (float)(Math.log(value / 100) / Math.log(10.0) * 20.0);
        volume_control.setValue(volume);
        System.out.format("ℹ️  Volume set to %d/100!\n", Math.round(value));
    }
    
    public void run()
    {
        try{
        System.out.println("Server started at port:" + Macros.COMS_PORT);
        try{    
        DatagramSocket serverSocket = new DatagramSocket(Macros.COMS_PORT);
        

        byte[] receiveData = new byte[512];

        Mixer.Info[] mixer_info = AudioSystem.getMixerInfo();
        
        dataLineInfo = new DataLine.Info(SourceDataLine.class, format);

        Mixer mixer = AudioSystem.getMixer(mixer_info[this.out_device]);
        sourceDataLine = (SourceDataLine) mixer.getLine(dataLineInfo);
        sourceDataLine.open(format);
                        System.out.println("hum");

        sourceDataLine.start();

        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

        ByteArrayInputStream baiss = new ByteArrayInputStream(receivePacket.getData());

        while (status == true) 
        {
            serverSocket.receive(receivePacket);
            ais = new AudioInputStream(baiss, format, receivePacket.getLength());
            toSpeaker(receivePacket.getData());
        }

        sourceDataLine.drain();
        sourceDataLine.close();
        }catch(LineUnavailableException e){
            e.printStackTrace();
    }

    }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void toSpeaker(byte soundbytes[]) {
        try 
        {
            sourceDataLine.write(soundbytes, 0, soundbytes.length);
        } catch (Exception e) {
            System.out.println("Not working in speakers...");
            e.printStackTrace();
        }
    }
}

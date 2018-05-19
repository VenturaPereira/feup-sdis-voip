
import java.io.ByteArrayInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.Mixer;



import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;

import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.Port.Info;


import javax.sound.sampled.LineUnavailableException;



public class ReceiveTest {

    AudioInputStream audioInputStream;
    static AudioInputStream ais;
    static AudioFormat format;
    static boolean status = true;
    static int port = 9000;
    static float sampleRate = 8000.0f;

    static DataLine.Info dataLineInfo;
    static SourceDataLine sourceDataLine;

    public static void main(String args[]) throws Exception 
    {
        try{
        System.out.println("Server started at port:"+port);
            
        DatagramSocket serverSocket = new DatagramSocket(port);
 

        byte[] receiveData = new byte[1024];

        Mixer.Info[] mixer_info = AudioSystem.getMixerInfo();
        
        format = new AudioFormat(sampleRate, 16, 2, true, true);
        dataLineInfo = new DataLine.Info(SourceDataLine.class, format);

        Mixer mixer = AudioSystem.getMixer(mixer_info[5]);
        sourceDataLine = (SourceDataLine) mixer.getLine(dataLineInfo);
        sourceDataLine.open(format);
                        System.out.println("hum");

        sourceDataLine.start();

        //FloatControl volumeControl = (FloatControl) sourceDataLine.getControl(FloatControl.Type.MASTER_GAIN);
        //volumeControl.setValue(1.00f);

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
    }

    public static void toSpeaker(byte soundbytes[]) {
        try 
        {
            System.out.println("At the speaker");
            sourceDataLine.write(soundbytes, 0, soundbytes.length);
            System.out.println(soundbytes.length);
        } catch (Exception e) {
            System.out.println("Not working in speakers...");
            e.printStackTrace();
        }
    }
}
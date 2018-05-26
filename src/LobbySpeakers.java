
import java.io.ByteArrayInputStream;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.InetAddress;


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






public class LobbySpeakers implements Runnable{

    AudioInputStream audioInputStream;
    static AudioInputStream ais;
    static AudioFormat format;
    static boolean status = true;
    static float sampleRate = 8000.0f;
    private int out_device, port;
    private InetAddress addr;
    static DataLine.Info dataLineInfo;
    static SourceDataLine sourceDataLine;

 


    public LobbySpeakers(int out_device, int port, InetAddress addr){
        
        this.out_device=out_device;
        this.port=port;
        this.addr=addr;
    }


     public void run()
    {
        try{
        System.out.println("Server started at port:" + this.port);
        byte[] receiveData = new byte[1024];
        try(MulticastSocket serverSocket = new MulticastSocket(this.port)){
            serverSocket.setBroadcast(true);
        serverSocket.joinGroup(InetAddress.getByName("225.0.0.3"));

        

        Mixer.Info[] mixer_info = AudioSystem.getMixerInfo();
        
      format = new AudioFormat(sampleRate, 16, 2, true, true);
        dataLineInfo = new DataLine.Info(SourceDataLine.class, format);

        Mixer mixer = AudioSystem.getMixer(mixer_info[this.out_device]);
        sourceDataLine = (SourceDataLine) mixer.getLine(dataLineInfo);
        sourceDataLine.open(format);
                      System.out.println("hum");

        sourceDataLine.start();

        //FloatControl volumeControl = (FloatControl) sourceDataLine.getControl(FloatControl.Type.MASTER_GAIN);
        //volumeControl.setValue(1.00f);


      
        while (status == true) 
        {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
              ByteArrayInputStream baiss = new ByteArrayInputStream(receivePacket.getData());

            serverSocket.receive(receivePacket);
          //  String receiving = new String(receivePacket.getData(), 0, receivePacket.getData().length);
        //    System.out.println(receiving);
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
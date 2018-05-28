
import java.io.ByteArrayInputStream;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.Port.Info;


import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;

import java.io.IOException;

import java.util.*;

import java.util.Arrays;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;





public class LobbySpeakers implements Runnable{

    AudioInputStream audioInputStream;
    static AudioInputStream ais;
    static AudioFormat format;
    static float sampleRate = 44100.0f;
    private int out_device, port;
    private InetAddress addr;
    static DataLine.Info dataLineInfo;
    static SourceDataLine sourceDataLine;
    private static ExecutorService exec;




    public LobbySpeakers(int out_device, int port, InetAddress addr){
        this.exec = Executors.newFixedThreadPool(1000);
        this.out_device=out_device;
        this.port=port;
        this.addr=addr;
        this.format = new AudioFormat(sampleRate, 16, 2, true, true);
    }

    public Mixer select_speakers_device() throws LineUnavailableException{
      Mixer.Info[] mixer_info = AudioSystem.getMixerInfo();
      return AudioSystem.getMixer(mixer_info[this.out_device]);
    }


    public void open_lines(Mixer speakers_mixer) throws LineUnavailableException{
      dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
      sourceDataLine = (SourceDataLine) speakers_mixer.getLine(dataLineInfo);
      sourceDataLine.open(format);
    }

    public void speakers_listen() throws LineUnavailableException, IOException{

        try(MulticastSocket serverSocket = new MulticastSocket(this.port)){
            Queue<DatagramPacket> queue = new ConcurrentLinkedQueue<DatagramPacket>();
            sourceDataLine.start();

            serverSocket.joinGroup(InetAddress.getByName("225.0.0.3"));
            Thread t = new Thread(new SpeakersWriter(sourceDataLine, queue));

            t.start();

            while (true)
            {

                try{
              byte[] receiveData = new byte[4096];
              DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
              serverSocket.receive(receivePacket);
                Thread r = new Thread(new PacketQueue(receivePacket, queue));
                r.start();
                //DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                try{
                  Thread.sleep(1);
                }catch(Exception e){
                  e.printStackTrace();
                }
            }catch(Exception e){
              e.printStackTrace();
            }


          }
}

  }



     public void run()
    {
      try{
      Mixer speaker_mixer = this.select_speakers_device();
      this.open_lines(speaker_mixer);
      this.speakers_listen();
    }catch(LineUnavailableException e){
      e.printStackTrace();
    }catch(IOException e){
      e.printStackTrace();
    }

    }















}

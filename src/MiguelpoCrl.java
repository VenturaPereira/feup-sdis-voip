
import java.io.ByteArrayInputStream;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.InetAddress;
import java.util.*;

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



import java.util.Arrays;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.concurrent.ConcurrentLinkedQueue;




public class MiguelpoCrl implements Runnable{



        private MulticastSocket socket;
        private Queue<DatagramPacket> queue;




        public MiguelpoCrl(MulticastSocket socket, Queue<DatagramPacket> queue){
            this.queue = queue;
            this.socket = socket;


        }


        public void run(){
              try{
              byte[] receiveData = new byte[3072];
              DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
              this.socket.receive(receivePacket);
              if(!receivePacket.getAddress().toString().replace("/","").trim().equals(InetAddress.getLocalHost().getHostAddress().toString().replace("/","").trim())){
                queue.add(receivePacket);
              }

            }catch(Exception e){
              e.printStackTrace();
            }




        }











}

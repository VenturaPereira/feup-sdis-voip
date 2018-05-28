import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentLinkedQueue;

import java.net.DatagramPacket;
import java.util.*;


public class SpeakersWriter implements Runnable{

        private SourceDataLine target;
        private Queue<DatagramPacket> queue;

        public SpeakersWriter(SourceDataLine target, Queue<DatagramPacket> queue){
          this.target = target;
          this.queue = queue;
        }


        public void run(){

          try
          {
              while (true) {
                DatagramPacket packet = queue.poll();

		if (packet == null) continue;
                this.target.write(packet.getData(), 0, packet.getData().length);


              }



          } catch (Exception e) {
              System.out.println("Not working in speakers...");
              e.printStackTrace();
          }



        }








}

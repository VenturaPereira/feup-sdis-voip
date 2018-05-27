import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class SpeakersWriter implements Runnable{

        private byte[] to_write;
        private SourceDataLine target;

        public SpeakersWriter(byte[] info, SourceDataLine target){
          this.to_write = info;
          this.target = target;
        }


        public void run(){

          try
          {

              this.target.write(this.to_write, 0, this.to_write.length);
              this.target.drain();
              this.target.close();

          } catch (Exception e) {
              System.out.println("Not working in speakers...");
              e.printStackTrace();
          }



        }








}

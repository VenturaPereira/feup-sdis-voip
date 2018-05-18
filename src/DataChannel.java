import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.sound.sampled.*;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.UnknownHostException;
import java.net.DatagramSocket;
import java.io.IOException;
import java.net.InetAddress;
import java.util.*;





public class DataChannel implements Runnable{

    private static  DatagramSocket socket;
    private static int port;
    private static InetAddress address;


    public DataChannel(String address, int port) throws UnknownHostException{

        try{
        this.address=InetAddress.getByName(address);
        this.port=port;
        }
        	catch (UnknownHostException e) {
			e.printStackTrace();
		}

    }


    public static void openSocket(){
        try{
            socket = new DatagramSocket(port);
        }
        catch(IOException e){
			e.printStackTrace();
		}
    }

    public static void sendAudioMessage(byte[] content) throws UnknownHostException, InterruptedException{

        try(DatagramSocket senderSocket = new DatagramSocket()){

            DatagramPacket msgPacket= new DatagramPacket(content, content.length,address,port);
            senderSocket.send(msgPacket);
        }catch(IOException ex){
            ex.printStackTrace();
        }

    }

    @Override
	public void run(){
		

		byte[] buf = new byte[1024];
		openSocket();

		try{
			while(true){

				DatagramPacket msgReceiverPacket = new DatagramPacket(buf,buf.length);
				socket.receive(msgReceiverPacket);

				
				byte[] toSend = Arrays.copyOfRange(buf,0, msgReceiverPacket.getLength());

			}




		}catch(IOException ex){
			ex.printStackTrace();
		}
		

	}













}
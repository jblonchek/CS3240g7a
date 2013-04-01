import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lejos.nxt.LCD;
import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.NXTConnection;


public class CommThread extends Thread {

	@Override
	public void run() {

	    String connected = "Connected";
	    String waiting = "Waiting...";
	    String closing = "Closing...";

	    Packet packetRecieved;
	    Message messageRecieved;
	    Packet ackPacket = new Packet(Packet.PacketType.STATETYPE, new Message.NullMessage());
	    
	    LCD.drawString(waiting,0,0);
	    NXTConnection connection = Bluetooth.waitForConnection(); 
	    LCD.clear();
	    LCD.drawString(connected,0,0);

	    
	    while (true) {


	      
	      DataInputStream dis = connection.openDataInputStream();
	      DataOutputStream dos = connection.openDataOutputStream();

	      packetRecieved = Packet.deserialize(dis);
	      messageRecieved = packetRecieved.msg;

	      //Message.NullMessage mess = new Message.NullMessage();
	      //Packet pack = new Packet(Packet.PacketType.STATETYPE, mess);
	      

	      
	      if(messageRecieved.type == Message.MessageType.NULLMSG){
	          LCD.clear();
	          LCD.drawString(messageRecieved.toString(), 0, 0);
	          try {
				dos.write(ackPacket.seralize());
		        dos.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
	          break;
	      } else if(messageRecieved.type == Message.MessageType.MOVE) {
	    	   
	      } else if(messageRecieved.type == Message.MessageType.TURN) {
	    	  
	      } else if(messageRecieved.type == Message.MessageType.STARTQ) {
	    	  
	      } else if(messageRecieved.type == Message.MessageType.ABORTQ) {
	    	  
	      } else if(messageRecieved.type == Message.MessageType.CLEARQ) {
	    	  
	      } else if(messageRecieved.type == Message.MessageType.GOTOS) {
	    	  
	      } else if(messageRecieved.type == Message.MessageType.ITER) {
	    	  
	      } else if(messageRecieved.type == Message.MessageType.PAUSEQ) {
	    	  
	      } else if(messageRecieved.type == Message.MessageType.READR) {
	    	  
	      } else if(messageRecieved.type == Message.MessageType.READS) {
	    	  
	      } else if(messageRecieved.type == Message.MessageType.STEPQ) {
	    	  
	      } 
	      
	      
	      else {
	          LCD.clear();
	          LCD.drawString("NOTNULL", 0, 0);
	      }
	    	  
	      


	      

	    try {
	    	dis.close();
	    	dos.close();
	    	connection.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	    }
	    try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	
	
}

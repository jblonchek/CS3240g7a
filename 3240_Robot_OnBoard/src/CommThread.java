import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


import lejos.nxt.LCD;
import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.NXTConnection;


public class CommThread extends Thread {

	public static boolean end = false;
	
	@Override
	public void run() {

	    String connected = "Connected";
	    String waiting = "Waiting...";
	    String closing = "Closing...";
	    int ackno = 0;
	    Packet packetRecieved;
	    Message messageRecieved = null;;
	    Packet ackPacket = new Packet(Packet.PacketType.STATETYPE, new Message.NullMessage());
	    
	    LCD.drawString(waiting,0,0);
	    NXTConnection connection = Bluetooth.waitForConnection(); 
	    LCD.clear();
	    LCD.drawString(connected,0,0);

	    
	    while (!end) {


	      
	      DataInputStream dis = connection.openDataInputStream();
	      DataOutputStream dos = connection.openDataOutputStream();

	      packetRecieved = Packet.deserialize(dis);
	      if(packetRecieved != null){
	    	  ackno = packetRecieved.seqno;
	          ackPacket = new Packet(Packet.PacketType.COMMANDTYPE, new Message.NullMessage(), Packet.EACKSEQNO, ackno);
	    	  messageRecieved = packetRecieved.msg;
    		  LCD.clear();
    		  LCD.drawString("HI" + ackno, 0, 0);
	    	  try {	
	    		  dos.write(ackPacket.seralize());
	    		  dos.flush();
	    	  } catch (IOException e) {
	    		  e.printStackTrace();
	    	  }
	      } else {
	          ackPacket = new Packet(Packet.PacketType.COMMANDTYPE, new Message.NullMessage(), Packet.ENACKSEQNO, ackno);
    		  LCD.clear();
    		  LCD.drawString("BYE" + ackno, 0, 0);
	          try {	
	        	  dos.write(ackPacket.seralize());
	        	  dos.flush();
	          } catch (IOException e) {
	        	  e.printStackTrace();
	          }
	      }
	      //Message.NullMessage mess = new Message.NullMessage();
	      //Packet pack = new Packet(Packet.PacketType.STATETYPE, mess);
	      
	      
	      
	      if(packetRecieved != null) {
	    	  if(messageRecieved.type == Message.MessageType.NULLMSG){
	    		  LCD.clear();
	    		  LCD.drawString("NULL", 0, 0);
	    		  //break;
	    	  } else if(messageRecieved.type == Message.MessageType.MOVE) {
	    		  LCD.clear();
	    		  LCD.drawString(((Message.MoveMessage)messageRecieved).dist + "", 0, 0);
	    	  } else if(messageRecieved.type == Message.MessageType.TURN) {
	    	  
	    	  } else if(messageRecieved.type == Message.MessageType.STARTQ) {
	    	  
	    	  } else if(messageRecieved.type == Message.MessageType.ABORTQ) {
	    		  break;
	    	  } else if(messageRecieved.type == Message.MessageType.CLEARQ) {
	    	  
	    	  } else if(messageRecieved.type == Message.MessageType.GOTOS) {
	    	  
	    	  } else if(messageRecieved.type == Message.MessageType.ITER) {
	    	  
	    	  } else if(messageRecieved.type == Message.MessageType.PAUSEQ) {
	    	  
	    	  } else if(messageRecieved.type == Message.MessageType.READR) {
	    	  
	    	  } else if(messageRecieved.type == Message.MessageType.READS) {
	    	  
	    	  } else if(messageRecieved.type == Message.MessageType.STEPQ) {
	    	  
	    	  } 
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

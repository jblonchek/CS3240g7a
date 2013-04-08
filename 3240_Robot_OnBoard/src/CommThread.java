import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lejos.nxt.LCD;
import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.NXTConnection;


public class CommThread extends Thread {

	public static boolean end = false;
	Brick lejosBrick=new Brick();
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
					LCD.drawString(messageRecieved.toString(), 0, 0);

					if (messageRecieved.pri.equals(Message.Priority.IMMEDIATE)){
						Message.MoveMessage moveMessage=(Message.MoveMessage) messageRecieved;
						lejosBrick.move(0, moveMessage.dist, 0);
					} else {
						lejosBrick.addToQueue(messageRecieved);
					}
				} else if(messageRecieved.type == Message.MessageType.TURN) {
					LCD.clear();
					LCD.drawString(messageRecieved.toString(), 0, 0);

					if (messageRecieved.pri.equals(Message.Priority.IMMEDIATE)){
						Message.TurnMessage turnMessage=(Message.TurnMessage) messageRecieved;
						lejosBrick.turn(turnMessage.angle);
					} else {
						lejosBrick.addToQueue(messageRecieved);
					}
				} else if(messageRecieved.type == Message.MessageType.STARTQ) {
					LCD.clear();
					LCD.drawString(messageRecieved.toString(), 0, 0);

					lejosBrick.startQueue();
				} else if(messageRecieved.type == Message.MessageType.ABORTQ) {
					LCD.clear();
					LCD.drawString(messageRecieved.toString(), 0, 0);

					lejosBrick.abortQueue();
				} else if(messageRecieved.type == Message.MessageType.CLEARQ) {

					LCD.clear();
					LCD.drawString(messageRecieved.toString(), 0, 0);

					lejosBrick.clearQueue();
				} else if(messageRecieved.type == Message.MessageType.GOTOS) {
					LCD.clear();
					LCD.drawString(messageRecieved.toString(), 0, 0);
				} else if(messageRecieved.type == Message.MessageType.ITER) {
					LCD.clear();
					LCD.drawString(messageRecieved.toString(), 0, 0);
				} else if(messageRecieved.type == Message.MessageType.PAUSEQ) {
					LCD.clear();
					LCD.drawString(messageRecieved.toString(), 0, 0);

					lejosBrick.pauseQueue();
				} else if(messageRecieved.type == Message.MessageType.READR) {
					LCD.clear();
					LCD.drawString(messageRecieved.toString(), 0, 0);

					if (messageRecieved.pri.equals("IMMEDIATE")){
						Message.ReadsMessage readSensorMessage=(Message.ReadsMessage) messageRecieved;

					} else {

						lejosBrick.addToQueue(messageRecieved);
					}
				} else if(messageRecieved.type == Message.MessageType.READS) {

					LCD.clear();
					LCD.drawString(messageRecieved.toString(), 0, 0);
					if()

				} else if(messageRecieved.type == Message.MessageType.STEPQ) {

					LCD.clear();
					LCD.drawString(messageRecieved.toString(), 0, 0);

					lejosBrick.stepQueue();
				} 

			}
		}
	}
}

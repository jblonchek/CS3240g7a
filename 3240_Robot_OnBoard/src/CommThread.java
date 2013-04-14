import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;


import lejos.nxt.Battery;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.NXTConnection;
import lejos.nxt.remote.DeviceInfo;


public class CommThread extends Thread {

	public static boolean end = false;
	Brick lejosBrick=new Brick();
	DataInputStream dis;
	DataOutputStream dos;
	boolean expectAck = false;
	
	LinkedList<Packet> sendQueue = new LinkedList<Packet>(); //Queue<Packet>(); <- doesn't work, interface



	@Override
	public void run() {

		String connected 		= 		"Connected";
		String waiting 			= 		"Waiting...";
		String closing 			= 		"Closing...";
		int ackno 				=		0;
		long timeLastSent 		= 		0;
		long timeLastRecieved 	= 		0;
		Packet packetRecieved;
		Packet packToSend 		= 		null;
		Message messageRecieved =		null;;
		Packet keepAlive;


		LCD.drawString(waiting,0,0);
		NXTConnection connection = Bluetooth.waitForConnection(); 
		LCD.clear();
		LCD.drawString(connected,0,0);

		dis = connection.openDataInputStream();
		dos = connection.openDataOutputStream();

		while (!end) {

			packetRecieved = Packet.deserialize(dis);

			if(expectAck){ //based on Charles last message, we need to change what has expectAck=true, or remove this conditional statement (but keep effects)
				if(packetRecieved == null &&  System.currentTimeMillis() - timeLastSent >= 750 || (packetRecieved.ackno < packToSend.seqno && (packetRecieved.seqno != Packet.EACKSEQNO && packetRecieved.msg.type != Message.MessageType.NULLMSG)) || (packetRecieved.seqno == Packet.ENACKSEQNO && packetRecieved.msg.type == Message.MessageType.NULLMSG)){
					timeLastSent = System.currentTimeMillis();
					try {
						dos.write(packToSend.seralize());
						dos.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					expectAck = false;
				}
			} else if(!sendQueue.isEmpty()){
				packToSend = (Packet) sendQueue.get(0);
				sendQueue.remove(0);
				try {
					dos.write(packToSend.seralize());
				} catch (IOException e) {
					e.printStackTrace();
				}
				timeLastSent = System.currentTimeMillis();
				expectAck = true;
			}



			if(packetRecieved != null) {
				timeLastRecieved = System.currentTimeMillis();
				if(packetRecieved.msg.type != Message.MessageType.NULLMSG && (packetRecieved.seqno != Packet.EACKSEQNO || packetRecieved.seqno != Packet.ENACKSEQNO)){
					sendAckOrNack(packetRecieved);
					ackno = packetRecieved.seqno;
				}
				messageRecieved = packetRecieved.msg;
				if(messageRecieved.type == Message.MessageType.NULLMSG){
//					LCD.clear();
					LCD.drawString("NULL", 0, 0);
					//break;
				} else if(messageRecieved.type == Message.MessageType.MOVE) {
//					LCD.clear();
					LCD.drawString(messageRecieved.toString(), 0, 0);

					if (messageRecieved.pri.equals(Message.Priority.IMMEDIATE)){
						Message.MoveMessage moveMessage=(Message.MoveMessage) messageRecieved;
						lejosBrick.move(0, moveMessage.dist, 0);
					} else {
						lejosBrick.addToQueue(messageRecieved);
					}
				} else if(messageRecieved.type == Message.MessageType.TURN) {
//					LCD.clear();
					LCD.drawString(messageRecieved.toString(), 0, 0);

					if (messageRecieved.pri.equals(Message.Priority.IMMEDIATE)){
						Message.TurnMessage turnMessage=(Message.TurnMessage) messageRecieved;
						lejosBrick.turn(turnMessage.angle);
					} else {
						lejosBrick.addToQueue(messageRecieved);
					}
				} else if(messageRecieved.type == Message.MessageType.STARTQ) {
//					LCD.clear();
					LCD.drawString(messageRecieved.toString(), 0, 0);

					lejosBrick.startQueue();
				} else if(messageRecieved.type == Message.MessageType.ABORTQ) {
//					LCD.clear();
					LCD.drawString(messageRecieved.toString(), 0, 0);

					lejosBrick.abortQueue();
				} else if(messageRecieved.type == Message.MessageType.CLEARQ) {

//					LCD.clear();
					LCD.drawString(messageRecieved.toString(), 0, 0);
					if (messageRecieved.pri.equals(Message.Priority.IMMEDIATE)){
						lejosBrick.clearQueue();
					} else {
						lejosBrick.addToQueue(messageRecieved);
					}
				} else if(messageRecieved.type == Message.MessageType.GOTOS) {
<<<<<<< HEAD
//					LCD.clear();
=======
					
					LCD.clear();
>>>>>>> Error Fixing of Mohammed Queue Work
					LCD.drawString(messageRecieved.toString(), 0, 0);
					lejosBrick.addToQueue(messageRecieved);

				
				} else if(messageRecieved.type == Message.MessageType.ITER) {
<<<<<<< HEAD
//					LCD.clear();
=======
				
					LCD.clear();
>>>>>>> Error Fixing of Mohammed Queue Work
					LCD.drawString(messageRecieved.toString(), 0, 0);
					lejosBrick.addToQueue(messageRecieved);

			
				} else if(messageRecieved.type == Message.MessageType.PAUSEQ) {
<<<<<<< HEAD
//					LCD.clear();
=======
				
					LCD.clear();
>>>>>>> Error Fixing of Mohammed Queue Work
					LCD.drawString(messageRecieved.toString(), 0, 0);
					if (messageRecieved.pri.equals(Message.Priority.IMMEDIATE)){
						lejosBrick.pauseQueue();
					} else {
						lejosBrick.addToQueue(messageRecieved);
					}
				} else if(messageRecieved.type == Message.MessageType.READS) {
//					LCD.clear();
					LCD.drawString(messageRecieved.toString(), 0, 0);

					if (messageRecieved.pri.equals(Message.Priority.IMMEDIATE)){
						Message.ReadsMessage readSensorMessage=(Message.ReadsMessage) messageRecieved;
						enqueuePacketToSend(makeReadSensorPacket(readSensorMessage));

					} else {
						lejosBrick.addToQueue(messageRecieved);
					}
				} else if(messageRecieved.type == Message.MessageType.READR) {

//					LCD.clear();
					LCD.drawString(messageRecieved.toString(), 0, 0);
					if(messageRecieved.pri.equals(Message.Priority.IMMEDIATE)) {
						Message.ReadrMessage readRobotMessage = (Message.ReadrMessage) messageRecieved;
						enqueuePacketToSend(makeReadRoboPacket(readRobotMessage));

					} else { 
						lejosBrick.addToQueue(messageRecieved);
					}

				} else if(messageRecieved.type == Message.MessageType.STEPQ) {

//					LCD.clear();
					LCD.drawString(messageRecieved.toString(), 0, 0);

					lejosBrick.stepQueue();
				} 

			} else if(System.currentTimeMillis() - timeLastRecieved >= 250){
				keepAlive = new Packet(Packet.PacketType.COMMANDTYPE, new Message.NullMessage(), Packet.EACKSEQNO, ackno);		    	  
				LCD.clear();
				LCD.drawString("HI" + ackno, 0, 0);
				try {	
					dos.write(keepAlive.seralize());
					dos.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void enqueuePacketToSend(Packet p){
		sendQueue.add(p);
	}
	
	public Packet makeReadRoboPacket(Message.ReadrMessage readRobotMessage){
		double resp = 0;
		if(readRobotMessage.param == RoboData.BatteryStrength) {
			resp = Battery.getVoltage();
		} else if(readRobotMessage.param == RoboData.Direction)	{
			resp = RoboData.getDirection();
		} else if(readRobotMessage.param == RoboData.DistTraveled)	{
			resp = RoboData.getDistTraveled();
		} else if(readRobotMessage.param == RoboData.FlashMemory)	{
			resp = RoboData.getFlashMemory();
		} else if(readRobotMessage.param == RoboData.FreeMemory)	{
			resp = RoboData.getFlashMemory();//(new DeviceInfo()).freeFlash;
		} else if(readRobotMessage.param == RoboData.SignalStrength)	{
			resp = RoboData.getSignalStrength();//Bluetooth.getSignalStrength((byte)0);
		} else if(readRobotMessage.param == RoboData.Speed)	{
			resp = RoboData.getSpeed();//(Motor.A.getSpeed() + Motor.B.getSpeed() + Motor.C.getSpeed());
		}

		Message.ReadrMessage data = new Message.ReadrMessage(readRobotMessage.param, resp);
		data.pri = Message.Priority.IMMEDIATE;
		Packet dataPack = new Packet(Packet.PacketType.COMMANDTYPE, data);

		return dataPack;

	}

	public Packet makeReadSensorPacket(Message.ReadsMessage readSensorMessage){
		SensorData s = null;
		if(readSensorMessage.s == Sensor.SENSORCS){
			s = new ColorSensorClass(); //We need a dummy value initialized somewhere for this. We can't actually connect a color sensor to the robot even if we have one, so while comm protocol dictates we must be able to respond with updates, we can't do anything meaningful
		} else if(readSensorMessage.s == Sensor.SENSORLS) {
			//s = new LightSensorClass();
		} else if(readSensorMessage.s == Sensor.SENSORSS) {
			//s = new SoundSensorClass();
		} else if(readSensorMessage.s == Sensor.SENSORTS) {
			//s = new TouchSensorClass();
		} else if(readSensorMessage.s == Sensor.SENSORUS) {
			//s = new UltrasonicSensorClass();
		}

		Message.ReadsMessage data = new Message.ReadsMessage(readSensorMessage.s, s.update());
<<<<<<< HEAD
		data.pri=Message.Priority.IMMEDIATE;
		Packet dataPack = new Packet(Packet.PacketType.COMMANDTYPE, data);
		
		try {
			dos.write(dataPack.seralize());
			dos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private Message sendAckOrNack(Packet packetRecieved){
	    int ackno = 0;
	    Packet ackPacket = new Packet(Packet.PacketType.STATETYPE, new Message.NullMessage());
	    Message messageRecieved = null;
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
	      
	      return messageRecieved;
=======
		data.pri = Message.Priority.IMMEDIATE;
		Packet dataPack = new Packet(Packet.PacketType.COMMANDTYPE, data);

		return dataPack;

>>>>>>> Error Fixing of Mohammed Queue Work
	}

	private void sendAckOrNack(Packet packetRecieved){
		int ackno = 0;
		Packet ackPacket = new Packet(Packet.PacketType.STATETYPE, new Message.NullMessage());
		if(packetRecieved != null){
			ackno = packetRecieved.seqno;
			ackPacket = new Packet(Packet.PacketType.COMMANDTYPE, new Message.NullMessage(), Packet.EACKSEQNO, ackno);
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

	}



}
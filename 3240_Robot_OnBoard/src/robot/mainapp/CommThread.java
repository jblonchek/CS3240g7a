package robot.mainapp;


import robot.mainapp.Message.MessageType;
import robot.mainapp.Packet.PacketType;
import robot.sensors.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import lejos.nxt.LCD;
import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.NXTConnection;


public class CommThread extends Thread {

	public static boolean end = false;
	Brick lejosBrick;
	DataInputStream dataInputStream;
	DataOutputStream dataOutputStream;
	boolean expectAck = false;
	long timeLastSent 		= 0;
	int ackno = 0;

	LinkedList<Packet> sendQueue = new LinkedList<Packet>();
	LinkedList<Message> ackQueue = new LinkedList<Message>();
	
	@Override
	public void run() {

		String connected 		= "Connected";
		String waiting 			= "Waiting...";

		
		long timeLastRecieved 	= 0;
		Packet packetRecieved	= null;
		Packet packToSend 		= null;
		Packet ackToSend 		= null;
		Message messageRecieved = null;

		lejosBrick = new Brick(this);
		

		LCD.drawString(waiting, 0, 0);

		LCD.clear();
		LCD.drawString(connected, 0, 0);
		NXTConnection connection;
		
		connection = Bluetooth.waitForConnection();
		dataInputStream  = connection.openDataInputStream();
		dataOutputStream = connection.openDataOutputStream();
		
		//Watch connection until otherwise told
		while (!end) {
			

			
			packetRecieved = Packet.deserialize(dataInputStream);
			try {
				//Send acks if expected or the send queue is not empty. Communicate state otherwise
				if (expectAck) {
					if (packetRecieved == null
							&& System.currentTimeMillis() - timeLastSent >= 750
							|| (packetRecieved.ackno < packToSend.seqno && (packetRecieved.seqno != Packet.EACKSEQNO && packetRecieved.msg.type != Message.MessageType.NULLMSG))
							|| (packetRecieved.seqno == Packet.ENACKSEQNO && packetRecieved.msg.type == Message.MessageType.NULLMSG)) {
					
						timeLastSent = System.currentTimeMillis();
						try {
							dataOutputStream.write(packToSend.seralize());
							dataOutputStream.flush();
						} catch (IOException e) {
							e.printStackTrace();
						}
						
					} else {
						expectAck = false;
					}
				} else if (!ackQueue.isEmpty()) {
					sendAck();
				} else if (!sendQueue.isEmpty()) {
					sendMessage();
				}
			} catch (Exception e) {
				connection = Bluetooth.waitForConnection();
				dataInputStream  = connection.openDataInputStream();
				dataOutputStream = connection.openDataOutputStream();				
			}
			
			if(packetRecieved != null && packetRecieved.type == PacketType.DEBUGCMDTYPE && messageRecieved.type == MessageType.NULLMSG  && packetRecieved.seqno != Packet.EACKSEQNO && packetRecieved.seqno != Packet.ENACKSEQNO) {
				
				if(sendQueue.get(0).type != Packet.PacketType.DEBUGCMDTYPE){
					sendQueue.clear();
					ackno = 0;
				}
				
				messageRecieved = packetRecieved.msg;
				ArrayList<Message> pastCommands = lejosBrick.getPastCommands();
				for(int i = 0; i < pastCommands.size() ; i++){
					sendQueue.add(new Packet(Packet.PacketType.DEBUGCMDTYPE, pastCommands.get(i), Packet.EACKSEQNO, ackno));
				}

			// Handles case of mismatching expected/received ack values
			} else if (packetRecieved != null && packetRecieved.seqno > ackno) {
				timeLastRecieved = System.currentTimeMillis();

				messageRecieved = packetRecieved.msg;
				
				if (packetRecieved.seqno != Packet.EACKSEQNO && packetRecieved.seqno != Packet.EACKSEQNO) {

					ackno = packetRecieved.seqno;

				}

				//decides following actions based on message type; includes clauses for whether it is an immediate action or not
				switch(messageRecieved.type.ordinal()){
				
				case 0:
					
					LCD.clear();
					LCD.drawString("NULL", 0, 0);
					break;
					
				case 1: 
					LCD.clear();
					LCD.drawString(messageRecieved.toString(), 0, 0);
					
					if (messageRecieved.pri.equals(Message.Priority.IMMEDIATE)) {
						Message.MoveMessage moveMessage = (Message.MoveMessage) messageRecieved;
						lejosBrick.move(0, moveMessage.dist, 0);
					} else {
						lejosBrick.addToQueue(messageRecieved);
					}
					break;
					
				case 2:
					LCD.clear();
					LCD.drawString(messageRecieved.toString(), 0, 0);

					if (messageRecieved.pri.equals(Message.Priority.IMMEDIATE)) {
						Message.TurnMessage turnMessage = (Message.TurnMessage) messageRecieved;
						lejosBrick.turn(turnMessage.angle);
					} else {
						lejosBrick.addToQueue(messageRecieved);
					}
					break;
					
				case 4:
					LCD.clear();
					LCD.drawString(messageRecieved.toString(), 0, 0);

					lejosBrick.startQueue();
					break;
					
				case 5:
					LCD.clear();
					LCD.drawString(messageRecieved.toString(), 0, 0);

					lejosBrick.abortQueue();
					break;
					
				case 3:

					LCD.clear();
					LCD.drawString(messageRecieved.toString(), 0, 0);
					
					if (messageRecieved.pri.equals(Message.Priority.IMMEDIATE)) {
						lejosBrick.clearQueue();
					} else {
						lejosBrick.addToQueue(messageRecieved);
					}
					break;
					
				case 8:

					LCD.clear();
					LCD.drawString(messageRecieved.toString(), 0, 0);
					
					lejosBrick.addToQueue(messageRecieved);
					break;
					
				case 10:

					LCD.clear();
					LCD.drawString(messageRecieved.toString(), 0, 0);
					
					lejosBrick.addToQueue(messageRecieved);
					break;

				case 6:

					LCD.clear();
					LCD.drawString(messageRecieved.toString(), 0, 0);
					
					if (messageRecieved.pri.equals(Message.Priority.IMMEDIATE)) {
						lejosBrick.pauseQueue();
					} else {
						lejosBrick.addToQueue(messageRecieved);
					}
					break;
					
				case 9:
					LCD.clear();
					LCD.drawString(messageRecieved.toString(), 0, 0);

					if (messageRecieved.pri.equals(Message.Priority.IMMEDIATE)) {
						Message.ReadsMessage readSensorMessage = (Message.ReadsMessage) messageRecieved;
						enqueuePacketToSend(makeReadSensorPacket(readSensorMessage));

					} else {
						lejosBrick.addToQueue(messageRecieved);
					}
					break;
					
				case 11:

					LCD.clear();
					LCD.drawString(messageRecieved.toString(), 0, 0);
					if (messageRecieved.pri.equals(Message.Priority.IMMEDIATE)) {
						Message.ReadrMessage readRobotMessage = (Message.ReadrMessage) messageRecieved;
						enqueuePacketToSend(makeReadRoboPacket(readRobotMessage));

					} else {
						lejosBrick.addToQueue(messageRecieved);
					}
					break;

				case 7:

					LCD.clear();
					LCD.drawString(messageRecieved.toString(), 0, 0);

					lejosBrick.stepQueue();
					break;
					
				}

				
				
				
				if (packetRecieved.seqno != Packet.EACKSEQNO && packetRecieved.seqno != Packet.EACKSEQNO) {

					ackQueue.add(messageRecieved);

				}

			//Acknowledges a missing packet
			} else if (packetRecieved == null) {

				Packet nackPacket = new Packet(Packet.PacketType.COMMANDTYPE,
					new Message.NullMessage(), Packet.ENACKSEQNO, ackno);
				LCD.clear();
				LCD.drawString(Integer.toString(ackno), 0, 0);
				try {
					dataOutputStream.write(nackPacket.seralize());
					dataOutputStream.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}

			} 
			//Send a packet should the accepted time between packets elapse
			if (System.currentTimeMillis() - timeLastRecieved >= 250) {
				Packet keepAlive = new Packet(Packet.PacketType.COMMANDTYPE,
						new Message.NullMessage(), Packet.EACKSEQNO, ackno);
				LCD.clear();
				LCD.drawString(Integer.toString(ackno), 0, 0);
				try {
					dataOutputStream.write(keepAlive.seralize());
					dataOutputStream.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}



		}
	}

	
	public void enqueuePacketToSend(Packet p) {
		sendQueue.add(p);
	}
	public void enqueueAck(Message m) {
		ackQueue.add(m);
	}

	public Packet makeAckPacket(Message m){
		
		Packet p = new Packet(Packet.PacketType.COMMANDTYPE, m, Packet.EACKSEQNO, ackno);
		return p;
		
	}

	public Packet makeReadRoboPacket(Message.ReadrMessage readRobotMessage) {
		double resp = 0;
		if(readRobotMessage.param == RobotData.BatteryStrength) {
			resp = RobotData.getBatteryStrength();
		} else if(readRobotMessage.param == RobotData.Direction)	{
			resp = lejosBrick.direction%360;
		} else if(readRobotMessage.param == RobotData.DistTraveled)	{
			resp = lejosBrick.distanceTraveled;
		} else if(readRobotMessage.param == RobotData.FlashMemory)	{
			resp = RobotData.getFlashMemory();
		} else if(readRobotMessage.param == RobotData.FreeMemory)	{
			resp = RobotData.getFreeMemory();
		} else if(readRobotMessage.param == RobotData.SignalStrength)	{
			resp = RobotData.getSignalStrength();
		} else if(readRobotMessage.param == RobotData.Speed)	{
			resp=lejosBrick.pilot.getTravelSpeed();
		}


		Message.ReadrMessage data = new Message.ReadrMessage(
				readRobotMessage.param, resp);
		data.pri = Message.Priority.IMMEDIATE;
		Packet dataPack = new Packet(Packet.PacketType.COMMANDTYPE, data);

		return dataPack;

	}


	public Packet makeReadSensorPacket(Message.ReadsMessage readSensorMessage) {
		SensorData s = null;
		if (readSensorMessage.s == Sensor.SENSORCS) {
			// Will always return 0,not connected due to design constraints.
			s = new robot.sensors.ColorSensor();
		} else if (readSensorMessage.s == Sensor.SENSORLS) {
			 s = new robot.sensors.LightSensor();
		} else if (readSensorMessage.s == Sensor.SENSORSS) {
			 s = new robot.sensors.SoundSensor();
		} else if (readSensorMessage.s == Sensor.SENSORTS) {
			 s = new robot.sensors.TouchSensor();
		} else if (readSensorMessage.s == Sensor.SENSORUS) {
			s = new robot.sensors.UltrasonicSensor();
		}

		Message.ReadsMessage data = new Message.ReadsMessage(
				readSensorMessage.s, s.update());
		data.pri = Message.Priority.IMMEDIATE;
		Packet dataPack = new Packet(Packet.PacketType.COMMANDTYPE, data);

		return dataPack;

	}
	
	
	private void sendMessage(){
		Packet packToSend = (Packet) sendQueue.get(0);
		packToSend.ackno = ackno;
		sendQueue.remove(0);
		try {
			dataOutputStream.write(packToSend.seralize());
			dataOutputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		timeLastSent = System.currentTimeMillis();
		expectAck = true;
	}
	

	
	private void sendAck(){
		Packet ackToSend = makeAckPacket(ackQueue.get(0));
		ackQueue.remove(0);
		try {
			dataOutputStream.write(ackToSend.seralize());
			dataOutputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
		
	private void sendPing(){
		Packet keepAlive = new Packet(Packet.PacketType.DEBUGCMDTYPE,
				new Message.NullMessage(), Packet.EACKSEQNO, ackno);

		try {
			dataOutputStream.write(keepAlive.seralize());
			dataOutputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	


}

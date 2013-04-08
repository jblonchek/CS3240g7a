import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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

		dis = connection.openDataInputStream();
		dos = connection.openDataOutputStream();

		while (!end) {

			packetRecieved = Packet.deserialize(dis);
		    messageRecieved = sendAckOrNack(packetRecieved);

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
				} else if(messageRecieved.type == Message.MessageType.READS) {
					LCD.clear();
					LCD.drawString(messageRecieved.toString(), 0, 0);

					if (messageRecieved.pri.equals(Message.Priority.IMMEDIATE)){
						Message.ReadsMessage readSensorMessage=(Message.ReadsMessage) messageRecieved;
						
						respondToReadSensor(readSensorMessage);
						
					} else {
						lejosBrick.addToQueue(messageRecieved);
					}
				} else if(messageRecieved.type == Message.MessageType.READR) {

					LCD.clear();
					LCD.drawString(messageRecieved.toString(), 0, 0);
					if(messageRecieved.pri.equals(Message.Priority.IMMEDIATE)) {
						Message.ReadrMessage readRobotMessage = (Message.ReadrMessage) messageRecieved;

						respondToReadRobo(readRobotMessage);
						
					} else { 
						lejosBrick.addToQueue(messageRecieved);
					}

				} else if(messageRecieved.type == Message.MessageType.STEPQ) {

					LCD.clear();
					LCD.drawString(messageRecieved.toString(), 0, 0);

					lejosBrick.stepQueue();
				} 

			}
		}
	}
	
	
	public void respondToReadRobo(Message.ReadrMessage readRobotMessage){
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
		Packet dataPack = new Packet(Packet.PacketType.COMMANDTYPE, data);
		try {
			dos.write(dataPack.seralize());
			dos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void respondToReadSensor(Message.ReadsMessage readSensorMessage){
		SensorData s = null;
		if(readSensorMessage.s == Sensor.SENSORCS){
			s = new ColorSensorClass();
		} else if(readSensorMessage.s == Sensor.SENSORLS) {
			s = new LightSensorClass();
		} else if(readSensorMessage.s == Sensor.SENSORSS) {
			s = new SoundSensorClass();
		} else if(readSensorMessage.s == Sensor.SENSORTS) {
			s = new TouchSensorClass();
		} else if(readSensorMessage.s == Sensor.SENSORUS) {
			s = new UltrasonicSensorClass();
		}
		
		Message.ReadsMessage data = new Message.ReadsMessage(readSensorMessage.s, s.update());
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
	}
	
	private boolean recieveAckOrNack(Packet pack){
		
		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Packet packetRecieved = Packet.deserialize(dis);
		
	    if(packetRecieved == null){
	    	return false;
	    } else {
	    	if(packetRecieved.msg.type == Message.MessageType.NULLMSG && packetRecieved.ackno == pack.seqno && packetRecieved.seqno == 0){
	    		return true;
	    	} else {
	    		return false;
	    	}
	    }

	}
	
}
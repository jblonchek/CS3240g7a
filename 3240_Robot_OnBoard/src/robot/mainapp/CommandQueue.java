package robot.mainapp;
import java.util.LinkedList;

import robot.sensors.ColorSensor;
import robot.sensors.LightSensor;
import robot.sensors.Sensor;
import robot.sensors.SensorData;
import robot.sensors.SoundSensor;
import robot.sensors.TouchSensor;

import robot.sensors.UltrasonicSensor;


public class CommandQueue extends Thread{


	private LinkedList<Message> queue;
	private int programCounter;
	private boolean paused;
	private CommThread comm;

	// Initializes a new CommandQueue based on an associated CommThread
	// queue defaults to paused at start; only immediate-priority messages
	// and explicit calls to StepQ() will executes pushed commands.
	
	public CommandQueue(CommThread comm) {
		queue = new LinkedList<Message>();
		programCounter = 0;
		paused = true;
		this.comm = comm;
	}

	// Once started, execution runs until the thread is interrupted or a pause is called
	
	public void run() {
		while (!(paused || this.interrupted() || queue.size() > programCounter)) {
			stepQ();
		}
	}

	// Pushes a message into the queue.
	
	public void enqueue(Message message) {
		queue.add(message);

	}

	// Starts normal execution of the queue. Required to remove "paused" flag
	
	public void startQ() {
		paused = false;
		this.start();
	}

	// Interprets and executes a single entry from the queue
	
	public void stepQ() {
		if(queue.size() > programCounter){
			Message curInst = queue.get(programCounter);
			switch(curInst.type.ordinal()){
				case 1:
					comm.lejosBrick.move(0, ((Message.MoveMessage) curInst).dist, 0);
					break;
				
				case 2:
					comm.lejosBrick.turn(((Message.TurnMessage) curInst).angle);
					break;
				
				case 8:
					gotos();
					break;
				
				case 10:
					iterQ();
					break;
				case 6:
					pauseQ();
					break;
				
				case 9:
					comm.enqueuePacketToSend(comm
							.makeReadSensorPacket((Message.ReadsMessage) curInst));
					break;
				
				case 11:
					comm.enqueuePacketToSend(comm
							.makeReadRoboPacket((Message.ReadrMessage) curInst));
					break;
				
				case 3:
					clearQ();
					break;


			}

			// An ack is send post execution
			comm.enqueueAck(curInst);
	
			programCounter++;
		}
	}

	// Pauses the queue. 
	
	public void pauseQ() {
		paused = true;
		try {
			this.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		this.interrupt();
	}
	
	// Aborts the queue. This both pauses execution and clears the queue

	public void abortQ() {
		pauseQ();
		clearQ();

	}

	// Removes all entries from the queue
	
	public void clearQ() {
		queue.clear();
		programCounter = 0;
		paused = true;
	}
	
	// Iterates through a set number of commands in the queue

	public void iterQ() {
		Message.IterMessage message = (Message.IterMessage) queue.get(programCounter);
		int curIterInst = programCounter;
		for (int i = 0; i < message.count; i++) {
			programCounter = (byte) message.destination;
			while (programCounter < curIterInst) {
				stepQ();
			}
		}

	}

	// Jumps to a command if a sensor value threshold is met
	
	public void gotos() {
		Message.GotosMessage message = (Message.GotosMessage) queue.get(programCounter);

		double sdata = getData(message.sensor);
		boolean cmpVal = false;
		
		if (message.compareMode == Message.GotosMessage.Condition.LT) {

			cmpVal = (sdata < message.threshold + message.epsilon) ? true : false;

		} else if (message.compareMode == Message.GotosMessage.Condition.LTE) {

			cmpVal = (sdata <= message.threshold + message.epsilon) ? true : false;

		} else if (message.compareMode == Message.GotosMessage.Condition.EQ) {

			cmpVal = (sdata < message.threshold + message.epsilon && sdata > message.threshold
					- message.epsilon) ? true : false;

		} else if (message.compareMode == Message.GotosMessage.Condition.GTE) {

			cmpVal = (sdata >= message.threshold - message.epsilon) ? true : false;

		} else if (message.compareMode == Message.GotosMessage.Condition.GT) {

			cmpVal = (sdata > message.threshold - message.epsilon) ? true : false;

		} else if (message.compareMode == Message.GotosMessage.Condition.NE) {

			cmpVal = (sdata > message.threshold + message.epsilon || sdata < message.threshold- message.epsilon) ? true : false;

		}

		if (cmpVal) {
			programCounter = (byte) message.destination;
		}

	}

	// Reads a given sensor and returns the detected value. 
	
	private double getData(Sensor sensor) {

		SensorData sdata = null;
		if (sensor == Sensor.SENSORCS) {
			sdata = new ColorSensor(); 
		} else if (sensor == Sensor.SENSORLS) {
			sdata = new LightSensor();
		} else if (sensor == Sensor.SENSORSS) {
			sdata = new SoundSensor();
		} else if (sensor == Sensor.SENSORTS) {
			sdata = new TouchSensor();
		} else if (sensor == Sensor.SENSORUS) {
			sdata = new UltrasonicSensor();
		}

		return sdata.update();

	}
}

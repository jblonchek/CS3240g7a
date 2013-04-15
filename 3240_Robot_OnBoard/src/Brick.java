import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.robotics.navigation.DifferentialPilot;

import java.util.*;

public class Brick {
	CommandQueue 			brickQueue;
	NXTRegulatedMotor 		motorA = Motor.A;
	NXTRegulatedMotor 		motorB = Motor.B;
	NXTRegulatedMotor 		motorC = Motor.C;
	float					wheelDiameter = 56f;
	float					trackWidth = 110f;
	int						averageSpeed = 10;
	String					screenDisplay;
	int						batteryLevel;
	ArrayList<SensorData> 	currentData;
	ArrayList<Message>		commandQueue;

	
	
	DifferentialPilot pilot= new DifferentialPilot(wheelDiameter,trackWidth,motorA,motorC);
	
	public Brick(CommThread thread){
		brickQueue = new CommandQueue(thread);
	}
	
	boolean addToQueue(Message message){
		brickQueue.enqueue(message);
		return true;
		
	}
	
	boolean move(int rotationDegrees, double dist, int speed) {

		pilot.travel(dist*1000);
		pilot.stop();
		return true;
	}
	
	boolean turn(double angle) {
		pilot.rotate(angle);
		return true;
	}
	
	boolean clearQueue() {
		brickQueue.clearQ();
		return false;
	}
	
	boolean startQueue() {
		brickQueue.startQ();
		return false;
	}
	
	boolean abortQueue() {
		brickQueue.abortQ();
		return false;
	}
	
	boolean pauseQueue() {
		brickQueue.pauseQ();
		return false;
	}
	
	boolean stepQueue() {
		brickQueue.stepQ();
		return false;
	}
	
	boolean goToStep() {
		brickQueue.gotos();
		return false;
	}
}
	

import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.robotics.navigation.DifferentialPilot;

import java.util.*;

public class Brick {
	ArrayList<Message> 		brickQueue= new ArrayList<Message>();
	NXTRegulatedMotor 		motorA=Motor.A;
	NXTRegulatedMotor 		motorB=Motor.B;
	NXTRegulatedMotor 		motorC=Motor.C;
	ArrayList<SensorData> 	currentData;
	String					screenDisplay;
	int						batteryLevel;
	ArrayList<Message>		commandQueue;
	float					wheelDiameter=56f;
	float					trackWidth=110f;
	int						averageSpeed=10;
	DifferentialPilot pilot= new DifferentialPilot(
								wheelDiameter,trackWidth,motorA,motorC
								);
	//test three
	
	boolean addToQueue(Message message){
		brickQueue.add(message);
		return true;
		
	}
	
	boolean move(int rotationDegrees, double dist, int speed) {
//		if (speed!=0){
//			pilot.setTravelSpeed(speed);
//		}
//		pilot.rotate(rotationDegrees);
		pilot.travel(dist*1000);
		pilot.stop();
//		pilot.setTravelSpeed(averageSpeed);
		
		return true;
	}
	
	boolean turn(double angle) {
		pilot.rotate(angle);
		return true;
	}
	
	boolean clearQueue() {
		return false;
	}
	
	boolean startQueue() {
		return false;
	}
	
	boolean abortQueue() {
		return false;
	}
	
	boolean pauseQueue() {
		return false;
	}
	
	boolean stepQueue() {
		return false;
	}
	
	boolean goToStep() {
		return false;
	}
	
	boolean readSensor() {
		return false;
	}
	
	boolean iterate() {
		return false;
	}
	
	boolean readRobotData() {
		return false;
	}
	
	boolean updateSensorData(SensorData sensor) {
		return false;
	}
	
	boolean displayToScreen() {
		return false;
	}
}
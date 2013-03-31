import lejos.nxt.Motor;

import java.util.*;

public class Brick {
	Motor 					motorA;
	Motor 					motorB;
	Motor 					motorC;
	ArrayList<SensorData> 	currentData;
	String					screenDisplay;
	int						batteryLevel;
	ArrayList<Message>		commandQueue;
	
	
	void executeCommand() {
		
	}
	
	boolean move(int rotationDegrees, int distance, int speed) {
		return false;
	}
	
	boolean turn(int rotationDegrees) {
		return false;
	}
	
	boolean clear() {
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
	
	boolean readStep() {
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
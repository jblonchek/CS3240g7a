package robot.mainapp;

import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.robotics.navigation.DifferentialPilot;

import java.util.*;

import robot.sensors.SensorData;

public class Brick {

	final int maxBufferSize = 20;

	CommandQueue brickQueue;
	NXTRegulatedMotor motorA = Motor.A;
	NXTRegulatedMotor motorB = Motor.B;
	NXTRegulatedMotor motorC = Motor.C;
	final float wheelDiameter = 56f;
	final float trackWidth = 110f;
	int averageSpeed = 10;
	String screenDisplay;
	int batteryLevel;
	ArrayList<SensorData> currentData;
	ArrayList<Message> pastCommands;
	

	int distanceTraveled = 0;
	int direction = 0;

	
	public ArrayList<Message> getPastCommands() {
		return pastCommands;
	}
	
	/**
	 * Internal NXT class used for all movement based commands
	 */

	DifferentialPilot pilot = new DifferentialPilot(wheelDiameter, trackWidth,
			motorA, motorC);

	/**
	 * Creates the Brick object to be used in CommThread. The Brick is matched
	 * with a CommandQueue at initialization
	 * 
	 * @param thread
	 */

	public Brick(CommThread thread) {
		brickQueue = new CommandQueue(thread);
		pastCommands = new ArrayList<Message>();
	}

	/**
	 * Adds any received message to the current CommandQueue
	 * 
	 * @param message
	 * @return
	 */

	boolean addToQueue(Message message) {
		brickQueue.enqueue(message);
		pastCommands.add(message);
		if (pastCommands.size() > maxBufferSize) {
			pastCommands.remove(pastCommands.size() - 1);
		}
		return true;

	}

	/**
	 * Executes robot movement and notes distance traveled
	 * 
	 * @param rotationDegrees
	 * @param dist
	 * @param speed
	 * @return
	 */

	void move(int rotationDegrees, double dist, int speed) {

		pilot.travel(dist * 1000);
		pilot.stop();
		distanceTraveled += Math.abs(dist * 1000);
	}

	/**
	 * Turns robot based on inputed angle. Can handle negative values
	 * 
	 * @param angle
	 * @return
	 */

	void turn(double angle) {
		pilot.rotate(angle);
		direction += angle;
	}

	/**
	 * Empties CommandQueue
	 * 
	 * @return
	 */

	void clearQueue() {
		brickQueue.clearQ();
	}

	/**
	 * Starts the CommandQueue running
	 * 
	 * @return
	 */

	void startQueue() {
		brickQueue.startQ();
	}

	/**
	 * Cancels all current actions on CommandQueue and pauses any further
	 * operation
	 * 
	 * @return
	 */

	void abortQueue() {
		brickQueue.abortQ();
	}

	/**
	 * Pauses CommandQueue execution
	 * 
	 * @return
	 */

	void pauseQueue() {
		brickQueue.pauseQ();
	}

	/**
	 * Executes a single entry from the CommandQueue
	 * 
	 * @return
	 */

	void stepQueue() {
		brickQueue.stepQ();
	}

	/**
	 * Jumps to a given command if a sensor value matches a given threshold
	 * 
	 * @return
	 */

	void goToStep() {
		brickQueue.gotos();
	}
}

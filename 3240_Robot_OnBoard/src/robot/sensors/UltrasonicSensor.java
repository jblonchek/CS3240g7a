package robot.sensors;
import java.util.ArrayList;
import lejos.nxt.*;

public class UltrasonicSensor implements SensorData{

	/**
	 * Variable to scale readings from 0.0-1.0
	 */
	final double scaleVariable=255.0;
	

	/**
	 * Port of the Ultrasonic sensor on the brick
	 */
	final SensorPort ultrasonicSensorPort=SensorPort.S1;
	
	/**
	 * Acceptable range for UltraSonic Sensor to be between 0-range.
	 */
	int range;
	
	/**
	 * This ArrayList stores all past readings of the sound sensor class 
	 * retrieved by update()
	 */
	ArrayList<Double> pastReadings;
	lejos.nxt.UltrasonicSensor sonic=new lejos.nxt.UltrasonicSensor(ultrasonicSensorPort); 

	/**
	 * Reads the distance from the Ultrasonic sensor to a given object in front
	 * Function returns a scaled value between 0-1.0
	 */

	@Override
	public double update() {
		double dist = sonic.getDistance()/scaleVariable;
		pastReadings.add(dist);
		return dist;
	}

}

package robot.sensors;
import lejos.nxt.ColorSensor.Color;

/**
 * The color sensor is unused on the current robot configuration
 * Color sensor exists as expandable skeleton code to match specification
 */

public class ColorSensor implements SensorData{

	Color color;
	
	/**
	 * Update retrieves the value of the Sensor at that time.
	 * @Override
	 */
	
	public double update() {
		return 0;
	}

}

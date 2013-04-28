package robot.sensors;

/**
 * Interface for all of the specific sensor classes
 * update() return the value the sensor is currently detecting in addition to adding it to the list of previous readings
 * Expected range of return value is based on individual sensor
 */
public interface SensorData {
	double update();
}
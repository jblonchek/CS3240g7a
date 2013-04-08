import java.util.ArrayList;
import lejos.nxt.*;

public class UltrasonicSensorClass implements SensorData{

	int range;
	double currentReading;
	ArrayList<Double> pastReadings;
	UltrasonicSensor sonic=new UltrasonicSensor(SensorPort.S1);
	@Override
	public double update() {
		//int[] data = sonic.ping();
		//Thread.sleep(25);
		double dist = sonic.getDistance()/255;
		pastReadings.add(currentReading);
		currentReading = dist;
		return dist;
	}

	boolean calibrate(){
		return false;
	}
}

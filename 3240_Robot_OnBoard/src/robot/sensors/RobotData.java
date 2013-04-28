package robot.sensors;

import lejos.nxt.Battery;
import lejos.nxt.NXT;
import lejos.nxt.comm.Bluetooth;

public enum RobotData {
	FlashMemory,
	FreeMemory,
	BatteryStrength,
	SignalStrength,
	Speed,
	Direction,
	DistTraveled;

	/**
	 * Method gets the available flash memory of the system 
	 * @return
	 */
	public static double getFlashMemory() {
		return NXT.getUserPages();
	}
	
	/**
	 * Method returns the signal strength of the bluetooth connection
	 * ranging from 0.0-1.0, 1 being full signal strength
	 * @return
	 */
	public static double getSignalStrength() {
		
		return Bluetooth.getSignalStrength((byte) 0)/256.0;
	}
	
	/**
	 * Method returns the battery strength of the brick 
	 * in terms of voltage
	 * @return
	 */
	public static double getBatteryStrength(){
		return Battery.getVoltage();
	}
	
	/**
	 * Method returns the overall free memory available on the brick.
	 * @return
	 */
	public static double getFreeMemory(){
		return System.getRuntime().freeMemory();
	}
	

}

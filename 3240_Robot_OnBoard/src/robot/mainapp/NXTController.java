package robot.mainapp;

import lejos.nxt.Button;

/**
 *  Class is used as the starting point that kicks off
 *  execution threads for program functionality.
 *  Start a new CommThread to begin program. If Debug flag set, a
 *  special version of CommThread is initialized that performs debugging
 *  functions.
 */
public class NXTController {

	/**
	 * Describes whether or not the system is in debug mode
	 */
	final static boolean DEBUG = false;

	public static void main(String[] args) {

			CommThread commThread = new CommThread();
			CommThread.end = false;
			commThread.start();

			Button.waitForAnyPress();
			CommThread.end = true;

		}

	}




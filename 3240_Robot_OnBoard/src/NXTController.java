import lejos.nxt.Button;

public class NXTController {

	final static boolean DEBUG = false;
	
	public static void main(String[] args) {
		
		//Start a new Comm Thread to begin program.
		
		if(DEBUG){
			//DebugCommThread commThread = new CommThread();
			//DebugCommThread.end = false;
//			commThread.start();
//			Button.waitForAnyPress();	
//			CommThread.end = true;
			
		}else{
			CommThread commThread = new CommThread();
			CommThread.end = false;
			commThread.start();
			
			Button.waitForAnyPress();
			
			CommThread.end = true;
			
		}
		

	}

}

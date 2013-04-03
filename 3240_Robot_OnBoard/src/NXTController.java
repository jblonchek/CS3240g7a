import lejos.nxt.Button;

public class NXTController {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//System.out.println("Hello World");
		//Button.waitForAnyPress();
		
		CommThread commThread = new CommThread();
		CommThread.end = false;
		commThread.start();
		
		Button.waitForAnyPress();
		
		CommThread.end = true;
		

	}

}

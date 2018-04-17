package appcr.controller.main;

import coppelia.remoteApi;
import appcr.controller.UAV.Quadricopter;

/**
 * 
 * @author miguelgarse
 *
 */
public class Orchestrator {
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		float stepSize = (float)0.035;
		float angleStepSize = (float)0.1;
		int sleepTime = 80;
		double batteryLevel = 100;
		
		Thread drone_1 = new Thread(new Quadricopter((long)1,"Quadricopter_target", batteryLevel, stepSize, angleStepSize, sleepTime));
//		Thread drone_2 = new Thread(new Quadricopter((long)2,"Quadricopter_target#0", batteryLevel, stepSize, angleStepSize, sleepTime));
		
		drone_1.start();
//		drone_2.start();
		
		while(true){
			
			
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
}

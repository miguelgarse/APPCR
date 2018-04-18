package appcr.controller.main;

import coppelia.remoteApi;

import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

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
		Queue<String> messages = new ConcurrentLinkedQueue<String>();
		Map<Long, Integer> actions = new ConcurrentHashMap<Long, Integer> ();
//		Map<Long, String> messages = new ConcurrentHashMap<Long, String> ();
		
		float stepSize = (float)0.035;
		float angleStepSize = (float)0.1;
		int sleepTime = 80;
		double batteryLevel = 100;
		
		Thread drone_1 = new Thread(new Quadricopter((long)1,"Quadricopter_target", batteryLevel, stepSize, angleStepSize, sleepTime, actions, messages));
//		Thread drone_2 = new Thread(new Quadricopter((long)2,"Quadricopter_target#0", batteryLevel, stepSize, angleStepSize, sleepTime));
		
		drone_1.start();
//		drone_2.start();
		
		while(true){
			String message;
			
			while( (message = messages.poll()) != null){
				
				System.out.println("[Message: " + message + " ]  " + messages.size());
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
}

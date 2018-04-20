package appcr.controller.main;

import coppelia.FloatWA;
import coppelia.IntW;
import coppelia.remoteApi;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import appcr.controller.UAV.Quadricopter;
import appcr.controller.communication.Message;
import appcr.controller.communication.Order;

/**
 * 
 * @author Miguel García
 * @version 1.0
 *
 */
public class Orchestrator {
	private static final Rectangle2D.Float delimited_area = new Rectangle2D.Float((float)-2.1250, (float)5.8250, (float)10, (float)15);
	private static remoteApi vrep;
	private static int clientID;
	/**
	 * 
	 * @param args Received arguments from the command line. It is not used.
	 */
	public static void main(String[] args) {
		Queue<Message> messages = new ConcurrentLinkedQueue<Message>();
		Queue<Order> actions = new ConcurrentLinkedQueue<Order>();
//		Map<Long, String> messages = new ConcurrentHashMap<Long, String> ();
		
		float stepSize = (float)0.035;
		float angleStepSize = (float)0.1;
		int sleepTime = 10;
		double batteryLevel = 100;
		
		vrep = new remoteApi();
		vrep.simxFinish(-1); // just in case, close all opened connections
		clientID = vrep.simxStart("127.0.0.1", 19999, true, true, 50000, 5);
		
		Thread drone_1 = new Thread(new Quadricopter((long)1,"Quadricopter_target", clientID, vrep, batteryLevel, stepSize, angleStepSize, sleepTime, actions, messages));
//		Thread drone_2 = new Thread(new Quadricopter((long)2,"Quadricopter_target#0", batteryLevel, stepSize, angleStepSize, sleepTime));
		
		drone_1.start();
//		drone_2.start();
		
		while(true){
			Message message;
			
			if(!delimited_area.contains(getAnimalPosition("Escaped_Cow"))) {	//The animal has escaped
				
			}
			
			while( (message = messages.poll()) != null){
				
				System.out.println("[" + message.getTimeStamp() + "] [ " + message.getQuadricopIden() + " ] >> " + message.getMessage());
			}
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private static Point2D.Float getAnimalPosition(String animalID) {

		IntW targetOBJ = new IntW(1);
		vrep.simxGetObjectHandle(clientID, animalID, targetOBJ, remoteApi.simx_opmode_blocking);

		FloatWA position = new FloatWA(3);
		vrep.simxGetObjectPosition(clientID, targetOBJ.getValue(), -1, position, remoteApi.simx_opmode_blocking);
		float posArray[] = position.getArray();

		return new Point2D.Float(posArray[0], posArray[1]);
	}
}

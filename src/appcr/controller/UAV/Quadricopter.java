package appcr.controller.UAV;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ThreadLocalRandom;

import appcr.controller.communication.Message;
import appcr.controller.communication.Order;
import appcr.controller.main.Constants;
import coppelia.BoolW;
import coppelia.FloatWA;
import coppelia.IntW;
import coppelia.remoteApi;

public class Quadricopter implements Runnable{
	private String id_target;
	private float lastAltitude = 0;
	private long id;
	private int clientID;
	private remoteApi vrep;
	private double batteryLevel;
	private float stepSize;
	private float angleStepSize = (float)0.1;
	private int sleepTime;
	private List<Point2D> pointsList;
	private Point2D.Float actual_animal_position;
	private Queue<Order> actions;
	private Queue<Message> messages;

	boolean detected = false;
	boolean detected_1 = false;
	boolean obstacle_passed = false;
	boolean obstacle_found = false;

	private int estado = Constants.ON_ROTATION;

	/**
	 * 
	 * @param id
	 * @param id_target
	 * @param clientID
	 * @param batteryLevel
	 * @param stepSize
	 * @param angleStepSize
	 * @param sleepTime
	 */
	public Quadricopter(long id, String id_target, int clientID, remoteApi vrep, double batteryLevel, float stepSize, float angleStepSize, int sleepTime, Queue<Order> actions, Queue<Message> messages) {
		this.id = id;
		this.id_target = id_target;
		this.clientID = clientID;
		this.vrep = vrep;
		this.batteryLevel = batteryLevel;
		this.stepSize = stepSize;
		this.angleStepSize = angleStepSize;
		this.sleepTime = sleepTime;
		this.actions = actions;
		this.messages = messages;
		
		this.messages.add(new Message(id_target,"Drone " + id + " started"));
		
		if (clientID != -1) {
			this.messages.add(new Message(id_target,"Connected to remote API server"));
		} else {
			this.messages.add(new Message(id_target,"Cannot connect to remote API server"));
		}
	}

	/**
	 * Método especificado por la clase Runnable
	 */
	@Override
	public void run() {

		// Manejador de la bola
		IntW targetOBJ = new IntW(1);
		int ret = vrep.simxGetObjectHandle(clientID, id_target, targetOBJ, remoteApi.simx_opmode_blocking);

		if (ret == remoteApi.simx_return_ok) {
			this.messages.add(new Message(id_target,"Getting Target Handle (OK)\n"));

			// Obtener posici�n actual
			FloatWA position = new FloatWA(3);
			ret = vrep.simxGetObjectPosition(clientID, targetOBJ.getValue(), -1, position,
					remoteApi.simx_opmode_blocking);
			float posArray[] = position.getArray();
			this.messages.add(new Message(id_target,"X: " + posArray[0] + " Y: " + posArray[1] + " Z: " + posArray[2]));

			// Obtener orientaci�n actual
			FloatWA orientationAngles = new FloatWA(3);
			ret = vrep.simxGetObjectOrientation(clientID, targetOBJ.getValue(), -1, orientationAngles,
					remoteApi.simx_opmode_blocking);
			float orientationArray[] = orientationAngles.getArray();
			this.messages.add(new Message(id_target,"Alpha: " + orientationArray[0] + " Beta: " + orientationArray[1] + " Gamma: " + orientationArray[2]));

			// Ejemplo Sensor de proximidad 1
			IntW sensorProximidad = new IntW(1);
			ret = vrep.simxGetObjectHandle(clientID, "Sensor_proximidad", sensorProximidad,	remoteApi.simx_opmode_oneshot_wait);
			
			if (ret == remoteApi.simx_return_ok) {
				this.messages.add(new Message(id_target,"Proximity sensor ID: " + sensorProximidad.getValue()));
			} else {
				this.messages.add(new Message(id_target,"Cannot get proximity sensor handle"));
			}

			// variables usadas en la lectura del sensor de proximodad
			BoolW detectionState = new BoolW(false);
			FloatWA detectedPoint = new FloatWA(0);
			IntW detectedObjectHandle = new IntW(0);
			FloatWA detectedSurfaceNormalVector = new FloatWA(0);

			ret = vrep.simxReadProximitySensor(clientID, sensorProximidad.getValue(), detectionState, detectedPoint,
					detectedObjectHandle, detectedSurfaceNormalVector, remoteApi.simx_opmode_streaming);
			if (ret != remoteApi.simx_return_ok) {
				this.messages.add(new Message(id_target,"Cannot get proximite sensor read (STREAM)"));
			}

			// Ejemplo Sensor de proximidad 2
			IntW sensorProximidad_1 = new IntW(1);
			ret = vrep.simxGetObjectHandle(clientID, "Sensor_proximidad_1", sensorProximidad_1, remoteApi.simx_opmode_oneshot_wait);
			if (ret == remoteApi.simx_return_ok) {
				this.messages.add(new Message(id_target,"Proximity sensor ID: " + sensorProximidad_1.getValue()));
			} else {
				this.messages.add(new Message(id_target,"Cannot get proximity sensor handle"));
			}

			// variables usadas en la lectura del sensor de proximodad
			BoolW detectionState_1 = new BoolW(false);
			FloatWA detectedPoint_1 = new FloatWA(0);
			IntW detectedObjectHandle_1 = new IntW(0);
			FloatWA detectedSurfaceNormalVector_1 = new FloatWA(0);

			ret = vrep.simxReadProximitySensor(clientID, sensorProximidad_1.getValue(), detectionState_1, detectedPoint_1, detectedObjectHandle_1, detectedSurfaceNormalVector_1, remoteApi.simx_opmode_streaming);
			if (ret != remoteApi.simx_return_ok) {
				this.messages.add(new Message(id_target,"Cannot get proximite sensor read (STREAM)"));
			}

			// Valores iniciales de los movimientos
			float X_StepFactor = stepSize;
//			float yStep = (float) 0;
			float Z_StepFactor = (float) 0;

			actual_animal_position = getAnimalPosition("Escaped_Cow");
			pointsList = getListPoints(getAnimalPosition("Escaped_Cow"), new Point2D.Float(posArray[0], posArray[1]));

//			for (int i = 0; i < pointsList.size(); i++) {
//				System.out.println(pointsList.get(i));
//			}

			int point = 0;

			while (true) {
				
//				System.out.println("Orientation: " + orientationArray[2] + " Angle: " + getAngle(new Point2D.Float(posArray[0], posArray[1]), getAnimalPosition("Escaped_Cow")));
				
				if (!getAnimalPosition("Escaped_Cow").equals(actual_animal_position)) { 
					// Comprobamos si el animal se ha movido para recalcular la ruta
					
					if(orientationArray[2] != getAngle(new Point2D.Float(posArray[0], posArray[1]), getAnimalPosition("Escaped_Cow"))) {
						estado = Constants.ON_ROTATION;
					}
					actual_animal_position = getAnimalPosition("Escaped_Cow");
					pointsList = getListPoints(getAnimalPosition("Escaped_Cow"), new Point2D.Float(posArray[0], posArray[1]));
					point = 0;
				}

				ret = vrep.simxReadProximitySensor(clientID, sensorProximidad.getValue(), detectionState, detectedPoint,
						detectedObjectHandle, detectedSurfaceNormalVector, remoteApi.simx_opmode_buffer);
				if (ret != remoteApi.simx_return_ok) {
					this.messages.add(new Message(id_target,"Cannot get proximite sensor read"));
				}

				// valor booleano que indica si se detecta un ejemplo en el rango del sensor
				if (detectionState != null)
					detected = detectionState.getValue();

				ret = vrep.simxReadProximitySensor(clientID, sensorProximidad_1.getValue(), detectionState_1, detectedPoint_1, detectedObjectHandle_1, detectedSurfaceNormalVector_1, remoteApi.simx_opmode_streaming);
				if (ret != remoteApi.simx_return_ok)
					this.messages.add(new Message(id_target,"Cannot get proximite sensor read (STREAM)"));
				
				
				if (detectionState_1 != null) {
					detected_1 = detectionState_1.getValue();
				}

				this.messages.add(new Message(id_target,"Estado actual: " + Constants.getState(estado)));
				
				switch (estado) {
					case Constants.GO_AHEAD:
						Z_StepFactor = 0;
						X_StepFactor = 1;
	
						if (detected_1)
							estado = Constants.GO_UP;
	
						if (detected)
							estado = Constants.PASS_OBSTACLE;
	
						break;
					case Constants.GO_UP:
						Z_StepFactor = 1;
						X_StepFactor = 0;
						
						if (!detected_1)
							estado = Constants.GO_AHEAD;
						
						break;
					case Constants.PASS_OBSTACLE:
						if (detected) {
							X_StepFactor = 0;
							Z_StepFactor = 1;
							
							switch(ThreadLocalRandom.current().nextInt(1, 3 + 1)) {
								case 1:	//Pass the obstacle over it
									
									break;
								case 2:	//Pass the obstacle from the right
									
									break;
								case 3:	//Pass the obstacle from the left
									
									break;
							}
							
						}
						
						if (!detected) {
							if (detected_1) {
								obstacle_found = true;
								this.messages.add(new Message(id_target,"The obstacle has been found"));
								
								if( lastAltitude==0 )
									lastAltitude = posArray[2];
								
							} else if (!detected_1 && obstacle_found) {
								obstacle_passed = true;
								this.messages.add(new Message(id_target,"The obstacle has been passed"));
							}
	
							if (obstacle_passed) { // Empieza a bajar
								estado = Constants.GO_DOWN;
								obstacle_passed = false;
								obstacle_found = false;
							} else { // avanza
								Z_StepFactor = 0;
								X_StepFactor = 1;
							}
						}
						
						break;
					case Constants.GO_DOWN:
						Z_StepFactor = -1;
						X_StepFactor = 0;
	
						if (detected_1 && posArray[2] < lastAltitude) {
							estado = Constants.GO_AHEAD;
							lastAltitude = 0;
						}
	
						break;
					case Constants.ON_ROTATION:
						this.messages.add(new Message(id_target,orientationArray[2] + " -- " + getAngle(new Point2D.Float(posArray[0], posArray[1]), getAnimalPosition("Escaped_Cow"))));
						if ( Math.abs((double) orientationArray[2] - getAngle(new Point2D.Float(posArray[0], posArray[1]), getAnimalPosition("Escaped_Cow"))) < angleStepSize){
							X_StepFactor = 1;
	
							estado = Constants.GO_AHEAD;
						} else {
							// Especificar nuevo angulo
							X_StepFactor = 0;
							
							orientationArray[2] = (float) getAngle(new Point2D.Float(posArray[0], posArray[1]), getAnimalPosition("Escaped_Cow"));
							ret = vrep.simxSetObjectOrientation(clientID, targetOBJ.getValue(), -1, orientationAngles, remoteApi.simx_opmode_blocking);
						}
	
						break;
				}

				// Especificar nueva posición
				if (point < pointsList.size() && X_StepFactor != 0) {
					posArray[0] = (float) pointsList.get(point).getX();
					posArray[1] = (float) pointsList.get(point).getY();
					point++;
				}
				posArray[2] += Z_StepFactor*stepSize;
				ret = vrep.simxSetObjectPosition(clientID, targetOBJ.getValue(), -1, position, remoteApi.simx_opmode_blocking);


				// Paramos el bucle unos milisegundos
				try {
					Thread.sleep(sleepTime); // velocidad del tick
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
			} // while

		} // if

		// Now close the connection to V-REP:
		this.messages.add(new Message(id_target,"Closing connection with V-REP"));
		vrep.simxFinish(clientID);
	}

	/**
	 * In this method we define all the possible movements that the drone can make
	 * in order to separate the logic of the movement from the logic of the
	 * adaptability
	 * 
	 * @param moving
	 * @param posArray
	 * @param orientationArray
	 */
	// private void move(int moving, float posArray[], float orientationArray[]) {
	//
	// switch(moving) {
	// case Constants.ON_MOVE:
	// posArray[0] += stepSize;
	// //posArray[1] += yStep;
	// vrep.simxSetObjectPosition(clientID, targetOBJ.getValue(), -1, position,
	// vrep.simx_opmode_blocking );
	//
	// break;
	// case Constants.ON_MOVE_BACKWARD:
	// posArray[0] -= stepSize;
	// //posArray[1] -= yStep;
	// vrep.simxSetObjectPosition(clientID, targetOBJ.getValue(), -1, position,
	// vrep.simx_opmode_blocking );
	//
	// break;
	//
	// case Constants.ON_ROTATION:
	// orientationArray[2] += angleStep;
	// vrep.simxSetObjectOrientation(clientID, targetOBJ.getValue(), -1,
	// orientationAngles, vrep.simx_opmode_blocking );
	//
	// break;
	// }
	// }

	/**
	 * 
	 * @return
	 */
	private Point2D.Float getAnimalPosition(String animalID) {

		IntW targetOBJ = new IntW(1);
		vrep.simxGetObjectHandle(clientID, animalID, targetOBJ, remoteApi.simx_opmode_blocking);

		FloatWA position = new FloatWA(3);
		vrep.simxGetObjectPosition(clientID, targetOBJ.getValue(), -1, position, remoteApi.simx_opmode_blocking);
		float posArray[] = position.getArray();
//		System.out.println("Animal position => X: " + posArray[0] + " Y: " + posArray[1] + " Z: " + posArray[2]);

		return new Point2D.Float(posArray[0], posArray[1]);
	}

	/**
	 * 
	 * @param animalPosition
	 * @param dronePosition
	 * @return
	 */
	private List<Point2D> getListPoints(Point2D animalPosition, Point2D dronePosition) {
		this.messages.add(new Message(id_target,"Calculating Path"));
		List<Point2D> pointsList = new ArrayList<Point2D>();
		float x1, y1, x2, y2, x3 = 0, y3 = 0;
		float d = stepSize;
		x1 = (float) dronePosition.getX();
		y1 = (float) dronePosition.getY();
		x2 = (float) animalPosition.getX();
		y2 = (float) animalPosition.getY();
		float D = (float) Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));

		while( D > 2 * d) {
			x3 = x1 + ((d / D) * (x2 - x1));
			y3 = y1 + ((d / D) * (y2 - y1));
			// System.out.println("X: " + x3 + "Y: " + y3);

			pointsList.add(new Point2D.Float(x3, y3));

			x1 = x3;
			y1 = y3;
			D = (float) Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));

		}

		return pointsList;
	}

	/**
	 * 
	 * @param p1 Posicion del drone
	 * @param p2 Posicion del animal
	 * @return
	 */
	public double getAngle(Point2D p1, Point2D p2) {
		double pendiente = (p2.getY() - p1.getY()) / (p2.getX() - p1.getX()); // Calculamos la pendiente de la recta
//		double angulo = (double) Math.toDegrees(Math.atan(pendiente)); // Pasamos la pendiente a la arcotangente para que nos de el angulo

		double angulo = Math.atan(pendiente); // Pasamos la pendiente a la arcotangente para que nos de el angulo
		
		if((p2.getX() - p1.getX()) < 0) {
			angulo = angulo + (Math.PI);
		}
		
		return angulo;
	}
}


package appcr.controller.communication;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 * 
 * @author Miguel García
 * @version 1.0
 *
 */
public class Order {
	
	private int iden_message;
	private String quadricopter;
	private String message;
	private String time;
	private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss:SSS");
	
	/**
	 * Constructor of the message class.
	 */
	public Order (String quadricopter, String message, int iden_message){
		this.quadricopter = quadricopter;
		this.message = message;
		this.iden_message = iden_message;
		
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		time = sdf.format(timestamp);
	}
	
	/**
	 * Second constructor of the message class.
	 */
	public Order (String quadricopter, String message){
		this.quadricopter = quadricopter;
		this.message = message;
		
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		time = sdf.format(timestamp);
	}
	
	/**
	 * 
	 * @return It returns an integer related to the identification of the message.
	 */
	public int getIdenMensaje (){
		return this.iden_message;
	}
	
	/**
	 * 
	 * @return It returns a String related to the identification of the Quadricopter that has created this message.
	 */
	public String getQuadricopIden (){
		return this.quadricopter;
	}
	
	/**
	 * 
	 * @return It returns a String that contains the sequence of the message.
	 */
	public String getMessage (){
		return this.message;
	}
	
	/**
	 * 
	 * @return It returns a String that contains the sequence of the message.
	 */
	public String getTimeStamp (){
		return this.time;
	}
}

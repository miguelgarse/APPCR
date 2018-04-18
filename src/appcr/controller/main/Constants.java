package appcr.controller.main;

public class Constants {
	public static final int GO_AHEAD = 1;
	public static final int GO_UP = 2;
	public static final int GO_DOWN = 3;
	public static final int PASS_OBSTACLE = 4;
	public static final int ON_MOVE = 5;
	public static final int ON_MOVE_BACKWARD = 6;
	public static final int ON_ROTATION = 7;
	
	/**
	 * 
	 * @param state
	 * @return
	 */
	static public String getState(int state) {
		String state_string = "";
		switch(state) {
		case 1:
			state_string = "GO_AHEAD";
			break;
		case 2:
			state_string = "GO_UP";
			break;
		case 3:
			state_string = "GO_DOWN";
			break;
		case 4:
			state_string = "PASS_OBSTACLE";
			break;
		case 5:
			state_string = "ON_MOVE";
			break;
		case 6:
			state_string = "ON_MOVE_BACKWARD";
			break;
		case 7:
			state_string = "ON_ROTATION";
			break;
		
		}
		
		return state_string;
	}
}

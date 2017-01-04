package testingrobot;

import battlecode.common.*;

public class RobotPlayer {
	static RobotController rc; //the robot controller
	static Unit robot; //the actual class used for the unit
	
	public static void run(RobotController newRC) {
		rc = newRC;
		RobotType rt = rc.getType();
		if (rt.equals(RobotType.ARCHON))
			robot = new Archon(rc);
		else if (rt.equals(RobotType.GUARD))
			robot = new Guard(rc);
		else if (rt.equals(RobotType.SCOUT))
			robot = new Scout(rc);
		else if (rt.equals(RobotType.SOLDIER))
			robot = new Soldier(rc);
		else if (rt.equals(RobotType.TTM) || rt.equals(RobotType.TURRET))
			robot = new Turret(rc);
		else
			robot = new Viper(rc);
		while (true) {
			try {
				robot.run();
			} catch (GameActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Clock.yield();
		}
	}
}

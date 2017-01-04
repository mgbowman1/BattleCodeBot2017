package testingrobot;

import battlecode.common.*;

public abstract class Unit {
	
	int trueHeight = GameConstants.MAP_MIN_HEIGHT; //the height of the map
	int trueWidth = GameConstants.MAP_MIN_WIDTH; //the width of the map
	final int[][] rubbleMap = new int[GameConstants.MAP_MAX_WIDTH][GameConstants.MAP_MAX_HEIGHT]; //the rubble amounts on each map tile
	final int[][] roundMap = new int[GameConstants.MAP_MAX_WIDTH][GameConstants.MAP_MAX_HEIGHT]; //the round in which this rubbleMap location was last updated
	final Direction[] directions = new Direction[]{Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.NORTH_EAST, Direction.NORTH_WEST, Direction.SOUTH_WEST, Direction.SOUTH_EAST}; //an array of possible directions for when needed to iterate through (has diagonal directions appear last)
	final RobotController rc;
	
	public Unit(RobotController rc) {
		this.rc = rc;
	}
	
	public abstract void run() throws GameActionException; //actual run code for each class (only runs once each time the while loop is in the main code
	
}

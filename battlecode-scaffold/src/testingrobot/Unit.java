package testingrobot;

import java.util.LinkedList;

import battlecode.common.*;

public abstract class Unit {
	
	int trueHeight = GameConstants.MAP_MIN_HEIGHT; //the height of the map
	int trueWidth = GameConstants.MAP_MIN_WIDTH; //the width of the map
	final double[][] RUBBLEMAP = new double[GameConstants.MAP_MAX_WIDTH][GameConstants.MAP_MAX_HEIGHT]; //the rubble amounts on each map tile
	final int[][] ROUNDMAP = new int[GameConstants.MAP_MAX_WIDTH][GameConstants.MAP_MAX_HEIGHT]; //the round in which this rubbleMap location was last updated
	final Direction[] DIRECTIONS = new Direction[]{Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.NORTH_EAST, Direction.NORTH_WEST, Direction.SOUTH_WEST, Direction.SOUTH_EAST}; //an array of possible directions for when needed to iterate through (has diagonal directions appear last)
	final LinkedList<Direction> PATHMAP = new LinkedList<>(); //the list of directions (starting from the front of the list) to get to a destination
	final LinkedList<RobotController> SQUAD = new LinkedList<>(); //the list of squad members
	final RobotController RC;
	final int BIGZOMBIETHREAT = 5;
	final int FASTZOMBIETHREAT = 3;
	final int GUARDTHREAT = 2;
	final int RANGEDZOMBIETHREAT = 3;
	final int SCOUTTHREAT = 1;
	final int SOLDIERTHREAT = 2;
	final int STANDARDZOMBIETHREAT = 2;
	final int TTMTHREAT = 1;
	final int TURRETTHREAT = 4;
	final int VIPERTHREAT = 2;
	final int ZOMBIEDENTHREAT = 5;
	final int THISTHREAT;
	final int THISZOMBIETHREAT;
	
	class PathLoc {
		
		int x;
		int y;
		double points;
		int heuristic;
		PathLoc head;
		PathLoc tail;
		
		public PathLoc(int x, int y, double points, int heuristic, PathLoc head) {
			this.x = x;
			this.y = y;
			this.points = points;
			this.heuristic = heuristic;
			this.head = head;
			tail = null;
		}
		
		public Direction toDirection() {
			int diffx = x - head.x;
			int diffy = y - head.y;
			switch (diffx) {
			case -1:
				switch (diffy) {
				case -1:
					return Direction.NORTH_WEST;
				case 0:
					return Direction.WEST;
				case 1:
					return Direction.SOUTH_WEST;
				}
				break;
			case 0:
				switch (diffy) {
				case -1:
					return Direction.NORTH;
				case 1:
					return Direction.SOUTH;
				}
				break;
			case 1:
				switch (diffy) {
				case -1:
					return Direction.NORTH_EAST;
				case 0:
					return Direction.EAST;
				case 1:
					return Direction.SOUTH_EAST;
				}
				break;
			}
			return null;
		}
		
		@Override
		public boolean equals(Object o) {
			if (o instanceof PathLoc) {
				if (((PathLoc)o).x == x && ((PathLoc)o).y == y) return true;
				else return false;
			} else return false;
		}
		
	}
	
	public Unit(RobotController rc, int thisThreat, int thisZombieThreat) {
		this.RC = rc;
		this.THISTHREAT = thisThreat;
		this.THISZOMBIETHREAT = thisZombieThreat;
	}
	
	public abstract void run() throws GameActionException; //actual run code for each class (only runs once each time the while loop is in the main code
	
	public void findPath(MapLocation src, MapLocation dest, LinkedList<MapLocation> dontMove) { //uses A* pathfinding to create a path (which may include digging through rubble)
		int srcx = src.x;
		int srcy = src.y;
		int destx = dest.x;
		int desty = dest.y;
		if (dontMove == null) dontMove = new LinkedList<>();
		LinkedList<PathLoc> open = new LinkedList<>();
		LinkedList<PathLoc> closed = new LinkedList<>();
		double rubbleWall = GameConstants.RUBBLE_OBSTRUCTION_THRESH;
		double rubbleSlow = GameConstants.RUBBLE_SLOW_THRESH;
		double rubbleClearFlat = GameConstants.RUBBLE_CLEAR_FLAT_AMOUNT;
		double rubbleClearPercent = GameConstants.RUBBLE_CLEAR_PERCENTAGE;
		double diag = GameConstants.DIAGONAL_DELAY_MULTIPLIER;
		closed.add(new PathLoc(srcx, srcy, RUBBLEMAP[srcx][srcy], 0, null)); //add the current tile
		PathLoc destination = new PathLoc(destx, desty, RUBBLEMAP[destx][desty], 0, null); //final tile
		while (!closed.getLast().equals(destination)) {
			for (int x = -1; x < 2; x++) {
				for (int y = -1; y < 2; y++) {
					int realx = closed.getLast().x + x;
					int realy = closed.getLast().y + y;
					if (realx >= 0 && realy >= 0 && realx < trueWidth && realy < trueHeight && realx != 0 && realy != 0 && !dontMove.contains(new MapLocation(realx, realy))) {
						double adj = 1;
						if (Math.abs(realx) == Math.abs(realy)) adj = diag;
						PathLoc temp = new PathLoc(realx, realy, closed.getLast().points + adj, Math.abs(realx - destx) + Math.abs(realy - desty), closed.getLast());
						if (RUBBLEMAP[realx][realy] >= rubbleSlow) temp.points += adj;
						if (RUBBLEMAP[realx][realy] >= rubbleWall) {
							double currentRubble = RUBBLEMAP[realx][realy];
							int rounds = 0;
							while (currentRubble >= rubbleWall) {
								currentRubble -= currentRubble * rubbleClearPercent + rubbleClearFlat;
								rounds++;
							}
							temp.points += rounds;
						}
						boolean keepGoing = true;
						if (closed.contains(temp)) {
							PathLoc current = closed.get(closed.indexOf(temp));
							double change = temp.points - current.points;
							if (change > 0) {
								current.head.tail = null;
								current.head = temp.head;
								current.points = temp.points;
								PathLoc tails = current.tail;
								while (tails != null) {
									tails.points -= change;
									tails = tails.tail;
								}
							}
							keepGoing = false;
						}
						if (open.contains(temp)) {
							PathLoc current = closed.get(closed.indexOf(temp));
							double change = temp.points - current.points;
							if (change > 0) {
								current.head.tail = null;
								current.head = temp.head;
								current.points = temp.points;
							}
							keepGoing = false;
						}
						if (keepGoing) {
							open.add(temp);
						}
					}
				}
			}
			double minPoints = open.getFirst().points + open.getFirst().heuristic;
			PathLoc min = open.getFirst();
			if (open.contains(destination)) min = open.get(open.indexOf(destination));
			else {
				for (PathLoc o : open) {
					if (o.points + o.heuristic <= minPoints) {
						minPoints = o.points + o.heuristic;
						min = o;
					}
				}
			}
			open.remove(min);
			closed.add(min);
		}
		PATHMAP.clear();
		closed.remove();
		while (!closed.isEmpty()) {
			PATHMAP.add(closed.remove().toDirection());
		}
	}
	
	public void updateMap(double rubble, MapLocation loc, int round) { //updates a specific location of map
		RUBBLEMAP[loc.x][loc.y] = rubble;
		ROUNDMAP[loc.x][loc.y] = round;
	}
	
	public void updateMap(double[][] rubble, int[][] round) { //updates map from another map
		for (int x = 0; x < trueWidth; x++) {
			for (int y = 0; y < trueHeight; y++) {
				if (round[x][y] > ROUNDMAP[x][y]) updateMap(rubble[x][y], new MapLocation(x, y), round[x][y]);
			}
		}
	}
	
	public double getZombieThreat(int i) { //returns the current threat of a zombie
		return i * (1 + Math.floor(RC.getRoundNum() / 2) / 10);
	}
	
	public double getViperThreat() { //returns the current threat of a viper to this unit
		return getZombieThreat(THISZOMBIETHREAT) + VIPERTHREAT;
	}
	
	public double getSquadThreat() { //returns the total threat of the squad
		double threat = 0;
		for (RobotController r : SQUAD) {
			if (r.getType().equals(RobotType.GUARD)) threat += GUARDTHREAT;
			else if (r.getType().equals(RobotType.SCOUT)) threat += SCOUTTHREAT;
			else if (r.getType().equals(RobotType.SOLDIER)) threat += SOLDIERTHREAT;
			else if (r.getType().equals(RobotType.TTM)) threat += TTMTHREAT;
			else if (r.getType().equals(RobotType.TURRET)) threat += TURRETTHREAT;
			else if (r.getType().equals(RobotType.VIPER)) threat += getViperThreat();
		}
		threat -= THISTHREAT;
		return threat;
	}
	
	public boolean checkThreat(RobotType[] bad, double nearbySquads) { //assesses threats and returns false if a retreat is needed
		double goodThreat = nearbySquads + getSquadThreat();
		double threat = 0;
		for (int i = 0; i < bad.length; i++) {
			if (bad[i].equals(RobotType.BIGZOMBIE)) threat += getZombieThreat(BIGZOMBIETHREAT);
			else if (bad[i].equals(RobotType.FASTZOMBIE)) threat += getZombieThreat(FASTZOMBIETHREAT);
			else if (bad[i].equals(RobotType.GUARD)) threat += GUARDTHREAT;
			else if (bad[i].equals(RobotType.RANGEDZOMBIE)) threat += getZombieThreat(RANGEDZOMBIETHREAT);
			else if (bad[i].equals(RobotType.SCOUT)) threat += SCOUTTHREAT;
			else if (bad[i].equals(RobotType.SOLDIER)) threat += SOLDIERTHREAT;
			else if (bad[i].equals(RobotType.STANDARDZOMBIE)) threat += STANDARDZOMBIETHREAT;
			else if (bad[i].equals(RobotType.TTM)) threat += TTMTHREAT;
			else if (bad[i].equals(RobotType.TURRET)) threat += TURRETTHREAT;
			else if (bad[i].equals(RobotType.VIPER)) threat += getViperThreat();
			else if (bad[i].equals(RobotType.ZOMBIEDEN)) threat += getZombieThreat(ZOMBIEDENTHREAT);
		}
		if (threat <= goodThreat) return true;
		return false;
	}
	
}

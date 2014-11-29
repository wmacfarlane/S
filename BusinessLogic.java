import java.util.*;
// This class represents the numerical requirement for the number of lines
// to border a particular cell.
class LineReq {
	private int value;
	public LineReq(int v) {
		value = v;
	}
	public boolean hasValue() {
		return value != -1;
	}
	public int getValue() {
		return value;
	}
}
class Wall implements Comparable<Wall> {
	int r, c;
	boolean vertical;
	Wall (int row, int col, boolean v) {
		r = row;
		c = col;
		vertical = v;
	}
	Wall (Wall other) {
		r = other.r;
		c = other.c;
		vertical = other.vertical;
	}
	public boolean isValid() {
		int maxR = vertical ? Board.r - 1 : Board.r;
		int maxC = vertical ? Board.c : Board.c - 1;
		return r >= 0 && r <= maxR && c >= 0 && c <= maxC;
	}
	@Override
	public int hashCode() {
		return r + (c * 31) + (vertical ? 0 : 955); //r <= 25, c <= 30
	}
	@Override
	public boolean equals(Object o) {
		return hashCode() == o.hashCode();
	}
	public int compareTo(Wall w) {
		if (r < w.r)
			return -1;
		if (r > w.r)
			return 1;
		if (c < w.c)
			return -1;
		if (c > w.c)
			return 1;
		if (!vertical && w.vertical)
			return -1;
		if (vertical && !w.vertical)
			return 1;
		return 0;
	}
	public String toString() {
		return "r: " + r + ", c: " + c + ", side: " + (vertical ? "left" : "top");
	}
	public static Wall makeWall(int r, int c, Side side) {
		switch (side) {
			case TOP:
				return new Wall(r, c, false);
			case RIGHT:
				return new Wall(r, c+1, true);
			case BOTTOM:
				return new Wall(r+1, c, false);
			case LEFT:
				return new Wall(r, c, true);
			default:
				throw new IllegalArgumentException();
		}
	}
	public Coord[] getCoords() {
		Coord[] coords = new Coord[2];
		coords[0] = new Coord(r, c);
		coords[1] = vertical ? new Coord(r+1, c) : new Coord(r, c+1);
		return coords;
	}
}
class Coord implements Comparable<Coord> {
	int r, c;
	public Coord(int r, int c) {
		this.r = r;
		this.c = c;
	}
	public int compareTo(Coord o) {
		if (r < o.r)
			return -1;
		if (r > o.r)
			return 1;
		if (c < o.c)
			return -1;
		if (c > o.c)
			return 1;
		return 0;
	}
	@Override
	public boolean equals(Object o) {
		Coord coord = (Coord) o;
		return r == coord.r && c == coord.c;
	}
	Wall[] intersects() {
		Wall[] arr = new Wall[4];
		arr[0] = Wall.makeWall(r, c, Side.TOP);
		arr[1] = Wall.makeWall(r, c, Side.LEFT);
		arr[2] = Wall.makeWall(r, c-1, Side.TOP);
		arr[3] = Wall.makeWall(r-1, c, Side.LEFT);
		return arr;
	}
}
class State {
	private int[] leftWalls; // navigate rows by [], cols by bit
	private int[] topWalls;
	private int[] leftAvailableWalls;
	private int[] topAvailableWalls;
	private TreeSet<Coord> lineEndings;
	public State() {
		leftWalls = new int[Board.r];
		topWalls = new int[Board.r+1];
		leftAvailableWalls = new int[Board.r];
		topAvailableWalls = new int[Board.r+1];
		lineEndings = new TreeSet<Coord>();
	}
	public State(State s) {
		leftWalls = s.leftWalls.clone();
		topWalls = s.topWalls.clone();
		leftAvailableWalls = s.leftAvailableWalls.clone();
		topAvailableWalls = s.topAvailableWalls.clone();
		lineEndings = new TreeSet<Coord>(s.lineEndings);
	}
	public boolean eliminated(Wall wall) {
		return check(wall, true);
	}
	public void eliminate(Wall wall) {
		if (wall.isValid())
			note(wall, true);
	}
	public boolean marked(Wall wall) {
		return check(wall, false);
	}
	public boolean mark(Wall wall) {
		if (!wall.isValid())
			return false;
		note(wall, false);
		boolean success = updateLineEndings(wall);
		if (!success) {
			unnote(wall, false);
			if (Slither.humanPlay) {
				System.out.println("You'll never end with a single continuous loop with a move like that.");
			}
		}
		return success;
	}
	private void unnote(Wall wall, boolean theoretical) {
		if (!wall.isValid())
			return;
		int[] walls = getAppropriateIntArray(wall.vertical, theoretical);
		walls[wall.r] &= ~(1 << Board.c - wall.c);
	}
	private void note(Wall wall, boolean theoretical) {
		int[] walls = getAppropriateIntArray(wall.vertical, theoretical);
		walls[wall.r] |= (1 << Board.c - wall.c);
	}
	private boolean check(Wall wall, boolean theoretical) {
		if (!wall.isValid())
			return false;
		int[] walls = getAppropriateIntArray(wall.vertical, theoretical);
		return (walls[wall.r] & (1 << (Board.c - wall.c))) > 0; 
	}
	private int[] getAppropriateIntArray(boolean vertical, boolean theoretical) {
		if (theoretical) {
			if (vertical) {
				return leftAvailableWalls;
			} else {
				return topAvailableWalls;
			}
		} else {
			if (vertical) {
				return leftWalls;
			} else {
				return topWalls;
			}
		}
	}
	private boolean updateLineEndings(Wall wall) { // returns true if the wall placement was possible
		Coord[] ends = wall.getCoords();
		int[] cornerCounts = { getCornerCount(ends[0]), getCornerCount(ends[1]) };
		if (cornerCounts[0] > 2 || cornerCounts[1] > 2)
			return false;
		for (int i = 0; i < 2; i++) {
			if (cornerCounts[i] == 2) {
				lineEndings.remove(ends[i]);
			} else if (cornerCounts[i] == 1) {
				lineEndings.add(ends[i]);
			} else {
				throw new RuntimeException("Impossible lineEndings state. Count is 0.");
			}
		}
		return true;
	}
	public int getCornerCount(Coord c) {
		Wall[] intersectingWalls = c.intersects();
		int count = 0;
		for (Wall w : intersectingWalls) {
			if (marked(w)) {
				count++;
			}
		}
		return count;
	}
	/*
	private boolean updateLineEndings(Wall wall, boolean originalCall) {
		class CoordMap {
			Coord c;
			int count;
			CoordMap(Coord c) {
				this.c = c;
				count = 0;
			}
		}
		CoordMap[] coordMaps = new CoordMap[2];
		Coord[] baseCoords = wall.getCoords();
		coordMaps[0] = new CoordMap(baseCoords[0]);
		coordMaps[1] = new CoordMap(baseCoords[1]);
		List<Wall> markedNeighbors = new LinkedList<Wall>();
		Iterator<Wall> it = getTouchingWalls(wall).iterator();
		while (it.hasNext()) {
			Wall w = it.next();
			if (!marked(w)) {
				continue;
			}
			markedNeighbors.add(w);
			Coord[] coords = w.getCoords();
			for (Coord u : coords) {
				for (CoordMap v : coordMaps) {
					if (u.equals(v.c)) {
						v.count++;
					}
				}
			}
		}
		if (coordMaps[0].count >= 2 || coordMaps[1].count >= 2) {
			return false; // impossible to form a circle becauase we've formed a T or a +
		}
		if (coordMaps[0].count == 0 || coordMaps[1].count == 0) {
			lineEndings.add(wall);
		} else {
			lineEndings.remove(wall);
		}
		if (originalCall) { // remove neighbors from the list of endings
			for (Wall w : markedNeighbors) {
				updateLineEndings(w, false);
			}
		}
		return true;
	}
	*/
	/*
		_|_|_
		 | |
	*/
	public List<Wall> getTouchingWalls(Wall base) {
		List<Wall> touchingWalls = new ArrayList<Wall>();
		Wall w;
		if (base.vertical) { // LEFT
			for (int r = base.r - 1; r <= base.r+1; r++) {
				if (r == base.r) // don't add yourself
					continue;
				w = Wall.makeWall(r, base.c, Side.LEFT);
				if (marked(w)) {
					touchingWalls.add(w);
				}
			}
			for (int r = base.r; r <= base.r + 1; r++) {
				for (int c = base.c - 1; c <= base.c; c++) {
					w = Wall.makeWall(r, c, Side.TOP);
					if (marked(w)) {
						touchingWalls.add(w);
					}
				}
			}
		} else { // TOP
			for (int c = base.c - 1; c <= base.c + 1; c++) {
				if (c == base.c) // don't add yourself
					continue;
				w = Wall.makeWall(base.r, c, Side.TOP);
				if (marked(w)) {
					touchingWalls.add(w);
				}
			}
			for (int r = base.r - 1; r <= base.r; r++) {
				for (int c = base.c; c <= base.c + 1; c++) {
					w = Wall.makeWall(r, c, Side.LEFT);
					if (marked(w)) {
						touchingWalls.add(w);
					}
				}
			}
		}
		return touchingWalls;
	}
}
class Board {
	static int r, c;
	static LineReq[][] lineReqs;
	State state;
	public Board () {
		state = new State();
	}
	public static void setDimensions(int row, int col) {
		r = row;
		c = col;
		lineReqs = new LineReq[r][c];
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("   ");
		for (int j = 1; j <= c; j++) {
			sb.append(" " + (j % 10));
		}
		sb.append("\n\n");
		sb.append("   ");
		addWallsToString(sb, 0, Side.TOP);
		for (int i = 0; i < r; i++) {
			sb.append((i+1) + " ");
			if (i+1 < 10)
				sb.append(" ");
			addContentsToString(sb, i);
			sb.append("   ");
			addWallsToString(sb, i, Side.BOTTOM);
		}
		return sb.toString();
	}
	private void addContentsToString(StringBuilder sb, int row) {
		Wall w = Wall.makeWall(row, 0, Side.LEFT);
		if (state.marked(w)) {
			sb.append("|");
		} else {
			sb.append(" ");
		}
		for (int j = 0; j < c; j++) {
			// square marking
			if (lineReqs[row][j].hasValue()) {
				sb.append(lineReqs[row][j].getValue());
			} else {
				sb.append(" ");
			}
			// wall marking
			w = Wall.makeWall(row, j, Side.RIGHT);
			if (state.marked(w)) {
				sb.append("|");
			} else {
				sb.append(" ");
			}
		}
		sb.append("\n");
		return;
	}
	private void addWallsToString(StringBuilder sb, int row, Side side) {
		for (int j = 0; j < c; j++) {
			Wall w = Wall.makeWall(row, j, side);
			sb.append("+");
			if (state.marked(w)) {
				sb.append("-");
			} else {
				sb.append(" ");
			}
		}
		sb.append("+\n");
	}
}

enum Side {
	TOP, RIGHT, BOTTOM, LEFT;
	public static final Side[] RELEVANT_SIDES = {Side.TOP, Side.LEFT};
	static Side translate (char c) {
		switch (c) {
			case 't':
			case 'T':
				return Side.TOP;
			case 'r':
			case 'R':
				return Side.RIGHT;
			case 'b':
			case 'B':
				return Side.BOTTOM;
			case 'l':
			case 'L':
				return Side.LEFT;
			default:
				throw new IllegalArgumentException(
					"You incorrectly specified the side that you wanted to mark!"
				);
		}
	}
}
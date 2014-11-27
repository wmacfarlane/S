import java.util.*;
import java.io.*;

public class Slither {
	public static void main (String [] args) throws IOException {
		Scanner sc = new Scanner(System.in);
		System.out.println(introduction());
		String fileName = sc.next();
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String[] line = br.readLine().split(" ");
		int r = Integer.parseInt(line[0]);
		int c = Integer.parseInt(line[1]);
		Board.setDimensions(r, c);
		Board board = new Board();
		for (int i = 0; i < r; i++) {
			line = br.readLine().split(" ");
			for (int j = 0; j < c; j++) {
				Board.lineReqs[i][j] = new LineReq(Integer.parseInt(line[j]));
			}
		}
		br.close();
		slither(sc, board);
	}
	static void slither(Scanner sc, Board board) {
		System.out.println(instructions());
		BoardLogic boardLogic = new BoardLogic(board);
		while (true) {
			System.out.print(board);
			if (boardLogic.solved()) {
				System.out.println("Congratulations! You solved it!");
				break;
			}
			int r = sc.nextInt() - 1; // 0-indexing
			int c = sc.nextInt() - 1;
			Side side = Side.translate(sc.next().charAt(0));
			board.state.markWall(Wall.makeWall(r, c, side));
		}
	}
	static String instructions() {
		return 
		"\n<Instructions>\n" + 
		"Type \"N N L\", where the first two Ns represent the row and column " + 
		"of the cell whose adjacent wall you wish to fill in and L represents the side " + 
		"of the cell where you'd like to put the wall " + 
		"(T for top, B for bottom, L for left, and R for right).\n" +
		"</Instructions>\n";
	}
	static String introduction() {
		String s = 
		"Welcome to Slither!\n\nSlither is a puzzle game. " + 
		"If you can create a continuous loop that adheres to " + 
		"the numbered requirement squares seen on the board, " + 
		"you win!\n\n";
		s += 
		"For instance, the following is a winning solution because it has " + 
		"as many lines around each square as the number specified " +
		"in the center of that square.\n\n" +
		"It also contains exactly one continuous loop.\n\n";
		s +=
		"    1 2 \n" +
		"        \n" +
		"   +-+-+\n" + 
		"1  |3 3|\n" +
		"   +-+-+\n" +
		"2       \n" +
		"   + + +\n\n";

		s += "Please enter the name of the input file (e.g., \"Slither.in\").";
		return s;
	}
}
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
class BoardLogic {
	Board board;
	BoardLogic(Board board) {
		this.board = board;
	}
	/*
		There are two requirements that a solved puzzle must satisfy.
		1) A single looped path has been generated, with no crosses or branches
		2) Each line requirement must be met.
		
		My solution for (1) is:
			for each end of a 1-unit line segment,
				add both corners of the segment to a list of corners.
		A list in which each corner appears 0 or 2 times guarantees
		that we have a connected loop without crosses or branches.
	*/
	public boolean solved() {
		return rightNumberOfCorners() && metLineRequirements() && justOneLoop();
	}
	private boolean rightNumberOfCorners() {
		int[][] corners = new int[Board.r+1][Board.c+1];
		// The value at each cell corresponds to the number of times the top-left
		// corner of that cell layed at the end of a 1-unit line segment.
		for (int i = 0; i <= Board.r; i++) {
			for (int j = 0; j < Board.c; j++) {
				Wall w = Wall.makeWall(i, j, Side.TOP);
				if (board.state.wallMarked(w)) {
					corners[i][j]++;
					corners[i][j+1]++;
				}
			}
		}
		for (int i = 0; i < Board.r; i++) {
			for (int j = 0; j <= Board.c; j++) {
				Wall w = Wall.makeWall(i, j, Side.LEFT);
				if (board.state.wallMarked(w)) {
					corners[i][j]++;
					corners[i+1][j]++;
				}
			}
		}
		// Now let's count our corners.
		for (int i = 0; i <= Board.r; i++) {
			for (int j = 0; j <= Board.c; j++) {
				if (corners[i][j] != 0 && corners[i][j] != 2)
					return false;
			}
		}
		return true;
	}
	private boolean metLineRequirements() {
		// Now let's make sure we've met the line requirements.
		for (int i = 0; i < Board.r; i++) {
			for (int j = 0; j < Board.c; j++) {
				if (!board.lineReqs[i][j].hasValue())
					continue;
				int target = board.lineReqs[i][j].getValue();
				int count = 0;
				for (Side side : Side.values()) {
					Wall w = Wall.makeWall(i, j, side);
					if (board.state.wallMarked(w))
						count++;
				}
				if (target != count)
					return false;
			}
		}
		return true;
	}
	private boolean justOneLoop() {
		Wall start = null;
		SEARCH:
		for (int i = 0; i <= Board.r; i++) {
			for (int j = 0; j <= Board.c; j++) {
				for (Side s : Side.RELEVANT_SIDES) {
					Wall w = Wall.makeWall(i, j, s);
					if (board.state.wallMarked(w)) {
						start = w;
						break SEARCH;
					}
				}
			}
		}
		if (start == null) {
			return false;
		}
		HashSet<Wall> hs = new HashSet<Wall>();
		LinkedList<Wall> q = new LinkedList<Wall>();
		q.offer(start);
		hs.add(start);
		while(!q.isEmpty()) {
			Wall u = q.poll();
			Iterator<Wall> it = getTouchingWalls(u).iterator();
			while (it.hasNext()) {
				Wall v = it.next();
				if (!hs.contains(v)) {
					hs.add(v);
					q.add(v);
				}
			}
		}
		for (int i = 0; i <= Board.r; i++) {
			for (int j = 0; j <= Board.c; j++) {
				for (Side s : Side.RELEVANT_SIDES) {
					Wall w = Wall.makeWall(i, j, s);
					if (board.state.wallMarked(w) && !hs.contains(w)) {
						return false;
					}
				}
			}
		}
		return true; // no line was found that wasn't touching the main loop
	}
	public List<Wall> getTouchingWalls(Wall base) {
		List<Wall> touchingWalls = new ArrayList<Wall>();
		Wall w;
		if (base.vertical) { // LEFT
			for (int r = base.r - 1; r <= base.r+1; r++) {
				w = Wall.makeWall(r, base.c, Side.LEFT);
				if (board.state.wallMarked(w)) {
					touchingWalls.add(w);
				}
			}
			for (int r = base.r; r <= base.r + 1; r++) {
				for (int c = base.c - 1; c <= base.c; c++) {
					w = Wall.makeWall(r, c, Side.TOP);
					if (board.state.wallMarked(w)) {
						touchingWalls.add(w);
					}
				}
			}
		} else { // TOP
			for (int c = base.c - 1; c <= base.c + 1; c++) {
				w = Wall.makeWall(base.r, c, Side.TOP);
				if (board.state.wallMarked(w)) {
					touchingWalls.add(w);
				}
			}
			for (int r = base.r - 1; r <= base.r; r++) {
				for (int c = base.c; c <= base.c + 1; c++) {
					w = Wall.makeWall(r, c, Side.LEFT);
					if (board.state.wallMarked(w)) {
						touchingWalls.add(w);
					}
				}
			}
		}
		return touchingWalls;
	}
}
class Wall {
	int r, c;
	boolean vertical;
	Wall (int row, int col, boolean v) {
		r = row;
		c = col;
		vertical = v;
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
}
class State {
	private int[] leftWalls; // navigate rows by [], cols by bit
	private int[] topWalls;
	Wall lastPlacedWall;
	public State() {
		leftWalls = new int[Board.r];
		topWalls = new int[Board.r+1];
		lastPlacedWall = null;
	}
	public boolean wallMarked(Wall wall) {
		if (!wall.isValid())
			return false;
		int[] walls = wall.vertical ? leftWalls : topWalls;
		return (walls[wall.r] & (1 << (Board.c - wall.c))) > 0; 
	}
	public void markWall(Wall wall) {
		if (!wall.isValid())
			return;
		int[] walls = wall.vertical ? leftWalls : topWalls;
		walls[wall.r] |= (1 << Board.c - wall.c);
		lastPlacedWall = wall;
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
		if (state.wallMarked(w)) {
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
			if (state.wallMarked(w)) {
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
			if (state.wallMarked(w)) {
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
import java.util.*;
import java.io.*;

public class Slither {
	static boolean humanPlay;
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

		System.out.println(humanQuery());
		humanPlay = (sc.next().charAt(0) == 'H');
		if (humanPlay) {
			humanSlither(sc, board);
		} else {
			automatedSlither(board);
		}
	}
	static void humanSlither(Scanner sc, Board board) {
		System.out.println(instructions());
		BoardIntelligence boardIntelligence = new BoardIntelligence(board);
		while (true) {
			System.out.print(board);
			if (boardIntelligence.solved()) {
				System.out.println("Congratulations! You solved it!");
				break;
			}
			int r = sc.nextInt() - 1; // 0-indexing
			int c = sc.nextInt() - 1;
			Side side = Side.translate(sc.next().charAt(0));
			board.state.mark(Wall.makeWall(r, c, side));
		}
	}
	static void automatedSlither(Board board) {
		BoardIntelligence boardIntelligence = new BoardIntelligence(board);
		// AI ai = new AI();
		LessGuesswork lg = new LessGuesswork(board);
		lg.executeStaticIntuition();
		while (true) {
			System.out.println(board);
			if (boardIntelligence.solved()) {
				System.out.println("Congratulations! You solved it!");
				break;
			}
			Scanner sc = new Scanner(System.in);
			int x = sc.nextInt();
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
	static String humanQuery() {
		return
		"Type \"H\" if you would like to play, or \"A\" if you would like to see an automated solution.";
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
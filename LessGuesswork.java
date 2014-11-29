class LessGuesswork {
	Board board;
	LessGuesswork(Board b) {
		board = b;
	}
	public void executeStaticIntuition() {
		zeroesAndFours();
		boardCorners();
	}
	private void zeroesAndFours() {
		for (int r = 0; r < Board.r; r++) {
			for (int c = 0; c < Board.c; c++) {
				switch (Board.lineReqs[r][c].getValue()) {
				case 0:
					for (Side s : Side.values()) {
						Wall w = Wall.makeWall(r, c, s);
						board.state.eliminate(w);
					}
					break;
				case 4:
					for (Side s : Side.values()) {
						Wall w = Wall.makeWall(r, c, s);
						board.state.mark(w);
					}
					break;
				default:
					break;
				}
			}
		}
	}
	private void boardCorners() {
		int[] rs = {0, Board.c - 1, Board.c - 1, 0};
		int[] cs = {0, 0, Board.r - 1, Board.r - 1};
		for (int i = 0; i < 4; i++) {
			Wall horiWall, vertWall;
			switch (Board.lineReqs[rs[i]][cs[i]].getValue()) {
				case 1:
					if (rs[i] == 0) {
						horiWall = Wall.makeWall(rs[i], cs[i], Side.TOP);
					} else {
						horiWall = Wall.makeWall(rs[i], cs[i], Side.BOTTOM);
					}
					if (cs[i] == 0) {
						vertWall = Wall.makeWall(rs[i], cs[i], Side.LEFT);
					} else {
						vertWall = Wall.makeWall(rs[i], cs[i], Side.RIGHT);
					}
					board.state.eliminate(horiWall);
					board.state.eliminate(vertWall);
					break;
				case 2:
					if (rs[i] == 0) {
						if (cs[i] == 0) {
							board.state.mark(Wall.makeWall(0, 1, Side.TOP));
							board.state.mark(Wall.makeWall(1, 0, Side.LEFT));
						} else {
							board.state.mark(Wall.makeWall(0, cs[i] - 1, Side.TOP));
							board.state.mark(Wall.makeWall(1, cs[i], Side.RIGHT));
						}
					} else {
						if (cs[i] == 0) {
							board.state.mark(Wall.makeWall(rs[i] - 1, 0, Side.LEFT));
							board.state.mark(Wall.makeWall(rs[i], 1, Side.BOTTOM));
						} else {
							board.state.mark(Wall.makeWall(rs[i] - 1, cs[i], Side.RIGHT));
							board.state.mark(Wall.makeWall(rs[i], cs[i] - 1, Side.BOTTOM));
						}
					}
					break;
				case 3:
					if (rs[i] == 0) {
						horiWall = Wall.makeWall(rs[i], cs[i], Side.TOP);
					} else {
						horiWall = Wall.makeWall(rs[i], cs[i], Side.BOTTOM);
					}
					if (cs[i] == 0) {
						vertWall = Wall.makeWall(rs[i], cs[i], Side.LEFT);
					} else {
						vertWall = Wall.makeWall(rs[i], cs[i], Side.RIGHT);
					}
					board.state.mark(horiWall);
					board.state.mark(vertWall);
					break;
				default:
					break;
			}
		}
	}
}
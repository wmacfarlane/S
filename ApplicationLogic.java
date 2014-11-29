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
	/*
		_|_|_
		 | |
	*/
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
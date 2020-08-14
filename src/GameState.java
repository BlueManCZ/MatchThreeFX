package MatchThree;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class GameState {

	static final int ROWS = 8;
	static final int COLUMNS = 8;

	private static final int TYPES = 6;

    private int gameGrid[][] = new int[ROWS][COLUMNS];

	private BooleanProperty active = new SimpleBooleanProperty(false);
	private IntegerProperty score = new SimpleIntegerProperty(0);

	private boolean turn = false;

	private Point2D startingPosition;

	private List<Point2D> turnGems = new ArrayList<>();

	GameState() {
        reinitialize();
	}

	void reinitialize() {
		setDefaultValues();
	}

	private void setDefaultValues() {
        for (int w = 0; w < COLUMNS; w++) {
            for (int h = 0; h < ROWS; h++) {
                Random r = new Random();
                int number = r.nextInt(TYPES);
                gameGrid[h][w] = number;
            }
        }
	}

    void startTurn() {
		setTurn(true);
	}

	boolean endTurn() {
    	int selected = turnGems.size();
		if (selected > 2) {
			//System.out.println("Gems: " + selected);
			if (selected > 4) {
				Point2D lastPoint = turnGems.get(turnGems.size() - 1);
				increaseScore(removeAllTiles(gameGrid[(int)lastPoint.getY()][(int)lastPoint.getX()]) * 4);
			} else {
				score.setValue(score.getValue() + (2 << selected - 1));
				for (var point : turnGems) {
					gameGrid[(int)point.getY()][(int)point.getX()] = -1;
				}
			}
			moveGemsDown();
			fillEmptyPlaces();
		}
		turnGems.clear();
		boolean over = !detectPossibleMoves();
		//System.out.println("GameOver? " + over);
		//System.out.println("Current score: " + score.getValue());
		setTurn(false);
		if (over) {
			setActive(false);
			return true;
		}
		return false;
	}

	private boolean processPoint(Point2D point) {
    	//System.out.println("Processing point");
    	int startColor = gameGrid[(int)startingPosition.getY()][(int)startingPosition.getX()];
    	int currentColor = gameGrid[(int)point.getY()][(int)point.getX()];
    	if (turnGems.size() > 0) {
			Point2D lastPoint = turnGems.get(turnGems.size() - 1);
			double distance = Math.abs(point.getX() - lastPoint.getX()) + Math.abs(point.getY() - lastPoint.getY());
			//System.out.println(distance);
			if (distance > 1) return false;
			if (distance == 1 && turnGems.contains(point)) {
				turnGems.remove(lastPoint);
			}
		}

    	if (startColor == currentColor) {
    		if (!turnGems.contains(point)) {
    			turnGems.add(point);
    			return true;
			}
		}
		return false;
	}

	boolean addPoint(Point2D point) {
    	if (turn) return processPoint(point);
    	setStartingPosition(point);
    	return false;
	}

	private boolean upperGemExists(int x, int y) {
		for (int h = 0; h < y; h++) {
			if (gameGrid[h][x] > -1) return true;
		}
		return false;
	}

	private void moveGemsDown() {
		for (int h = gameGrid.length - 1; h > 0; h--) {
			for (int w = 0; w < gameGrid[0].length; w++) {
				while (gameGrid[h][w] == -1 && upperGemExists(w, h)) {
					for (int r = h; r > 0; r--) {
						gameGrid[r][w] = gameGrid[r - 1][w];
						gameGrid[r - 1][w] = -1;
					}
				}
			}
		}
	}

	private void fillEmptyPlaces() {
		for (int w = 0; w < COLUMNS; w++) {
			for (int h = 0; h < ROWS; h++) {
				if (gameGrid[h][w] == -1) {
					Random r = new Random();
					int number = r.nextInt(TYPES);
					gameGrid[h][w] = number;
				}
			}
		}
	}

	private int scanNeighbours(int x, int y, List<Point2D> list) {
    	// RIGHT
    	if ((x + 1 < COLUMNS && gameGrid[y][x + 1] == gameGrid[y][x]) && !list.contains(new Point2D(x + 1, y))) {
    		list.add(new Point2D(x + 1, y));
    		return 1 + scanNeighbours(x + 1, y, list);
		}
		// DOWN
		if ((y + 1 < ROWS && gameGrid[y + 1][x] == gameGrid[y][x]) && !list.contains(new Point2D(x, y + 1))) {
			list.add(new Point2D(x, y + 1));
			return 1 + scanNeighbours(x, y + 1, list);
		}
		// LEFT
		if ((x - 1 >= 0 && gameGrid[y][x - 1] == gameGrid[y][x]) && !list.contains(new Point2D(x - 1, y))) {
			list.add(new Point2D(x - 1, y));
			return 1 + scanNeighbours(x - 1, y, list);
		}
		// UP
		if ((y - 1 >= 0 && gameGrid[y - 1][x] == gameGrid[y][x]) && !list.contains(new Point2D(x, y - 1))) {
			list.add(new Point2D(x, y - 1));
			return 1 + scanNeighbours(x, y - 1, list);
		}
		return 0;
	}

	private boolean detectPossibleMoves() {
		for (int w = 0; w < COLUMNS; w++) {
			for (int h = 0; h < ROWS; h++) {
				ArrayList<Point2D> list = new ArrayList<>();
				list.add(new Point2D(w, h));
				if (scanNeighbours(w, h, list) > 1) return true;
			}
		}
    	return false;
	}

	private int removeAllTiles(int type) {
    	int counter = 0;
		for (int w = 0; w < COLUMNS; w++) {
			for (int h = 0; h < ROWS; h++) {
				if (gameGrid[h][w] == type) {
					gameGrid[h][w] = -1;
					counter++;
				}
			}
		}
		return counter;
	}

    int[][] getGameGrid() {
        return gameGrid;
    }

    private void increaseScore(int number) {
    	score.setValue(score.getValue() + number);
	}

	void resetScore() { score.setValue(0); }

	final BooleanProperty activeProperty() { return active; }

	final IntegerProperty getScoreProperty() { return score; }
	
	final boolean isActive() { return this.activeProperty().get(); }
	
	final void setActive(final boolean active) { this.activeProperty().set(active); }

	private void setTurn(boolean turn) {
		this.turn = turn;
	}

	private void setStartingPosition(Point2D position) {
		startingPosition = position;
	}
}

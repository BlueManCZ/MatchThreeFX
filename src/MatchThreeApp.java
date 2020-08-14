package MatchThree;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MatchThreeApp extends Application {


    private static final int UNIT_SIZE = 40; // px

    private static final int RADIUS = 20; // px
    private static final int SELECTED_RADIUS = 10; // px

    private static final int PADDING = 10; // px

    private GameState game = new GameState();

    private ScoreManager manager = new ScoreManager("BestScores.xml");

    private Pane gemsLayer;

    private List<Rectangle> selectedRectangles = new ArrayList<>();
    private List<Point2D> selectedPoints = new ArrayList<>();


    @Override
    public void start(Stage primaryStage) {
        createGameLayers();

        Button btnStart = new Button("Start");
        btnStart.setPrefWidth(100);
        btnStart.setStyle("-fx-font: 24 arial;");
        btnStart.setOnAction(e -> {
            game.reinitialize();
            redrawGame();
            game.setActive(true);
            gemsLayer.setOpacity(1);
        });

        Button btnScores = new Button("Records");
        btnScores.setPrefWidth(100);
        btnScores.setStyle("-fx-font: 18 arial;");
        btnScores.setOnAction(e -> showHighScoresDialog());

        VBox menu = new VBox(20);
        menu.visibleProperty().bind(game.activeProperty().not());

        menu.getChildren().addAll(btnStart, btnScores);

        menu.setAlignment(Pos.CENTER);

        StackPane gameStack = new StackPane();
        gameStack.getChildren().addAll(gemsLayer, menu);

        BorderPane root = new BorderPane();

        Text lbScore = new Text();
        lbScore.textProperty().bind(game.getScoreProperty().asString());

        HBox scorePane = new HBox(1, new Text("Score: "), lbScore);
        scorePane.setPadding(new Insets(1, 0, 5, 9));
        scorePane.setStyle("-fx-font-weight: bold");

        root.setBottom(scorePane);
        root.setCenter(gameStack);

        Scene scene = new Scene(root, GameState.COLUMNS * UNIT_SIZE + PADDING, GameState.ROWS * UNIT_SIZE + PADDING + 15);
        //scene.setOnKeyPressed(this::dispatchKeyEvents);

        scene.setOnMousePressed(this::onMousePress);
        scene.setOnMouseReleased(e -> onMouseRelease());

        primaryStage.setTitle("Match 3+ Tiles");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void createGameLayers() {
        gemsLayer = new Pane();

        gemsLayer.addEventFilter(MouseEvent.DRAG_DETECTED , mouseEvent -> gemsLayer.startFullDrag());

        gemsLayer.setMaxWidth(UNIT_SIZE * GameState.COLUMNS);
        gemsLayer.setMaxHeight(UNIT_SIZE * GameState.ROWS);
    }

    private Point2D calculateGem(double x, double y) {
        x = (x - PADDING / 2.) / UNIT_SIZE;
        y = (y - PADDING / 2.) / UNIT_SIZE;
        if (x > GameState.COLUMNS - 1) x = GameState.ROWS - 1;
        if (y > GameState.ROWS - 1) y = GameState.ROWS - 1;
        return new Point2D((int)x, (int)y);
    }

    private void redrawGame() {
        gemsLayer.getChildren().removeAll(gemsLayer.getChildren());

        int[][] grid = game.getGameGrid();

        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < grid[0].length; c++) {

                Rectangle rect = new Rectangle(UNIT_SIZE, UNIT_SIZE);

                Color fill = Templates.getColor(grid[r][c]);
                Color shadow = Templates.getColor(grid[r][c]).darker();

                InnerShadow is = new InnerShadow();
                is.setOffsetY(1.0);
                is.setOffsetX(1.0);
                is.setColor(shadow);

                rect.setScaleX(0.9);
                rect.setScaleY(0.9);
                rect.setX(c * UNIT_SIZE);
                rect.setY(r * UNIT_SIZE);
                rect.setArcWidth(RADIUS);
                rect.setArcHeight(RADIUS);
                rect.setFill(fill);
                rect.setUserData(fill);
                rect.setVisible(true);
                rect.setEffect(is);

                int finalC = c;
                int finalR = r;

                rect.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
                    selectedRectangles.add((Rectangle)e.getSource());
                    selectedPoints.add(new Point2D(finalC, finalR));
                    setSelected(rect);
                });

                rect.addEventFilter(MouseDragEvent.MOUSE_DRAG_ENTERED, e -> {
                    // Check, if entered tile can be selected
                    if (game.addPoint(new Point2D(finalC, finalR))) {
                        selectedRectangles.add((Rectangle)e.getSource());
                        selectedPoints.add(new Point2D(finalC, finalR));
                        setSelected(rect);
                    } else {
                        Point2D point = new Point2D(finalC, finalR);
                        if (selectedPoints.size() > 0) {
                            Point2D lastPoint = selectedPoints.get(selectedPoints.size() - 1);
                            double distance = Math.abs(point.getX() - lastPoint.getX()) + Math.abs(point.getY() - lastPoint.getY());
                            // If you enter already selected tile from last tile, last tile will be deselected
                            if (distance == 1 && selectedPoints.contains(point)) {
                                selectedPoints.remove(lastPoint);
                                Rectangle lastRect = selectedRectangles.get(selectedRectangles.size() - 1);
                                setDefault(lastRect);
                                selectedRectangles.remove(lastRect);
                            }
                        }
                    }
                });

                rect.addEventFilter(MouseEvent.MOUSE_ENTERED, e -> game.addPoint(new Point2D(finalC, finalR)));

                gemsLayer.getChildren().add(rect);
            }
        }
    }

    private void gameOver() {
        gemsLayer.setOpacity(0.2);
    }

    private void setSelected(Rectangle rect) {
        Color fill = (Color)rect.getUserData();
        rect.setScaleX(0.95);
        rect.setScaleY(0.95);
        rect.setArcWidth(SELECTED_RADIUS);
        rect.setArcHeight(SELECTED_RADIUS);
        rect.setFill(fill.saturate());

        InnerShadow is = new InnerShadow();
        is.setOffsetY(1.0);
        is.setOffsetX(1.0);
        is.setColor(fill.darker().darker().darker().saturate());
        rect.setEffect(is);
    }

    private void setDefault(Rectangle rect) {
        Color fill = (Color)rect.getUserData();
        rect.setFill(fill);
        rect.setScaleX(0.9);
        rect.setScaleY(0.9);
        rect.setArcWidth(RADIUS);
        rect.setArcHeight(RADIUS);

        InnerShadow is = new InnerShadow();
        is.setOffsetY(1.0);
        is.setOffsetX(1.0);
        is.setColor(fill.darker());
        rect.setEffect(is);
    }

    private void setDefaultValues() {
        for (var rect : selectedRectangles) {
            setDefault(rect);
        }
        selectedRectangles.clear();
        selectedPoints.clear();
    }

    private String showNameDialog(int result, boolean highScore) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Game over");
        if (highScore) dialog.setHeaderText("New high score: " + result);
        else
            dialog.setHeaderText("Game over. Your score: " + result);

        dialog.setContentText("Please enter your name:");

        Optional<String> name = dialog.showAndWait();
        return name.orElse(null);
    }

    private void showHighScoresDialog() {
        // Create the custom dialog.
        Dialog dialog = new Dialog<>();
        dialog.setTitle("High Scores");
        dialog.setHeaderText("TOP 10 High Scores");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        List<Score> scores = manager.getScores();
        for (int i = 0; i < 10; i++) {
            if (scores.size() > i) {
                grid.add(new Label(scores.get(i).getName()), 0, i);
                Label label = new Label(String.valueOf(scores.get(i).getResult()));
                label.setStyle("-fx-font-weight: bold");
                grid.add(label, 1, i);
            }
        }

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait();
    }

    private void onMousePress(MouseEvent e) {
        if (game.isActive()) {
            game.startTurn();
            game.addPoint(calculateGem(e.getSceneX(), e.getSceneY()));
        }
    }

    private void onMouseRelease() {
        if (game.isActive()) {
            if (game.endTurn()) {
                gameOver();
                int score = game.getScoreProperty().get();
                boolean highScore = true;

                if (manager.getScores().size() > 0) {
                    highScore = manager.getScores().get(0).getResult() < score;
                }

                String name = showNameDialog(score, highScore);
                if (name != null) {
                    manager.appendScore(new Score(name, score));
                    showHighScoresDialog();
                }
                game.resetScore();



            }
            setDefaultValues();
            redrawGame();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}

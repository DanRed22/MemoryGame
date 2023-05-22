package application;
import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;


public class Main extends Application {
    private static final int WIDTH = 1366;
    private static final int HEIGHT = 768;
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    private static final int ROW_COUNT = 8;
    private static final int COL_COUNT = 6;

    public static final int TILE_SIZE = 100;

    private GridPane gridPane = new GridPane();
    private BorderPane root = new BorderPane();
    private Scene scene;
    private static Tile[][] tiles = new Tile[ROW_COUNT][COL_COUNT];
    private static Tile firstTile = null;
    private static Tile secondTile = null;
    private static int remainingPairs;
    private List<String> fruits = new ArrayList<>(Arrays.asList(
            "Apple", "Apple",
            "Carrot", "Carrot",
            "Lemon", "Lemon"
    ));

    private static final Image BACKGROUND_IMAGE = new Image(Main.class.getResource("/res/bg_clouds.png").toExternalForm());
    private static final BackgroundSize BACKGROUND_SIZE = new BackgroundSize(WIDTH, HEIGHT, false, false, false, false);
    private static final Background BACKGROUND = new Background(new BackgroundImage(
            BACKGROUND_IMAGE, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
            BackgroundPosition.DEFAULT, BACKGROUND_SIZE
    ));

    public enum Fruit {
        APPLE,
        CARROT,
        LEMON
    };

    
    private VBox powerUpsBox;
    
    private Text scoreText;
    private Text livesText;
    private Text multiplierText;
    private Text shieldText;
    private int score = 0;
    private boolean gameWon = false;
    
    private Pane overlayPane;
    Button retryButton, exitButton;

    
    private MediaPlayer mediaPlayer;
    
    private static Media chainFeedback;
    private static MediaPlayer chainSound;
    
    private static int multiplier = 1;
    
    private static int lives = 10;
    private static int shield = 0;
    
    
    private String powerUpSFX;
    Media mediaPowerUpSFX;
    MediaPlayer mediaPlayerPowerUpSFX;
    private Stage primaryStage = new Stage();

    @Override
    public void start(Stage stage) {
        try {
            scene = new Scene(root, WIDTH, HEIGHT, BACKGROUND_COLOR);
            
            
            powerUpsBox = new VBox(10);
            powerUpsBox.setAlignment(Pos.CENTER_LEFT);
            powerUpsBox.setPadding(new Insets(10));
            powerUpsBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.7);");
            root.setLeft(powerUpsBox);

            // Create power-ups
            PowerUp shieldPowerUp = new PowerUp(PowerUp.PowerUpType.SHIELD, 500);
            PowerUp lovePotionPowerUp = new PowerUp(PowerUp.PowerUpType.LOVE_POTION, 500);
            PowerUp multiplierPowerUp = new PowerUp(PowerUp.PowerUpType.MULTIPLIER, 300);
            PowerUp thirdEyePowerUp = new PowerUp(PowerUp.PowerUpType.THIRD_EYE, 1000);

            // Add power-ups to the power-ups box
            powerUpsBox.getChildren().addAll(shieldPowerUp, lovePotionPowerUp, multiplierPowerUp, thirdEyePowerUp);

            // Handle power-up clicks
            shieldPowerUp.setOnMouseClicked(event -> handlePowerUpClick(PowerUp.PowerUpType.SHIELD));
            lovePotionPowerUp.setOnMouseClicked(event -> handlePowerUpClick(PowerUp.PowerUpType.LOVE_POTION));
            multiplierPowerUp.setOnMouseClicked(event -> handlePowerUpClick(PowerUp.PowerUpType.MULTIPLIER));
            thirdEyePowerUp.setOnMouseClicked(event -> handlePowerUpClick(PowerUp.PowerUpType.THIRD_EYE));

            
            
         // Load and play the background music
            String musicFilePath = getClass().getResource("/res/bgmusic.mp3").toExternalForm();
            Media musicMedia = new Media(musicFilePath);
            mediaPlayer = new MediaPlayer(musicMedia);
            mediaPlayer.setVolume(0.5);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            
            mediaPlayer.play();
            
            ColumnConstraints gameAreaColumnConstraints = new ColumnConstraints(TILE_SIZE);
            gameAreaColumnConstraints.setHgrow(Priority.ALWAYS);
            gridPane.getColumnConstraints().addAll(gameAreaColumnConstraints);

            RowConstraints gameAreaRowConstraints = new RowConstraints(TILE_SIZE);
            gameAreaRowConstraints.setVgrow(Priority.ALWAYS);
            gridPane.getRowConstraints().addAll(gameAreaRowConstraints);

            gridPane.setStyle("-fx-grid-lines-visible: true; -fx-border-width: 2px;");
            gridPane.setPadding(new Insets(10));
            gridPane.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
                if (gameWon) {
                    //event.consume(); // Consume the event, preventing further interaction
                }
            });
            root.setCenter(gridPane);
            root.setBackground(BACKGROUND);

            for (int x = 0; x < ROW_COUNT; x++) {
                for (int y = 0; y < COL_COUNT; y++) {
                    tiles[x][y] = new Tile(getRandomFruit(), x, y);
                    int finalX = x;
                    int finalY = y;
                    tiles[x][y].setOnMouseClicked(event -> handleTileClick(finalX, finalY, this.scoreText));
                    gridPane.add(tiles[x][y], x, y);
                }
            }

            primaryStage.setTitle("Memory Game 1.0 Beta");
            primaryStage.setResizable(false);
            primaryStage.setFullScreen(false);
            primaryStage.setScene(scene);
            
            String iconPath = getClass().getResource("/res/AppleIcon.png").toExternalForm();
            Image iconImage = new Image(iconPath);
            primaryStage.getIcons().add(iconImage);
            primaryStage.show();

            remainingPairs = ROW_COUNT * COL_COUNT / 2;

            AnimationTimer gameLoop = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    updateGame(now);
                    //remainingPairs = 0;
                }
            };

            gameLoop.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void handlePowerUpClick(PowerUp.PowerUpType powerUpType) {
    	powerUpSFX = "src/res/cancel.wav";
        switch (powerUpType) {
            case SHIELD:
            	
                // Implement shield power-up functionality
            	if (getScore() >= 500) {
            		powerUpSFX = "src/res/shield.mp3";
                    // Apply shield power-up
                    // Deduct points from the score
                    setScore(getScore() - 500);
                    // TODO: Implement shield logic
                    this.shield = 5;
                    
                }
                break;
            case LOVE_POTION:
            	
                // Implement love potion power-up functionality
                if (getScore() >= 500) {
                	powerUpSFX = "src/res/sparkle.mp3";
                    // Apply love potion power-up
                    // Deduct points from the score
                    setScore(getScore() - 500);
                    // TODO: Implement love potion logic
                    this.lives+= 5;
                }
                break;
            case MULTIPLIER:
            	
                // Implement multiplier power-up functionality
                if (getScore() >= 300) {
                	powerUpSFX = "src/res/powerup.mp3";
                    // Apply multiplier power-up
                    // Deduct points from the score
                    setScore(getScore() - 300);
                    // TODO: Implement multiplier logic
                    this.multiplier = 5;
                }
                break;
            case THIRD_EYE:
                // Implement third eye power-up functionality
            	
            	if (getScore() >= 1000) {
            		powerUpSFX = "src/res/thirdeye.mp3";
                    // Apply third eye power-up
                    // Deduct points from the score
                    setScore(getScore() - 1000);
                    // TODO: Implement third eye logic
                    for (int x = 0; x < ROW_COUNT; x++) {
                    	for (int y = 0; y  < COL_COUNT; y++) {
                    		tiles[x][y].reveal();
                    	}
                    }
                    pause(2000, () -> {
                        for (int x = 0; x < ROW_COUNT; x++) {
                            for (int y = 0; y < COL_COUNT; y++) {
                                if (!tiles[x][y].isMatched()) {
                                    tiles[x][y].hide();
                                }
                            }
                        }
                    });
                }
                break;
        }
        try {
        	mediaPowerUpSFX = new Media(new File(powerUpSFX).toURI().toString());
            mediaPlayerPowerUpSFX = new MediaPlayer(mediaPowerUpSFX);
            mediaPlayerPowerUpSFX.setVolume(0.3);
            mediaPlayerPowerUpSFX.play();
        	if (powerUpSFX.equals("src/res/cancel.wav")){
        		showInsufficientCoinsAlert();
        	}
        	
        }catch(Exception e) {
        	e.printStackTrace();
        }
        
    }
    
    private void showInsufficientCoinsAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Insufficient Coins");
        alert.setHeaderText("Not Enough Coins");
        alert.setContentText("You do not have enough coins to use this power-up.");
        alert.showAndWait();
    }

    private void updateGame(long now) {
        
    	if (lives <= 0) {
    		showGameWonOverlay();
    		return;
    	}
        if (gameWon == true) {
        	showGameWonOverlay();
            return;
        }else {
        	root.getChildren().removeAll(scoreText, livesText, multiplierText);
        	livesText = new Text("Lives: "+ lives);
        	livesText.setFont(Font.font(50));
        	livesText.setFill(Color.BLACK);
        	scoreText = new Text("Score: " + score);
            scoreText.setFont(Font.font(50));
            scoreText.setFill(Color.BLACK);
            scoreText.setLayoutX(WIDTH - 300);
            scoreText.setLayoutY(100);
            
            livesText.setLayoutX(WIDTH - 300);
            livesText.setLayoutY(200);
            
            multiplierText = new Text("Current Multipler: " + multiplier+"x");
            multiplierText.setFont(Font.font(20));
            multiplierText.setLayoutX(WIDTH - 300);
            multiplierText.setLayoutY(300);
            
            if (shield >= 0) {
            	root.getChildren().removeAll(shieldText);
            	shieldText = new Text("Current Shield: " + shield);
                shieldText.setFont(Font.font(20));
                shieldText.setLayoutX(WIDTH - 300);
                shieldText.setLayoutY(320);
                root.getChildren().add(shieldText);
            }
            
            
            
            BorderPane.setMargin(scoreText, new Insets(10));
            BorderPane.setAlignment(scoreText, javafx.geometry.Pos.TOP_RIGHT);
            BorderPane.setAlignment(livesText, javafx.geometry.Pos.TOP_RIGHT);
            root.getChildren().addAll(scoreText, livesText, multiplierText);
        }

        if (remainingPairs == 0) {
            gameWon = true;
            showGameWonOverlay();
        }
    }

    private void showGameWonOverlay() {
    	root.getChildren().clear();
        overlayPane = new Pane();
        overlayPane.setLayoutX(500);
        overlayPane.setLayoutY(400);
        overlayPane.setStyle("-fx-background-color: rgba(128, 128, 128, 1);");
        Text gameWonText = new Text("Game Over! Score: " + score);
        gameWonText.setFont(Font.font(50));
        gameWonText.setFill(Color.BLACK);
        overlayPane.getChildren().add(gameWonText);

        retryButton = new Button("Retry");
        retryButton.setFont(Font.font(20));
        retryButton.setLayoutX(100);
        retryButton.setMouseTransparent(true);
        retryButton.setLayoutY(150);
        
       
        retryButton.setOnMouseClicked(event -> {
            // Handle retry button action
            System.out.println("RESET!");
            resetGame();
            primaryStage.setScene(scene);
            overlayPane.setVisible(false);
        });

        exitButton = new Button("Exit");
        
        exitButton.setFont(Font.font(20));
        exitButton.setLayoutX(200);
        exitButton.setLayoutY(150);
        exitButton.setMouseTransparent(true);
        exitButton.setOnMouseClicked(event -> {
            // Handle exit button action
            primaryStage.close();
        });
        retryButton.setMouseTransparent(true);
        exitButton.setMouseTransparent(true);
        overlayPane.getChildren().addAll(retryButton, exitButton);
        root.getChildren().add(overlayPane);
        
    }

    private void resetGame() {
        // Reset game state and variables
        remainingPairs = ROW_COUNT * COL_COUNT / 2;
        score = 0;
        multiplier = 1;
        lives = 10;
        shield = 0;
        gameWon = false;
        tiles = new Tile[ROW_COUNT][COL_COUNT];
        // Re-create tiles and add them to the gridPane
        for (int x = 0; x < ROW_COUNT; x++) {
            for (int y = 0; y < COL_COUNT; y++) {
                tiles[x][y] = new Tile(getRandomFruit(), x, y);
                int finalX = x;
                int finalY = y;
                tiles[x][y].setOnMouseClicked(event -> handleTileClick(finalX, finalY, this.scoreText));
                gridPane.add(tiles[x][y], x, y);
            }
        }
    }



    public void handleTileClick(int x, int y, Text scoreText) {
        if (firstTile != null && secondTile != null) {
            return; // Ignore click when two tiles are already clicked
        }

        Tile clickedTile = tiles[x][y];

        if (clickedTile.isMatched() || clickedTile == firstTile) {
            return; // Ignore click on matched tile or when the same tile is clicked twice
        }

        String soundFilePath = "src/res/Confirm1.wav";
        Media media = new Media(new File(soundFilePath).toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.play();

        if (firstTile == null) {
            firstTile = clickedTile;
            firstTile.reveal();
        } else if (secondTile == null) {
            secondTile = clickedTile;
            secondTile.reveal();
            checkMatchingTiles(scoreText);
        }
    }

    private static void pause(int durationMillis, Runnable action) {
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(durationMillis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            action.run();
        });
        thread.start();
    }

    public void checkMatchingTiles(Text scoreText) {
    	String filepath = "src/res/powerup.mp3";
        if (firstTile.getFruit() == secondTile.getFruit()) {
            String soundFilePath = "src/res/FruitCollect1.wav";
            Media media = new Media(new File(soundFilePath).toURI().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            mediaPlayer.play();

            firstTile.setMatched(true);
            secondTile.setMatched(true);
            score +=  (multiplier*100);
            //System.out.println("SCORE: " + score);
            remainingPairs--;
            firstTile = null;
            secondTile = null;
            multiplier++;
            
            if (multiplier >= 2) {
            	filepath = "src/res/powerup.mp3";
            	chainFeedback = new Media(new File(filepath).toURI().toString());
                chainSound = new MediaPlayer(chainFeedback);
                chainSound.play();
            }
        } else {
        	if (shield > 0) {
        		shield--;
        	}else {
        		shield = -1;
        		multiplier = 1;
        		lives--;
        	}
        	
            String soundFilePath = "src/res/Low_Health.wav";
            Media media = new Media(new File(soundFilePath).toURI().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            mediaPlayer.play();
            
            pause(450, () -> {
                firstTile.hide();
                secondTile.hide();
                firstTile = null;
                secondTile = null;
            });
        }
    }

    private String getRandomFruit() {
        if (fruits.size() == 0) {
            fruits = new ArrayList<>(Arrays.asList(
                    "Apple", "Apple",
                    "Carrot", "Carrot",
                    "Lemon", "Lemon"
            ));
        }
        Random random = new Random();
        int index = random.nextInt(fruits.size());
        return fruits.remove(index);
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    public int getScore() {
    	return this.score;
    }
    
    public void setScore(int newscore) {
    	this.score = newscore;
    }
}

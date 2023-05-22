package application;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class Tile extends StackPane {
    private String fruit;
    private int row;
    private int col;
    private boolean matched;
    private ImageView imageView;
    private static final Color HIDING_COLOR = Color.rgb(128, 128, 128, 0.5); // Partially transparent gray color
    private static final Color REVEAL_COLOR = Color.rgb(128, 128, 128, 0.5); // Partially transparent gray color

    public Tile(String fruit, int row, int col) {
        this.fruit = fruit;
        this.row = row;
        this.col = col;
        this.matched = false;
        this.imageView = new ImageView();

        // Set the size of the image view to match the tile size
        imageView.setFitWidth(Main.TILE_SIZE);
        imageView.setFitHeight(Main.TILE_SIZE);

        // Initially set the background color to the hiding color
        setBackground(new Background(new BackgroundFill(HIDING_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));

        // Add the image view to the tile
        getChildren().add(imageView);
    }

    public String getFruit() {
        return fruit;
    }

    public void reveal() {
        // Load the fruit sprite image based on the fruit type
        String imageName = fruit+ ".png";
        Image image = new Image("file:src/res/" + imageName);

        // Set the image in the image view
        imageView.setImage(image);

        // Set the background color to transparent
        setBackground(new Background(new BackgroundFill(REVEAL_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));
    }

    public void hide() {
        // Set the background color to the hiding color
    	imageView.setImage(null);
        setBackground(new Background(new BackgroundFill(HIDING_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));

    }

    public boolean isMatched() {
        return matched;
    }

    public void setMatched(boolean matched) {
        this.matched = matched;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }
}

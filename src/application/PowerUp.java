package application;

import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class PowerUp extends VBox {
    private PowerUpType type;
    private int price;
    private ImageView icon;
    private Text priceText;
    
    public PowerUp(PowerUpType type, int price) {
        this.type = type;
        this.price = price;
        icon = new ImageView(type.getImage());
        icon.setFitWidth(50);
        icon.setFitHeight(50);
        icon.setPreserveRatio(true);
        icon.setSmooth(true);

        priceText = new Text(String.valueOf(this.price));
        priceText.setStyle("-fx-font-size: 12px;");

        getChildren().addAll(icon, priceText);
        setAlignment(Pos.CENTER);
    }

    public PowerUpType getType() {
        return type;
    }
    
    public int getPrice() {
    	return this.price;
    }

    public enum PowerUpType {
        SHIELD("/res/shield.png"),
        LOVE_POTION("/res/heal.png"),
        MULTIPLIER("/res/multiplier.png"),
        THIRD_EYE("/res/thirdeye.png");

        private String imagePath;

        PowerUpType(String imagePath) {
            this.imagePath = imagePath;
        }

        public Image getImage() {
            return new Image(getClass().getResourceAsStream(imagePath));
        }
    }
}

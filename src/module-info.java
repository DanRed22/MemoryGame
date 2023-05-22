module MemoryGame {
	requires javafx.controls;
	requires javafx.graphics;
	requires javafx.base;
	requires javafx.media;
	
	opens application to javafx.graphics, javafx.fxml;
}

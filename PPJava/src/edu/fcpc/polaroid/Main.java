package edu.fcpc.polaroid;

import java.io.IOException;

import edu.fcpc.polaroid.windows.NewUserWindow;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Main extends Application {
	private GraphicsContext graphicsContext;
	private Screen screen;
	private Image[] image = {null, null, null};

	public static void main(String[] args) throws IOException {
	    launch(args);	
	}
	
	public void start(Stage primaryStage){
		SQLHelper.prepareConnection();
		
		screen = Screen.getPrimary();
		primaryStage.setFullScreen(true);
		primaryStage.setOnCloseRequest(e -> System.exit(0));
		Canvas canvas = new Canvas(screen.getBounds().getWidth(), screen.getBounds().getHeight());
		graphicsContext = canvas.getGraphicsContext2D();
		StackPane borderPane = new StackPane();
		borderPane.getChildren().add(canvas);
		Scene scene = new Scene(borderPane, 300, 250);
		scene.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>(){
			@Override
			public void handle(KeyEvent event) {
				switch(event.getCode()){
					case ESCAPE:
						// Close the program
						System.exit(0);
						break;
					case N:
						// Show the new user create screen
						new NewUserWindow();
						break;
					case D:
						// Delete a users
						break;
					default:
						// Do nothing
				}
			}
		});
		
		primaryStage.setScene(scene);
		//primaryStage.show();
		
		clearScreen();
		
		WiFiHelper bluetoothHelper = new WiFiHelper(this);
		Thread thread = new Thread(bluetoothHelper);
		thread.start();
	}

	public void clearScreen(){
		graphicsContext.setFont(new Font("sans-serif", 25));
		graphicsContext.setTextAlign(TextAlignment.CENTER);
		graphicsContext.setTextBaseline(VPos.CENTER);
		
		// Clear the screen
		graphicsContext.setFill(Paint.valueOf("black"));
		graphicsContext.fillRect(0, 0, screen.getBounds().getWidth(), screen.getBounds().getHeight());

		// Draw white rectangles
		graphicsContext.setFill(Paint.valueOf("white"));
		double xSpacing = (screen.getBounds().getWidth() - 900) / 4;
		double ySpacing = (screen.getBounds().getHeight() - 360) / 2;
		graphicsContext.fillRect(xSpacing            , ySpacing, 300, 360);
		graphicsContext.fillRect((xSpacing * 2) + 300, ySpacing, 300, 360);
		graphicsContext.fillRect((xSpacing * 3) + 600, ySpacing, 300, 360);
		
		// Draw blanking plates
		graphicsContext.setFill(Paint.valueOf("black"));
		graphicsContext.fillRect(xSpacing             + 10, ySpacing + 10, 280, 280);
		graphicsContext.fillRect((xSpacing * 2) + 300 + 10, ySpacing + 10, 280, 280);
		graphicsContext.fillRect((xSpacing * 3) + 600 + 10, ySpacing + 10, 280, 280);
		
		// Draw images
		if (this.image[0] != null)
			graphicsContext.drawImage(this.image[0], xSpacing             + 10, ySpacing + 10, 280, 280);
		if (this.image[1] != null)
			graphicsContext.drawImage(this.image[1], (xSpacing * 2) + 300 + 10, ySpacing + 10, 280, 280);
		if (this.image[2] != null)
			graphicsContext.drawImage(this.image[2], (xSpacing * 3) + 600 + 10, ySpacing + 10, 280, 280);
	}
	
	public void writeError(String message){
		clearScreen();
		graphicsContext.setFill(Paint.valueOf("red"));
		graphicsContext.fillText(message, screen.getBounds().getWidth()/2, screen.getBounds().getHeight()/8);
	}
	
	public void writeMessage(String message){
		clearScreen();
		graphicsContext.setFill(Paint.valueOf("white"));
		graphicsContext.fillText(message, screen.getBounds().getWidth()/2, screen.getBounds().getHeight()/8);
	}
	
	public void setImage(Image image){
//		writeMessage("Resizing... ");
//		ImageView tempImage = new ImageView(image);
//		tempImage.setPreserveRatio(true);
//		tempImage.setFitHeight(260);
//		Image shrinkImage = tempImage.snapshot(null, null);
//		writeMessage("Cropping... ");
//		Rectangle2D croppingViewport = new Rectangle2D((shrinkImage.getWidth()-280)/2, 0, 280, 280);
//		tempImage = new ImageView(shrinkImage);
//		tempImage.setViewport(croppingViewport);
//		Image cropImage = tempImage.snapshot(null, null);
		
		writeMessage("Setting arrays... ");
		this.image[2] = this.image[1];
		this.image[1] = this.image[0];
		this.image[0] = image;
		
		//this.image = image;
		clearScreen();
	}
}
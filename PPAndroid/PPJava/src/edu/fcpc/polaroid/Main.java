package edu.fcpc.polaroid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Panel;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;

public class Main extends Frame {
    private Panel panel;
    private Graphics graphics;
    private Logger logger = LoggerFactory.getLogger(Main.class);

    public Main() {
        super();
        panel = new Panel();
        graphics = panel.getGraphics();

        add(panel);
        pack();
        setExtendedState(Frame.MAXIMIZED_BOTH);
        //setUndecorated(true);
        setVisible(true);

        // Start running the helper
        WiFiHelper bluetoothHelper = new WiFiHelper(this);
        Thread thread = new Thread(bluetoothHelper);
        thread.start();
    }

    public static void main(String... args) {
        new Main();
    }

    public void setImage(byte[] imageByteArray) {
        Graphics2D g2D = (Graphics2D) graphics;
        try {
            InputStream in = new ByteArrayInputStream(imageByteArray);
            BufferedImage image = ImageIO.read(in);
            g2D.drawImage(image, 0, 0, Main.this);
            g2D.setPaint(Color.BLACK);
            panel.paintComponents(g2D);
        } catch (Exception e) {
            logger.error(e.getMessage());
            System.exit(-1);
        }
    }


//	private GraphicsContext graphicsContext;
//	private Screen screen;
//	private Image[] image = {null, null, null};
//
//	public static void main(String[] args) throws IOException {
//	    launch(args);
//	}
//
//	public void start(Stage primaryStage){
//		// Prepare the SQL
//		SQLHelper.prepareConnection();
//
//		screen = Screen.getPrimary();
//		primaryStage.setFullScreen(true);
//		primaryStage.setOnCloseRequest(e -> System.exit(0));
//		Canvas canvas = new Canvas(screen.getBounds().getWidth(), screen.getBounds().getHeight());
//		graphicsContext = canvas.getGraphicsContext2D();
//		StackPane borderPane = new StackPane();
//		borderPane.getChildren().add(canvas);
//		Scene scene = new Scene(borderPane, 300, 250);
//		scene.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>(){
//			@Override
//			public void handle(KeyEvent event) {
//				switch(event.getCode()){
//					case ESCAPE:
//						// Close the program
//						System.exit(0);
//						break;
//					case N:
//						// Show the new user create screen
//						new NewUserWindow();
//						break;
//					case D:
//						// Delete a users
//						break;
//					default:
//						// Do nothing
//				}
//			}
//		});
//
//		primaryStage.setScene(scene);
//		//primaryStage.show();
//
//		clearScreen();
//
//		WiFiHelper bluetoothHelper = new WiFiHelper(this);
//		Thread thread = new Thread(bluetoothHelper);
//		thread.start();
//	}
//
//	public void clearScreen(){
//		graphicsContext.setFont(new Font("sans-serif", 25));
//		graphicsContext.setTextAlign(TextAlignment.CENTER);
//		graphicsContext.setTextBaseline(VPos.CENTER);
//
//		// Clear the screen
//		graphicsContext.setFill(Paint.valueOf("black"));
//		graphicsContext.fillRect(0, 0, screen.getBounds().getWidth(), screen.getBounds().getHeight());
//
//		// Draw white rectangles
//		graphicsContext.setFill(Paint.valueOf("white"));
//		double xSpacing = (screen.getBounds().getWidth() - 900) / 4;
//		double ySpacing = (screen.getBounds().getHeight() - 360) / 2;
//		graphicsContext.fillRect(xSpacing            , ySpacing, 300, 360);
//		graphicsContext.fillRect((xSpacing * 2) + 300, ySpacing, 300, 360);
//		graphicsContext.fillRect((xSpacing * 3) + 600, ySpacing, 300, 360);
//
//		// Draw blanking plates
//		graphicsContext.setFill(Paint.valueOf("black"));
//		graphicsContext.fillRect(xSpacing             + 10, ySpacing + 10, 280, 280);
//		graphicsContext.fillRect((xSpacing * 2) + 300 + 10, ySpacing + 10, 280, 280);
//		graphicsContext.fillRect((xSpacing * 3) + 600 + 10, ySpacing + 10, 280, 280);
//
//		// Draw images
//		if (this.image[0] != null)
//			graphicsContext.drawImage(this.image[0], xSpacing             + 10, ySpacing + 10, 280, 280);
//		if (this.image[1] != null)
//			graphicsContext.drawImage(this.image[1], (xSpacing * 2) + 300 + 10, ySpacing + 10, 280, 280);
//		if (this.image[2] != null)
//			graphicsContext.drawImage(this.image[2], (xSpacing * 3) + 600 + 10, ySpacing + 10, 280, 280);
//	}
//
//	public void writeError(String message){
//		clearScreen();
//		graphicsContext.setFill(Paint.valueOf("red"));
//		graphicsContext.fillText(message, screen.getBounds().getWidth()/2, screen.getBounds().getHeight()/8);
//	}
//
//	public void writeMessage(String message){
//		clearScreen();
//		graphicsContext.setFill(Paint.valueOf("white"));
//		graphicsContext.fillText(message, screen.getBounds().getWidth()/2, screen.getBounds().getHeight()/8);
//	}
//
//	public void setImage(Image image){
//		writeMessage("Setting arrays... ");
//		this.image[2] = this.image[1];
//		this.image[1] = this.image[0];
//		this.image[0] = image;
//
//		clearScreen();
//	}
}
package edu.fcpc.polaroid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Label;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;

public class Main extends Frame {
    private Label label = new Label("", Label.CENTER);
    private Logger logger = LoggerFactory.getLogger(Main.class);
    private BufferedImage[] image = new BufferedImage[]{null, null, null};

    public Main() {
        super();
        setBackground(Color.BLACK);

        // Initialize components
        label.setForeground(Color.GREEN);
        label.setFont(new Font("San-Serif", Font.PLAIN, 96));
        label.setText("Starting PPJava...");
        add(label);
        pack();
        setExtendedState(Frame.MAXIMIZED_BOTH);
        setVisible(true);

        // Prepare SQL database
        label.setText("Starting SQL...");
        SQLHelper.prepareConnection();

        // Start running the helper
        label.setText("Starting background daemons...");
        WiFiHelper bluetoothHelper = new WiFiHelper(this);
        Thread thread = new Thread(bluetoothHelper);
        thread.start();
    }

    public void removeStatus() {
        // Display the stage
        remove(label);
        pack();
        setExtendedState(Frame.MAXIMIZED_BOTH);
        setVisible(true);
        clearScreen();
    }

    public static void main(String... args) {
        new Main();
    }

    private int accessor = 0;
    public void setImage(byte[] imageByteArray) {
        try {
            InputStream in = new ByteArrayInputStream(imageByteArray);
            BufferedImage image = ImageIO.read(in);
            this.image[accessor] = image;
            accessor++;
            if(accessor > 3)
                accessor = 0;
        } catch (Exception e) {
            logger.error(e.getMessage());
            System.exit(-1);
        }
        clearScreen();
    }

    public void clearScreen() {
        Graphics2D g2D = (Graphics2D) getGraphics();
        g2D.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // Clear the screen
        g2D.setPaint(Color.BLACK);
        g2D.fillRect(0, 0, getWidth(), getHeight());

        // Draw white rectangles
        g2D.setPaint(Color.WHITE);
        int xSpacing = (int) (getWidth() - 900) / 4;
        int ySpacing = (int) (getHeight() - 360) / 2;
        g2D.fillRect(xSpacing, ySpacing, 300, 360);
        g2D.fillRect((xSpacing * 2) + 300, ySpacing, 300, 360);
        g2D.fillRect((xSpacing * 3) + 600, ySpacing, 300, 360);

        // Draw blanking plates
        g2D.setPaint(Color.BLACK);
        g2D.fillRect(xSpacing + 10, ySpacing + 10, 280, 280);
        g2D.fillRect((xSpacing * 2) + 300 + 10, ySpacing + 10, 280, 280);
        g2D.fillRect((xSpacing * 3) + 600 + 10, ySpacing + 10, 280, 280);

        // Draw images
        if (this.image[0] != null)
            g2D.drawImage(this.image[0], xSpacing + 10, ySpacing + 10, 280, 280, Main.this);
        if (this.image[1] != null)
            g2D.drawImage(this.image[1], (xSpacing * 2) + 300 + 10, ySpacing + 10, 280, 280, Main.this);
        if (this.image[2] != null)
            g2D.drawImage(this.image[2], (xSpacing * 3) + 600 + 10, ySpacing + 10, 280, 280, Main.this);
    }
}
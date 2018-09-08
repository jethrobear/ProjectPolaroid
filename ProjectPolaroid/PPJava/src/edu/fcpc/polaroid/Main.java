package edu.fcpc.polaroid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import edu.fcpc.polaroid.helper.SQLHelper;

public class Main extends JFrame {
    private JPanel panel = new JPanel();
    private JLabel label = new JLabel("", SwingConstants.CENTER);
    private Logger logger = LoggerFactory.getLogger(Main.class);

    public Main() {
        super();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBackground(Color.BLACK);

        // Initialize components
        label.setForeground(Color.GREEN);
        label.setFont(new Font("San-Serif", Font.PLAIN, 96));
        label.setText("Starting PPJava...");
        add(label);
        pack();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
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
        panel.setBackground(Color.BLACK);
        add(panel);
        pack();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
    }

    public static void main(String... args) {
        new Main();
    }

    public void setImage(byte[] imageByteArray) {
        panel.repaint();

        Graphics2D g2D = (Graphics2D) panel.getGraphics();
        try {
            InputStream in = new ByteArrayInputStream(imageByteArray);
            BufferedImage image = ImageIO.read(in);

            float panelWidth, panelHeight, marginLeft, marginTop;
            if (image.getWidth() > image.getHeight()) {
                panelWidth = panel.getWidth();
                panelHeight = image.getHeight() / ((float) image.getWidth() / (float) panel.getWidth());
            } else {
                panelWidth = image.getWidth() / ((float) image.getHeight() / (float) panel.getHeight());
                panelHeight = panel.getHeight();
            }
            marginLeft = (panel.getWidth() - panelWidth) / 2;
            marginTop = (panel.getHeight() - panelHeight) / 2;

            g2D.setPaint(Color.BLACK);
            g2D.fillRect(0, 0, panel.getWidth(), panel.getHeight());
            g2D.drawImage(image, (int) marginLeft, (int) marginTop, (int) panelWidth, (int) panelHeight, Main.this);

            panel.paintComponents(g2D);
        } catch (Exception e) {
            logger.error(e.getMessage());
            System.exit(-1);
        }
    }
}
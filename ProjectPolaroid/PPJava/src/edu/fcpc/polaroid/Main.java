package edu.fcpc.polaroid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Main extends JFrame {
    private JPanel panel = new JPanel();
    private JLabel label;
    private Logger logger = LoggerFactory.getLogger(Main.class);

    public Main() throws MalformedURLException {
        super();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBackground(Color.BLACK);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);

        // Initialize components
        File file = new File(".", "loading.gif");
        URL url = file.toURI().toURL();
        logger.info(file.getAbsolutePath());
        Icon icon = new ImageIcon(url);
        label = new JLabel(icon);
        label.setSize(800, 600);
        add(label);
        pack();
        setLocationRelativeTo(null);

        // Start running the helper
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

    public static void main(String... args) throws MalformedURLException {
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
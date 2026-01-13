package novalynx;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;

public class WelcomeScreen {
    private JFrame frame;
    private Timer fadeTimer;
    private float opacity = 0f;
    private JPanel mainPanel;
    private Image logoImage;

    public void launch() {
        frame = new JFrame("NovaLynx");
        NovaTheme.applyFrameSettings(frame);
        frame.setUndecorated(true); // Remove window decorations for sleek look

        // Load logo
        try {
            URL logoUrl = getClass().getResource("/assets/logo.png");
            if (logoUrl == null) {
                logoUrl = new java.io.File("src/assets/logo.png").toURI().toURL();
            }
            logoImage = new ImageIcon(logoUrl).getImage();
        } catch (Exception e) {
            System.err.println("Could not load logo for welcome screen.");
        }

        // Create main panel with custom painting for fade effect
        mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                // Set opacity
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

                // Create gradient background
                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(30, 40, 60),
                        0, getHeight(), new Color(15, 20, 30));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Draw logo if loaded
                if (logoImage != null) {
                    int logoWidth = 150;
                    int logoHeight = 150;
                    int logoX = (getWidth() - logoWidth) / 2;
                    int logoY = getHeight() / 2 - 220;
                    g2d.drawImage(logoImage, logoX, logoY, logoWidth, logoHeight, null);
                }

                // Draw welcome text
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 48));
                g2d.setColor(new Color(220, 230, 255));
                String welcomeText = "NovaLynx";
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(welcomeText)) / 2;
                int textY = getHeight() / 2 - 50;
                g2d.drawString(welcomeText, textX, textY);

                // Draw subtitle
                g2d.setFont(new Font("Segoe UI Light", Font.PLAIN, 24));
                g2d.setColor(new Color(180, 190, 210));
                String subtitleText = "Next-generation OS Virtualization Architecture";
                fm = g2d.getFontMetrics();
                textX = (getWidth() - fm.stringWidth(subtitleText)) / 2;
                textY = getHeight() / 2 + 20;
                g2d.drawString(subtitleText, textX, textY);

                g2d.dispose();
            }
        };
        mainPanel.setBackground(new Color(30, 40, 60));
        frame.add(mainPanel);

        // Create fade-in effect
        fadeTimer = new Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                opacity += 0.05f;
                if (opacity >= 1f) {
                    opacity = 1f;
                    fadeTimer.stop();
                    // Wait for 2 seconds then transition to control panel
                    Timer transitionTimer = new Timer(2000, evt -> {
                        frame.dispose();
                        SwingUtilities.invokeLater(() -> {
                            ControlPanel panel = new ControlPanel();
                            panel.launch();
                        });
                    });
                    transitionTimer.setRepeats(false);
                    transitionTimer.start();
                }
                mainPanel.repaint();
            }
        });

        frame.setVisible(true);
        fadeTimer.start();
    }
}
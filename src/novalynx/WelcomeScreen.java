/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package novalynx;

/**
 *
 * @author DELL
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class WelcomeScreen {
    private JFrame frame;
    private Timer fadeTimer;
    private float opacity = 0f;
    private JPanel mainPanel;
    private Image logoImage; 
    
    // --- Updated NovaLynx Color Palette ---
    private final Color DEEP_SPACE = new Color(11, 11, 30);      
    private final Color CYAN_BRIGHT = new Color(0, 210, 255);    
    private final Color PURPLE_VIBE = new Color(122, 43, 255);   
    private final Color ACCENT_ORANGE = new Color(255, 184, 0);  

    public void launch() {
        frame = new JFrame("NovaLynx Operating System");
        frame.setSize(1000, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setUndecorated(true); 

        // Load and scale the logo image
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/assets/logo.png"));
            // Scaling the logo to 150x150 for the welcome screen
            logoImage = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
            frame.setIconImage(icon.getImage());
        } catch (Exception e) {
            System.out.println("Logo file not found, using text-only welcome.");
        }

        mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
                
                // Background Gradient
                GradientPaint gradient = new GradientPaint(0, 0, DEEP_SPACE, getWidth(), getHeight(), new Color(20, 20, 45));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;

                // 1. Draw Logo Image (if loaded)
                if (logoImage != null) {
                    int logoX = centerX - (logoImage.getWidth(null) / 2);
                    int logoY = centerY - 220; // Positioned above the text
                    g2d.drawImage(logoImage, logoX, logoY, null);
                }

                // 2. Draw Title: NovaLynx
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 72));
                String welcomeText = "NovaLynx";
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(welcomeText)) / 2;
                int textY = centerY + 20;

                GradientPaint textGradient = new GradientPaint(textX, 0, CYAN_BRIGHT, textX + fm.stringWidth(welcomeText), 0, PURPLE_VIBE);
                g2d.setPaint(textGradient);
                g2d.drawString(welcomeText, textX, textY);

                // 3. Draw Subtitle
                g2d.setFont(new Font("Segoe UI Semilight", Font.PLAIN, 18));
                g2d.setColor(ACCENT_ORANGE);
                String subtitleText = "NEXT-GENERATION COMPUTING INTERFACE";
                fm = g2d.getFontMetrics();
                g2d.drawString(subtitleText, (getWidth() - fm.stringWidth(subtitleText)) / 2, textY + 50);

                g2d.dispose();
            }
        };
        
        frame.add(mainPanel);

        fadeTimer = new Timer(30, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                opacity += 0.03f;
                if (opacity >= 1f) {
                    opacity = 1f;
                    fadeTimer.stop();
                    
                    Timer transitionTimer = new Timer(2500, evt -> {
                        frame.dispose();
                        SwingUtilities.invokeLater(() -> new ControlPanel().launch());
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
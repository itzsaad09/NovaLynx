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

public class ControlPanel {
    private JFrame frame;
    private static ProcessManagement processManagement; 
    private Image logoImage; 
    
    // --- NovaLynx Color Palette (Matched to Logo) ---
    private final Color DEEP_SPACE = new Color(11, 11, 30);      //
    private final Color CYAN_BRIGHT = new Color(0, 210, 255);    // Nova Cyan
    private final Color PURPLE_VIBE = new Color(122, 43, 255);   // Lynx Purple
    private final Color ACCENT_ORANGE = new Color(255, 184, 0);  // Star Gold
    private final Color SURFACE_DARK = new Color(25, 25, 50);    //

    public ControlPanel() {
        if (processManagement == null) {
            processManagement = new ProcessManagement();
        }
    }

    public void launch() {
        frame = new JFrame("NovaLynx OS - Control Center");
        frame.setSize(1000, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/assets/logo.png"));
            // Scaling the logo to 150x150 for the welcome screen
            logoImage = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
            frame.setIconImage(icon.getImage());
        } catch (Exception e) {
            System.out.println("Logo file not found, using text-only welcome.");
        }

        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                // Brand Gradient Background
                g2d.setPaint(new GradientPaint(0, 0, DEEP_SPACE, 0, getHeight(), new Color(5, 5, 15)));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };

        // --- Header Section ---
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(40, 0, 20, 0));

        try {
            ImageIcon rawLogo = new ImageIcon(getClass().getResource("/assets/logo.png"));
            Image scaledLogo = rawLogo.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            JLabel logoLabel = new JLabel(new ImageIcon(scaledLogo));
            logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            headerPanel.add(logoLabel);
            headerPanel.add(Box.createVerticalStrut(15));
        } catch (Exception e) {}

        JLabel title = new JLabel("NovaLynx Control Center");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(title);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // --- Buttons Section ---
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.setOpaque(false);
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(30, 100, 40, 100));

        // Loading Icons (Ensure these exist in your /assets/ folder)
        ImageIcon procIcon = prepareIcon("/assets/process.png", 30, 30);
        ImageIcon memIcon = prepareIcon("/assets/memory-card.png", 30, 30);
        ImageIcon syncIcon = prepareIcon("/assets/sync.png", 30, 30);

        // Creating Pill Buttons with Icons and Logo Colors
        JButton processBtn = createPillButton("Process Management", CYAN_BRIGHT, procIcon);
        JButton memoryBtn = createPillButton("Memory Management", PURPLE_VIBE, memIcon);
        JButton syncBtn = createPillButton("Process Synchronization", ACCENT_ORANGE, syncIcon);

        // Action for Process Management
        processBtn.addActionListener(e -> {
            frame.dispose();
            // processManagement.launch(); 
        });

        // Layout placement
        buttonsPanel.add(processBtn);
        buttonsPanel.add(Box.createVerticalStrut(25));
        buttonsPanel.add(memoryBtn);
        buttonsPanel.add(Box.createVerticalStrut(25));
        buttonsPanel.add(syncBtn);

        mainPanel.add(buttonsPanel, BorderLayout.CENTER);
        frame.setContentPane(mainPanel);
        frame.setVisible(true);
    }

    /**
     * Helper to load and scale icons correctly
     */
    private ImageIcon prepareIcon(String path, int w, int h) {
        try {
            java.net.URL imgURL = getClass().getResource(path);
            if (imgURL != null) {
                ImageIcon icon = new ImageIcon(imgURL);
                Image img = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
                return new ImageIcon(img);
            }
        } catch (Exception e) {
            System.out.println("Icon not found: " + path);
        }
        return null;
    }

    /**
     * Custom styled button with NovaLynx glow and icon support
     */
    private JButton createPillButton(String text, Color glowColor, ImageIcon icon) {
        JButton button = new JButton(text, icon) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Background
                g2.setColor(SURFACE_DARK);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 35, 35);
                
                // Branded Border Glow
                g2.setStroke(new BasicStroke(2f));
                g2.setColor(glowColor);
                g2.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 35, 35);
                
                super.paintComponent(g2);
                g2.dispose();
            }
        };

        button.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 18));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setIconTextGap(25); // Space between icon and text
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(BorderFactory.createEmptyBorder(0, 40, 0, 0));
        
        button.setMaximumSize(new Dimension(550, 65));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover Effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { button.setForeground(glowColor); }
            @Override
            public void mouseExited(MouseEvent e) { button.setForeground(Color.WHITE); }
        });

        return button;
    }
}
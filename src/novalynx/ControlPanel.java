package novalynx;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.List;

public class ControlPanel {
    private JFrame frame;
    private static ProcessManagement processManagement;

    public ControlPanel() {
        if (processManagement == null) {
            processManagement = new ProcessManagement();
        }
    }

    public void launch() {
        frame = new JFrame();
        frame.setTitle("NovaLynx - Control Panel");
        NovaTheme.applyFrameSettings(frame);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(0, 0));
        mainPanel.setBackground(NovaTheme.DEEP_NAVY);

        // Custom Title Bar
        JLabel title = new JLabel("Welcome to NovaLynx", SwingConstants.CENTER);
        title.setFont(NovaTheme.HEADER_FONT);
        title.setForeground(NovaTheme.NOVA_PURPLE);
        title.setBorder(BorderFactory.createEmptyBorder(40, 0, 30, 0));
        mainPanel.add(title, BorderLayout.NORTH);

        // Buttons Panel
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.setOpaque(false);
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(0, 100, 40, 100));

        // Create modern buttons with icons
        JButton processBtn = createPillButton("Process Management", new Color(255, 121, 121), "/assets/process.png");
        JButton memoryBtn = createPillButton("Memory Management", new Color(0, 184, 148), "/assets/memory-card.png");
        JButton syncBtn = createPillButton("Process Synchronization", new Color(9, 132, 227), "/assets/sync.png");

        // Add buttons with spacing
        addButtonWithSpacing(buttonsPanel, processBtn);
        addButtonWithSpacing(buttonsPanel, memoryBtn);
        addButtonWithSpacing(buttonsPanel, syncBtn);

        // Footer
        JLabel footer = new JLabel("Developed by Faiqa Riaz & Hafiz Muhammad Saad", SwingConstants.CENTER);
        footer.setFont(NovaTheme.SUBHEADER_FONT);
        footer.setForeground(NovaTheme.LYNX_BLUE);
        footer.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(buttonsPanel, BorderLayout.CENTER);
        centerPanel.add(footer, BorderLayout.SOUTH);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Button Actions
        processBtn.addActionListener(e -> {
            frame.dispose();
            processManagement.launch();
        });

        memoryBtn.addActionListener(e -> {
            frame.dispose();
            new MemoryManagement().launch();
        });

        syncBtn.addActionListener(e -> {
            frame.dispose();
            List<PCB> pcbList = processManagement.getProcessList();
            SynchronizationPanel syncPanel = new SynchronizationPanel(pcbList);
            syncPanel.launch();
        });

        frame.setContentPane(mainPanel);
        frame.setVisible(true);
    }

    private void addButtonWithSpacing(JPanel panel, JButton button) {
        panel.add(button);
        panel.add(Box.createVerticalStrut(20));
    }

    private JButton createPillButton(String text, Color color, String iconPath) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 50, 50);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        button.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 20));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorder(BorderFactory.createEmptyBorder(15, 40, 15, 40));
        button.setMaximumSize(new Dimension(400, 60));
        button.setPreferredSize(new Dimension(400, 60));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Load and resize icon
        try {
            URL iconUrl = getClass().getResource(iconPath);
            if (iconUrl == null) {
                // Fallback for IDE
                iconUrl = new java.io.File("src/" + iconPath).toURI().toURL();
            }
            if (iconUrl != null) {
                ImageIcon originalIcon = new ImageIcon(iconUrl);
                Image img = originalIcon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
                button.setIcon(new ImageIcon(img));
                button.setIconTextGap(15);
            }
        } catch (Exception e) {
            System.err.println("Could not load icon: " + iconPath);
        }

        return button;
    }
}

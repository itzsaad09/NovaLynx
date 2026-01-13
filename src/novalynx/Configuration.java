package novalynx;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Properties;

public class Configuration {
    private JFrame frame;
    private JTextArea configArea;
    private Properties config;

    public Configuration() {
        config = new Properties();
        loadConfiguration();
    }

    public void launch() {
        frame = new JFrame();
        frame.setTitle("NovaLynx - Configuration");
        NovaTheme.applyFrameSettings(frame);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(NovaTheme.DEEP_NAVY);

        // Custom Title Bar
        JLabel title = new JLabel("System Configuration", SwingConstants.CENTER);
        title.setFont(NovaTheme.HEADER_FONT);
        title.setForeground(NovaTheme.NOVA_PURPLE);
        title.setBorder(BorderFactory.createEmptyBorder(24, 0, 16, 0));
        mainPanel.add(title, BorderLayout.NORTH);

        // Content Panel
        JPanel contentPanel = new JPanel(new BorderLayout(24, 24));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 32, 24, 32));

        // Configuration Card
        JPanel configCard = createCardPanel("Configuration Settings", new Color(9, 132, 227));
        configArea = new JTextArea();
        configArea.setEditable(true);
        configArea.setFont(NovaTheme.TERMINAL_FONT);
        configArea.setBackground(NovaTheme.DARK_CARD);
        configArea.setForeground(Color.WHITE);
        configArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        configArea.setText(getConfigurationText());
        JScrollPane configScrollPane = new JScrollPane(configArea);
        configScrollPane.setBorder(null);
        configCard.add(configScrollPane, BorderLayout.CENTER);

        // Buttons Panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 16));
        buttonsPanel.setOpaque(false);
        JButton saveButton = createPillButton("Save Configuration", new Color(0, 184, 148));
        JButton loadButton = createPillButton("Load Configuration", new Color(255, 121, 121));
        JButton resetButton = createPillButton("Reset to Default", new Color(253, 121, 168));
        JButton backButton = createPillButton("Back", new Color(155, 89, 182));

        buttonsPanel.add(saveButton);
        buttonsPanel.add(loadButton);
        buttonsPanel.add(resetButton);
        buttonsPanel.add(backButton);

        saveButton.addActionListener(e -> saveConfiguration());
        loadButton.addActionListener(e -> loadConfiguration());
        resetButton.addActionListener(e -> resetConfiguration());
        backButton.addActionListener(e -> {
            if (frame != null) {
                frame.dispose();
                new ControlPanel().launch();
            }
        });

        contentPanel.add(configCard, BorderLayout.CENTER);
        contentPanel.add(buttonsPanel, BorderLayout.SOUTH);

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        frame.setContentPane(mainPanel);
        frame.setVisible(true);
    }

    private JPanel createCardPanel(String title, Color accent) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(NovaTheme.DARK_CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accent, 2, true),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        JLabel header = new JLabel(title, SwingConstants.CENTER);
        header.setFont(NovaTheme.SUBHEADER_FONT);
        header.setForeground(accent);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        panel.add(header, BorderLayout.NORTH);
        panel.setOpaque(true);
        return panel;
    }

    private JButton createPillButton(String text, Color color) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        button.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void loadConfiguration() {
        try {
            File file = new File("config.properties");
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                config.load(fis);
                fis.close();
                configArea.setText(getConfigurationText());
                JOptionPane.showMessageDialog(frame, "Configuration loaded successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, "Configuration file not found!", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error loading configuration: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveConfiguration() {
        try {
            String configText = configArea.getText();
            String[] lines = configText.split("\n");
            config.clear();

            for (String line : lines) {
                if (line.contains("=")) {
                    String[] parts = line.split("=", 2);
                    config.setProperty(parts[0].trim(), parts[1].trim());
                }
            }

            FileOutputStream fos = new FileOutputStream("config.properties");
            config.store(fos, "NovaLynx Configuration");
            fos.close();

            JOptionPane.showMessageDialog(frame, "Configuration saved successfully!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error saving configuration: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetConfiguration() {
        config.clear();
        config.setProperty("default.algorithm", "Round Robin");
        config.setProperty("time.quantum", "2");
        config.setProperty("memory.size", "1024");
        configArea.setText(getConfigurationText());
        JOptionPane.showMessageDialog(frame, "Configuration reset to default values!", "Success",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private String getConfigurationText() {
        StringBuilder sb = new StringBuilder();
        sb.append("default.algorithm=").append(config.getProperty("default.algorithm", "Round Robin")).append("\n");
        sb.append("time.quantum=").append(config.getProperty("time.quantum", "2")).append("\n");
        sb.append("memory.size=").append(config.getProperty("memory.size", "1024")).append("\n");
        return sb.toString();
    }
}

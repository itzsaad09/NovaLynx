package novalynx;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MemoryManagementPanel extends JPanel {
    private JFrame frame;
    private JList<String> processList;
    private DefaultListModel<String> processListModel;
    private JTextArea memoryStatusArea;
    private JPanel memoryVisualizationPanel;
    private MemoryManager memoryManager;
    private List<PCB> processes;

    public MemoryManagementPanel(List<PCB> processes, MemoryManager memoryManager) {
        this.processes = processes;
        this.memoryManager = memoryManager;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(245, 246, 250));

        // Custom Title Bar
        JLabel title = new JLabel("💾 Memory Management", SwingConstants.CENTER);
        title.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 32));
        title.setForeground(new Color(0, 184, 148));
        title.setBorder(BorderFactory.createEmptyBorder(24, 0, 16, 0));
        add(title, BorderLayout.NORTH);

        // Main Content Panel
        JPanel contentPanel = new JPanel(new BorderLayout(24, 24));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 32, 24, 32));

        // Left Panel - Process List and Status
        JPanel leftPanel = new JPanel(new BorderLayout(16, 16));
        leftPanel.setOpaque(false);

        // Process List Card
        JPanel processListPanel = createCardPanel("📋 Ready Processes", new Color(255, 121, 121));
        processListModel = new DefaultListModel<>();
        processList = new JList<>(processListModel);
        styleList(processList, new Color(255, 255, 255), new Color(255, 121, 121));
        JScrollPane processScrollPane = new JScrollPane(processList);
        processScrollPane.setBorder(null);
        processListPanel.add(processScrollPane, BorderLayout.CENTER);
        leftPanel.add(processListPanel, BorderLayout.NORTH);

        // Memory Status Card
        JPanel statusPanel = createCardPanel("📊 Memory Status", new Color(9, 132, 227));
        memoryStatusArea = new JTextArea();
        memoryStatusArea.setEditable(false);
        memoryStatusArea.setFont(new Font("Tahoma", Font.PLAIN, 16));
        memoryStatusArea.setBackground(new Color(255, 255, 255));
        memoryStatusArea.setForeground(new Color(45, 52, 54));
        memoryStatusArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JScrollPane statusScrollPane = new JScrollPane(memoryStatusArea);
        statusScrollPane.setBorder(null);
        statusPanel.add(statusScrollPane, BorderLayout.CENTER);
        leftPanel.add(statusPanel, BorderLayout.CENTER);

        // Memory Visualization Card
        JPanel vizPanel = createCardPanel("🎨 Memory Visualization", new Color(253, 121, 168));
        memoryVisualizationPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawMemoryVisualization(g);
            }
        };
        memoryVisualizationPanel.setPreferredSize(new Dimension(0, 200));
        memoryVisualizationPanel.setBackground(new Color(255, 255, 255));
        memoryVisualizationPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        vizPanel.add(memoryVisualizationPanel, BorderLayout.CENTER);

        // Control Buttons
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 16));
        controlPanel.setOpaque(false);
        JButton allocateButton = createPillButton("💾 Allocate Memory", new Color(0, 184, 148));
        JButton deallocateButton = createPillButton("🗑️ Deallocate Memory", new Color(255, 121, 121));
        JButton backButton = createPillButton("⬅️ Back", new Color(155, 89, 182));
        controlPanel.add(allocateButton);
        controlPanel.add(deallocateButton);
        controlPanel.add(backButton);

        allocateButton.addActionListener(e -> allocateMemory());
        deallocateButton.addActionListener(e -> deallocateMemory());
        backButton.addActionListener(e -> {
            if (frame != null) {
                frame.dispose();
                new ControlPanel().launch();
            }
        });

        // Layout
        contentPanel.add(leftPanel, BorderLayout.WEST);
        contentPanel.add(vizPanel, BorderLayout.CENTER);
        contentPanel.add(controlPanel, BorderLayout.SOUTH);

        add(contentPanel, BorderLayout.CENTER);

        // Update the display
        updateProcessList();
        updateMemoryStatus();
    }

    private JPanel createCardPanel(String title, Color accent) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(255, 255, 255));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accent, 3, true),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        JLabel header = new JLabel(title, SwingConstants.CENTER);
        header.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 18));
        header.setForeground(accent);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        panel.add(header, BorderLayout.NORTH);
        panel.setOpaque(true);
        return panel;
    }

    private void styleList(JList<String> list, Color bg, Color accent) {
        list.setBackground(bg);
        list.setForeground(new Color(45, 52, 54));
        list.setFont(new Font("Tahoma", Font.BOLD, 16));
        list.setSelectionBackground(accent);
        list.setSelectionForeground(Color.WHITE);
        list.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        list.setFixedCellHeight(32);
        list.setOpaque(true);
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

    private void updateProcessList() {
        processListModel.clear();
        processes.stream()
                .filter(p -> p.getState() == ProcessState.READY)
                .forEach(p -> processListModel.addElement(
                        String.format("PID: %d - %s (Priority: %d)",
                                p.getPid(), p.getProcessName(), p.getPriority())));
    }

    private void updateMemoryStatus() {
        StringBuilder status = new StringBuilder();
        status.append("Total Memory: ").append(memoryManager.getTotalMemory()).append(" bytes\n");
        status.append("Available Memory: ").append(memoryManager.getAvailableFrames() * memoryManager.getPageSize())
                .append(" bytes\n");
        status.append("Page Size: ").append(memoryManager.getPageSize()).append(" bytes\n");
        status.append("Total Frames: ").append(memoryManager.getTotalMemory() / memoryManager.getPageSize())
                .append("\n");
        status.append("Available Frames: ").append(memoryManager.getAvailableFrames()).append("\n\n");

        status.append("Allocated Memory:\n");
        processes.stream()
                .filter(p -> p.getState() != ProcessState.TERMINATED)
                .forEach(p -> status.append(String.format("PID %d: %d bytes\n",
                        p.getPid(), p.getMemoryRequired())));

        memoryStatusArea.setText(status.toString());
        memoryVisualizationPanel.repaint();
    }

    private void drawMemoryVisualization(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = memoryVisualizationPanel.getWidth() - 40;
        int height = memoryVisualizationPanel.getHeight() - 40;
        int x = 20;
        int y = 20;

        // Draw total memory rectangle
        g2d.setColor(new Color(240, 240, 240));
        g2d.fillRoundRect(x, y, width, height, 20, 20);
        g2d.setColor(new Color(0, 184, 148));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(x, y, width, height, 20, 20);

        // Draw allocated memory blocks
        int currentX = x;
        for (PCB process : processes) {
            if (process.getState() != ProcessState.TERMINATED && process.getMemoryRequired() > 0) {
                int blockWidth = (int) ((double) process.getMemoryRequired() / memoryManager.getTotalMemory() * width);
                if (blockWidth > 0) {
                    g2d.setColor(new Color(255, 121, 121));
                    g2d.fillRoundRect(currentX, y, blockWidth, height, 15, 15);
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Tahoma", Font.BOLD, 12));
                    g2d.drawString("PID " + process.getPid(), currentX + 5, y + 15);
                    currentX += blockWidth;
                }
            }
        }
    }

    private void allocateMemory() {
        String selected = processList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a process to allocate memory to.");
            return;
        }

        int pid = Integer.parseInt(selected.split(" - ")[0].split(": ")[1]);
        PCB process = processes.stream()
                .filter(p -> p.getPid() == pid)
                .findFirst()
                .orElse(null);

        if (process != null) {
            int memoryRequired = process.getMemoryRequired();
            if (memoryRequired > 0) {
                boolean allocated = memoryManager.allocateMemory(process);
                if (allocated) {
                    process.setAllocatedMemory(memoryRequired);
                    updateMemoryStatus();
                    JOptionPane.showMessageDialog(this,
                            "Memory allocated successfully for Process " + pid);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Insufficient memory to allocate for Process " + pid);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Process " + pid + " has no memory requirement.");
            }
        }
    }

    private void deallocateMemory() {
        String selected = processList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a process to deallocate memory from.");
            return;
        }

        int pid = Integer.parseInt(selected.split(" - ")[0].split(": ")[1]);
        PCB process = processes.stream()
                .filter(p -> p.getPid() == pid)
                .findFirst()
                .orElse(null);

        if (process != null) {
            memoryManager.deallocateMemory(process);
            process.setAllocatedMemory(0);
            updateMemoryStatus();
            JOptionPane.showMessageDialog(this,
                    "Memory deallocated successfully for Process " + pid);
        }
    }

    public void refresh() {
        updateProcessList();
        updateMemoryStatus();
    }

    public void launch() {
        frame = new JFrame();
        frame.setTitle("AURA OS - Memory Management");
        frame.setSize(1200, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setContentPane(this);
        frame.setVisible(true);
    }
}
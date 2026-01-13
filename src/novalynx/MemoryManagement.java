package novalynx;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class MemoryManagement {
    private MemoryManager memoryManager;
    private JFrame frame;
    private JTextArea memoryStatusArea;
    private JPanel pagingPanel;
    private static final int FRAME_SIZE = 4096; // 4KB per frame
    private static final int NUM_FRAMES = 64; // Example: 64 frames (256KB total)

    public MemoryManagement() {
        memoryManager = new MemoryManager(); // Use default constructor
    }

    public void launch() {
        initializeGUI();
        updateMemoryStatus();
        frame.setVisible(true);
    }

    private void initializeGUI() {
        frame = new JFrame();
        frame.setTitle("NovaLynx - Memory Management");
        NovaTheme.applyFrameSettings(frame);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(NovaTheme.DEEP_NAVY);

        // Title
        JLabel title = new JLabel("Memory Management", SwingConstants.CENTER);
        title.setFont(NovaTheme.HEADER_FONT);
        title.setForeground(NovaTheme.LYNX_BLUE);
        title.setBorder(BorderFactory.createEmptyBorder(24, 0, 16, 0));
        mainPanel.add(title, BorderLayout.NORTH);

        // Content
        JPanel contentPanel = new JPanel(new BorderLayout(24, 24));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 32, 24, 32));

        // Top panels
        JPanel topPanel = new JPanel(new BorderLayout(24, 0));
        topPanel.setOpaque(false);

        // Memory Status Card
        JPanel statusCard = createCardPanel("Memory Status", new Color(9, 132, 227));
        memoryStatusArea = new JTextArea();
        memoryStatusArea.setEditable(false);
        memoryStatusArea.setFont(NovaTheme.TERMINAL_FONT);
        memoryStatusArea.setBackground(NovaTheme.DARK_CARD);
        memoryStatusArea.setForeground(Color.WHITE);
        memoryStatusArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JScrollPane statusScroll = new JScrollPane(memoryStatusArea);
        statusScroll.setBorder(null);
        statusCard.add(statusScroll, BorderLayout.CENTER);
        topPanel.add(statusCard, BorderLayout.WEST);

        // Paging Visualization
        pagingPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawPagingVisualization(g);
            }

            @Override
            public Dimension getPreferredSize() {
                // This calculation ensures the scroll pane knows the full height of the content
                MemoryManager.Frame[] frames = memoryManager.getFrames();
                if (frames == null || frames.length == 0) {
                    return super.getPreferredSize();
                }

                final int padding = 10;
                final int topOffset = 30;
                int panelWidth = getParent() != null ? getParent().getWidth() : 800;

                // Dynamic grid calculation
                int boxSize = 40;
                int boxGap = 5;
                int cols = (panelWidth - padding * 2) / (boxSize + boxGap);
                if (cols == 0)
                    cols = 1;

                int numRows = (int) Math.ceil((double) frames.length / cols);
                int requiredHeight = topOffset + padding + (numRows * (boxSize + boxGap)) + padding;

                return new Dimension(panelWidth, requiredHeight);
            }
        };
        pagingPanel.setBackground(new Color(245, 246, 250));
        JScrollPane pagingScroll = new JScrollPane(pagingPanel);
        pagingScroll.setBorder(BorderFactory.createTitledBorder("Paging Visualization"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, statusCard, pagingScroll);
        splitPane.setResizeWeight(0.3);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);

        // Control buttons
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        controlPanel.setOpaque(false);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        JButton allocateBtn = createPillButton("Allocate Memory", new Color(0, 184, 148));
        JButton deallocateBtn = createPillButton("Deallocate Memory", new Color(255, 121, 121));
        JButton changePageSizeBtn = createPillButton("Change Page Size", new Color(253, 121, 168));
        JButton resetBtn = createPillButton("Reset Memory", new Color(255, 165, 2));
        JButton backBtn = createPillButton("Back", new Color(155, 89, 182));

        controlPanel.add(allocateBtn);
        controlPanel.add(deallocateBtn);
        controlPanel.add(changePageSizeBtn);
        controlPanel.add(resetBtn);
        controlPanel.add(backBtn);

        allocateBtn.addActionListener(e -> allocateMemory());
        deallocateBtn.addActionListener(e -> deallocateMemory());
        changePageSizeBtn.addActionListener(e -> changePageSize());
        resetBtn.addActionListener(e -> resetMemory());
        backBtn.addActionListener(e -> {
            if (frame != null) {
                frame.dispose();
                new ControlPanel().launch();
            }
        });

        // Layout
        contentPanel.add(splitPane, BorderLayout.CENTER);
        contentPanel.add(controlPanel, BorderLayout.SOUTH);

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        frame.setContentPane(mainPanel);
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
        button.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void allocateMemory() {
        // Get process list from ProcessManagement that aren't already in memory
        List<PCB> processList = ProcessManagement.getProcessList();
        if (processList == null || processList.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No processes available to allocate!", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // This is a bit of a simplification. We should ideally be checking which
        // processes
        // are NOT yet allocated in memoryManager. For now, we present all.
        PCB[] pcbArray = processList.toArray(new PCB[0]);
        PCB selectedProcess = (PCB) JOptionPane.showInputDialog(frame, "Select a process to allocate memory:",
                "Allocate Memory", JOptionPane.QUESTION_MESSAGE, null, pcbArray, pcbArray[0]);

        if (selectedProcess == null)
            return;

        // The process already has its memory requirement set on creation.
        // We don't need to ask for it again.
        if (selectedProcess.getMemoryRequired() <= 0) {
            JOptionPane.showMessageDialog(frame, "Selected process has no memory requirement set.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean success = memoryManager.allocateMemory(selectedProcess);
        if (success) {
            updateAllDisplays();
            JOptionPane.showMessageDialog(frame, "Memory allocated successfully!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(frame, "Failed to allocate memory. Not enough free frames!", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deallocateMemory() {
        // Get process list from ProcessManagement that ARE allocated in memory
        List<PCB> processList = ProcessManagement.getProcessList();
        if (processList == null || processList.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No processes to deallocate!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // This is a simplification. We should get the list of allocated processes
        // directly from the memoryManager's processAllocations map keys.
        PCB[] pcbArray = processList.toArray(new PCB[0]);
        PCB selectedProcess = (PCB) JOptionPane.showInputDialog(frame, "Select a process to deallocate memory:",
                "Deallocate Memory", JOptionPane.QUESTION_MESSAGE, null, pcbArray, pcbArray[0]);

        if (selectedProcess == null)
            return;

        memoryManager.deallocateMemory(selectedProcess);

        // We don't zero out the requirement, as it's part of the process definition.
        // selectedProcess.setMemoryRequired(0);

        updateAllDisplays();
        JOptionPane.showMessageDialog(frame, "Memory deallocated for Process " + selectedProcess.getPid() + "!",
                "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void changePageSize() {
        String newSizeStr = JOptionPane.showInputDialog(frame, "Enter new page size (in KB):", "Change Page Size",
                JOptionPane.QUESTION_MESSAGE);
        if (newSizeStr == null || newSizeStr.trim().isEmpty()) {
            return;
        }

        try {
            int newSizeKB = Integer.parseInt(newSizeStr.trim());
            if (newSizeKB <= 0) {
                JOptionPane.showMessageDialog(frame, "Page size must be a positive number.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean success = memoryManager.setPageSize(newSizeKB * 1024); // Convert KB to bytes

            if (success) {
                updateAllDisplays();
                JOptionPane.showMessageDialog(frame, "Page size changed successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame,
                        "Cannot change page size while memory is allocated to processes. Please deallocate all memory first.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(frame, "Please enter a valid integer for page size.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetMemory() {
        int result = JOptionPane.showConfirmDialog(frame,
                "Are you sure you want to reset all memory allocations?",
                "Reset Memory", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            memoryManager = new MemoryManager();
            updateAllDisplays();
            JOptionPane.showMessageDialog(frame, "Memory reset successfully!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void updateAllDisplays() {
        updateMemoryStatus();
        pagingPanel.repaint();
    }

    private void updateMemoryStatus() {
        int totalFrames = memoryManager.getFrames().length;
        int usedFrames = 0;
        for (MemoryManager.Frame f : memoryManager.getFrames()) {
            if (f.isAllocated())
                usedFrames++;
        }
        int freeFrames = totalFrames - usedFrames;

        int totalMemoryKB = memoryManager.getTotalMemory() / 1024;
        int usedMemoryKB = (usedFrames * memoryManager.getPageSize()) / 1024;
        int pageSizeKB = memoryManager.getPageSize() / 1024;

        StringBuilder sb = new StringBuilder();
        sb.append(String.format(
                "Total Memory: %d KB | Page/Frame Size: %d KB\n" +
                        "Total Frames: %d | Used: %d | Free: %d\n",
                totalMemoryKB, pageSizeKB, totalFrames, usedFrames, freeFrames));

        sb.append("========================================\n");
        sb.append("Allocated Process Details:\n");

        Map<Integer, List<Integer>> allocations = memoryManager.getProcessAllocations();
        if (allocations.isEmpty()) {
            sb.append("No processes are currently allocated in memory.\n");
        } else {
            for (Map.Entry<Integer, List<Integer>> entry : allocations.entrySet()) {
                int pid = entry.getKey();
                int numPages = entry.getValue().size();
                int memKB = numPages * pageSizeKB;
                sb.append(String.format("- Process %d: %d pages (%d KB)\n", pid, numPages, memKB));
            }
        }

        memoryStatusArea.setText(sb.toString());
    }

    private void drawPagingVisualization(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        MemoryManager.Frame[] frames = memoryManager.getFrames();
        if (frames == null || frames.length == 0) {
            g2d.drawString("Memory not initialized.", 20, 40);
            g2d.dispose();
            return;
        }

        final int padding = 10;
        final int topOffset = 30;
        int panelWidth = pagingPanel.getWidth();
        int panelHeight = pagingPanel.getHeight();

        // Dynamic grid calculation
        int boxSize = 40;
        int boxGap = 5;
        int cols = (panelWidth - padding * 2) / (boxSize + boxGap);
        if (cols == 0)
            cols = 1;

        Map<Integer, Color> processColors = new HashMap<>();
        List<Color> colors = Arrays.asList(
                new Color(116, 185, 255), new Color(85, 239, 196), new Color(255, 234, 167),
                new Color(250, 177, 160), new Color(255, 118, 117), new Color(9, 132, 227),
                new Color(0, 184, 148), new Color(214, 48, 49), new Color(232, 67, 147));
        int colorIndex = 0;

        g2d.setFont(new Font("Arial", Font.BOLD, 10));

        for (int i = 0; i < frames.length; i++) {
            int row = i / cols;
            int col = i % cols;
            int x = padding + col * (boxSize + boxGap);
            int y = topOffset + padding + row * (boxSize + boxGap);

            MemoryManager.Frame frame = frames[i];
            Color color;
            String text;

            if (frame.isAllocated()) {
                int pid = frame.getProcessId();
                color = processColors.computeIfAbsent(pid, k -> {
                    return colors.get(processColors.size() % colors.size());
                });
                text = "P" + pid + ":" + frame.getPageNumber();
                g2d.setColor(color);
            } else {
                color = new Color(223, 230, 233);
                text = "Free";
                g2d.setColor(Color.DARK_GRAY);
            }

            g2d.fillRoundRect(x, y, boxSize, boxSize, 10, 10);

            // Draw border
            g2d.setColor(color.darker());
            g2d.drawRoundRect(x, y, boxSize, boxSize, 10, 10);

            // Draw text
            g2d.setColor(Color.BLACK);
            FontMetrics fm = g2d.getFontMetrics();
            int stringWidth = fm.stringWidth(text);
            g2d.drawString(text, x + (boxSize - stringWidth) / 2, y + boxSize / 2 + 5);
        }
        g2d.dispose();
    }
}

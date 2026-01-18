package novalynx;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ProcessManagement {
    private static List<PCB> processList;
    private Scheduler scheduler;
    private JFrame frame;
    private JTextArea statusArea;
    private JTextArea processListArea;
    private int totalMemory;
    private int usedMemory;
    private JLabel memoryStatusLabel;
    private JLabel timeStatusLabel;
    private JTextArea runningArea;
    private JTextArea blockedArea;
    private JTextArea suspendedArea;
    private JTextArea readyArea;
    private JTextArea completedArea;
    private GanttChartPanel ganttChartPanel;
    private Map<Integer, Integer> processMemoryMap; // Maps process ID to allocated memory size
    private int nextArrivalTime = 0; // Counter for sequential arrival times
    private javax.swing.Timer simulationTimer; // Timer for gradual simulation

    public ProcessManagement() {
        if (processList == null)
            processList = new ArrayList<>();
        scheduler = new Scheduler();
        totalMemory = 1024 * 1024; // 1MB total memory
        usedMemory = 0;
        processMemoryMap = new HashMap<>();
    }

    public void launch() {
        initializeGUI();
        updateProcessList();
        updateStatus();
        updateGanttChart();
        frame.setVisible(true);
    }

    private void initializeGUI() {
        frame = new JFrame();
        frame.setTitle("NovaLynx - Process Management");
        NovaTheme.applyFrameSettings(frame);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(NovaTheme.DEEP_NAVY);

        // Custom Title Bar
        JLabel title = new JLabel("Process Management", SwingConstants.CENTER);
        title.setFont(NovaTheme.HEADER_FONT);
        title.setForeground(NovaTheme.LYNX_BLUE);
        title.setBorder(BorderFactory.createEmptyBorder(24, 0, 16, 0));
        mainPanel.add(title, BorderLayout.NORTH);

        // Main Content Panel
        JPanel contentPanel = new JPanel(new BorderLayout(24, 24));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 32, 24, 32));

        // Top Panel - Process List and Status
        JPanel topPanel = new JPanel(new BorderLayout(24, 0));
        topPanel.setOpaque(false);

        // Process List Card
        JPanel processListCard = createCardPanel("Process List", new Color(255, 121, 121));
        processListArea = new JTextArea();
        processListArea.setEditable(false);
        processListArea.setFont(NovaTheme.TERMINAL_FONT);
        processListArea.setBackground(NovaTheme.DARK_CARD);
        processListArea.setForeground(Color.WHITE);
        processListArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JScrollPane processScrollPane = new JScrollPane(processListArea);
        processScrollPane.setBorder(null);
        processScrollPane.setPreferredSize(new Dimension(0, 90)); // Limit height further as requested
        processListCard.add(processScrollPane, BorderLayout.CENTER);

        // Status Card
        JPanel statusCard = createCardPanel("System Status", new Color(9, 132, 227));
        statusArea = new JTextArea();
        statusArea.setEditable(false);
        statusArea.setFont(NovaTheme.TERMINAL_FONT);
        statusArea.setBackground(NovaTheme.DARK_CARD);
        statusArea.setForeground(Color.WHITE);
        statusArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JScrollPane statusScrollPane = new JScrollPane(statusArea);
        statusScrollPane.setBorder(null);
        statusScrollPane.setPreferredSize(new Dimension(0, 90)); // Limit height further as requested
        statusCard.add(statusScrollPane, BorderLayout.CENTER);

        // Memory and Time Status Panel
        JPanel bottomStatusPanel = new JPanel();
        bottomStatusPanel.setLayout(new BoxLayout(bottomStatusPanel, BoxLayout.Y_AXIS));
        bottomStatusPanel.setOpaque(false);
        bottomStatusPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

        memoryStatusLabel = new JLabel("Memory: " + (usedMemory / 1024) + "/" + (totalMemory / 1024) + " KB used");
        memoryStatusLabel.setFont(NovaTheme.SUBHEADER_FONT);
        memoryStatusLabel.setForeground(NovaTheme.CYBER_GREEN);
        memoryStatusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        memoryStatusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        timeStatusLabel = new JLabel("Time: 0", SwingConstants.CENTER);
        timeStatusLabel.setFont(NovaTheme.SUBHEADER_FONT);
        timeStatusLabel.setForeground(NovaTheme.LYNX_BLUE);
        timeStatusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timeStatusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        bottomStatusPanel.add(memoryStatusLabel);
        bottomStatusPanel.add(timeStatusLabel);
        statusCard.add(bottomStatusPanel, BorderLayout.SOUTH);

        // Use a JSplitPane to allow resizing between Process List and Status
        JSplitPane topSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, processListCard, statusCard);
        topSplitPane.setResizeWeight(0.6); // Give more initial space to the left component
        topSplitPane.setOpaque(false);
        topSplitPane.setBorder(null);
        topPanel.add(topSplitPane, BorderLayout.CENTER);

        // New: Process State Areas (now 5 areas)
        // New: Process State Areas (now 5 areas)
        JPanel statePanel = new JPanel(new GridLayout(1, 5, 12, 0));
        statePanel.setOpaque(false);

        runningArea = new JTextArea();
        statePanel.add(createStatePanel("Running", new Color(46, 204, 113), runningArea));

        blockedArea = new JTextArea();
        statePanel.add(createStatePanel("Blocked", new Color(255, 159, 67), blockedArea));

        suspendedArea = new JTextArea();
        statePanel.add(createStatePanel("Suspended", new Color(155, 89, 182), suspendedArea));

        readyArea = new JTextArea();
        statePanel.add(createStatePanel("Ready Queue", new Color(52, 152, 219), readyArea));

        completedArea = new JTextArea();
        statePanel.add(createStatePanel("Completed", new Color(149, 165, 166), completedArea));
        // Add statePanel below statusCard
        JPanel topWithStates = new JPanel(new BorderLayout(0, 12));
        topWithStates.setOpaque(false);
        topWithStates.add(topPanel, BorderLayout.NORTH);
        topWithStates.add(statePanel, BorderLayout.CENTER);

        // Gantt Chart Card (shorter)
        JPanel ganttCard = createCardPanel("Gantt Chart", new Color(0, 184, 148));
        ganttChartPanel = new GanttChartPanel();
        JScrollPane ganttScrollPane = new JScrollPane(ganttChartPanel);
        ganttScrollPane.setBorder(null);
        ganttScrollPane.setPreferredSize(new Dimension(0, 150)); // Increased height for better visibility
        ganttScrollPane.setMinimumSize(new Dimension(0, 120)); // Prevent it from being too squished
        ganttCard.add(ganttScrollPane, BorderLayout.CENTER);

        // Control Buttons Panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        controlPanel.setOpaque(false);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        // Create buttons with emoji icons
        // Create buttons with unified color (Professional Blue)
        Color btnColor = new Color(9, 132, 227); // Unified Blue
        JButton createBtn = createPillButton("Create Process", btnColor);
        JButton destroyBtn = createPillButton("Destroy Process", btnColor);
        JButton suspendBtn = createPillButton("Suspend Process", btnColor);
        JButton resumeBtn = createPillButton("Resume Process", btnColor);
        JButton blockBtn = createPillButton("Block Process", btnColor);
        JButton wakeupBtn = createPillButton("Wakeup Process", btnColor);
        JButton dispatchBtn = createPillButton("Run Step", btnColor);
        JButton runAllBtn = createPillButton("Run All", btnColor);
        JButton priorityBtn = createPillButton("Change Priority", btnColor);
        JButton statsBtn = createPillButton("Show Statistics", btnColor);
        JButton algorithmBtn = createPillButton("Change Algorithm", btnColor);
        JButton backBtn = createPillButton("Back", btnColor);

        JCheckBox preemptiveBox = new JCheckBox("Preemptive Scheduling");
        preemptiveBox.setForeground(Color.DARK_GRAY); // Dark text for white background
        preemptiveBox.setOpaque(false);
        preemptiveBox.setFont(new Font("Segoe UI", Font.BOLD, 14));
        preemptiveBox.setSelected(false);
        preemptiveBox.addActionListener(e -> {
            scheduler.setSchedulingType(preemptiveBox.isSelected() ? "Preemptive" : "Non-Preemptive");
            updateStatus();
        });

        // Single row layout for minimal UI
        controlPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10)); // Horizontal flow with spacing

        // Add buttons directly to the panel in a logical linear order
        controlPanel.add(createBtn);
        controlPanel.add(destroyBtn);
        controlPanel.add(suspendBtn);
        controlPanel.add(resumeBtn);
        controlPanel.add(blockBtn);
        controlPanel.add(wakeupBtn);

        // Separator or space could be added here, but FlowLayout handles it gently
        controlPanel.add(dispatchBtn);
        controlPanel.add(runAllBtn);
        controlPanel.add(preemptiveBox);

        controlPanel.add(priorityBtn);
        controlPanel.add(statsBtn);
        controlPanel.add(algorithmBtn);
        controlPanel.add(backBtn);

        // Add action listeners
        createBtn.addActionListener(e -> createProcess());
        destroyBtn.addActionListener(e -> destroyProcess());
        suspendBtn.addActionListener(e -> suspendProcess());
        resumeBtn.addActionListener(e -> resumeProcess());
        blockBtn.addActionListener(e -> blockProcess());
        wakeupBtn.addActionListener(e -> wakeupProcess());
        dispatchBtn.addActionListener(e -> dispatchProcess());

        runAllBtn.addActionListener(e -> {
            runAllSimulation();
        });
        priorityBtn.addActionListener(e -> changePriority());
        statsBtn.addActionListener(e -> showStatistics());
        algorithmBtn.addActionListener(e -> changeAlgorithm());
        backBtn.addActionListener(e -> {
            if (frame != null) {
                frame.dispose();
                new ControlPanel().launch();
            }
        });

        // Wrap controlPanel in a JScrollPane for horizontal scrolling
        JScrollPane controlScrollPane = new JScrollPane(controlPanel, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        controlScrollPane.setBorder(null);
        controlScrollPane.getHorizontalScrollBar().setUnitIncrement(20);
        controlScrollPane.setPreferredSize(new Dimension(0, 90)); // Increased height to prevent cutoff

        // Layout
        contentPanel.add(topWithStates, BorderLayout.NORTH);
        contentPanel.add(ganttCard, BorderLayout.CENTER);
        contentPanel.add(controlScrollPane, BorderLayout.SOUTH);

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        frame.setContentPane(mainPanel);
    }

    private JPanel createCardPanel(String title, Color accent) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        JLabel header = new JLabel(title);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setForeground(accent);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        panel.add(header, BorderLayout.NORTH);
        return panel;
    }

    private JPanel createStatePanel(String title, Color accent, JTextArea area) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        // Minimal border
        panel.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1));

        JLabel header = new JLabel(title, SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setForeground(Color.WHITE);
        header.setOpaque(true);
        header.setBackground(accent);
        header.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        area.setEditable(false);
        area.setFont(new Font("Consolas", Font.PLAIN, 14));
        area.setForeground(Color.DARK_GRAY);
        area.setRows(3); // Reduced height from 6 to 3 to save space
        area.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        panel.add(header, BorderLayout.NORTH);
        panel.add(new JScrollPane(area), BorderLayout.CENTER);
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

    private void createProcess() {
        JDialog dialog = new JDialog(frame, "Create New Process", true);
        dialog.setLayout(new BorderLayout(16, 16));
        dialog.getContentPane().setBackground(new Color(245, 246, 250));
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(frame);

        JPanel contentPanel = new JPanel(new BorderLayout(16, 16));
        contentPanel.setBackground(new Color(245, 246, 250));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Form fields
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 12, 12));
        formPanel.setOpaque(false);

        JLabel nameLabel = new JLabel("Process Name:");
        nameLabel.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 16));
        nameLabel.setForeground(new Color(45, 52, 54));
        JTextField nameField = createStyledTextField();

        JLabel priorityLabel = new JLabel("Priority (1-9):");
        priorityLabel.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 16));
        priorityLabel.setForeground(new Color(45, 52, 54));
        JTextField priorityField = createStyledTextField();
        priorityField.setText("5"); // Default priority

        JLabel burstLabel = new JLabel("Burst Time (sec):");
        burstLabel.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 16));
        burstLabel.setForeground(new Color(45, 52, 54));
        JTextField burstField = createStyledTextField();

        JLabel memoryLabel = new JLabel("Memory (KB):");
        memoryLabel.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 16));
        memoryLabel.setForeground(new Color(45, 52, 54));
        JTextField memoryField = createStyledTextField();
        memoryField.setText("8"); // Default memory is 8 KB

        JLabel quantumLabel = new JLabel("Time Quantum (2-6):");
        quantumLabel.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 16));
        quantumLabel.setForeground(new Color(45, 52, 54));
        JTextField quantumField = createStyledTextField();
        quantumField.setText("2");
        quantumLabel.setVisible(false);
        quantumField.setVisible(false);

        // Initially hide priority field - only show for Priority Scheduling
        priorityLabel.setVisible(false);
        priorityField.setVisible(false);

        formPanel.add(nameLabel);
        formPanel.add(nameField);
        formPanel.add(priorityLabel);
        formPanel.add(priorityField);
        formPanel.add(burstLabel);
        formPanel.add(burstField);
        formPanel.add(memoryLabel);
        formPanel.add(memoryField);
        formPanel.add(quantumLabel);
        formPanel.add(quantumField);

        contentPanel.add(formPanel, BorderLayout.CENTER);

        // Show quantum only if Round Robin is selected
        String currentAlgo = scheduler.getCurrentAlgorithm();
        if (currentAlgo != null && currentAlgo.equalsIgnoreCase("ROUND_ROBIN")) {
            quantumLabel.setVisible(true);
            quantumField.setVisible(true);
        }

        // Show priority only if Priority Scheduling is selected
        if (currentAlgo != null && currentAlgo.equalsIgnoreCase("PRIORITY")) {
            priorityLabel.setVisible(true);
            priorityField.setVisible(true);
        }

        JButton createBtn = createPillButton("Create", new Color(0, 184, 148));
        createBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            int priority = 5, burstTime, memoryKB, quantum = 2; // Default priority is 5
            try {
                burstTime = Integer.parseInt(burstField.getText().trim());
                memoryKB = Integer.parseInt(memoryField.getText().trim());

                // Only get priority if Priority Scheduling is selected
                if (priorityField.isVisible()) {
                    priority = Integer.parseInt(priorityField.getText().trim());
                    if (priority < 1 || priority > 9)
                        throw new NumberFormatException();
                }

                if (quantumField.isVisible()) {
                    quantum = Integer.parseInt(quantumField.getText().trim());
                    if (quantum < 2 || quantum > 6)
                        throw new NumberFormatException();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid numeric values.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (name.isEmpty() || burstTime <= 0 || memoryKB <= 0) {
                JOptionPane.showMessageDialog(dialog, "Please fill all fields with valid values.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            PCB newProcess = new PCB(name, priority, burstTime);
            newProcess.setMemoryRequired(memoryKB * 1024); // store as bytes
            newProcess.setAllocatedMemory(memoryKB * 1024);
            newProcess.setOriginalBurstTime(burstTime);
            newProcess.setArrivalTime(nextArrivalTime++); // Use sequential arrival time

            if (quantumField.isVisible()) {
                newProcess.setQuantum(quantum);
            }
            newProcess.setState(ProcessState.READY);

            // Allocate memory
            processMemoryMap.put(newProcess.getPid(), memoryKB * 1024);
            usedMemory += memoryKB * 1024;

            processList.add(newProcess);
            updateProcessList();
            updateStatus();
            scheduler.addProcess(newProcess);
            ganttChartPanel.repaint();
            dialog.dispose();
        });
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.setOpaque(false);
        btnPanel.add(createBtn);
        contentPanel.add(btnPanel, BorderLayout.SOUTH);
        dialog.setContentPane(contentPanel);
        dialog.setVisible(true);
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Tahoma", Font.PLAIN, 16));
        field.setBackground(new Color(255, 255, 255));
        field.setForeground(new Color(45, 52, 54));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(155, 89, 182), 2, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        return field;
    }

    private void destroyProcess() {
        if (processList.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No processes to destroy!", "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        PCB selectedProcess = showProcessSelectionDialog("Select Process to Destroy");
        if (selectedProcess != null) {
            processList.remove(selectedProcess);
            scheduler.removeProcess(selectedProcess);
            updateProcessList();
            updateStatus();
            JOptionPane.showMessageDialog(frame, "Process " + selectedProcess.getPid() + " destroyed successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void suspendProcess() {
        PCB toSuspend = processList.stream()
                .filter(p -> p.getState() == ProcessState.RUNNING)
                .findFirst()
                .orElse(null);

        if (toSuspend == null) {
            JOptionPane.showMessageDialog(frame, "No running process to suspend.", "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        toSuspend.setState(ProcessState.SUSPENDED);
        updateProcessList();
        updateStatus();
        JOptionPane.showMessageDialog(frame, "Process suspended successfully!", "Success",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void resumeProcess() {
        List<PCB> suspendedProcesses = processList.stream()
                .filter(p -> p.getState() == ProcessState.SUSPENDED)
                .collect(Collectors.toList());

        if (suspendedProcesses.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No suspended processes to resume!", "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        PCB selectedProcess = showProcessSelectionDialog("Select Process to Resume", suspendedProcesses);
        if (selectedProcess != null) {
            selectedProcess.setState(ProcessState.READY);
            updateProcessList();
            updateStatus();
            JOptionPane.showMessageDialog(frame, "Process " + selectedProcess.getPid() + " resumed successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void blockProcess() {
        PCB toBlock = processList.stream()
                .filter(p -> p.getState() == ProcessState.RUNNING)
                .findFirst()
                .orElse(null);

        if (toBlock == null) {
            JOptionPane.showMessageDialog(frame, "No running process to block.", "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        toBlock.setState(ProcessState.BLOCKED);
        updateProcessList();
        updateStatus();
        JOptionPane.showMessageDialog(frame, "Process blocked successfully!", "Success",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void wakeupProcess() {
        List<PCB> blockedProcesses = processList.stream()
                .filter(p -> p.getState() == ProcessState.BLOCKED)
                .collect(Collectors.toList());

        if (blockedProcesses.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No blocked processes to wake up!", "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        PCB selectedProcess = showProcessSelectionDialog("Select Process to Wake Up", blockedProcesses);
        if (selectedProcess != null) {
            selectedProcess.setState(ProcessState.READY);
            updateProcessList();
            updateStatus();
            JOptionPane.showMessageDialog(frame, "Process " + selectedProcess.getPid() + " woken up successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void dispatchProcess() {
        if (scheduler.stepSimulation()) {
            updateProcessList();
            updateStatus();
            ganttChartPanel.revalidate();
            ganttChartPanel.repaint();
        } else {
            JOptionPane.showMessageDialog(frame,
                    "No processes ready to run at simulation time " + scheduler.getCurrentTime(), "Info",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void runAllSimulation() {
        if (simulationTimer != null && simulationTimer.isRunning()) {
            return;
        }

        simulationTimer = new javax.swing.Timer(500, e -> {
            if (scheduler.stepSimulation()) {
                updateProcessList();
                updateStatus();
                ganttChartPanel.revalidate(); // Ensure size updates
                ganttChartPanel.repaint();
            } else {
                stopSimulation();
                ganttChartPanel.repaint(); // Force final repaint
                JOptionPane.showMessageDialog(frame, "Simulation complete!", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        simulationTimer.start();
    }

    private void stopSimulation() {
        if (simulationTimer != null) {
            simulationTimer.stop();
        }
        // Find the "Run All" and "Stop" buttons to reset their states if needed
        // but since we don't have direct references here, we rely on the
        // listeners we added in initializeGUI.
        // Actually, we should probably pass them or find them.
        // For now, let's just stop the timer.
    }

    private void changePriority() {
        List<PCB> nonTerminatedProcesses = processList.stream()
                .filter(p -> p.getState() != ProcessState.TERMINATED)
                .collect(Collectors.toList());

        if (nonTerminatedProcesses.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No active processes to change priority!", "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        PCB selectedProcess = showProcessSelectionDialog("Select Process to Change Priority", nonTerminatedProcesses);
        if (selectedProcess != null) {
            String newPriorityStr = JOptionPane.showInputDialog(frame,
                    "Enter new priority (1-9) for Process " + selectedProcess.getPid() + ":",
                    "Change Priority", JOptionPane.QUESTION_MESSAGE);

            if (newPriorityStr != null) {
                try {
                    int newPriority = Integer.parseInt(newPriorityStr);
                    if (newPriority >= 1 && newPriority <= 9) {
                        selectedProcess.setPriority(newPriority);
                        updateProcessList();
                        updateStatus();
                        JOptionPane.showMessageDialog(frame, "Priority changed successfully!", "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(frame, "Priority must be between 1 and 9!", "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Please enter a valid number!", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void changeAlgorithm() {
        String[] algorithms = { "FCFS", "Priority Scheduling", "Round Robin" };
        String selected = (String) JOptionPane.showInputDialog(frame,
                "Select scheduling algorithm:",
                "Change Algorithm",
                JOptionPane.QUESTION_MESSAGE,
                null,
                algorithms,
                algorithms[0]);

        if (selected != null) {
            scheduler.setAlgorithm(selected);
            updateStatus();
            JOptionPane.showMessageDialog(frame, "Algorithm changed to " + selected + "!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showStatistics() {
        // Gather all PCBs from all queues and running/completed
        List<PCB> allPCBs = new ArrayList<>();
        allPCBs.addAll(processList);

        // Remove duplicates (in case)
        Map<Integer, PCB> pcbMap = new HashMap<>();
        for (PCB pcb : allPCBs) {
            pcbMap.put(pcb.getPid(), pcb);
        }
        allPCBs = new ArrayList<>(pcbMap.values());

        // Calculate completion times based on Gantt chart for processes that haven't
        // completed yet

        // Table columns for scheduling table
        String[] columns = { "Process ID", "Name", "State", "Memory Allocated", "Burst Time", "Arrival Time",
                "Completion Time", "Turnaround Time", "Waiting Time", "Priority" };
        Object[][] data = new Object[allPCBs.size()][columns.length];
        for (int i = 0; i < allPCBs.size(); i++) {
            PCB pcb = allPCBs.get(i);
            data[i][0] = pcb.getPid();
            data[i][1] = pcb.getProcessName();
            data[i][2] = pcb.getState().toString();
            data[i][3] = (pcb.getMemoryRequired() / 1024) + " KB";
            data[i][4] = pcb.getOriginalBurstTime();
            data[i][5] = (pcb.getArrivalTime() >= 0) ? String.valueOf(pcb.getArrivalTime()) : "N/A";

            if (pcb.getState() == ProcessState.TERMINATED) {
                data[i][6] = String.valueOf(pcb.getCompletionTime());
                data[i][7] = String.valueOf(pcb.getTurnaroundTime());
                data[i][8] = String.valueOf(pcb.getWaitingTime());
            } else {
                data[i][6] = "N/A"; // Completion Time
                data[i][7] = "N/A"; // Turnaround Time

                // Calculate estimated waiting time for non-terminated processes if needed for
                // display
                // For now, we only show waiting time for terminated processes.
                data[i][8] = "N/A"; // Waiting Time
            }

            // Show priority only for Priority Scheduling, N/A for others
            String algorithm = scheduler.getCurrentAlgorithm();
            if (algorithm != null && algorithm.equalsIgnoreCase("PRIORITY") && pcb.getPriority() > 0) {
                data[i][9] = String.valueOf(pcb.getPriority());
            } else {
                data[i][9] = "N/A";
            }
        }
        JTable table = new JTable(data, columns);
        table.setFillsViewportHeight(true);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(24);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(900, 300));
        JOptionPane.showMessageDialog(frame, scrollPane, "Scheduling Table", JOptionPane.INFORMATION_MESSAGE);
    }

    private PCB showProcessSelectionDialog(String title) {
        return showProcessSelectionDialog(title, processList);
    }

    private PCB showProcessSelectionDialog(String title, List<PCB> processes) {
        if (processes.isEmpty())
            return null;

        PCB[] processArray = processes.toArray(new PCB[0]);
        return (PCB) JOptionPane.showInputDialog(frame,
                "Select a process:",
                title,
                JOptionPane.QUESTION_MESSAGE,
                null,
                processArray,
                processArray[0]);
    }

    private void updateProcessList() {
        StringBuilder all = new StringBuilder();
        StringBuilder running = new StringBuilder();
        StringBuilder blocked = new StringBuilder();
        StringBuilder suspended = new StringBuilder();
        StringBuilder ready = new StringBuilder();
        StringBuilder completed = new StringBuilder();
        all.append(String.format("%-5s %-15s %-10s %-10s %-10s %-15s %-10s %-10s\n", "PID", "Name", "Priority", "State",
                "Burst", "Remaining", "Memory", "Owner"));
        for (PCB pcb : processList) {
            all.append(String.format("%-5d %-15s %-10d %-10s %-10d %-15d %-10d %-10s\n",
                    pcb.getPid(), pcb.getProcessName(), pcb.getPriority(), pcb.getState(), pcb.getOriginalBurstTime(),
                    pcb.getRemainingTime(), pcb.getMemoryRequired() / 1024, pcb.getOwner()));
            switch (pcb.getState()) {
                case RUNNING:
                    running.append(pcb.getProcessName()).append(" (PID ").append(pcb.getPid()).append(")\n");
                    break;
                case BLOCKED:
                    blocked.append(pcb.getProcessName()).append(" (PID ").append(pcb.getPid()).append(")\n");
                    break;
                case SUSPENDED:
                    suspended.append(pcb.getProcessName()).append(" (PID ").append(pcb.getPid()).append(")\n");
                    break;
                case READY:
                    ready.append(pcb.getProcessName()).append(" (PID ").append(pcb.getPid()).append(")\n");
                    break;
                case TERMINATED:
                    completed.append(pcb.getProcessName()).append(" (PID ").append(pcb.getPid()).append(")\n");
                    break;
                default:
                    break;
            }
        }
        processListArea.setText(all.toString());
        runningArea.setText(running.toString());
        blockedArea.setText(blocked.toString());
        suspendedArea.setText(suspended.toString());
        readyArea.setText(ready.toString());
        completedArea.setText(completed.toString());
    }

    private void updateStatus() {
        StringBuilder sb = new StringBuilder();
        for (PCB pcb : processList) {
            sb.append(pcb.getProcessName()).append(": ").append(pcb.getState()).append("\n");
        }
        statusArea.setText(sb.toString());
        updateMemoryStatus();
        if (timeStatusLabel != null) {
            timeStatusLabel.setText("Time: " + scheduler.getCurrentTime());
        }
    }

    private void updateMemoryStatus() {
        memoryStatusLabel.setText("Memory: " + (usedMemory / 1024) + "/" + (totalMemory / 1024) + " KB used");
    }

    private void updateGanttChart() {
        // Gantt chart panel handles drawing
    }

    public static List<PCB> getProcessList() {
        return processList;
    }

    // Custom JPanel for Gantt chart
    private class GanttChartPanel extends JPanel {
        private final int TIME_UNIT_WIDTH = 40;
        private final int BAR_HEIGHT = 40; // Reduced height as requested
        private final int PADDING = 20;

        // Pre-defined list of nice colors for processes
        private final List<Color> colors = Arrays.asList(
                new Color(116, 185, 255), new Color(85, 239, 196), new Color(255, 234, 167),
                new Color(250, 177, 160), new Color(255, 118, 117), new Color(9, 132, 227),
                new Color(0, 184, 148), new Color(214, 48, 49), new Color(232, 67, 147));
        private Map<String, Color> processColorMap = new HashMap<>();

        private int nextColorIndex = 0;

        public GanttChartPanel() {
            setBackground(Color.WHITE);
        }

        @Override
        public Dimension getPreferredSize() {
            int width = Math.max(800, scheduler.getCurrentTime() * TIME_UNIT_WIDTH + PADDING * 2);
            // Height = Top Padding + Bar + Gap + Axis/Labels + Bottom Buffer
            return new Dimension(width, PADDING + BAR_HEIGHT + 50);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // Removed revalidate() from here to prevent loops

            try {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                List<Scheduler.GanttEntry> entries = scheduler.getGanttChart();
                if (entries.isEmpty()) {
                    g2d.setColor(Color.GRAY);
                    g2d.drawString("Gantt Chart is empty.", PADDING, PADDING + BAR_HEIGHT);
                    return;
                }

                int maxTime = scheduler.getCurrentTime();
                if (maxTime < 10)
                    maxTime = 10;

                // Draw process bars
                for (Scheduler.GanttEntry entry : entries) {
                    int startX = PADDING + (int) (entry.getStartTime() * TIME_UNIT_WIDTH);
                    int width = (int) ((entry.getEndTime() - entry.getStartTime()) * TIME_UNIT_WIDTH);

                    // Ensure color is assigned and consistent for each process
                    Color processColor = processColorMap.computeIfAbsent(entry.getProcessName(), name -> {
                        Color color = colors.get(nextColorIndex % colors.size());
                        nextColorIndex++;
                        return color;
                    });

                    g2d.setColor(processColor); // Use the assigned color
                    g2d.fillRoundRect(startX, PADDING, width, BAR_HEIGHT, 15, 15);
                    g2d.setColor(Color.BLACK);
                    g2d.drawRoundRect(startX, PADDING, width, BAR_HEIGHT, 15, 15);

                    g2d.setColor(Color.BLACK); // Set text color to black for better contrast
                    FontMetrics fm = g2d.getFontMetrics();
                    int stringWidth = fm.stringWidth(entry.getProcessName());
                    int stringHeight = fm.getAscent();
                    g2d.drawString(entry.getProcessName(), startX + (width - stringWidth) / 2,
                            PADDING + stringHeight + (BAR_HEIGHT - stringHeight) / 2);
                }

                // Draw time axis
                int axisY = PADDING + BAR_HEIGHT + 10;
                g2d.setColor(Color.BLACK);
                g2d.drawLine(PADDING, axisY, PADDING + maxTime * TIME_UNIT_WIDTH, axisY);

                // Draw ticks and labels
                for (int i = 0; i <= maxTime; i++) {
                    int tickX = PADDING + i * TIME_UNIT_WIDTH;
                    g2d.drawLine(tickX, axisY, tickX, axisY + 5);
                    g2d.drawString(String.valueOf(i),
                            tickX - (g2d.getFontMetrics().stringWidth(String.valueOf(i)) / 2),
                            axisY + 20);
                }
            } catch (Exception e) {
                e.printStackTrace();
                g.setColor(Color.RED);
                g.drawString("Error drawing Gantt Chart: " + e.getMessage(), 20, 30);
            }
        }
    }
}
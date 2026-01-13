package novalynx;

import javax.swing.*;
import java.awt.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.Timer;
import java.util.stream.Collectors;

public class ProcessManagement {
    private static List<PCB> processList;
    private Scheduler scheduler;
    private JFrame frame;
    private JTextArea statusArea;
    private JTextArea processListArea;
    private JTextArea ganttArea;
    private Timer statusTimer;
    private int totalMemory;
    private int usedMemory;
    private JLabel memoryStatusLabel;
    private JTextArea runningArea;
    private JTextArea blockedArea;
    private JTextArea suspendedArea;
    private JTextArea readyArea;
    private JTextArea completedArea;
    private List<GanttEntry> ganttEntries = new ArrayList<>();
    private long simulationWallClockStartTime = System.currentTimeMillis();
    private GanttChartPanel ganttChartPanel;
    private Map<Integer, Integer> processMemoryMap; // Maps process ID to allocated memory size
    private int currentTime = 0; // Simulated clock
    private int nextArrivalTime = 0; // Counter for sequential arrival times
    private PCB runningProcess = null; // The currently running process
    private Timer executionTimer = null; // Timer for the running process's execution burst
    private long sliceStartTime; // Wall-clock time when the current execution slice started

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
        statusCard.add(statusScrollPane, BorderLayout.CENTER);

        // Memory Status
        memoryStatusLabel = new JLabel("Memory: " + (usedMemory / 1024) + "/" + (totalMemory / 1024) + " KB used");
        memoryStatusLabel.setFont(NovaTheme.SUBHEADER_FONT);
        memoryStatusLabel.setForeground(NovaTheme.CYBER_GREEN);
        memoryStatusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        memoryStatusLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        statusCard.add(memoryStatusLabel, BorderLayout.SOUTH);

        // Use a JSplitPane to allow resizing between Process List and Status
        JSplitPane topSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, processListCard, statusCard);
        topSplitPane.setResizeWeight(0.6); // Give more initial space to the left component
        topSplitPane.setOpaque(false);
        topSplitPane.setBorder(null);
        topPanel.add(topSplitPane, BorderLayout.CENTER);

        // New: Process State Areas (now 5 areas)
        JPanel statePanel = new JPanel(new GridLayout(1, 5, 12, 0));
        statePanel.setOpaque(false);
        int stateAreaHeight = 120; // Reduced height from 180
        runningArea = new JTextArea();
        runningArea.setEditable(false);
        runningArea.setFont(new Font("Tahoma", Font.BOLD, 18));
        runningArea.setBackground(new Color(255, 255, 255));
        runningArea.setForeground(new Color(39, 174, 96));
        runningArea.setBorder(BorderFactory.createTitledBorder("Running"));
        runningArea.setPreferredSize(new Dimension(0, stateAreaHeight));
        blockedArea = new JTextArea();
        blockedArea.setEditable(false);
        blockedArea.setFont(new Font("Tahoma", Font.BOLD, 18));
        blockedArea.setBackground(new Color(255, 255, 255));
        blockedArea.setForeground(new Color(255, 165, 2));
        blockedArea.setBorder(BorderFactory.createTitledBorder("Blocked"));
        blockedArea.setPreferredSize(new Dimension(0, stateAreaHeight));
        suspendedArea = new JTextArea();
        suspendedArea.setEditable(false);
        suspendedArea.setFont(new Font("Tahoma", Font.BOLD, 18));
        suspendedArea.setBackground(new Color(255, 255, 255));
        suspendedArea.setForeground(new Color(155, 89, 182));
        suspendedArea.setBorder(BorderFactory.createTitledBorder("Suspended"));
        suspendedArea.setPreferredSize(new Dimension(0, stateAreaHeight));
        readyArea = new JTextArea();
        readyArea.setEditable(false);
        readyArea.setFont(new Font("Tahoma", Font.BOLD, 18));
        readyArea.setBackground(new Color(255, 255, 255));
        readyArea.setForeground(new Color(9, 132, 227));
        readyArea.setBorder(BorderFactory.createTitledBorder("Ready Queue"));
        readyArea.setPreferredSize(new Dimension(0, stateAreaHeight));
        completedArea = new JTextArea();
        completedArea.setEditable(false);
        completedArea.setFont(new Font("Tahoma", Font.BOLD, 18));
        completedArea.setBackground(new Color(255, 255, 255));
        completedArea.setForeground(new Color(127, 140, 141));
        completedArea.setBorder(BorderFactory.createTitledBorder("Completed"));
        completedArea.setPreferredSize(new Dimension(0, stateAreaHeight));
        statePanel.add(new JScrollPane(runningArea));
        statePanel.add(new JScrollPane(blockedArea));
        statePanel.add(new JScrollPane(suspendedArea));
        statePanel.add(new JScrollPane(readyArea));
        statePanel.add(new JScrollPane(completedArea));
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
        ganttScrollPane.setPreferredSize(new Dimension(0, 40));
        ganttCard.add(ganttScrollPane, BorderLayout.CENTER);

        // Control Buttons Panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        controlPanel.setOpaque(false);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        // Create buttons with emoji icons
        JButton createBtn = createPillButton("Create Process", new Color(0, 184, 148));
        JButton destroyBtn = createPillButton("Destroy Process", new Color(255, 121, 121));
        JButton suspendBtn = createPillButton("Suspend Process", new Color(253, 121, 168));
        JButton resumeBtn = createPillButton("Resume Process", new Color(9, 132, 227));
        JButton blockBtn = createPillButton("Block Process", new Color(255, 165, 2));
        JButton wakeupBtn = createPillButton("Wakeup Process", new Color(155, 89, 182));
        JButton dispatchBtn = createPillButton("Dispatch Process", new Color(0, 184, 148));
        JButton priorityBtn = createPillButton("Change Priority", new Color(253, 121, 168));
        JButton statsBtn = createPillButton("Show Statistics", new Color(9, 132, 227));
        JButton algorithmBtn = createPillButton("Change Algorithm", new Color(255, 165, 2));
        JButton backBtn = createPillButton("Back", new Color(155, 89, 182));

        // Add buttons to panel
        controlPanel.add(createBtn);
        controlPanel.add(destroyBtn);
        controlPanel.add(suspendBtn);
        controlPanel.add(resumeBtn);
        controlPanel.add(blockBtn);
        controlPanel.add(wakeupBtn);
        controlPanel.add(dispatchBtn);
        controlPanel.add(priorityBtn);
        controlPanel.add(statsBtn);
        controlPanel.add(algorithmBtn);
        controlPanel.add(backBtn);

        // Add action listeners for all buttons
        createBtn.addActionListener(e -> createProcess());
        destroyBtn.addActionListener(e -> destroyProcess());
        suspendBtn.addActionListener(e -> suspendProcess());
        resumeBtn.addActionListener(e -> resumeProcess());
        blockBtn.addActionListener(e -> blockProcess());
        wakeupBtn.addActionListener(e -> wakeupProcess());
        dispatchBtn.addActionListener(e -> dispatchProcess());
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
        controlScrollPane.setPreferredSize(new Dimension(0, 80));

        // Layout
        contentPanel.add(topWithStates, BorderLayout.NORTH);
        contentPanel.add(ganttCard, BorderLayout.CENTER);
        contentPanel.add(controlScrollPane, BorderLayout.SOUTH);

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
        if (currentAlgo != null && currentAlgo.equalsIgnoreCase("PRIORITY_SCHEDULING")) {
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
            updateProcessList();
            updateStatus();
            JOptionPane.showMessageDialog(frame, "Process " + selectedProcess.getPid() + " destroyed successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void suspendProcess() {
        if (runningProcess == null) {
            JOptionPane.showMessageDialog(frame, "No running process to suspend.", "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (executionTimer != null && executionTimer.isRunning()) {
            executionTimer.stop();
            long elapsed = (System.currentTimeMillis() - sliceStartTime) / 1000;
            int remaining = runningProcess.getBurstTime() - (int) elapsed;
            runningProcess.setBurstTime(Math.max(0, remaining));
        }

        runningProcess.setState(ProcessState.SUSPENDED);
        runningProcess = null; // No longer running
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
        if (runningProcess == null) {
            JOptionPane.showMessageDialog(frame, "No running process to block.", "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (executionTimer != null && executionTimer.isRunning()) {
            executionTimer.stop();
            long elapsed = (System.currentTimeMillis() - sliceStartTime) / 1000;
            int remaining = runningProcess.getBurstTime() - (int) elapsed;
            runningProcess.setBurstTime(Math.max(0, remaining));
        }

        runningProcess.setState(ProcessState.BLOCKED);
        runningProcess = null; // No longer running
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
        // Check if a process is already running
        if (runningProcess != null) {
            JOptionPane.showMessageDialog(frame, "A process is already running.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<PCB> ready = processList.stream()
                .filter(p -> p.getState() == ProcessState.READY)
                .collect(Collectors.toList());

        if (ready.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No READY process to dispatch.", "Info",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Let the user select which ready process to dispatch
        PCB toRun = showProcessSelectionDialog("Select Process to Dispatch", ready);
        if (toRun == null) {
            return; // User cancelled
        }

        toRun.setState(ProcessState.RUNNING);

        // Calculate waiting time at the moment of dispatch
        int waitingTime = currentTime - toRun.getArrivalTime();
        toRun.setWaitingTime(waitingTime);

        updateProcessList();
        updateStatus();

        // Use simulated time for gantt and stats
        final int burst = toRun.getBurstTime();

        // The process starts either when it arrives or when the CPU is free, whichever
        // is later.
        final int start = Math.max(currentTime, toRun.getArrivalTime());
        final int end = start + burst;
        runningProcess = toRun;
        sliceStartTime = System.currentTimeMillis();

        // The timer is for visual delay; calculations use simulated time
        executionTimer = new Timer(burst * 1000, e -> {
            toRun.setState(ProcessState.TERMINATED);
            toRun.setCompletionTime(end);

            int turnaroundTime = end - toRun.getArrivalTime();
            toRun.setTurnaroundTime(turnaroundTime);

            // Waiting time is Turnaround - Burst
            int calculatedWaitingTime = turnaroundTime - toRun.getOriginalBurstTime();
            toRun.setWaitingTime(calculatedWaitingTime);

            usedMemory -= toRun.getMemoryRequired();

            ganttEntries.add(new GanttEntry(toRun.getProcessName(), start, end));
            currentTime = end; // Update global simulated time to the process's completion time
            runningProcess = null; // Process finished

            updateProcessList();
            updateStatus();
            ganttChartPanel.repaint();
        });
        executionTimer.setRepeats(false);
        executionTimer.start();
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
        Map<Integer, Integer> ganttCompletionTimes = calculateGanttCompletionTimes();

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
            if (algorithm != null && algorithm.equals("PRIORITY_SCHEDULING") && pcb.getPriority() > 0) {
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

    // Calculate completion times based on Gantt chart timeline
    private Map<Integer, Integer> calculateGanttCompletionTimes() {
        Map<Integer, Integer> completionTimes = new HashMap<>();

        // This method is now less critical as completion time is set on the PCB
        // directly
        // but we can leave it for potential future use or alternative calculations.
        for (PCB pcb : processList) {
            if (pcb.getState() == ProcessState.TERMINATED) {
                completionTimes.put(pcb.getPid(), pcb.getCompletionTime());
            }
        }

        return completionTimes;
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
        all.append(String.format("%-5s %-15s %-10s %-10s %-10s %-10s %-10s\n", "PID", "Name", "Priority", "State",
                "Burst", "Memory", "Owner"));
        for (PCB pcb : processList) {
            all.append(String.format("%-5d %-15s %-10d %-10s %-10d %-10d %-10s\n",
                    pcb.getPid(), pcb.getProcessName(), pcb.getPriority(), pcb.getState(), pcb.getBurstTime(),
                    pcb.getMemoryRequired() / 1024, pcb.getOwner()));
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

    private static class GanttEntry {
        String processName;
        long startTime;
        long endTime;

        GanttEntry(String processName, long startTime, long endTime) {
            this.processName = processName;
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }

    // Custom JPanel for Gantt chart
    private class GanttChartPanel extends JPanel {
        private final int TIME_UNIT_WIDTH = 40;
        private final int BAR_HEIGHT = 30;
        private final int PADDING = 20;

        // Pre-defined list of nice colors for processes
        private final List<Color> colors = Arrays.asList(
                new Color(116, 185, 255), new Color(85, 239, 196), new Color(255, 234, 167),
                new Color(250, 177, 160), new Color(255, 118, 117), new Color(9, 132, 227),
                new Color(0, 184, 148), new Color(214, 48, 49), new Color(232, 67, 147));
        private Map<String, Color> processColorMap = new HashMap<>();
        private int nextColorIndex = 0;

        public GanttChartPanel() {
            setBackground(new Color(240, 240, 240));
            setFont(new Font("Arial", Font.PLAIN, 12));
        }

        @Override
        public Dimension getPreferredSize() {
            int maxTime = 0;
            if (!ganttEntries.isEmpty()) {
                maxTime = (int) ganttEntries.stream().mapToLong(e -> e.endTime).max().orElse(0);
            }
            if (maxTime < 10)
                maxTime = 10; // Minimum width

            return new Dimension(PADDING * 2 + maxTime * TIME_UNIT_WIDTH, PADDING * 2 + BAR_HEIGHT + 30);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            revalidate(); // Ensure preferred size is respected by scroll pane

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (ganttEntries.isEmpty()) {
                g2d.setColor(Color.GRAY);
                g2d.drawString("Gantt Chart is empty.", PADDING, PADDING + BAR_HEIGHT);
                return;
            }

            int maxTime = (int) ganttEntries.stream().mapToLong(e -> e.endTime).max().orElse(0);
            if (maxTime < 10)
                maxTime = 10;

            // Draw process bars
            for (GanttEntry entry : ganttEntries) {
                int startX = PADDING + (int) (entry.startTime * TIME_UNIT_WIDTH);
                int width = (int) ((entry.endTime - entry.startTime) * TIME_UNIT_WIDTH);

                // Ensure color is assigned and consistent for each process
                Color processColor = processColorMap.computeIfAbsent(entry.processName, name -> {
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
                int stringWidth = fm.stringWidth(entry.processName);
                int stringHeight = fm.getAscent();
                g2d.drawString(entry.processName, startX + (width - stringWidth) / 2,
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
                g2d.drawString(String.valueOf(i), tickX - (g2d.getFontMetrics().stringWidth(String.valueOf(i)) / 2),
                        axisY + 20);
            }
        }
    }
}
package novalynx;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;

public class SynchronizationPanel extends JPanel {
    private JFrame frame;
    private List<PCB> processes;
    private DefaultListModel<String> criticalListModel;
    private DefaultListModel<String> waitingListModel;
    private DefaultListModel<String> completedListModel;
    private List<String> messageLog = new ArrayList<>();
    private JPanel processStatusPanel;
    private JTextArea processStatusTextArea;
    private JPanel buttonPanel;
    private JPanel bottomPanel;
    private final Semaphore semaphore = new Semaphore(1, true); // Binary semaphore (mutex), fair
    private Timer statusTimer;

    public SynchronizationPanel(List<PCB> processes) {
        this.processes = processes;
        initializeUI();
        // Timer to update the semaphore status display periodically
        statusTimer = new Timer(100, e -> updateProcessStatusArea(null));
        statusTimer.start();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(NovaTheme.DEEP_NAVY);

        // Custom Title Bar
        JLabel title = new JLabel("Process Synchronization", SwingConstants.CENTER);
        title.setFont(NovaTheme.HEADER_FONT);
        title.setForeground(NovaTheme.NOVA_PURPLE);
        title.setBorder(BorderFactory.createEmptyBorder(24, 0, 16, 0));
        add(title, BorderLayout.NORTH);

        // Section Cards
        JPanel sectionPanel = new JPanel(new GridLayout(1, 3, 24, 0));
        sectionPanel.setOpaque(false);
        sectionPanel.setBorder(BorderFactory.createEmptyBorder(0, 32, 0, 32));

        // Critical Section Card
        criticalListModel = new DefaultListModel<>();
        JList<String> criticalList = new JList<>(criticalListModel);
        styleList(criticalList, new Color(255, 255, 255), new Color(255, 121, 121));
        JPanel criticalPanel = createCardPanel("Critical Section", criticalList, new Color(255, 121, 121));

        // Waiting Section Card
        waitingListModel = new DefaultListModel<>();
        JList<String> waitingList = new JList<>(waitingListModel);
        styleList(waitingList, new Color(255, 255, 255), new Color(0, 184, 148));
        JPanel waitingPanel = createCardPanel("Waiting Section", waitingList, new Color(0, 184, 148));

        // Completed Section Card
        completedListModel = new DefaultListModel<>();
        JList<String> completedList = new JList<>(completedListModel);
        styleList(completedList, new Color(255, 255, 255), new Color(9, 132, 227));
        JPanel completedPanel = createCardPanel("Completed Section", completedList, new Color(9, 132, 227));

        sectionPanel.add(criticalPanel);
        sectionPanel.add(waitingPanel);
        sectionPanel.add(completedPanel);
        add(sectionPanel, BorderLayout.CENTER);

        // Process Status Area (like screenshot, above buttons)
        processStatusPanel = new JPanel(new BorderLayout());
        processStatusPanel.setOpaque(true);
        processStatusPanel.setBackground(NovaTheme.DEEP_NAVY);
        processStatusPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(NovaTheme.NOVA_PURPLE, 2, true),
                "Process Status",
                0, 0,
                NovaTheme.SUBHEADER_FONT,
                Color.WHITE));
        processStatusTextArea = new JTextArea();
        processStatusTextArea.setEditable(false);
        processStatusTextArea.setFont(NovaTheme.TERMINAL_FONT);
        processStatusTextArea.setBackground(NovaTheme.DEEP_NAVY);
        processStatusTextArea.setForeground(Color.WHITE);
        processStatusTextArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        processStatusTextArea.setOpaque(true);
        JScrollPane processStatusScrollPane = new JScrollPane(processStatusTextArea);
        processStatusScrollPane.setBorder(null);
        processStatusPanel.add(processStatusScrollPane, BorderLayout.CENTER);
        processStatusPanel.setPreferredSize(new Dimension(0, 180));

        // Action Buttons Row (always at the very bottom)
        buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 24, 18));
        JButton criticalBtn = createPillButton("Critical System", new Color(255, 121, 121));
        JButton messageBtn = createPillButton("Message Passing", new Color(0, 184, 148));
        JButton backBtn = createPillButton("Back", new Color(155, 89, 182));
        buttonPanel.add(criticalBtn);
        buttonPanel.add(messageBtn);
        buttonPanel.add(backBtn);

        // Combine status area and buttons in a vertical BoxLayout panel
        bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setOpaque(false);
        bottomPanel.add(processStatusPanel);
        bottomPanel.add(buttonPanel);
        add(bottomPanel, BorderLayout.SOUTH);

        criticalBtn.addActionListener(e -> handleCriticalSystem());
        messageBtn.addActionListener(e -> handleMessagePassing());
        backBtn.addActionListener(e -> {
            if (frame != null) {
                frame.dispose();
                new ControlPanel().launch();
            }
        });

        updateProcessStatusArea(null);
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

    private JPanel createCardPanel(String title, JList<String> list, Color accent) {
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
        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.setOpaque(true);
        panel.setPreferredSize(new Dimension(0, 220));
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
        button.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 18));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 32, 10, 32));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void handleCriticalSystem() {
        List<PCB> available = new ArrayList<>();
        for (PCB pcb : processes) {
            if (!criticalListModel.contains(pcbString(pcb)) &&
                    !waitingListModel.contains(pcbString(pcb)) &&
                    !completedListModel.contains(pcbString(pcb))) {
                available.add(pcb);
            }
        }
        if (available.isEmpty()) {
            showStatus("No available processes for critical section.", new Color(255, 121, 121));
            return;
        }
        PCB[] pcbArr = available.toArray(new PCB[0]);
        List<PCB> selected = showProcessMultiSelectDialog(pcbArr, "Select Processes for Critical Section");
        if (selected == null || selected.isEmpty())
            return;
        new Thread(() -> {
            for (PCB pcb : selected) {
                SwingUtilities.invokeLater(() -> {
                    waitingListModel.addElement(pcbString(pcb));
                    showStatus("Process " + pcb.getPid() + " added to waiting section.", new Color(0, 184, 148));
                });
            }
            for (PCB pcb : selected) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
                SwingUtilities.invokeLater(() -> {
                    waitingListModel.removeElement(pcbString(pcb));
                    criticalListModel.addElement(pcbString(pcb));
                    showStatus("Process " + pcb.getPid() + " entered critical section.", new Color(255, 121, 121));
                });
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {
                }
                SwingUtilities.invokeLater(() -> {
                    criticalListModel.removeElement(pcbString(pcb));
                    completedListModel.addElement(pcbString(pcb));
                    showStatus("Process " + pcb.getPid() + " completed and moved to completed section.",
                            new Color(9, 132, 227));
                });
            }
        }).start();
    }

    private void handleMessagePassing() {
        if (processes.isEmpty()) {
            showStatus("No processes available for message passing.", new Color(255, 121, 121));
            return;
        }
        PCB[] pcbArr = processes.toArray(new PCB[0]);
        PCB sender = showProcessSingleSelectDialog(pcbArr, "Select Sender Process");
        if (sender == null)
            return;
        // Exclude sender from receiver list
        List<PCB> receivers = new ArrayList<>();
        for (PCB pcb : processes) {
            if (pcb != sender)
                receivers.add(pcb);
        }
        if (receivers.isEmpty()) {
            showStatus("No valid receiver available.", new Color(255, 121, 121));
            return;
        }
        PCB[] receiverArr = receivers.toArray(new PCB[0]);
        PCB receiver = showProcessSingleSelectDialog(receiverArr, "Select Receiver Process");
        if (receiver == null)
            return;
        String msg = JOptionPane.showInputDialog(this, "Enter message to send:", "Message Passing",
                JOptionPane.PLAIN_MESSAGE);
        if (msg == null || msg.trim().isEmpty())
            return;
        String logEntry = "Message from P" + sender.getPid() + " to P" + receiver.getPid() + ": " + msg;
        showStatus(logEntry, new Color(0, 184, 148));
    }

    private void showStatus(String msg, Color accent) {
        updateProcessStatusArea(msg);
    }

    private void updateProcessStatusArea(String newMsg) {
        if (newMsg != null && !newMsg.isEmpty()) {
            messageLog.add(0, newMsg); // Add new messages to the top
            if (messageLog.size() > 10) {
                messageLog.remove(messageLog.size() - 1); // Keep log size manageable
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("--- Semaphore Status ---\n");
        sb.append("Available Permits: ").append(semaphore.availablePermits()).append(" (1 = Unlocked, 0 = Locked)\n");
        sb.append("\n--- Message Log ---\n");
        if (messageLog.isEmpty()) {
            sb.append("No activity yet.\n");
        } else {
            for (String log : messageLog) {
                sb.append(log).append("\n");
            }
        }

        sb.append("\n--- Process Queues ---\n");
        for (PCB pcb : processes) {
            String status = "Ready";
            if (criticalListModel.contains(pcbString(pcb)))
                status = "Critical";
            else if (waitingListModel.contains(pcbString(pcb)))
                status = "Waiting";
            else if (completedListModel.contains(pcbString(pcb)))
                status = "Completed";
            sb.append(String.format("P%d: %s, State: %s\n", pcb.getPid(), pcb.getProcessName(), status));
        }
        processStatusTextArea.setText(sb.toString());
        processStatusTextArea.setCaretPosition(0); // Scroll to top
    }

    private String pcbString(PCB pcb) {
        return "Process " + pcb.getPid() + " (" + pcb.getProcessName() + ")";
    }

    private List<PCB> showProcessMultiSelectDialog(PCB[] options, String title) {
        JList<PCB> list = new JList<>(options);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setFont(new Font("Tahoma", Font.PLAIN, 16));
        JScrollPane scroll = new JScrollPane(list);
        scroll.setPreferredSize(new Dimension(320, 180));
        int result = JOptionPane.showConfirmDialog(frame, scroll, title, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            return list.getSelectedValuesList();
        }
        return null;
    }

    private PCB showProcessSingleSelectDialog(PCB[] options, String title) {
        JComboBox<PCB> combo = new JComboBox<>(options);
        combo.setFont(new Font("Tahoma", Font.PLAIN, 16));
        int result = JOptionPane.showConfirmDialog(frame, combo, title, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            return (PCB) combo.getSelectedItem();
        }
        return null;
    }

    public void launch() {
        frame = new JFrame();
        frame.setTitle("NovaLynx - Process Synchronization");
        NovaTheme.applyFrameSettings(frame);
        frame.setContentPane(this);
        frame.setVisible(true);
    }
}
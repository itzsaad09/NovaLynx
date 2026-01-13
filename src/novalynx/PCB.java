package novalynx;

import javax.swing.*;
import java.util.ArrayList;

public class PCB {
    // I/O state enum
    public enum IOState {
        NONE, // No I/O operation
        READING, // Process is reading from I/O
        WRITING, // Process is writing to I/O
        WAITING // Process is waiting for I/O
    }

    private static int pidCounter = 0; // Static counter to generate unique PID
    private int pid;
    private String processName;
    private int priority;
    private int memoryRequired;
    private String processor;
    private ProcessState state;
    private int burstTime; // Burst time in seconds
    private int originalBurstTime; // Original burst time for statistics
    private int remainingTime; // Remaining execution time in seconds
    private String owner; // Process owner
    private PCB parent; // Pointer to parent process
    private ArrayList<PCB> children; // List of child processes
    private int[] registers; // CPU registers
    private int allocatedMemory; // Pointer to allocated memory
    private IOState ioState; // I/O state information
    private int quantum = 2; // Default quantum for Round Robin
    private String schedulingAlgorithm = "FCFS"; // Default scheduling algorithm
    private int arrivalTime = 0; // Arrival time for statistics
    private int completionTime = 0; // Completion time for statistics
    private int waitingTime = 0; // Waiting time for statistics
    private int turnaroundTime = 0; // Turnaround time for statistics

    // Constructor to initialize the process with the given details
    public PCB(String processName, int priority, int memoryRequired, String processor, String owner) {
        this.pid = ++pidCounter; // Increment PID counter to ensure unique PID
        this.processName = processName;
        this.priority = priority;
        this.memoryRequired = memoryRequired;
        this.processor = processor;
        this.state = ProcessState.NEW; // Default state
        this.burstTime = 0; // Initialize burst time
        this.originalBurstTime = 0; // Initialize original burst time
        this.remainingTime = 0; // Initialize remaining time
        this.owner = owner;
        this.parent = null;
        this.children = new ArrayList<>();
        this.registers = new int[8]; // Initialize CPU registers
        this.allocatedMemory = 0;
        this.ioState = IOState.NONE;
        this.schedulingAlgorithm = "FCFS";
        this.arrivalTime = 0;
        this.completionTime = 0;
        this.waitingTime = 0;
        this.turnaroundTime = 0;
    }

    // Simplified constructor for basic process creation
    public PCB(String processName, int priority, int burstTime) {
        this.pid = ++pidCounter;
        this.processName = processName;
        this.priority = priority;
        this.memoryRequired = 8 * 1024; // Default 8 KB in bytes
        this.processor = "CPU";
        this.state = ProcessState.NEW;
        this.burstTime = burstTime;
        this.originalBurstTime = burstTime;
        this.remainingTime = burstTime;
        this.owner = "System";
        this.parent = null;
        this.children = new ArrayList<>();
        this.registers = new int[8];
        this.allocatedMemory = this.memoryRequired; // Set allocated memory
        this.ioState = IOState.NONE;
        this.schedulingAlgorithm = "FCFS";
        this.arrivalTime = 0;
        this.completionTime = 0;
        this.waitingTime = 0;
        this.turnaroundTime = 0;
    }

    // Getters for the fields
    public int getPid() {
        return pid;
    }

    public String getProcessName() {
        return processName;
    }

    public int getPriority() {
        return priority;
    }

    public int getMemoryRequired() {
        return memoryRequired;
    }

    public String getProcessor() {
        return processor;
    }

    public ProcessState getState() {
        return state;
    }

    public int getBurstTime() {
        return burstTime;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public String getOwner() {
        return owner;
    }

    public PCB getParent() {
        return parent;
    }

    public ArrayList<PCB> getChildren() {
        return children;
    }

    public int[] getRegisters() {
        return registers;
    }

    public int getAllocatedMemory() {
        return allocatedMemory;
    }

    public IOState getIOState() {
        return ioState;
    }

    public int getQuantum() {
        return quantum;
    }

    // Setters for mutable fields
    public void setState(ProcessState newState) {
        this.state = newState;
    }

    public void setPriority(int newPriority) {
        if (newPriority >= 1 && newPriority <= 9) {
            this.priority = newPriority;
        } else {
            throw new IllegalArgumentException("Priority must be between 1 (highest) and 9 (lowest)");
        }
    }

    public void setBurstTime(int burstTime) {
        if (burstTime > 0) {
            this.burstTime = burstTime;
            this.remainingTime = burstTime; // Initialize remaining time with burst time
        } else {
            throw new IllegalArgumentException("Burst time must be greater than 0");
        }
    }

    public void setRemainingTime(int time) {
        this.remainingTime = time;
    }

    public void setParent(PCB parent) {
        this.parent = parent;
        if (parent != null) {
            parent.addChild(this);
        }
    }

    public void addChild(PCB child) {
        children.add(child);
    }

    public void setAllocatedMemory(int memory) {
        this.allocatedMemory = memory;
    }

    public void setIOState(IOState state) {
        this.ioState = state;
    }

    public void setMemoryRequired(int memoryRequired) {
        this.memoryRequired = memoryRequired;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setQuantum(int quantum) {
        this.quantum = quantum;
    }

    // Getters and setters for statistics fields
    public int getOriginalBurstTime() {
        return originalBurstTime;
    }

    public void setOriginalBurstTime(int originalBurstTime) {
        this.originalBurstTime = originalBurstTime;
    }

    public String getSchedulingAlgorithm() {
        return schedulingAlgorithm;
    }

    public void setSchedulingAlgorithm(String schedulingAlgorithm) {
        this.schedulingAlgorithm = schedulingAlgorithm;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(int arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public int getCompletionTime() {
        return completionTime;
    }

    public void setCompletionTime(int completionTime) {
        this.completionTime = completionTime;
    }

    public int getWaitingTime() {
        return waitingTime;
    }

    public void setWaitingTime(int waitingTime) {
        this.waitingTime = waitingTime;
    }

    public int getTurnaroundTime() {
        return turnaroundTime;
    }

    public void setTurnaroundTime(int turnaroundTime) {
        this.turnaroundTime = turnaroundTime;
    }

    // Method to display process details
    public void displayPCBDetails() {
        StringBuilder details = new StringBuilder();
        details.append("📑 Process Details:\n\n");
        details.append("PID: ").append(pid).append("\n");
        details.append("Process Name: ").append(processName).append("\n");
        details.append("Owner: ").append(owner).append("\n");
        details.append("Priority: ").append(priority).append("\n");
        details.append("Memory Required: ").append(memoryRequired / 1024).append(" KB\n");
        details.append("Allocated Memory: ").append(allocatedMemory / 1024).append(" KB\n");
        details.append("Processor: ").append(processor).append("\n");
        details.append("Burst Time: ").append(burstTime).append(" seconds\n");
        details.append("Remaining Time: ").append(remainingTime).append(" seconds\n");
        details.append("State: ").append(state).append("\n");
        details.append("I/O State: ").append(ioState).append("\n");

        if (parent != null) {
            details.append("Parent PID: ").append(parent.getPid()).append("\n");
        }

        if (!children.isEmpty()) {
            details.append("Child Processes: ");
            for (PCB child : children) {
                details.append(child.getPid()).append(" ");
            }
            details.append("\n");
        }

        JOptionPane.showMessageDialog(null, details.toString());
    }

    @Override
    public String toString() {
        return String.format(
                "PID: %d | Name: %s | Owner: %s | Priority: %d | Memory: %d/%d KB | Processor: %s | Burst: %d | Remaining: %d | State: %s | I/O: %s",
                pid, processName, owner, priority, allocatedMemory / 1024, memoryRequired / 1024, processor, burstTime,
                remainingTime, state, ioState);
    }
}
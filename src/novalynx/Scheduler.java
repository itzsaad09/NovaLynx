package novalynx;

import java.util.*;
import java.util.stream.Collectors;

public class Scheduler {
    private List<PCB> readyQueue = new ArrayList<>();
    private List<GanttEntry> ganttChart = new ArrayList<>();
    private SchedulingAlgorithm algorithm = SchedulingAlgorithm.FCFS;
    private int currentTime = 0;
    private String schedulingType = "Non-Preemptive"; // Default scheduling type
    private int timeQuantum = 3; // Default time quantum for Round Robin

    public enum SchedulingAlgorithm {
        FCFS,
        PRIORITY,
        ROUND_ROBIN
    }

    public static class GanttEntry {
        private PCB process;
        private int startTime;
        private int endTime;

        public GanttEntry(PCB process, int startTime, int endTime) {
            this.process = process;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public PCB getProcess() {
            return process;
        }

        public int getStartTime() {
            return startTime;
        }

        public int getEndTime() {
            return endTime;
        }
    }

    public void setAlgorithm(String algo) {
        if (algo.equalsIgnoreCase("FCFS") || algo.equalsIgnoreCase("First Come First Serve")) {
            algorithm = SchedulingAlgorithm.FCFS;
        } else if (algo.equalsIgnoreCase("Priority") || algo.equalsIgnoreCase("Priority Scheduling")) {
            algorithm = SchedulingAlgorithm.PRIORITY;
        } else if (algo.equalsIgnoreCase("Round Robin") || algo.equalsIgnoreCase("ROUND_ROBIN")) {
            algorithm = SchedulingAlgorithm.ROUND_ROBIN;
        }
    }

    public void addProcess(PCB pcb) {
        readyQueue.add(pcb);
        pcb.setState(ProcessState.READY);
    }

    public void dispatchProcess(PCB pcb) {
        if (!readyQueue.contains(pcb)) {
            readyQueue.add(pcb);
        }

        // Update Gantt chart
        int startTime = currentTime;
        int endTime = startTime + pcb.getBurstTime();
        ganttChart.add(new GanttEntry(pcb, startTime, endTime));
        currentTime = endTime;

        // Update process state
        pcb.setState(ProcessState.RUNNING);
        pcb.setRemainingTime(0); // Process is completed

        // Remove from ready queue
        readyQueue.remove(pcb);

        // Handle different scheduling algorithms
        if (algorithm == SchedulingAlgorithm.PRIORITY && schedulingType.equals("Preemptive")) {
            preemptivePriorityScheduling();
        } else if (algorithm == SchedulingAlgorithm.ROUND_ROBIN) {
            roundRobinScheduling();
        }
    }

    private void roundRobinScheduling() {
        if (readyQueue.isEmpty())
            return;

        PCB currentProcess = readyQueue.get(0);
        int startTime = currentTime;
        int executionTime = Math.min(timeQuantum, currentProcess.getRemainingTime());

        // Add entry to Gantt chart
        ganttChart.add(new GanttEntry(currentProcess, startTime, startTime + executionTime));

        // Update process state
        currentProcess.setRemainingTime(currentProcess.getRemainingTime() - executionTime);
        currentTime += executionTime;

        // Move process to end of queue if not completed
        readyQueue.remove(0);
        if (currentProcess.getRemainingTime() > 0) {
            currentProcess.setState(ProcessState.READY);
            readyQueue.add(currentProcess);
        } else {
            currentProcess.setState(ProcessState.TERMINATED);
        }
    }

    private void preemptivePriorityScheduling() {
        if (readyQueue.isEmpty())
            return;

        // Sort by priority (lower number = higher priority)
        readyQueue.sort(Comparator.comparingInt(PCB::getPriority));

        // Get highest priority process
        PCB highestPriorityProcess = readyQueue.get(0);

        // If there's a running process, check if we need to preempt
        PCB currentRunningProcess = readyQueue.stream()
                .filter(p -> p.getState() == ProcessState.RUNNING)
                .findFirst()
                .orElse(null);

        if (currentRunningProcess != null &&
                highestPriorityProcess.getPriority() < currentRunningProcess.getPriority()) {
            // Preempt current process
            currentRunningProcess.setState(ProcessState.READY);

            // Add entry to Gantt chart for preempted process
            ganttChart.add(new GanttEntry(
                    currentRunningProcess,
                    currentTime - currentRunningProcess.getRemainingTime(),
                    currentTime));

            // Start new process
            highestPriorityProcess.setState(ProcessState.RUNNING);
            highestPriorityProcess.setRemainingTime(highestPriorityProcess.getBurstTime());
        }
    }

    private void fcfsScheduling() {
        while (!readyQueue.isEmpty()) {
            PCB pcb = readyQueue.remove(0);
            int start = currentTime;
            int burst = pcb.getBurstTime();
            currentTime += burst;
            ganttChart.add(new GanttEntry(pcb, start, currentTime));
            pcb.setState(ProcessState.TERMINATED);
        }
    }

    public List<GanttEntry> getGanttChart() {
        return new ArrayList<>(ganttChart);
    }

    public void clearGanttChart() {
        ganttChart.clear();
        currentTime = 0;
    }

    public void setSchedulingType(String type) {
        this.schedulingType = type;
    }

    public void setTimeQuantum(int quantum) {
        if (quantum >= 3 && quantum <= 7) {
            this.timeQuantum = quantum;
        } else {
            throw new IllegalArgumentException("Time quantum must be between 3 and 7");
        }
    }

    public PCB selectNextProcess(List<PCB> processList) {
        if (processList.isEmpty()) {
            return null;
        }

        // Filter ready processes
        List<PCB> readyProcesses = processList.stream()
                .filter(p -> p.getState() == ProcessState.READY)
                .collect(Collectors.toList());

        if (readyProcesses.isEmpty()) {
            return null;
        }

        switch (algorithm) {
            case FCFS:
                return readyProcesses.get(0); // First come, first served
            case PRIORITY:
                if (schedulingType.equals("Preemptive")) {
                    // For preemptive, find highest priority process
                    return readyProcesses.stream()
                            .min(Comparator.comparingInt(PCB::getPriority))
                            .orElse(null);
                } else {
                    // For non-preemptive, find highest priority process that hasn't started
                    return readyProcesses.stream()
                            .filter(p -> p.getRemainingTime() == p.getBurstTime())
                            .min(Comparator.comparingInt(PCB::getPriority))
                            .orElse(null);
                }
            case ROUND_ROBIN:
                // Simple round robin implementation
                return readyProcesses.get(0);
            default:
                return readyProcesses.get(0);
        }
    }

    public String getCurrentAlgorithm() {
        switch (algorithm) {
            case FCFS:
                return "FCFS";
            case PRIORITY:
                return "PRIORITY_SCHEDULING";
            case ROUND_ROBIN:
                return "ROUND_ROBIN";
            default:
                return "FCFS";
        }
    }

    public String getSchedulingType() {
        return schedulingType;
    }

    public int getTimeQuantum() {
        return timeQuantum;
    }

    public int getCurrentTime() {
        return currentTime;
    }

    public void removeProcess(PCB process) {
        readyQueue.remove(process);
        // Remove from Gantt chart if present
        ganttChart.removeIf(entry -> entry.getProcess().getPid() == process.getPid());
    }
}
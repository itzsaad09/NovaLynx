package novalynx;

import java.util.*;
import java.util.stream.Collectors;

public class Scheduler {
    private List<PCB> processPool = new ArrayList<>(); // All processes in the system
    private List<PCB> readyQueue = new ArrayList<>(); // Processes that have arrived and are ready
    private List<GanttEntry> ganttChart = new ArrayList<>();
    private SchedulingAlgorithm algorithm = SchedulingAlgorithm.FCFS;
    private int currentTime = 0;
    private String schedulingType = "Non-Preemptive"; // Default scheduling type
    private int timeQuantum = 2; // Default time quantum for Round Robin
    private PCB currentRunningProcess = null;

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

        public String getProcessName() {
            return process.getProcessName();
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
        resetSimulation();
    }

    public void resetSimulation() {
        ganttChart.clear();
        currentTime = 0;
        currentRunningProcess = null;
        for (PCB pcb : readyQueue) {
            pcb.setRemainingTime(pcb.getOriginalBurstTime());
            pcb.setState(ProcessState.READY);
        }
    }

    public void addProcess(PCB pcb) {
        if (!processPool.contains(pcb)) {
            processPool.add(pcb);
            pcb.setState(ProcessState.READY);
            pcb.setRemainingTime(pcb.getOriginalBurstTime());
        }
    }

    public void removeProcess(PCB pcb) {
        processPool.remove(pcb);
        readyQueue.remove(pcb);
        if (currentRunningProcess == pcb) {
            currentRunningProcess = null;
        }
        ganttChart.removeIf(entry -> entry.getProcess() == pcb);
    }

    /**
     * Executes one step of the simulation.
     * Returns true if a process was executed, false if nothing to do.
     */
    public boolean stepSimulation() {
        // 1. Move processes from pool to readyQueue based on arrival time
        updateQueueForArrivals();

        // 2. Identify available processes (already in readyQueue or currently running)
        List<PCB> available = readyQueue.stream()
                .filter(p -> p.getState() == ProcessState.READY || p.getState() == ProcessState.RUNNING)
                .collect(Collectors.toList());

        if (available.isEmpty()) {
            // Check if there are processes that haven't arrived yet
            boolean moreToArrive = processPool.stream()
                    .anyMatch(p -> p.getArrivalTime() > currentTime && p.getState() != ProcessState.TERMINATED);
            if (moreToArrive) {
                currentTime++; // CPU Idle
                return true;
            }
            return false;
        }

        switch (algorithm) {
            case FCFS:
                runFCFS(available);
                break;
            case PRIORITY:
                if (schedulingType.equalsIgnoreCase("Preemptive")) {
                    runPreemptivePriority(available);
                } else {
                    runNonPreemptivePriority(available);
                }
                break;
            case ROUND_ROBIN:
                runRoundRobin(available);
                break;
        }
        return true;
    }

    private void updateQueueForArrivals() {
        // Collect all processes that arrive AT OR BEFORE current time and are not in
        // readyQueue yet
        List<PCB> newArrivals = processPool.stream()
                .filter(p -> p.getArrivalTime() <= currentTime)
                .filter(p -> !readyQueue.contains(p))
                .filter(p -> p.getState() != ProcessState.TERMINATED)
                .sorted(Comparator.comparingInt(PCB::getArrivalTime))
                .collect(Collectors.toList());

        readyQueue.addAll(newArrivals);
    }

    private void runFCFS(List<PCB> available) {
        // In FCFS, if a process is already running, continue running it
        if (currentRunningProcess != null && currentRunningProcess.getState() == ProcessState.RUNNING
                && currentRunningProcess.getRemainingTime() > 0) {
            executeOneTimeUnit(currentRunningProcess);
            return;
        }

        // Otherwise, pick the one with the earliest arrival time
        PCB toRun = available.stream()
                .min(Comparator.comparingInt(PCB::getArrivalTime))
                .orElse(null);

        if (toRun != null) {
            executeOneTimeUnit(toRun);
        }
    }

    private void runNonPreemptivePriority(List<PCB> available) {
        // If a process is already running, continue running it until finish
        if (currentRunningProcess != null && currentRunningProcess.getState() == ProcessState.RUNNING
                && currentRunningProcess.getRemainingTime() > 0) {
            executeOneTimeUnit(currentRunningProcess);
            return;
        }

        PCB toRun = available.stream()
                .min(Comparator.comparingInt(PCB::getPriority)
                        .thenComparingInt(PCB::getArrivalTime))
                .orElse(null);

        if (toRun != null) {
            executeOneTimeUnit(toRun);
        }
    }

    private void runPreemptivePriority(List<PCB> available) {
        PCB toRun = available.stream()
                .min(Comparator.comparingInt(PCB::getPriority)
                        .thenComparingInt(PCB::getArrivalTime))
                .orElse(null);

        if (toRun != null) {
            executeOneTimeUnit(toRun);
        }
    }

    private void runRoundRobin(List<PCB> available) {
        // If a process is already running, continue running it for this quantum
        if (currentRunningProcess != null && currentRunningProcess.getState() == ProcessState.RUNNING
                && currentRunningProcess.getRemainingTime() > 0) {
            // How long has it been running in THIS slice?
            // Actually executeQuantum already handles the whole quantum slice.
            // If we want it to be unit-by-unit visible, we need to track quantum counter.
            // But executeQuantum is already granular enough for RR.
            // Let's make it unit-by-unit as well for consistency.
            executeOneTimeUnit(currentRunningProcess);
            return;
        }

        PCB toRun = available.get(0);
        executeOneTimeUnit(toRun);
    }

    private void executeOneTimeUnit(PCB pcb) {
        int start = currentTime;
        pcb.setState(ProcessState.RUNNING);
        currentRunningProcess = pcb; // Track which one is running

        // Execute for 1 unit
        currentTime += 1;
        pcb.setRemainingTime(pcb.getRemainingTime() - 1);

        // Add or merge Gantt entry
        if (!ganttChart.isEmpty()) {
            GanttEntry last = ganttChart.get(ganttChart.size() - 1);
            if (last.getProcess() == pcb && last.endTime == start) {
                last.endTime = currentTime;
            } else {
                ganttChart.add(new GanttEntry(pcb, start, currentTime));
            }
        } else {
            ganttChart.add(new GanttEntry(pcb, start, currentTime));
        }

        if (pcb.getRemainingTime() <= 0) {
            pcb.setState(ProcessState.TERMINATED);
            pcb.setCompletionTime(currentTime);
            pcb.setTurnaroundTime(pcb.getCompletionTime() - pcb.getArrivalTime());
            pcb.setWaitingTime(pcb.getTurnaroundTime() - pcb.getOriginalBurstTime());
            currentRunningProcess = null; // Done

            if (algorithm == SchedulingAlgorithm.ROUND_ROBIN) {
                readyQueue.remove(pcb);
                // Do NOT add back to readyQueue if terminated
            }
        } else {
            // For Round Robin, we might need to cycle after quantum
            if (algorithm == SchedulingAlgorithm.ROUND_ROBIN) {
                // Check if it finished its quantum
                // For simplicity, let's just cycle it if we reach timeQuantum
                // We need to track how long it's been running.
                // Let's assume the user wants to see it running unit by unit.
                // If it reached quantum, or if it's preemptive priority and others are
                // better...
                // (Preemptive priority is handled in stepSimulation picking the next one)

                // Track quantum: calculate how many units it has run in the current slice
                GanttEntry last = ganttChart.get(ganttChart.size() - 1);
                int sliceDuration = last.endTime - last.startTime;
                if (sliceDuration >= timeQuantum) {
                    pcb.setState(ProcessState.READY);

                    // CRITICAL: Re-check arrivals BEFORE putting this process back at the end
                    // In many models, a process arriving at T=2 enters the queue BEFORE
                    // a process finished its quantum at T=2.
                    updateQueueForArrivals();

                    readyQueue.remove(pcb);
                    readyQueue.add(pcb);
                    currentRunningProcess = null;
                }
            } else if (algorithm == SchedulingAlgorithm.PRIORITY && schedulingType.equalsIgnoreCase("Preemptive")) {
                pcb.setState(ProcessState.READY);
                currentRunningProcess = null;
            } else {
                // FCFS or Non-Preemptive Priority: stays RUNNING
            }
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
        this.timeQuantum = quantum;
    }

    public String getCurrentAlgorithm() {
        return algorithm.toString();
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
}
package novalynx;

import java.util.ArrayList;
import java.util.List;

public class PCBManager {
    private static final List<PCB> processList = new ArrayList<>();

    public static PCB createProcess(String processName, int priority, int memory, String processor, String owner) {
        PCB newProcess = new PCB(processName, priority, memory, processor, owner);
        processList.add(newProcess);
        return newProcess;
    }

    public static List<PCB> getAllProcesses() {
        return processList;
    }

    public static int getProcessCount() {
        return processList.size();
    }
}

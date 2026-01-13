package novalynx;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryManager {
    private static final String CONFIG_FILE = "src/novalynx/config.properties";
    private static final java.util.Properties config = new java.util.Properties();

    private int pageSize;
    private int totalMemory;
    private int totalFrames;
    private Frame[] frames;
    private Map<Integer, List<Integer>> processAllocations; // PID -> List of frame numbers

    public MemoryManager() {
        loadConfig();
        initializeMemory();
    }

    private void loadConfig() {
        try {
            config.load(new FileInputStream(CONFIG_FILE));
            pageSize = Integer.parseInt(config.getProperty("page.size", "4096"));
            totalMemory = Integer.parseInt(config.getProperty("total.memory", "1048576"));
            totalFrames = totalMemory / pageSize;
        } catch (Exception e) {
            System.err.println("Error loading memory configuration: " + e.getMessage());
            // Set default values in KB
            pageSize = 4 * 1024; // 4KB per page
            totalMemory = 1024 * 1024; // 1MB total memory (1024 KB)
            totalFrames = totalMemory / pageSize;
        }
    }

    private void initializeMemory() {
        frames = new Frame[totalFrames];
        for (int i = 0; i < totalFrames; i++) {
            frames[i] = new Frame(i);
        }
        processAllocations = new ConcurrentHashMap<>();
    }

    public boolean allocateMemory(PCB process) {
        // Check if process is already allocated
        if (processAllocations.containsKey(process.getPid())) {
            return true;
        }

        int requiredPages = (int) Math.ceil((double) process.getMemoryRequired() / pageSize);
        List<Integer> freeFrames = new ArrayList<>();
        for (int i = 0; i < totalFrames; i++) {
            if (!frames[i].isAllocated()) {
                freeFrames.add(i);
            }
        }

        if (requiredPages > freeFrames.size()) {
            return false; // Not enough memory
        }

        List<Integer> allocatedFrameNumbers = new ArrayList<>();
        for (int i = 0; i < requiredPages; i++) {
            int frameNumber = freeFrames.get(i);
            frames[frameNumber].allocate(process.getPid(), i);
            allocatedFrameNumbers.add(frameNumber);
        }

        processAllocations.put(process.getPid(), allocatedFrameNumbers);
        return true;
    }

    public void deallocateMemory(PCB process) {
        List<Integer> allocatedFrames = processAllocations.remove(process.getPid());
        if (allocatedFrames != null) {
            for (int frameNumber : allocatedFrames) {
                frames[frameNumber].deallocate();
            }
        }
    }

    public Map<Integer, List<Integer>> getProcessAllocations() {
        return processAllocations;
    }

    public Frame[] getFrames() {
        return frames;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalMemory() {
        return totalMemory;
    }

    public int getAvailableFrames() {
        int count = 0;
        for (Frame frame : frames) {
            if (!frame.isAllocated()) {
                count++;
            }
        }
        return count;
    }

    public boolean setPageSize(int newPageSize) {
        if (!processAllocations.isEmpty()) {
            return false; // Cannot change page size when memory is allocated
        }

        if (newPageSize > 0 && newPageSize <= totalMemory) {
            this.pageSize = newPageSize;
            this.totalFrames = totalMemory / pageSize;

            // Re-initialize memory with the new structure
            initializeMemory();

            // Save to config file
            try {
                config.setProperty("page.size", String.valueOf(newPageSize));
                config.store(new FileOutputStream(CONFIG_FILE), "Updated page size");
            } catch (Exception e) {
                System.err.println("Error saving page size configuration: " + e.getMessage());
            }
            return true;
        }
        return false;
    }

    // Inner classes for Page and Frame
    public static class Frame {
        private final int frameNumber;
        private boolean isAllocated;
        private int processId;
        private int pageNumber;

        public Frame(int frameNumber) {
            this.frameNumber = frameNumber;
            this.isAllocated = false;
        }

        public boolean isAllocated() {
            return isAllocated;
        }

        public void allocate(int processId, int pageNumber) {
            this.processId = processId;
            this.pageNumber = pageNumber;
            this.isAllocated = true;
        }

        public void deallocate() {
            this.isAllocated = false;
            this.processId = 0;
            this.pageNumber = 0;
        }

        public int getProcessId() {
            return processId;
        }

        public int getPageNumber() {
            return pageNumber;
        }

        public int getFrameNumber() {
            return frameNumber;
        }
    }
}
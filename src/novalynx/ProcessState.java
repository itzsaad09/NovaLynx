package novalynx;

public enum ProcessState {
    NEW, // Process is being created
    READY, // Process is ready to run
    RUNNING, // Process is currently running
    BLOCKED, // Process is blocked waiting for I/O
    SUSPENDED, // Process is suspended
    TERMINATED // Process has completed execution
}
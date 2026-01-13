package novalynx;

public enum SchedulingAlgorithm {
    FCFS("First Come First Serve"),
    PRIORITY("Priority Scheduling");

    private final String description;

    SchedulingAlgorithm(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
# NovaLynx - Next-generation OS Virtualization Architecture

NovaLynx is a sophisticated Operating System simulator built with Java Swing. It provides a visual and interactive environment to demonstrate key OS concepts including Process Management, Memory Management, and Process Synchronization.

## 🌟 Features

### 🖥️ Process Management
A comprehensive dashboard to simulate process scheduling and lifecycle management.
- **Process Lifecycle Control**: Create, destroy, suspend, resume, block, wakeup, and dispatch processes.
- **Scheduling Algorithms**:
  - First-Come, First-Served (FCFS)
  - Priority Scheduling
  - Round Robin (with configurable time quantum)
- **Visualizations**:
  - Real-time Gantt Chart
  - Process State Queues (Ready, Running, Blocked, Suspended, Completed)
  - Detailed Process Control Block (PCB) information

### 💾 Memory Management
Simulates memory allocation using a paging mechanism.
- **Paging System**: Visual representation of memory frames and page allocation.
- **Configurable Memory**: Adjustable page size and total memory via `config.properties`.
- **Dynamic Allocation**: Allocate and deallocate memory for processes in real-time.
- **Visual Feedback**: visual status of used vs. total memory.

### 🔄 Process Synchronization
Demonstrates concurrency control and inter-process communication.
- **Critical Section**: Simulates mutex locks and semaphores to manage access to critical resources.
- **Message Passing**: Simulate sending and receiving messages between processes.
- **Queue Visualization**: Visual queues for processes Waiting, in Critical Section, and Completed.

### 🎨 Modern UI/UX
- **NovaTheme**: A custom dark-themed design system with deep navy backgrounds and neon accents.
- **Animations**: Smooth fade-in welcome screen and interactive UI elements.
- **User-Friendly Controls**: "Pill" shaped buttons and intuitive dialogs for input.

## 🚀 Getting Started

### Prerequisites
- Java Development Kit (JDK) 8 or higher.
- Apache Ant (for building the project) or a compatible IDE (NetBeans, IntelliJ IDEA, Eclipse).

### Installation & Running

1.  **Clone the repository** (or extract the source code).
2.  **Open the project** in your preferred IDE (NetBeans project files are included).
3.  **Build and Run**:
    - **Using Ant**:
      ```bash
      ant run
      ```
    - **Using IDE**: Run the `novalynx.NovaLynx` class as the main entry point.

### Configuration
You can customize the memory settings by editing `src/novalynx/config.properties`:
```properties
page.size=4096
total.memory=1048576
```

## 📂 Project Structure

- `src/novalynx/`: Source code package.
  - `NovaLynx.java`: Application entry point.
  - `ControlPanel.java`: Main dashboard.
  - `ProcessManagement.java`: Process scheduling logic and UI.
  - `MemoryManager.java`: core memory logic.
  - `SynchronizationPanel.java`: Synchronization demos.
  - `NovaTheme.java`: UI styling and constants.

## 👥 Credits
Developed by Hafiz Muhammad Saad.

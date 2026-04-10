# CodeCanvas

## Description

CodeCanvas is a sophisticated JavaFX desktop application for visualizing Data Structures and Algorithms (DSA). It provides interactive, step-by-step visualizations of 11 core algorithm modules, enabling students and developers to understand complex data structure operations through real-time animations. Built with Java 21 and JavaFX 21.0.6, this educational tool offers an immersive learning experience with smooth animations, code snippets, and theme support.

## Features

- **11 Interactive Modules**: Array, Stack, Queue, LinkedList, Binary Search Tree (BST), Heap, Graph, Sorting, Recursion, and Dynamic Programming (DP)
- **Real-Time Visualization**: Canvas and component-based graphics with smooth animations for step-by-step execution
- **Control Options**: Play, Pause, Next, Previous controls for manual stepping through algorithms
- **Code Display**: View actual Java implementation code for each algorithm, with copy functionality
- **Multiple Visualization Types**: Grid layouts, bar charts, trees, graphs with color-coded states
- **Light/Dark Themes**: Toggle between professional light and dark CSS themes
- **Speed Control**: Adjustable animation speed via sliders
- **Interactive Input**: Manual data entry or auto-generated test cases
- **Educational Narratives**: Context stories explaining each data structure and algorithm
- **Full-Screen Mode**: Immersive desktop experience
- **Algorithm Coverage**:
  - Data Structures: Array, Stack, Queue, LinkedList, BST, Heap, Graph
  - Sorting Algorithms: Bubble Sort, Selection Sort, Insertion Sort, Quick Sort, Merge Sort
  - Graph Algorithms: DFS, BFS, Minimum Spanning Tree (Prim's)
  - Tree Algorithms: In-Order, Pre-Order, Post-Order, Level-Order traversals
  - Dynamic Programming: 0/1 Knapsack, Coin Change, Fibonacci with Memoization, Longest Common Subsequence (LCS), Sequence Alignment
  - Advanced: Recursion call stack visualization

## Installation

### Prerequisites
- **Java 21**: Ensure you have Java Development Kit (JDK) 21 installed. You can download it from [Oracle](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html) or [Adoptium](https://adoptium.net/).
- **Gradle**: The project uses Gradle Wrapper, so no separate installation is needed. However, ensure Gradle is compatible (version 8.x recommended).

### Steps to Install and Run

1. **Clone the Repository**:
   ```
   git clone https://github.com/Ritaz13/DSA--visualizer.git
   cd DSA--visualizer
   ```

2. **Verify Java Version**:
   ```
   java -version
   ```
   Ensure it shows Java 21.

3. **Build the Project**:
   - On Windows:
     ```
     .\gradlew.bat build
     ```
   - On Linux/macOS:
     ```
     ./gradlew build
     ```
   This will download dependencies (JavaFX, ControlsFX, etc.) and compile the code.

4. **Run the Application**:
   - On Windows:
     ```
     .\gradlew.bat run
     ```
   - On Linux/macOS:
     ```
     ./gradlew run
     ```
   The application will launch with the home screen displaying the 11 algorithm modules.

### Additional Notes
- **First Run**: The build may take longer due to dependency downloads.
- **Full-Screen Mode**: Available in the application for an immersive experience.
- **Themes**: Switch between light and dark modes from any screen.
- **Troubleshooting**:
  - If you encounter JavaFX-related errors, ensure your JDK includes JavaFX or that the Gradle build is using the correct JavaFX version.
  - For Windows users, if `gradlew.bat` fails, try running as administrator or check PATH variables.
  - Ensure no firewall blocks the application (though it's a desktop app).

## Usage

1. **Launch**: Run `./gradlew run` to start CodeCanvas.
2. **Navigate**: From the home screen, click on any module (e.g., "Array", "Sorting") to open its visualization.
3. **Interact**:
   - Enter custom data or use random generation.
   - Use Play/Pause/Next/Previous to control animations.
   - Adjust speed with the slider.
   - View code snippets in the code display area.
   - Toggle themes and full-screen mode.
4. **Learn**: Read the educational narratives and observe step-by-step executions.
5. **Exit**: Use the back button to return to the home screen or close the application.

## Executable Location

After packaging with `jpackage`, the generated executable is available at:

`c:\Projects\DSA--visualizer\build\jpackage\CodeCanvas\CodeCanvas.exe`

If you want to share the working app with someone else, zip the entire `build\jpackage\CodeCanvas` folder so the bundled runtime stays together.

## Notes for the Executable

- Open the folder in Explorer and double-click `CodeCanvas.exe`.
- The `runtime` directory next to it contains the embedded Java runtime, so the app will run without requiring a separate JDK install.
- If the executable does not run, make sure the whole `CodeCanvas` folder remains intact.

[CodeCanvas on GitHub](https://github.com/Ritaz13/DSA--visualizer)


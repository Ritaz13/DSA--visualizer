package com.example.dsavisualizer.controller.modules;

import com.example.dsavisualizer.controller.ModuleController;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Graphcontroller extends ModuleController {


    // FXML injected fields
    @FXML
    private CheckBox directedCheck;
    @FXML
    private TextField nodeField;
    @FXML
    private Button addNodeBtn;
    @FXML
    private Button delNodeBtn;
    @FXML
    private TextField fromField;
    @FXML
    private TextField toField;
    @FXML
    private Button addEdgeBtn;
    @FXML
    private Button deledgeBtn;
    @FXML
    private TextField startNodeField;
    @FXML
    private Button dfsBtn;
    @FXML
    private Button bfsBtn;
    @FXML
    private Button randomGraphBtn;
    @FXML
    private Button showCodeBtn;
    @FXML
    private Button copyCodeBtn;
    @FXML
    private TextArea storyArea;
    @FXML
    private TextArea codeArea;
    @FXML
    private TextArea propsArea;
    @FXML
    private StackPane vizArea;
    @FXML
    private ScrollPane vizScroll;
    @FXML
    private Label verticesLabel;
    @FXML
    private Label edgesLabel;
    @FXML
    private Label treeLabel;
    @FXML
    private Label connectedLabel;
    @FXML
    private Button startBtn, stopBtn, nextBtn, prevBtn;
    @FXML
    private TextArea stackDisplay;
    @FXML
    private TextArea queueDisplay;
    @FXML
    private Slider speedSlider;


    private Timer stepTimer;
    private boolean isRunning = false;
    private static final long DEFAULT_SPEED_MS = 1500; // 1.5 seconds default


    private GraphData graphData;
    private Canvas canvas;
    private boolean isDirected = true;
    private boolean isPaused = false;
    private Queue<Integer> traversalQueue;
    private Set<Integer> visitedNodes;
    private int currentTraversalIndex = -1;
    private List<Integer> traversalOrder;
    private int traversalStartNode = -1;
    private boolean isDFS = true;
    private Map<Integer, Color> pathColors = new HashMap<>();
    private Map<Integer, String> nodeStatus = new HashMap<>(); // white, grey, black
    private Random random = new Random();

    // Data structure visualization
    private List<String> dsStates; // stores queue/stack states at each step
    private List<Set<Integer>> visitedStates; // visited nodes at each step

    @Override
    public void initialize() {
        super.initialize();

        titleLabel.setText("Graph");
        storyArea.setText("A graph consists of vertices (nodes) connected by edges.Think of a group of friends. Each friend is a node, and friendships are edges.\nSome friendships are one-sided (directed), others are mutual (undirected).");

        // Initialize graph data
        graphData = new GraphData();
        traversalQueue = new LinkedList<>();
        visitedNodes = new HashSet<>();
        traversalOrder = new ArrayList<>();
        dsStates = new ArrayList<>();
        visitedStates = new ArrayList<>();

        // Create canvas for visualization
        canvas = new Canvas(700, 400);
        canvas.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1;");
        if (vizArea != null) {
            vizScroll.setContent(canvas);
        }

        // Setup control buttons
        directedCheck.setSelected(isDirected);
        directedCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            isDirected = newVal;
            graphData.setDirected(newVal);
            redrawCanvas();
        });


        nextBtn.setOnAction(e -> nextStep());
        prevBtn.setOnAction(e -> prevStep());
        startBtn.setOnAction(e -> togglePauseTraversal());
        stopBtn.setOnAction(e -> stopTraversalAuto());

        // Speed slider setup with default 1.5s
        speedSlider.setMin(500);
        speedSlider.setMax(3000);
        speedSlider.setValue(DEFAULT_SPEED_MS);
        speedSlider.setBlockIncrement(100);
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (isRunning) {
                // Restart with new speed
                stopTraversalAuto();
                startTraversalAuto();
            }
        });

        addNodeBtn.setOnAction(e -> addNodeFromUI());
        addEdgeBtn.setOnAction(e -> addEdgeFromUI());
        delNodeBtn.setOnAction(e -> removeNode());
        deledgeBtn.setOnAction(e -> removeEdge());
        dfsBtn.setOnAction(e -> performDFS());
        bfsBtn.setOnAction(e -> performBFS());
        randomGraphBtn.setOnAction(e -> generateRandomGraph());
        showCodeBtn.setOnAction(e -> toggleCodeArea());
        copyCodeBtn.setOnAction(e -> copyCode());

        loadCodeFile();
        updateAdjacencyList();
        redrawCanvas();
    }

    private void updateAdjacencyList() {
        if (propsArea == null) return;

        StringBuilder sb = new StringBuilder();
        List<Integer> nodes = graphData.getNodes();

        //sb.append("Adjacency List:\n\n");
        for (Integer node : nodes) {
            sb.append(node).append(" -> ");
            List<Integer> neighbors = graphData.getNeighbors(node);
            if (neighbors.isEmpty()) {
                sb.append("(none)");
            } else {
                for (int i = 0; i < neighbors.size(); i++) {
                    sb.append(neighbors.get(i));
                    if (i < neighbors.size() - 1) sb.append(", ");
                }
            }
            sb.append("\n");
        }

        propsArea.setText(sb.toString());
        // propsArea.setStyle("-fx-font-size: 18px;");

        verticesLabel.setText(String.valueOf(graphData.getNodes().size()));
        edgesLabel.setText(String.valueOf(graphData.getEdgeCount()));
        treeLabel.setText(isTreeGraph() ? "Yes" : "No");
        connectedLabel.setText(isConnectedGraph() ? "Yes" : "No");

    }

    private void addNodeFromUI() {
        try {
            int value = Integer.parseInt(nodeField.getText().trim());
            graphData.addNode(value);
            nodeField.clear();
            redrawCanvas();
            updateAdjacencyList();
        } catch (NumberFormatException nfe) {
            showAlert("Enter valid number");
        }
    }

    private void addEdgeFromUI() {
        try {
            int from = Integer.parseInt(fromField.getText().trim());
            int to = Integer.parseInt(toField.getText().trim());
            graphData.addEdge(from, to);
            fromField.clear();
            toField.clear();
            redrawCanvas();
            updateAdjacencyList();
        } catch (NumberFormatException nfe) {
            showAlert("Enter valid numbers");
        }
    }

    private void removeNode() {
        try {
            int value = Integer.parseInt(nodeField.getText().trim());
            graphData.removeNode(value);
            resetTraversalState();
            nodeField.clear();
            redrawCanvas();
            updateAdjacencyList();
        } catch (NumberFormatException nfe) {
            showAlert("Enter valid number");
        }
    }


    private void toggleCodeArea() {
        if (codeArea != null) {
            codeArea.setVisible(!codeArea.isVisible());
        }
    }

    private void copyCode() {
        String code = codeArea.getText();
        javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
        javafx.scene.input.ClipboardContent cc = new javafx.scene.input.ClipboardContent();
        cc.putString(code);
        clipboard.setContent(cc);
        showAlert("Code copied to clipboard!");
    }

    private void loadCodeFile() {
        try (InputStream is = getClass().getResourceAsStream("/codes/graph.txt")) {
            if (is == null) {
                codeArea.setText(getDFSCode());
                return;
            }
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append('\n');
                }
                codeArea.setText(sb.toString());
            }
        } catch (Exception ex) {
            codeArea.setText(getDFSCode());
        }
    }

    private void performDFS() {
        try {
            int start = Integer.parseInt(startNodeField.getText().trim());
            if (graphData.hasNode(start)) {
                isDFS = true;
                speedSlider.setValue(DEFAULT_SPEED_MS); // Reset to default 1.5s
                startTraversal(start);
                startNodeField.clear();
            } else {
                showAlert("Node not found!");
            }
        } catch (NumberFormatException nfe) {
            showAlert("Enter valid number!");
        }
    }

    private void performBFS() {
        try {
            int start = Integer.parseInt(startNodeField.getText().trim());
            if (graphData.hasNode(start)) {
                isDFS = false;
                speedSlider.setValue(DEFAULT_SPEED_MS); // Reset to default 1.5s
                startTraversal(start);
                startNodeField.clear();
            } else {
                showAlert("Node not found!");
            }
        } catch (NumberFormatException nfe) {
            showAlert("Enter valid number!");
        }
    }

    private void startTraversal(int start) {
        traversalStartNode = start;
        visitedNodes.clear();
        traversalQueue.clear();
        traversalOrder.clear();
        pathColors.clear();
        nodeStatus.clear();
        dsStates.clear();
        visitedStates.clear();
        currentTraversalIndex = 0;

        if (isDFS) {
            performDFSTraversal(start);
            codeArea.setText(getDFSCode());
        } else {
            performBFSTraversal(start);
            codeArea.setText(getBFSCode());
        }

        // Show first step of visualization
        redrawCanvas();
        updateAdjacencyList();

        // Enable control buttons
        startBtn.setDisable(false);
        nextBtn.setDisable(false);
        prevBtn.setDisable(false);
        stopBtn.setDisable(false);

        startBtn.setText("▶ Start");
        isRunning = false;

        // Auto-start visualization with 1.5s interval
        startTraversalAuto();
    }

    private void resetTraversalState() {
        visitedNodes.clear();
        traversalOrder.clear();
        traversalQueue.clear();
        currentTraversalIndex = -1;
        traversalStartNode = -1;
        pathColors.clear();
        dsStates.clear();
        visitedStates.clear();
        stopTraversalAuto();
        
        // Reset all nodes to white (not visited)
        for (Integer n : graphData.getNodes()) {
            nodeStatus.put(n, "white");
            pathColors.put(n, Color.web("#CCCCCC")); // Gray
        }
        
        // Reset all edges to default (black, normal)
        for (Edge edge : graphData.getEdges()) {
            edge.edgeType = "normal";
            edge.color = Color.BLACK;
        }
        stackDisplay.clear(); queueDisplay.clear();
        
        redrawCanvas();
    }


    private void nextStep() {
        if (traversalOrder.isEmpty()) {
            showAlert("Start a traversal first!");
            return;
        }
        if (currentTraversalIndex < dsStates.size() - 1) {
            currentTraversalIndex++;
            redrawCanvas();
        }
    }

    private void prevStep() {
        if (traversalOrder.isEmpty()) {
            showAlert("Start a traversal first!");
            return;
        }
        if (currentTraversalIndex > 0) {
            currentTraversalIndex--;
            redrawCanvas();
        }
    }

    private void togglePauseTraversal() {
        if (isRunning) {
            stopTraversalAuto();
            startBtn.setText("▶ Start");
        } else {
            startTraversalAuto();
            startBtn.setText("⏸ Pause");
        }
    }

    private void startTraversalAuto() {
        if (traversalOrder.isEmpty()) {
            showAlert("Start a traversal first!");
            return;
        }

        stopTraversalAuto();

        isRunning = true;
        startBtn.setText("⏸ Pause");

        stepTimer = new Timer();
        long interval = (long) speedSlider.getValue();

        stepTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                javafx.application.Platform.runLater(() -> {
                    if (isRunning && currentTraversalIndex < dsStates.size() - 1) {
                        currentTraversalIndex++;
                        redrawCanvas();
                    } else if (currentTraversalIndex >= dsStates.size() - 1) {
                        stopTraversalAuto();
                    }
                });
            }
        }, interval, interval); // Start with delay and continue at interval
    }

    private void stopTraversalAuto() {
        if (stepTimer != null) {
            stepTimer.cancel();
            stepTimer = null;
        }
        isRunning = false;
        startBtn.setText("▶ Start");
    }


    /*private void performDFSTraversal(int start) {
        // Reset all nodes to white (not visited)
        for (Integer n : graphData.getNodes()) {
            nodeStatus.put(n, "white");
            pathColors.put(n, Color.web("#CCCCCC")); // Gray - Not visited
        }
        for (Edge edge : graphData.getEdges()) {
            edge.edgeType = "normal";
            edge.color = Color.BLACK;
        }

        Stack<Integer> stack = new Stack<>();
        stack.push(start);
        nodeStatus.put(start, "orange"); // In stack
        pathColors.put(start, Color.web("#FFA500"));

        traversalOrder.add(start);
        visitedStates.add(new HashSet<>(visitedNodes));
        dsStates.add("Stack: " + stack.toString());

        while (!stack.isEmpty()) {
            int node = stack.pop();

            // Only process if not visited
            if (!visitedNodes.contains(node)) {
                nodeStatus.put(node, "blue"); // Processing
                pathColors.put(node, Color.web("#2196F3"));

                visitedNodes.add(node);
                nodeStatus.put(node, "green"); // Finished
                pathColors.put(node, Color.web("#4CAF50"));

                // Record snapshot after visiting node
                visitedStates.add(new HashSet<>(visitedNodes));
                dsStates.add("Stack: " + stack.toString());

                // Process neighbors
                for (int neighbor : graphData.getNeighbors(node)) {
                    if (!visitedNodes.contains(neighbor)) {
                        // Only color edge and neighbor node when actually moving to it
                        int neighborStatus = 0;
                        for (Edge edge : graphData.getEdges()) {
                            if (edge.from == node && edge.to == neighbor) {
                                edge.edgeType = "tree";
                                edge.color = Color.GREEN;
                                neighborStatus = 1;
                                break;
                            }
                        }

                        // Add to stack
                        if (neighborStatus == 1) {
                            nodeStatus.put(neighbor, "orange"); // In stack
                            pathColors.put(neighbor, Color.web("#FFA500"));
                            stack.push(neighbor);
                            traversalOrder.add(neighbor);
                        }
                    } else {
                        // Back edge (only in undirected graphs, visited but in same path)
                        for (Edge edge : graphData.getEdges()) {
                            if (edge.from == node && edge.to == neighbor && nodeStatus.get(neighbor).equals("green")) {
                                edge.edgeType = "back";
                                edge.color = Color.RED;
                            }
                        }
                    }
                }
            }

            // Record snapshot after each step
            visitedStates.add(new HashSet<>(visitedNodes));
            dsStates.add("Stack: " + stack.toString());
        }

        // Reset non-tree edges to default
        for (Edge edge : graphData.getEdges()) {
            if (!edge.edgeType.equals("tree") && !edge.edgeType.equals("back")) {
                edge.edgeType = "normal";
                edge.color = Color.BLACK;
            }
        }
    }


    private void performBFSTraversal(int start) {
        // Reset all nodes to white (not visited)
        for (Integer n : graphData.getNodes()) {
            nodeStatus.put(n, "white");
            pathColors.put(n, Color.web("#CCCCCC")); // Gray - Not visited
        }
        for (Edge edge : graphData.getEdges()) {
            edge.edgeType = "normal";
            edge.color = Color.BLACK;
        }

        Queue<Integer> queue = new LinkedList<>();
        queue.add(start);
        nodeStatus.put(start, "yellow"); // In queue
        pathColors.put(start, Color.web("#FFFF00"));
        visitedNodes.add(start);

        traversalOrder.add(start);
        visitedStates.add(new HashSet<>(visitedNodes));
        dsStates.add("Queue: " + queue.toString());

        while (!queue.isEmpty()) {
            int node = queue.poll();
            nodeStatus.put(node, "blue"); // Processing
            pathColors.put(node, Color.web("#2196F3"));

            // Record snapshot after processing
            visitedStates.add(new HashSet<>(visitedNodes));
            dsStates.add("Queue: " + queue.toString());

            nodeStatus.put(node, "green"); // Finished
            pathColors.put(node, Color.web("#4CAF50"));

            // Process neighbors - only color edge when actually moving to unvisited neighbor
            for (int neighbor : graphData.getNeighbors(node)) {
                if (!visitedNodes.contains(neighbor)) {
                    // Mark as in queue
                    nodeStatus.put(neighbor, "yellow");
                    pathColors.put(neighbor, Color.web("#FFFF00"));
                    
                    // Mark edge as tree edge
                    for (Edge edge : graphData.getEdges()) {
                        if (edge.from == node && edge.to == neighbor) {
                            edge.edgeType = "tree";
                            edge.color = Color.GREEN;
                        }
                    }
                    
                    // Add to queue
                    queue.add(neighbor);
                    visitedNodes.add(neighbor);
                    traversalOrder.add(neighbor);

                    // Record snapshot
                    visitedStates.add(new HashSet<>(visitedNodes));
                    dsStates.add("Queue: " + queue.toString());
                } else {
                    // Back edge (only for undirected, when neighbor is visited and not parent)
                    for (Edge edge : graphData.getEdges()) {
                        if (edge.from == node && edge.to == neighbor && nodeStatus.get(neighbor).equals("green")) {
                            if (!isDirected) { // Only for undirected graphs
                                edge.edgeType = "back";
                                edge.color = Color.RED;
                            }
                        }
                    }
                }
            }
        }

        // Reset non-tree, non-back edges to default
        for (Edge edge : graphData.getEdges()) {
            if (!edge.edgeType.equals("tree") && !edge.edgeType.equals("back")) {
                edge.edgeType = "normal";
                edge.color = Color.BLACK;
            }
        }
    }
*/private void performDFSTraversal(int start) {
        // Reset nodes and edges
        for (Integer n : graphData.getNodes()) {
            nodeStatus.put(n, "white");
            pathColors.put(n, Color.web("#CCCCCC"));
        }
        for (Edge edge : graphData.getEdges()) {
            edge.edgeType = "normal";
            edge.color = Color.BLACK;
        }

        Stack<Integer> stack = new Stack<>();
        stack.push(start);
        nodeStatus.put(start, "orange");
        pathColors.put(start, Color.web("#FFA500"));

        traversalOrder.add(start);
        visitedStates.add(new HashSet<>(visitedNodes));
        dsStates.add("Stack: " + stack.toString());

        while (!stack.isEmpty()) {
            int node = stack.pop();

            if (!visitedNodes.contains(node)) {
                nodeStatus.put(node, "blue");
                pathColors.put(node, Color.web("#2196F3"));

                visitedNodes.add(node);
                nodeStatus.put(node, "green");
                pathColors.put(node, Color.web("#4CAF50"));

                visitedStates.add(new HashSet<>(visitedNodes));
                dsStates.add("Stack: " + stack.toString());

                for (int neighbor : graphData.getNeighbors(node)) {
                    if (!visitedNodes.contains(neighbor)) {
                        for (Edge edge : graphData.getEdges()) {
                            if (edge.from == node && edge.to == neighbor) {
                                edge.edgeType = "tree";
                                edge.color = Color.GREEN;
                            }
                        }
                        nodeStatus.put(neighbor, "orange");
                        pathColors.put(neighbor, Color.web("#FFA500"));
                        stack.push(neighbor);
                        traversalOrder.add(neighbor);
                    } else {
                        for (Edge edge : graphData.getEdges()) {
                            if (edge.from == node && edge.to == neighbor) {
                                edge.edgeType = "back";
                                edge.color = Color.RED;
                            }
                        }
                    }
                }
            }
            visitedStates.add(new HashSet<>(visitedNodes));
            dsStates.add("Stack: " + stack.toString());
        }
    }private void performBFSTraversal(int start) {
        for (Integer n : graphData.getNodes()) {
            nodeStatus.put(n, "white");
            pathColors.put(n, Color.web("#CCCCCC"));
        }
        for (Edge edge : graphData.getEdges()) {
            edge.edgeType = "normal";
            edge.color = Color.BLACK;
        }

        Queue<Integer> queue = new LinkedList<>();
        queue.add(start);
        nodeStatus.put(start, "yellow");
        pathColors.put(start, Color.web("#FFFF00"));
        visitedNodes.add(start);

        traversalOrder.add(start);
        visitedStates.add(new HashSet<>(visitedNodes));
        dsStates.add("Queue: " + queue.toString());

        while (!queue.isEmpty()) {
            int node = queue.poll();
            nodeStatus.put(node, "blue");
            pathColors.put(node, Color.web("#2196F3"));

            visitedStates.add(new HashSet<>(visitedNodes));
            dsStates.add("Queue: " + queue.toString());

            nodeStatus.put(node, "green");
            pathColors.put(node, Color.web("#4CAF50"));

            for (int neighbor : graphData.getNeighbors(node)) {
                if (!visitedNodes.contains(neighbor)) {
                    nodeStatus.put(neighbor, "yellow");
                    pathColors.put(neighbor, Color.web("#FFFF00"));

                    for (Edge edge : graphData.getEdges()) {
                        if (edge.from == node && edge.to == neighbor) {
                            edge.edgeType = "tree";
                            edge.color = Color.GREEN;
                        }
                    }

                    queue.add(neighbor);
                    visitedNodes.add(neighbor);
                    traversalOrder.add(neighbor);

                    visitedStates.add(new HashSet<>(visitedNodes));
                    dsStates.add("Queue: " + queue.toString());
                } else {
                    for (Edge edge : graphData.getEdges()) {
                        if (edge.from == node && edge.to == neighbor) {
                            edge.edgeType = "back";
                            edge.color = Color.RED;
                        }
                    }
                }
            }
        }
    }


    private Color getColorForPath ( int index){
            Color[] colors = {
                    Color.web("#FF6B6B"), // Red
                    Color.web("#4ECDC4"), // Teal
                    Color.web("#45B7D1"), // Blue
                    Color.web("#FFA07A"), // Light Salmon
                    Color.web("#98D8C8"), // Mint
                    Color.web("#F7DC6F"), // Yellow
                    Color.web("#BB8FCE"), // Purple
                    Color.web("#85C1E2"), // Sky Blue
            };
            return colors[index % colors.length];
        }

        private void generateRandomGraph () {
            graphData.clear();
            Set<Integer> nodes = new HashSet<>();
            Random r = new Random();

            // Generate 5-8 random nodes
            int nodeCount = 5 + r.nextInt(4);
            for (int i = 0; i < nodeCount; i++) {
                int node = r.nextInt(20) + 1;
                if (nodes.add(node)) {
                    graphData.addNode(node);
                }
            }

            // Generate more edges for better connectivity
            List<Integer> nodeList = new ArrayList<>(graphData.getNodes());
            int edgeCount = nodeList.size() * 2 + r.nextInt(nodeList.size());
            for (int i = 0; i < edgeCount; i++) {
                int from = nodeList.get(r.nextInt(nodeList.size()));
                int to = nodeList.get(r.nextInt(nodeList.size()));
                if (from != to) {
                    graphData.addEdge(from, to);
                }
            }

            redrawCanvas();
            updateAdjacencyList();
        }


        private String getDFSCode () {
            return "// Depth-First Search (DFS) Algorithm\n" +
                    "void dfs(int start) {\n" +
                    "    Stack<Integer> stack = new Stack<>();\n" +
                    "    Set<Integer> visited = new HashSet<>();\n" +
                    "    \n" +
                    "    stack.push(start);\n" +
                    "    \n" +
                    "    while (!stack.isEmpty()) {\n" +
                    "        int node = stack.pop();\n" +
                    "        \n" +
                    "        if (!visited.contains(node)) {\n" +
                    "            visited.add(node);\n" +
                    "            System.out.println(node);\n" +
                    "            \n" +
                    "            for (int neighbor : adj[node]) {\n" +
                    "                if (!visited.contains(neighbor)) {\n" +
                    "                    stack.push(neighbor);\n" +
                    "                }\n" +
                    "            }\n" +
                    "        }\n" +
                    "    }\n" +
                    "}";
        }

        private String getBFSCode () {
            return "// Breadth-First Search (BFS) Algorithm\n" +
                    "void bfs(int start) {\n" +
                    "    Queue<Integer> queue = new LinkedList<>();\n" +
                    "    Set<Integer> visited = new HashSet<>();\n" +
                    "    \n" +
                    "    queue.add(start);\n" +
                    "    visited.add(start);\n" +
                    "    \n" +
                    "    while (!queue.isEmpty()) {\n" +
                    "        int node = queue.poll();\n" +
                    "        System.out.println(node);\n" +
                    "        \n" +
                    "        for (int neighbor : adj[node]) {\n" +
                    "            if (!visited.contains(neighbor)) {\n" +
                    "                visited.add(neighbor);\n" +
                    "                queue.add(neighbor);\n" +
                    "            }\n" +
                    "        }\n" +
                    "    }\n" +
                    "}";
        }

        private void removeEdge () {
            try {
                int from = Integer.parseInt(fromField.getText().trim());
                int to = Integer.parseInt(toField.getText().trim());

                graphData.removeEdge(from, to);
                resetTraversalState();

                fromField.clear();
                toField.clear();

                redrawCanvas();
                updateAdjacencyList();

            } catch (NumberFormatException nfe) {
                showAlert("Enter valid numbers");
            }
        }


    private boolean isTreeGraph () {
            if (isDirected) return false;   // Directed graph tree না ধরবো

            if (graphData.getNodes().isEmpty()) return false;

            int vertices = graphData.getNodes().size();
            int edges = graphData.getEdgeCount();

            return edges == vertices - 1 && isConnectedGraph();
        }

        private boolean isConnectedGraph () {
            if (graphData.getNodes().isEmpty()) return true;

            Set<Integer> visited = new HashSet<>();
            Queue<Integer> queue = new LinkedList<>();
            int startNode = graphData.getNodes().get(0);
            queue.add(startNode);
            visited.add(startNode);

            while (!queue.isEmpty()) {
                int current = queue.poll();
                for (Integer neighbor : graphData.getNeighbors(current)) {
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }

            return visited.size() == graphData.getNodes().size();
        }
    private void redrawCanvas() {
        if (canvas == null) return;
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        Map<Integer, double[]> positions = calculateNodePositions(canvas.getWidth(), canvas.getHeight());

        // Current snapshot
        Set<Integer> currentVisited = new HashSet<>();
        if (currentTraversalIndex >= 0 && currentTraversalIndex < visitedStates.size()) {
            currentVisited = visitedStates.get(currentTraversalIndex);
        }

        // Draw edges
        for (Edge edge : graphData.getEdges()) {
            double[] fromPos = positions.get(edge.from);
            double[] toPos = positions.get(edge.to);
            if (fromPos != null && toPos != null) {
                drawArrow(gc, fromPos[0], fromPos[1], toPos[0], toPos[1], isDirected, edge);
            }
        }

        // Draw nodes
        for (Integer node : graphData.getNodes()) {
            double[] pos = positions.get(node);
            Color color = Color.LIGHTGRAY;
            if (currentVisited.contains(node)) {
                color = pathColors.getOrDefault(node, Color.LIGHTGRAY);
            }

            double radius = (currentTraversalIndex >= 0 && traversalOrder.size() > currentTraversalIndex
                    && traversalOrder.get(currentTraversalIndex).equals(node)) ? 30 : 20; // highlight active node

            gc.setFill(color);
            gc.fillOval(pos[0] - radius, pos[1] - radius, radius * 2, radius * 2);
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(2);
            gc.strokeOval(pos[0] - radius, pos[1] - radius, radius * 2, radius * 2);

            gc.setFill(Color.BLACK);
            gc.setFont(new javafx.scene.text.Font("Arial", 18)); // bigger font
            String label = String.valueOf(node);
            gc.fillText(label, pos[0] - 10, pos[1] + 5);
        }

        // Show stack/queue state prominently
        if (currentTraversalIndex >= 0 && currentTraversalIndex < dsStates.size()) {
            String dsState = dsStates.get(currentTraversalIndex);
            gc.setFill(Color.DARKBLUE);
            gc.setFont(new javafx.scene.text.Font("Arial", 16));
            gc.fillText(dsState, 20, canvas.getHeight() - 20); // bottom-left corner
        }
    }



       /* private void redrawCanvas () {
            if (canvas == null) return;
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

            Map<Integer, double[]> positions = calculateNodePositions(canvas.getWidth(), canvas.getHeight());

            // ✅ Use snapshot for current step
            Set<Integer> currentVisited = new HashSet<>();
            if (currentTraversalIndex >= 0 && currentTraversalIndex < visitedStates.size()) {
                currentVisited = visitedStates.get(currentTraversalIndex);
            }

            // Draw edges
            for (Edge edge : graphData.getEdges()) {
                double[] fromPos = positions.get(edge.from);
                double[] toPos = positions.get(edge.to);
                if (fromPos != null && toPos != null) {
                    drawArrow(gc, fromPos[0], fromPos[1], toPos[0], toPos[1], isDirected, edge);
                }
            }

            // Draw nodes
            for (Integer node : graphData.getNodes()) {
                double[] pos = positions.get(node);
                Color color = Color.LIGHTGRAY;
                if (currentVisited.contains(node)) {
                    color = pathColors.getOrDefault(node, Color.LIGHTGRAY);
                }
                gc.setFill(color);
                gc.fillOval(pos[0] - 20, pos[1] - 20, 40, 40);
                gc.setStroke(Color.BLACK);
                gc.strokeOval(pos[0] - 20, pos[1] - 20, 40, 40);
                gc.setFill(Color.BLACK);
               // gc.setFont(new javafx.scene.text.Font("Arial", 18)); // bigger font String label = String.valueOf(node);
                gc.fillText(String.valueOf(node), pos[0] - 10, pos[1] + 5);
            }

            // ✅ Show stack/queue state
            if (currentTraversalIndex >= 0 && currentTraversalIndex < dsStates.size()) {
                String dsState = dsStates.get(currentTraversalIndex);
                if (isDFS) stackDisplay.setText(dsState);
                else queueDisplay.setText(dsState);
            }
        }*/

        /*private void drawArrow(GraphicsContext gc, double startX, double startY, double endX, double endY,
                               boolean directed, Edge edge) {
            String edgeType = edge.edgeType;
            Color edgeColor = edge.color;

            // Set line style and color based on edge type
            switch (edgeType) {
                case "tree":
                    gc.setStroke(edgeColor != null ? edgeColor : Color.GREEN);
                    gc.setLineDashes(null); // solid line
                    gc.setLineWidth(2.5);
                    break;
                case "back":
                    gc.setStroke(edgeColor != null ? edgeColor : Color.RED);
                    gc.setLineDashes(5, 5); // dashed line (- - -)
                    gc.setLineWidth(2);
                    break;
                case "cross":
                    gc.setStroke(edgeColor != null ? edgeColor : Color.GRAY);
                    gc.setLineDashes(2, 4); // dotted line (. . . .)
                    gc.setLineWidth(2);
                    break;
                case "forward":
                    gc.setStroke(edgeColor != null ? edgeColor : Color.BLUE);
                    gc.setLineDashes(8, 4, 2, 4); // comma style
                    gc.setLineWidth(2);
                    break;
                default:
                    gc.setStroke(edgeColor != null ? edgeColor : Color.BLACK);
                    gc.setLineDashes(null); // solid line
                    gc.setLineWidth(2);
            }

            double dx = endX - startX;
            double dy = endY - startY;
            double distance = Math.sqrt(dx * dx + dy * dy);

            double nodeRadius = 18;
            double shortenStart = nodeRadius;
            double shortenEnd = nodeRadius;

            double ratio = shortenStart / distance;
            double actualStartX = startX + dx * ratio;
            double actualStartY = startY + dy * ratio;

            ratio = (distance - shortenEnd) / distance;
            double actualEndX = startX + dx * ratio;
            double actualEndY = startY + dy * ratio;

            gc.strokeLine(actualStartX, actualStartY, actualEndX, actualEndY);

            if (directed) {
                double angle = Math.atan2(dy, dx);
                double arrowSize = 10;

                double x1 = actualEndX - arrowSize * Math.cos(angle - Math.PI / 6);
                double y1 = actualEndY - arrowSize * Math.sin(angle - Math.PI / 6);
                double x2 = actualEndX - arrowSize * Math.cos(angle + Math.PI / 6);
                double y2 = actualEndY - arrowSize * Math.sin(angle + Math.PI / 6);

                gc.setFill(Color.BLACK);
                gc.fillPolygon(new double[]{actualEndX, x1, x2}, new double[]{actualEndY, y1, y2}, 3);
            }

            // Reset line dashes for next drawing
            gc.setLineDashes(null);
        }*/
        /*private void drawArrow(GraphicsContext gc, double startX, double startY, double endX, double endY,
                               boolean directed, Edge edge) {
            String edgeType = edge.edgeType;
            Color edgeColor = edge.color != null ? edge.color : Color.BLACK;

            switch (edgeType) {
                case "tree":
                    gc.setStroke(Color.GREEN);
                    gc.setLineDashes(null);
                    gc.setLineWidth(3);
                    break;
                case "back":
                    gc.setStroke(Color.RED);
                    gc.setLineDashes(6, 6);
                    gc.setLineWidth(2.5);
                    break;
                case "forward":
                    gc.setStroke(Color.BLUE);
                    gc.setLineDashes(10, 4);
                    gc.setLineWidth(2.5);
                    break;
                case "cross":
                    gc.setStroke(Color.GRAY);
                    gc.setLineDashes(2, 6);
                    gc.setLineWidth(2);
                    break;
                default:
                    gc.setStroke(edgeColor);
                    gc.setLineDashes(null);
                    gc.setLineWidth(2);
            }

            gc.strokeLine(startX, startY, endX, endY);

            if (directed) {
                double angle = Math.atan2(endY - startY, endX - startX);
                double arrowSize = 12;
                double x1 = endX - arrowSize * Math.cos(angle - Math.PI / 6);
                double y1 = endY - arrowSize * Math.sin(angle - Math.PI / 6);
                double x2 = endX - arrowSize * Math.cos(angle + Math.PI / 6);
                double y2 = endY - arrowSize * Math.sin(angle + Math.PI / 6);
                gc.fillPolygon(new double[]{endX, x1, x2}, new double[]{endY, y1, y2}, 3);
            }
        }*/
        private void drawArrow(GraphicsContext gc, double startX, double startY, double endX, double endY,
                               boolean directed, Edge edge) {
            switch (edge.edgeType) {
                case "tree":
                    gc.setStroke(Color.GREEN);
                    gc.setLineDashes(null);
                    gc.setLineWidth(3);
                    break;
                case "back":
                    gc.setStroke(Color.RED);
                    gc.setLineDashes(6, 6);
                    gc.setLineWidth(2.5);
                    break;
                default:
                    gc.setStroke(Color.BLACK);
                    gc.setLineDashes(null);
                    gc.setLineWidth(2);
            }

            gc.strokeLine(startX, startY, endX, endY);

            if (directed) {
                double angle = Math.atan2(endY - startY, endX - startX);
                double arrowSize = 12;
                double x1 = endX - arrowSize * Math.cos(angle - Math.PI / 6);
                double y1 = endY - arrowSize * Math.sin(angle - Math.PI / 6);
                double x2 = endX - arrowSize * Math.cos(angle + Math.PI / 6);
                double y2 = endY - arrowSize * Math.sin(angle + Math.PI / 6);
                gc.fillPolygon(new double[]{endX, x1, x2}, new double[]{endY, y1, y2}, 3);
            }
        }



    private Map<Integer, double[]> calculateNodePositions ( double width, double height){
            Map<Integer, double[]> positions = new HashMap<>();
            List<Integer> nodes = graphData.getNodes();

            if (nodes.isEmpty()) return positions;

            int numNodes = nodes.size();
            double centerX = width / 2;
            double centerY = height / 2;
            double radius = Math.min(width, height) / 3;

            for (int i = 0; i < numNodes; i++) {
                double angle = 2 * Math.PI * i / numNodes;
                double x = centerX + radius * Math.cos(angle);
                double y = centerY + radius * Math.sin(angle);
                positions.put(nodes.get(i), new double[]{x, y});
            }

            return positions;
        }

        protected void showAlert (String message){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Message");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }

        private static class GraphData {
            private List<Integer> nodes;
            private List<Edge> edges;
            private boolean directed;
            private Runnable changeListener;

            public GraphData() {
                this.nodes = new ArrayList<>();
                this.edges = new ArrayList<>();
                this.directed = true;
            }

            public void addChangeListener(Runnable listener) {
                this.changeListener = listener;
            }

            public void addNode(int value) {
                if (!nodes.contains(value)) {
                    nodes.add(value);
                    Collections.sort(nodes);
                    notifyChange();
                }
            }

            public void removeNode(int value) {
                if (!nodes.contains(value)) return;

                nodes.remove(Integer.valueOf(value));

                // remove all connected edges
                edges.removeIf(e -> e.from == value || e.to == value);

                notifyChange();
            }


            public void addEdge(int from, int to) {
                if (!nodes.contains(from) || !nodes.contains(to)) return;

                for (Edge e : edges) {
                    if (e.from == from && e.to == to) return;
                    if (!directed && e.from == to && e.to == from) return;
                    // avoid duplicate
                }

                edges.add(new Edge(from, to));

                if (!directed) {
                    edges.add(new Edge(to, from));
                }

                notifyChange();
            }

            public void removeEdge(int from, int to) {
                boolean removed = edges.removeIf(e -> e.from == from && e.to == to);

                if (!directed) {
                    edges.removeIf(e -> e.from == to && e.to == from);
                }

                notifyChange();
            }


            public boolean hasNode(int value) {
                return nodes.contains(value);
            }

            public List<Integer> getNodes() {
                return new ArrayList<>(nodes);
            }

            public List<Edge> getEdges() {
                return new ArrayList<>(edges);
            }

            public int getEdgeCount() {
                if (directed) {
                    return edges.size();
                }
                return edges.size() / 2;
            }

            public List<Integer> getNeighbors(int node) {
                List<Integer> neighbors = new ArrayList<>();
                for (Edge edge : edges) {
                    if (edge.from == node) {
                        neighbors.add(edge.to);
                    }
//                if (!directed && edge.to == node) neighbors.add(edge.from);

                }
                return neighbors;
            }

            public void setDirected(boolean directed) {
                this.directed = directed;
                notifyChange();
            }

            public void clear() {
                nodes.clear();
                edges.clear();
                notifyChange();
            }

            private void notifyChange() {
                if (changeListener != null) {
                    changeListener.run();
                }
            }
        }

        private static class Edge {
            int from;
            int to;
            String edgeType; // "tree", "back", "cross", "forward"
            Color color; // Color of the edge

            public Edge(int from, int to) {
                this.from = from;
                this.to = to;
                this.edgeType = "normal"; // default
                this.color = Color.BLACK;
            }

            public Edge(int from, int to, String edgeType) {
                this.from = from;
                this.to = to;
                this.edgeType = edgeType;
                this.color = Color.BLACK;
            }
        }
    }




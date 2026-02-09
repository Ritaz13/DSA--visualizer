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
    @FXML private CheckBox directedCheck;
    @FXML private CheckBox weightedCheck;
    @FXML private TextField nodeField;
    @FXML private Button addNodeBtn;
    @FXML private TextField fromField;
    @FXML private TextField toField;
    @FXML private TextField weightField;
    @FXML private Button addEdgeBtn;
    @FXML private TextField startNodeField;
    @FXML private Button dfsBtn;
    @FXML private Button bfsBtn;
    @FXML private Button randomGraphBtn;
    @FXML private Button showCodeBtn;
    @FXML private Button copyCodeBtn;
    @FXML private TextArea storyArea;
    @FXML private TextArea codeArea;
    @FXML private TextArea propsArea;
    @FXML private StackPane vizArea;
    @FXML private ScrollPane vizScroll;
    
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
    private Random random = new Random();

    @Override
    public void initialize() {
        super.initialize();

        titleLabel.setText("Graph");
        storyArea.setText("A graph consists of vertices (nodes) connected by edges.\nThink of a group of friends. Each friend is a node, and friendships are edges.\nSome friendships are one-sided (directed), others are mutual (undirected).");

        // Initialize graph data
        graphData = new GraphData();
        traversalQueue = new LinkedList<>();
        visitedNodes = new HashSet<>();
        traversalOrder = new ArrayList<>();

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

        weightedCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            weightField.setVisible(newVal);
            weightField.setManaged(newVal);
        });

        addNodeBtn.setOnAction(e -> addNodeFromUI());
        addEdgeBtn.setOnAction(e -> addEdgeFromUI());
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
        
        sb.append("Adjacency List:\n\n");
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
        currentTraversalIndex = 0;

        if (isDFS) {
            performDFSTraversal(start);
            codeArea.setText(getDFSCode());
        } else {
            performBFSTraversal(start);
            codeArea.setText(getBFSCode());
        }
        redrawCanvas();
        updateAdjacencyList();
    }

    private void performDFSTraversal(int start) {
        Stack<Integer> stack = new Stack<>();
        stack.push(start);
        int colorIndex = 0;

        while (!stack.isEmpty()) {
            int node = stack.pop();
            if (!visitedNodes.contains(node)) {
                visitedNodes.add(node);
                traversalOrder.add(node);

                if (!pathColors.containsKey(node)) {
                    pathColors.put(node, getColorForPath(colorIndex++));
                }

                List<Integer> neighbors = graphData.getNeighbors(node);
                for (int neighbor : neighbors) {
                    if (!visitedNodes.contains(neighbor)) {
                        stack.push(neighbor);
                    }
                }
            }
        }
    }

    private void performBFSTraversal(int start) {
        Queue<Integer> queue = new LinkedList<>();
        queue.add(start);
        visitedNodes.add(start);
        traversalOrder.add(start);
        int colorIndex = 0;

        if (!pathColors.containsKey(start)) {
            pathColors.put(start, getColorForPath(colorIndex++));
        }

        while (!queue.isEmpty()) {
            int node = queue.poll();
            List<Integer> neighbors = graphData.getNeighbors(node);

            for (int neighbor : neighbors) {
                if (!visitedNodes.contains(neighbor)) {
                    visitedNodes.add(neighbor);
                    traversalOrder.add(neighbor);
                    queue.add(neighbor);

                    if (!pathColors.containsKey(neighbor)) {
                        pathColors.put(neighbor, getColorForPath(colorIndex++));
                    }
                }
            }
        }
    }

    private Color getColorForPath(int index) {
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

    private void generateRandomGraph() {
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



    private String getDFSCode() {
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
    
    private String getBFSCode() {
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


    
    private void nextStep() {
        if (currentTraversalIndex < traversalOrder.size() - 1) {
            currentTraversalIndex++;
            redrawCanvas();
        }
    }

    private void previousStep() {
        if (currentTraversalIndex > 0) {
            currentTraversalIndex--;
            redrawCanvas();
        }
    }

    private boolean isTreeGraph() {
        if (graphData.getNodes().isEmpty()) return false;
        int vertices = graphData.getNodes().size();
        int edges = graphData.getEdgeCount();
        
        // A tree has vertices - 1 edges and is connected
        return edges == vertices - 1 && isConnectedGraph();
    }

    private boolean isConnectedGraph() {
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
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        
        gc.clearRect(0, 0, width, height);
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, width, height);
        
        if (graphData.getNodes().isEmpty()) {
            gc.setFill(Color.GRAY);
            gc.setFont(new javafx.scene.text.Font("Arial", 16));
            gc.fillText("Empty graph. Add nodes to visualize.", 20, height/2);
            return;
        }
        
        Map<Integer, double[]> positions = calculateNodePositions(width, height);
        
        // Draw edges
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        for (Edge edge : graphData.getEdges()) {
            double[] fromPos = positions.get(edge.from);
            double[] toPos = positions.get(edge.to);
            
            if (fromPos != null && toPos != null) {
                drawArrow(gc, fromPos[0], fromPos[1], toPos[0], toPos[1], isDirected);
            }
        }
        
        // Draw nodes
        for (Integer node : graphData.getNodes()) {
            double[] pos = positions.get(node);
            double x = pos[0];
            double y = pos[1];
            double radius = 20;
            
            Color color;
            if (traversalStartNode == node) {
                color = Color.web("#FFD93D");
            } else if (currentTraversalIndex >= 0 && traversalOrder.size() > 0
                       && traversalOrder.indexOf(node) <= currentTraversalIndex 
                       && traversalOrder.indexOf(node) >= 0) {
                color = Color.web("#74B9FF");
            } else if (visitedNodes.contains(node)) {
                color = Color.web("#74B9FF");
            } else {
                color = Color.web("#E0E0E0");
            }
            
            gc.setFill(color);
            gc.fillOval(x - radius, y - radius, 2 * radius, 2 * radius);
            
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(2);
            gc.strokeOval(x - radius, y - radius, 2 * radius, 2 * radius);
            
            gc.setFill(Color.BLACK);
            gc.setFont(new javafx.scene.text.Font("Arial", 16));
            String label = String.valueOf(node);
            double textWidth = label.length() * 8;
            gc.fillText(label, x - textWidth / 2, y + 6);
        }
    }

    private void drawArrow(GraphicsContext gc, double startX, double startY, double endX, double endY, boolean directed) {
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
    }

    private Map<Integer, double[]> calculateNodePositions(double width, double height) {
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

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Message");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /*private void updateAdjacencyList() {
        if (propsArea == null) return;
        
        StringBuilder adjList = new StringBuilder();
        adjList.append("=== Adjacency List ===\n\n");
        
        for (int node : graphData.getNodes()) {
            adjList.append("Node ").append(node).append(" → ");
            List<Integer> neighbors = graphData.getNeighbors(node);
            if (neighbors.isEmpty()) {
                adjList.append("No neighbors");
            } else {
                for (int i = 0; i < neighbors.size(); i++) {
                    adjList.append(neighbors.get(i));
                    if (i < neighbors.size() - 1) adjList.append(", ");
                }
            }
            adjList.append("\n");
        }
        
        adjList.append("\n=== Properties ===\n");
        adjList.append("Vertices: ").append(graphData.getNodes().size()).append("\n");
        adjList.append("Edges: ").append(graphData.getEdgeCount()).append("\n");
        adjList.append("Type: ").append(graphData.directed ? "Directed" : "Undirected").append("\n");
        
        propsArea.setText(adjList.toString());
    }*/
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
            nodes.remove(Integer.valueOf(value));
            edges.removeIf(e -> e.from == value || e.to == value);
            notifyChange();
        }

        public void addEdge(int from, int to) {
            if (nodes.contains(from) && nodes.contains(to)) {
                edges.add(new Edge(from, to));
                if (!directed) {
                    edges.add(new Edge(to, from));
                }
                notifyChange();
            }
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

        public Edge(int from, int to) {
            this.from = from;
            this.to = to;
        }
    }
}

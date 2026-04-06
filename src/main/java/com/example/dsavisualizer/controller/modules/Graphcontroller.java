package com.example.dsavisualizer.controller.modules;

import com.example.dsavisualizer.controller.ModuleController;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * GraphController — handles all graph visualization logic:
 *  - Directed / Undirected toggle with live adjacency-list update
 *  - Step-by-step DFS / BFS with white → gray → green node coloring
 *  - Edge classification (tree / back / forward / cross) per graph type
 *  - MST visualization via Prim's algorithm
 */
public class Graphcontroller extends ModuleController {

    // ── FXML fields ────────────────────────────────────────────────────────────

    @FXML private CheckBox directedCheck;
    @FXML private TextField nodeField, fromField, toField, startNodeField;
    @FXML private Button addNodeBtn, delNodeBtn, addEdgeBtn, deledgeBtn;
    @FXML private Button dfsBtn, bfsBtn, mstBtn, randomGraphBtn;
    @FXML private Button showCodeBtn, copyCodeBtn;
    @FXML private Button startBtn, stopBtn, nextBtn, prevBtn;
    @FXML private TextArea storyArea, codeArea, propsArea;
    @FXML private StackPane vizArea;
    @FXML private ScrollPane vizScroll;
    @FXML private Label titleLabel, verticesLabel, edgesLabel, treeLabel, connectedLabel;
    @FXML private Slider speedSlider;

    // ── State ──────────────────────────────────────────────────────────────────

    private static final long DEFAULT_SPEED_MS = 1200;

    private GraphData graphData;
    private Canvas canvas;

    // Each "frame" captures node colors + edge types at one traversal step
    private final List<TraversalFrame> frames = new ArrayList<>();
    private int currentFrame = -1;

    private Timer stepTimer;
    private boolean isRunning = false;
    private boolean isDFS = true;
    private boolean isMST = false;

    // ── Initialization ─────────────────────────────────────────────────────────

    @FXML
    @Override
    protected void initialize() {
        super.initialize();

        titleLabel.setText("Graph");
        storyArea.setText(
            "A graph consists of vertices (nodes) connected by edges.\n\n" +
            "Think of a group of friends — each friend is a node, and " +
            "friendships are edges. Some friendships are one-sided (directed), " +
            "others are mutual (undirected)."
        );

        graphData = new GraphData();

        canvas = new Canvas(800, 460);
        vizScroll.setContent(canvas);

        // ── Directed toggle ──
        directedCheck.setSelected(true);
        directedCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            graphData.setDirected(newVal);
            resetTraversalFrames();
            redrawCanvas(null);
            updateSidebar();
        });

        // ── Speed slider ──
        speedSlider.setMin(300);
        speedSlider.setMax(3000);
        speedSlider.setValue(DEFAULT_SPEED_MS);
        speedSlider.valueProperty().addListener((obs, o, n) -> {
            if (isRunning) { stopAuto(); startAuto(); }
        });

        // ── Button wiring ──
        addNodeBtn.setOnAction(e -> addNode());
        delNodeBtn.setOnAction(e -> deleteNode());
        addEdgeBtn.setOnAction(e -> addEdge());
        deledgeBtn.setOnAction(e -> deleteEdge());
        dfsBtn.setOnAction(e -> startDFS());
        bfsBtn.setOnAction(e -> startBFS());
        mstBtn.setOnAction(e -> startMST());
        randomGraphBtn.setOnAction(e -> generateRandomGraph());
        showCodeBtn.setOnAction(e -> toggleCodeArea());
        copyCodeBtn.setOnAction(e -> copyCode());
        startBtn.setOnAction(e -> togglePause());
        stopBtn.setOnAction(e -> stopAndReset());
        nextBtn.setOnAction(e -> nextStep());
        prevBtn.setOnAction(e -> prevStep());

        loadCodeFile();
        updateSidebar();
        redrawCanvas(null);
    }

    // ── Node / Edge operations ─────────────────────────────────────────────────

    private void addNode() {
        try {
            int v = Integer.parseInt(nodeField.getText().trim());
            graphData.addNode(v);
            nodeField.clear();
            resetTraversalFrames();
            redrawCanvas(null);
            updateSidebar();
        } catch (NumberFormatException e) {
            showAlert("Please enter a valid integer node label.");
        }
    }

    private void deleteNode() {
        try {
            int v = Integer.parseInt(nodeField.getText().trim());
            graphData.removeNode(v);
            nodeField.clear();
            resetTraversalFrames();
            redrawCanvas(null);
            updateSidebar();
        } catch (NumberFormatException e) {
            showAlert("Please enter a valid integer node label.");
        }
    }

    private void addEdge() {
        try {
            int from = Integer.parseInt(fromField.getText().trim());
            int to   = Integer.parseInt(toField.getText().trim());
            graphData.addEdge(from, to);
            fromField.clear();
            toField.clear();
            resetTraversalFrames();
            redrawCanvas(null);
            updateSidebar();
        } catch (NumberFormatException e) {
            showAlert("Please enter valid integer node labels.");
        }
    }

    private void deleteEdge() {
        try {
            int from = Integer.parseInt(fromField.getText().trim());
            int to   = Integer.parseInt(toField.getText().trim());
            graphData.removeEdge(from, to);
            fromField.clear();
            toField.clear();
            resetTraversalFrames();
            redrawCanvas(null);
            updateSidebar();
        } catch (NumberFormatException e) {
            showAlert("Please enter valid integer node labels.");
        }
    }

    // ── Traversal entry points ─────────────────────────────────────────────────

    private void startDFS() {
        int start = parseStartNode();
        if (start == -1) return;
        isDFS = true;
        isMST = false;
        codeArea.setText(getDFSCode());
        buildDFSFrames(start);
        beginPlayback();
    }

    private void startBFS() {
        int start = parseStartNode();
        if (start == -1) return;
        isDFS = false;
        isMST = false;
        codeArea.setText(getBFSCode());
        buildBFSFrames(start);
        beginPlayback();
    }

    private void startMST() {
        if (graphData.isDirected()) {
            showAlert("MST works on undirected graphs. Please uncheck 'Directed Graph'.");
            return;
        }
        if (graphData.getNodes().isEmpty()) {
            showAlert("Add some nodes first!");
            return;
        }
        isMST = true;
        codeArea.setText(getMSTCode());
        buildMSTFrames();
        beginPlayback();
    }

    private int parseStartNode() {
        try {
            int v = Integer.parseInt(startNodeField.getText().trim());
            if (!graphData.hasNode(v)) { showAlert("Node " + v + " not found in graph."); return -1; }
            startNodeField.clear();
            return v;
        } catch (NumberFormatException e) {
            showAlert("Please enter a valid start node.");
            return -1;
        }
    }

    // ── DFS frame builder ──────────────────────────────────────────────────────
    /**
     * Builds every step of DFS as a list of TraversalFrames.
     * Node color convention (matches CLRS):
     *   WHITE  = unvisited
     *   GRAY   = discovered / on stack (currently being processed)
     *   GREEN  = fully finished (popped off stack)
     * Edge types:
     *   tree    → green   (both directed and undirected)
     *   back    → red     (both directed and undirected — creates cycle)
     *   forward → blue    (directed only)
     *   cross   → orange  (directed only)
     */
    private void buildDFSFrames(int start) {
        frames.clear();

        // Initial color / type state
        Map<Integer, NodeColor> colors = new LinkedHashMap<>();
        Map<String, EdgeClass>  eCls   = new LinkedHashMap<>();
        for (int n : graphData.getNodes())  colors.put(n, NodeColor.WHITE);
        for (Edge e : graphData.getEdges()) eCls.put(edgeKey(e), EdgeClass.NORMAL);

        Map<Integer, Integer> disc   = new HashMap<>();
        Map<Integer, Integer> finish = new HashMap<>();
        int[] timer = {0};

        // Capture initial frame (all white)
        frames.add(snapshot(colors, eCls, -1, "Initial state — all nodes unvisited (white)"));

        dfsVisit(start, colors, eCls, disc, finish, timer);

        // Handle disconnected nodes
        for (int n : graphData.getNodes()) {
            if (colors.get(n) == NodeColor.WHITE) {
                dfsVisit(n, colors, eCls, disc, finish, timer);
            }
        }
    }

    private void dfsVisit(int u,
                          Map<Integer, NodeColor> colors,
                          Map<String, EdgeClass>  eCls,
                          Map<Integer, Integer>   disc,
                          Map<Integer, Integer>   finish,
                          int[]                   timer) {

        colors.put(u, NodeColor.GRAY);
        disc.put(u, timer[0]++);
        frames.add(snapshot(colors, eCls, u,
            "Discover node " + u + " — push on stack (gray)"));

        for (int v : graphData.getNeighbors(u)) {
            String key = edgeKey(u, v);

            if (colors.get(v) == NodeColor.WHITE) {
                // Tree edge
                eCls.put(key, EdgeClass.TREE);
                frames.add(snapshot(colors, eCls, u,
                    "Tree edge " + u + " → " + v));
                dfsVisit(v, colors, eCls, disc, finish, timer);

            } else if (colors.get(v) == NodeColor.GRAY) {
                // Back edge (cycle) — skip reverse edge in undirected
                if (!isReverseEdge(u, v, colors)) {
                    eCls.put(key, EdgeClass.BACK);
                    frames.add(snapshot(colors, eCls, u,
                        "Back edge " + u + " → " + v + " (cycle!)"));
                }
            } else { // BLACK / GREEN
                if (graphData.isDirected()) {
                    EdgeClass cls = disc.getOrDefault(u, 0) < disc.getOrDefault(v, 0)
                                    ? EdgeClass.FORWARD : EdgeClass.CROSS;
                    eCls.put(key, cls);
                    frames.add(snapshot(colors, eCls, u,
                        (cls == EdgeClass.FORWARD ? "Forward" : "Cross") +
                        " edge " + u + " → " + v));
                }
                // In undirected, once v is done it's just a tree edge already classified
            }
        }

        colors.put(u, NodeColor.GREEN);
        finish.put(u, timer[0]++);
        frames.add(snapshot(colors, eCls, u,
            "Finish node " + u + " — pop from stack (green)"));
    }

    /** True if (u→v) is the reverse of the tree edge (v→u) in an undirected graph */
    private boolean isReverseEdge(int u, int v, Map<Integer, NodeColor> colors) {
        if (graphData.isDirected()) return false;
        // In undirected DFS, a gray neighbor that is the "parent" means this is
        // just the back-pointer of the undirected edge — skip it.
        // We detect this by checking if the reverse key is already TREE.
        return false; // handled separately in snapshot — gray neighbor = back edge for undirected too
    }

    // ── BFS frame builder ──────────────────────────────────────────────────────
    /**
     * BFS color convention:
     *   WHITE  = unvisited
     *   GRAY   = in queue
     *   GREEN  = dequeued / fully processed
     * Edge types: tree (green) and cross (orange) only — no back/forward in BFS.
     */
    private void buildBFSFrames(int start) {
        frames.clear();

        Map<Integer, NodeColor> colors = new LinkedHashMap<>();
        Map<String, EdgeClass>  eCls   = new LinkedHashMap<>();
        for (int n : graphData.getNodes())  colors.put(n, NodeColor.WHITE);
        for (Edge e : graphData.getEdges()) eCls.put(edgeKey(e), EdgeClass.NORMAL);

        frames.add(snapshot(colors, eCls, -1, "Initial state — all nodes unvisited (white)"));

        Queue<Integer> queue = new LinkedList<>();
        colors.put(start, NodeColor.GRAY);
        queue.add(start);
        frames.add(snapshot(colors, eCls, start,
            "Enqueue start node " + start + " (gray)"));

        while (!queue.isEmpty()) {
            int u = queue.poll();
            colors.put(u, NodeColor.GREEN);
            frames.add(snapshot(colors, eCls, u,
                "Dequeue node " + u + " — fully processed (green)"));

            for (int v : graphData.getNeighbors(u)) {
                String key = edgeKey(u, v);
                if (colors.get(v) == NodeColor.WHITE) {
                    eCls.put(key, EdgeClass.TREE);
                    colors.put(v, NodeColor.GRAY);
                    queue.add(v);
                    frames.add(snapshot(colors, eCls, u,
                        "Tree edge " + u + " → " + v + " | enqueue " + v));
                } else if (colors.get(v) != NodeColor.GREEN) {
                    // cross edge in BFS (target already discovered but not from u)
                    if (eCls.get(key) == EdgeClass.NORMAL) {
                        eCls.put(key, EdgeClass.CROSS);
                        frames.add(snapshot(colors, eCls, u,
                            "Cross edge " + u + " → " + v));
                    }
                }
            }
        }

        // Handle disconnected components
        for (int n : graphData.getNodes()) {
            if (colors.get(n) == NodeColor.WHITE) {
                colors.put(n, NodeColor.GRAY);
                queue.add(n);
                frames.add(snapshot(colors, eCls, n, "Disconnected component — enqueue " + n));
                while (!queue.isEmpty()) {
                    int u = queue.poll();
                    colors.put(u, NodeColor.GREEN);
                    frames.add(snapshot(colors, eCls, u, "Process " + u));
                    for (int v : graphData.getNeighbors(u)) {
                        if (colors.get(v) == NodeColor.WHITE) {
                            eCls.put(edgeKey(u, v), EdgeClass.TREE);
                            colors.put(v, NodeColor.GRAY);
                            queue.add(v);
                            frames.add(snapshot(colors, eCls, u, "Tree edge " + u + " → " + v));
                        }
                    }
                }
            }
        }
    }

    // ── MST frame builder (Prim's algorithm) ───────────────────────────────────
    /**
     * Prim's MST visualization:
     *   - MST nodes  → GREEN
     *   - Frontier   → GRAY  (reachable but not yet in MST)
     *   - Unvisited  → WHITE
     *   - MST edges  → GREEN (tree)
     *   - Rejected   → ORANGE (cross / non-tree)
     */
    private void buildMSTFrames() {
        frames.clear();

        List<Integer> nodes = graphData.getNodes();
        if (nodes.isEmpty()) return;

        Map<Integer, NodeColor> colors = new LinkedHashMap<>();
        Map<String, EdgeClass>  eCls   = new LinkedHashMap<>();
        for (int n : nodes)             colors.put(n, NodeColor.WHITE);
        for (Edge e : graphData.getEdges()) eCls.put(edgeKey(e), EdgeClass.NORMAL);

        frames.add(snapshot(colors, eCls, -1, "MST (Prim's) — all nodes unvisited"));

        // key[v] = minimum weight edge connecting v to the growing MST
        // We use unit weights (1) since GraphData doesn't store weights.
        Set<Integer> inMST = new HashSet<>();
        // Priority queue: {cost, node, parent}
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[0]));

        int src = nodes.get(0);
        pq.offer(new int[]{0, src, -1});

        while (!pq.isEmpty()) {
            int[] top  = pq.poll();
            int u      = top[1];
            int parent = top[2];

            if (inMST.contains(u)) continue;

            inMST.add(u);
            colors.put(u, NodeColor.GREEN);

            if (parent != -1) {
                // Mark MST edge (both directions for undirected)
                eCls.put(edgeKey(parent, u), EdgeClass.TREE);
                eCls.put(edgeKey(u, parent), EdgeClass.TREE);
                frames.add(snapshot(colors, eCls, u,
                    "Add node " + u + " to MST via edge " + parent + " — " + u));
            } else {
                frames.add(snapshot(colors, eCls, u,
                    "Start MST from node " + u));
            }

            for (int v : graphData.getNeighbors(u)) {
                if (!inMST.contains(v)) {
                    colors.put(v, NodeColor.GRAY);   // frontier
                    pq.offer(new int[]{1, v, u});    // weight=1 (unweighted)
                    frames.add(snapshot(colors, eCls, u,
                        "Add " + v + " to frontier (reachable from MST)"));
                } else {
                    // Already in MST — this edge is rejected
                    String key = edgeKey(u, v);
                    if (eCls.get(key) != EdgeClass.TREE) {
                        eCls.put(key, EdgeClass.CROSS);
                        frames.add(snapshot(colors, eCls, u,
                            "Edge " + u + " — " + v + " rejected (both ends in MST)"));
                    }
                }
            }
        }
    }

    // ── Snapshot helper ────────────────────────────────────────────────────────

    private TraversalFrame snapshot(Map<Integer, NodeColor> colors,
                                     Map<String, EdgeClass>  eCls,
                                     int activeNode,
                                     String label) {
        return new TraversalFrame(
            new LinkedHashMap<>(colors),
            new LinkedHashMap<>(eCls),
            activeNode,
            label
        );
    }

    // ── Playback controls ──────────────────────────────────────────────────────

    private void beginPlayback() {
        if (frames.isEmpty()) return;
        currentFrame = 0;
        redrawCanvas(frames.get(0));
        startBtn.setDisable(false);
        nextBtn.setDisable(false);
        prevBtn.setDisable(false);
        stopBtn.setDisable(false);
        startAuto();
    }

    private void togglePause() {
        if (isRunning) { stopAuto(); startBtn.setText("▶ Start"); }
        else           { startAuto(); startBtn.setText("⏸ Pause"); }
    }

    private void startAuto() {
        if (frames.isEmpty()) { showAlert("Start a traversal first!"); return; }
        stopAuto();
        isRunning = true;
        startBtn.setText("⏸ Pause");
        long interval = (long) speedSlider.getValue();
        stepTimer = new Timer(true);
        stepTimer.schedule(new TimerTask() {
            @Override public void run() {
                javafx.application.Platform.runLater(() -> {
                    if (currentFrame < frames.size() - 1) {
                        currentFrame++;
                        redrawCanvas(frames.get(currentFrame));
                    } else {
                        stopAuto();
                    }
                });
            }
        }, interval, interval);
    }

    private void stopAuto() {
        if (stepTimer != null) { stepTimer.cancel(); stepTimer = null; }
        isRunning = false;
        startBtn.setText("▶ Start");
    }

    private void stopAndReset() {
        stopAuto();
        resetTraversalFrames();
        redrawCanvas(null);
    }

    private void nextStep() {
        if (frames.isEmpty()) { showAlert("Start a traversal first!"); return; }
        stopAuto();
        if (currentFrame < frames.size() - 1) {
            currentFrame++;
            redrawCanvas(frames.get(currentFrame));
        }
    }

    private void prevStep() {
        if (frames.isEmpty()) { showAlert("Start a traversal first!"); return; }
        stopAuto();
        if (currentFrame > 0) {
            currentFrame--;
            redrawCanvas(frames.get(currentFrame));
        }
    }

    private void resetTraversalFrames() {
        stopAuto();
        frames.clear();
        currentFrame = -1;
    }

    // ── Canvas rendering ───────────────────────────────────────────────────────

    private void redrawCanvas(TraversalFrame frame) {
        if (canvas == null) return;
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double W = canvas.getWidth(), H = canvas.getHeight();
        gc.clearRect(0, 0, W, H);

        // Soft background
        gc.setFill(Color.web("#f8f9fa"));
        gc.fillRect(0, 0, W, H);

        Map<Integer, double[]> pos = circleLayout(W, H);
        if (pos.isEmpty()) return;

        boolean directed = graphData.isDirected();

        // ── Draw edges ──
        for (Edge e : graphData.getEdges()) {
            double[] fp = pos.get(e.from), tp = pos.get(e.to);
            if (fp == null || tp == null) continue;

            EdgeClass cls = EdgeClass.NORMAL;
            if (frame != null) cls = frame.edgeClass.getOrDefault(edgeKey(e), EdgeClass.NORMAL);

            Color   edgeColor = edgeColor(cls);
            double  width     = cls == EdgeClass.TREE ? 3.5 : 2.0;
            boolean dashed    = cls == EdgeClass.FORWARD || cls == EdgeClass.CROSS;

            gc.setStroke(edgeColor);
            gc.setLineWidth(width);
            if (dashed) gc.setLineDashes(8, 5);
            else        gc.setLineDashes(null);

            if (directed && e.from == findReverseFrom(e, graphData.getEdges())) {
                // Draw slightly curved to distinguish A→B from B→A
                drawCurvedEdge(gc, fp[0], fp[1], tp[0], tp[1], edgeColor, directed);
            } else {
                gc.strokeLine(fp[0], fp[1], tp[0], tp[1]);
                if (directed) drawArrowHead(gc, fp[0], fp[1], tp[0], tp[1], edgeColor);
            }
            gc.setLineDashes(null);
        }

        // ── Draw nodes ──
        double R = 22;
        for (int node : graphData.getNodes()) {
            double[] p = pos.get(node);
            if (p == null) continue;

            NodeColor nc = (frame != null) ? frame.nodeColor.getOrDefault(node, NodeColor.WHITE) : NodeColor.WHITE;
            boolean isActive = (frame != null && frame.activeNode == node);

            Color fill   = nodeColorToFx(nc, isActive);
            Color stroke = isActive ? Color.web("#f39c12") : Color.web("#2c3e50");
            double strokeW = isActive ? 3.5 : 2.0;

            // Shadow
            gc.setFill(Color.web("#00000022"));
            gc.fillOval(p[0] - R + 2, p[1] - R + 3, 2 * R, 2 * R);

            gc.setFill(fill);
            gc.fillOval(p[0] - R, p[1] - R, 2 * R, 2 * R);
            gc.setStroke(stroke);
            gc.setLineWidth(strokeW);
            gc.strokeOval(p[0] - R, p[1] - R, 2 * R, 2 * R);

            // Label
            String label = String.valueOf(node);
            gc.setFill(nc == NodeColor.WHITE ? Color.web("#2c3e50") : Color.WHITE);
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
            gc.fillText(label, p[0] - (label.length() > 1 ? 8 : 5), p[1] + 5);
        }

        // ── Step label ──
        if (frame != null && frame.label != null && !frame.label.isEmpty()) {
            gc.setFill(Color.web("#2c3e50cc"));
            gc.fillRoundRect(10, H - 38, W - 20, 30, 8, 8);
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
            gc.fillText(frame.label, 20, H - 17);
        }

        // ── Legend ──
        drawLegend(gc, W);
    }

    private void drawArrowHead(GraphicsContext gc, double sx, double sy, double ex, double ey, Color color) {
        double R      = 22;
        double angle  = Math.atan2(ey - sy, ex - sx);
        double tip_x  = ex - R * Math.cos(angle);
        double tip_y  = ey - R * Math.sin(angle);
        double arrowL = 13;
        double spread = Math.PI / 6.5;

        double x1 = tip_x - arrowL * Math.cos(angle - spread);
        double y1 = tip_y - arrowL * Math.sin(angle - spread);
        double x2 = tip_x - arrowL * Math.cos(angle + spread);
        double y2 = tip_y - arrowL * Math.sin(angle + spread);

        gc.setFill(color);
        gc.fillPolygon(new double[]{tip_x, x1, x2}, new double[]{tip_y, y1, y2}, 3);
    }

    private void drawCurvedEdge(GraphicsContext gc,
                                double sx, double sy, double ex, double ey,
                                Color color, boolean directed) {
        double mx  = (sx + ex) / 2;
        double my  = (sy + ey) / 2;
        double dx  = ey - sy, dy = sx - ex;
        double len = Math.sqrt(dx * dx + dy * dy);
        double cx  = mx + 30 * dx / len;
        double cy  = my + 30 * dy / len;

        gc.beginPath();
        gc.moveTo(sx, sy);
        gc.quadraticCurveTo(cx, cy, ex, ey);
        gc.stroke();

        if (directed) drawArrowHead(gc, cx, cy, ex, ey, color);
    }

    /** Checks if a reverse edge (to→from) exists, for curved drawing. */
    private int findReverseFrom(Edge e, List<Edge> edges) {
        for (Edge other : edges) {
            if (other.from == e.to && other.to == e.from) return e.from;
        }
        return -1;
    }

    private void drawLegend(GraphicsContext gc, double W) {
        String[] labels = {"Unvisited", "Discovering", "Finished", "Active"};
        Color[]  fills  = {Color.WHITE, Color.web("#3498db"), Color.web("#2ecc71"), Color.web("#e67e22")};

        double x = W - 150, y = 14, sz = 14, gap = 22;
        gc.setFont(Font.font("Segoe UI", 11));
        for (int i = 0; i < labels.length; i++) {
            gc.setFill(fills[i]);
            gc.fillOval(x, y + i * gap, sz, sz);
            gc.setStroke(Color.web("#555"));
            gc.setLineWidth(1);
            gc.strokeOval(x, y + i * gap, sz, sz);
            gc.setFill(Color.web("#333"));
            gc.fillText(labels[i], x + sz + 5, y + i * gap + 11);
        }
    }

    // ── Color mappings ─────────────────────────────────────────────────────────

    private Color nodeColorToFx(NodeColor nc, boolean isActive) {
        if (isActive) return Color.web("#e67e22");           // orange  — currently active
        return switch (nc) {
            case WHITE  -> Color.WHITE;                      // unvisited
            case GRAY   -> Color.web("#3498db");             // in stack / queue
            case GREEN  -> Color.web("#2ecc71");             // fully processed
        };
    }

    private Color edgeColor(EdgeClass cls) {
        return switch (cls) {
            case TREE    -> Color.web("#27ae60");   // green
            case BACK    -> Color.web("#e74c3c");   // red
            case FORWARD -> Color.web("#2980b9");   // blue
            case CROSS   -> Color.web("#e67e22");   // orange
            case NORMAL  -> Color.web("#95a5a6");   // gray
        };
    }

    // ── Layout ─────────────────────────────────────────────────────────────────

    private Map<Integer, double[]> circleLayout(double W, double H) {
        Map<Integer, double[]> pos = new LinkedHashMap<>();
        List<Integer> nodes = graphData.getNodes();
        if (nodes.isEmpty()) return pos;
        double cx = W / 2, cy = (H - 50) / 2;
        double r  = Math.min(W, H - 50) * 0.36;
        int n = nodes.size();
        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * i / n - Math.PI / 2;
            pos.put(nodes.get(i), new double[]{cx + r * Math.cos(angle), cy + r * Math.sin(angle)});
        }
        return pos;
    }

    // ── Sidebar (adjacency list + graph info) ──────────────────────────────────

    private void updateSidebar() {
        if (propsArea == null) return;
        boolean directed = graphData.isDirected();
        StringBuilder sb = new StringBuilder();
        sb.append(directed ? "[ Directed ]\n\n" : "[ Undirected ]\n\n");

        for (int node : graphData.getNodes()) {
            sb.append(node).append("  →  ");
            List<Integer> nbrs = graphData.getNeighbors(node);
            sb.append(nbrs.isEmpty() ? "(none)" : String.join(", ",
                nbrs.stream().map(String::valueOf).toList()));
            sb.append("\n");
        }
        propsArea.setText(sb.toString());

        verticesLabel.setText(String.valueOf(graphData.getNodes().size()));
        edgesLabel.setText(String.valueOf(graphData.getEdgeCount()));
        treeLabel.setText(isTree() ? "Yes" : "No");
        connectedLabel.setText(isConnected() ? "Yes" : "No");
    }

    // ── Graph property checks ──────────────────────────────────────────────────

    private boolean isTree() {
        if (graphData.isDirected()) return false;
        int V = graphData.getNodes().size();
        int E = graphData.getEdgeCount();
        return V > 0 && E == V - 1 && isConnected();
    }

    private boolean isConnected() {
        List<Integer> nodes = graphData.getNodes();
        if (nodes.isEmpty()) return true;
        Set<Integer> visited = new HashSet<>();
        Queue<Integer> q = new LinkedList<>();
        q.add(nodes.get(0));
        visited.add(nodes.get(0));
        while (!q.isEmpty()) {
            for (int nb : graphData.getNeighbors(q.poll())) {
                if (visited.add(nb)) q.add(nb);
            }
        }
        return visited.size() == nodes.size();
    }

    // ── Random graph ───────────────────────────────────────────────────────────

    private void generateRandomGraph() {
        graphData.clear();
        Random rnd = new Random();
        int nodeCount = 5 + rnd.nextInt(4);          // 5–8 nodes
        List<Integer> ids = new ArrayList<>();
        while (ids.size() < nodeCount) {
            int v = rnd.nextInt(20) + 1;
            if (!ids.contains(v)) { ids.add(v); graphData.addNode(v); }
        }
        int edgeTarget = nodeCount + rnd.nextInt(nodeCount);
        for (int i = 0; i < edgeTarget; i++) {
            int from = ids.get(rnd.nextInt(ids.size()));
            int to   = ids.get(rnd.nextInt(ids.size()));
            if (from != to) graphData.addEdge(from, to);
        }
        resetTraversalFrames();
        redrawCanvas(null);
        updateSidebar();
    }

    // ── Code area ─────────────────────────────────────────────────────────────

    private void toggleCodeArea() {
        if (codeArea != null) codeArea.setVisible(!codeArea.isVisible());
    }

    private void copyCode() {
        javafx.scene.input.ClipboardContent cc = new javafx.scene.input.ClipboardContent();
        cc.putString(codeArea.getText());
        javafx.scene.input.Clipboard.getSystemClipboard().setContent(cc);
        showAlert("Code copied!");
    }

    private void loadCodeFile() {
        try (InputStream is = getClass().getResourceAsStream("/codes/graph.txt")) {
            if (is == null) { codeArea.setText(getDFSCode()); return; }
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) sb.append(line).append('\n');
            }
            codeArea.setText(sb.toString());
        } catch (Exception ex) {
            codeArea.setText(getDFSCode());
        }
    }

    private String getDFSCode() {
        return """
                // Depth-First Search (DFS) — iterative with edge classification
                void dfs(int start, List<List<Integer>> adj, int n) {
                    int[] color = new int[n]; // 0=WHITE, 1=GRAY, 2=BLACK
                    int[] disc  = new int[n], fin = new int[n];
                    int   timer = 0;
                    Stack<int[]> stack = new Stack<>(); // {node, neighborIndex}
                    stack.push(new int[]{start, 0});
                    color[start] = 1;   // GRAY

                    while (!stack.isEmpty()) {
                        int[] top  = stack.peek();
                        int   u    = top[0], idx = top[1];
                        List<Integer> nbrs = adj.get(u);

                        if (idx == 0) disc[u] = timer++;   // first visit

                        boolean pushed = false;
                        while (idx < nbrs.size()) {
                            int v = nbrs.get(idx++);
                            top[1] = idx;
                            if (color[v] == 0) {            // WHITE → tree edge
                                color[v] = 1;
                                stack.push(new int[]{v, 0});
                                pushed = true; break;
                            } // else: back / forward / cross edge
                        }

                        if (!pushed) {                      // all neighbors done
                            color[u] = 2;                   // BLACK
                            fin[u]   = timer++;
                            stack.pop();
                        }
                    }
                }
                """;
    }

    private String getBFSCode() {
        return """
                // Breadth-First Search (BFS)
                void bfs(int start, List<List<Integer>> adj) {
                    Set<Integer>    visited = new HashSet<>();
                    Queue<Integer>  queue   = new LinkedList<>();

                    queue.add(start);
                    visited.add(start);

                    while (!queue.isEmpty()) {
                        int node = queue.poll();
                        System.out.println("Visit: " + node);

                        for (int neighbor : adj.get(node)) {
                            if (!visited.contains(neighbor)) {
                                visited.add(neighbor);
                                queue.add(neighbor);   // tree edge
                            }
                            // else: cross edge
                        }
                    }
                }
                """;
    }

    private String getMSTCode() {
        return """
                // Minimum Spanning Tree — Prim's Algorithm (unit weights)
                int[] primMST(List<List<Integer>> adj, int n) {
                    int[]     parent = new int[n];    Arrays.fill(parent, -1);
                    int[]     key    = new int[n];    Arrays.fill(key, Integer.MAX_VALUE);
                    boolean[] inMST  = new boolean[n];

                    PriorityQueue<int[]> pq =
                        new PriorityQueue<>(Comparator.comparingInt(a -> a[0]));

                    key[0] = 0;
                    pq.offer(new int[]{0, 0});   // {weight, node}

                    while (!pq.isEmpty()) {
                        int u = pq.poll()[1];
                        if (inMST[u]) continue;
                        inMST[u] = true;

                        for (int v : adj.get(u)) {
                            int weight = 1;   // unit weight graph
                            if (!inMST[v] && weight < key[v]) {
                                key[v]    = weight;
                                parent[v] = u;
                                pq.offer(new int[]{weight, v});
                            }
                        }
                    }
                    return parent;   // parent[v] = MST parent of v
                }
                """;
    }

    // ── Utility helpers ────────────────────────────────────────────────────────

    private static String edgeKey(Edge e)       { return e.from + ">" + e.to; }
    private static String edgeKey(int u, int v) { return u + ">" + v; }

    protected void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Info"); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }

    // ── Inner types ────────────────────────────────────────────────────────────

    enum NodeColor { WHITE, GRAY, GREEN }

    enum EdgeClass { NORMAL, TREE, BACK, FORWARD, CROSS }

    /** Immutable snapshot of the full graph state at one traversal step. */
    private record TraversalFrame(
        Map<Integer, NodeColor> nodeColor,
        Map<String, EdgeClass>  edgeClass,
        int                     activeNode,
        String                  label
    ) {}

    // ── GraphData ──────────────────────────────────────────────────────────────

    private static class GraphData {
        private final List<Integer> nodes = new ArrayList<>();
        private final List<Edge>    edges = new ArrayList<>();
        private boolean directed = true;

        boolean isDirected() { return directed; }

        void addNode(int v) {
            if (!nodes.contains(v)) { nodes.add(v); Collections.sort(nodes); }
        }

        void removeNode(int v) {
            nodes.remove(Integer.valueOf(v));
            edges.removeIf(e -> e.from == v || e.to == v);
        }

        void addEdge(int from, int to) {
            if (!nodes.contains(from) || !nodes.contains(to)) return;
            // Prevent duplicate
            for (Edge e : edges) {
                if (e.from == from && e.to == to) return;
                if (!directed && e.from == to && e.to == from) return;
            }
            edges.add(new Edge(from, to));
            if (!directed) edges.add(new Edge(to, from));
        }

        void removeEdge(int from, int to) {
            edges.removeIf(e -> e.from == from && e.to == to);
            if (!directed) edges.removeIf(e -> e.from == to && e.to == from);
        }

        boolean hasNode(int v) { return nodes.contains(v); }

        List<Integer> getNodes() { return new ArrayList<>(nodes); }

        List<Edge> getEdges() { return new ArrayList<>(edges); }

        int getEdgeCount() { return directed ? edges.size() : edges.size() / 2; }

        List<Integer> getNeighbors(int node) {
            List<Integer> nb = new ArrayList<>();
            for (Edge e : edges) if (e.from == node) nb.add(e.to);
            return nb;
        }

        void setDirected(boolean d) {
            this.directed = d;
            // Rebuild edges: collect unique (from,to) pairs, then re-expand
            Set<String> seen = new LinkedHashSet<>();
            List<Edge> unique = new ArrayList<>();
            for (Edge e : edges) {
                String k = e.from + ">" + e.to;
                if (seen.add(k)) unique.add(new Edge(e.from, e.to));
            }
            edges.clear();
            for (Edge e : unique) {
                edges.add(new Edge(e.from, e.to));
                if (!d) edges.add(new Edge(e.to, e.from));
            }
        }

        void clear() { nodes.clear(); edges.clear(); }
    }

    // ── Edge ──────────────────────────────────────────────────────────────────

    private static class Edge {
        final int from, to;
        Edge(int from, int to) { this.from = from; this.to = to; }
    }
}

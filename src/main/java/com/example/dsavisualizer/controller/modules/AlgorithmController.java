
package com.example.dsavisualizer.controller.modules;

import com.example.dsavisualizer.controller.ModuleController;
import com.example.dsavisualizer.manager.SceneManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.join;

public class AlgorithmController extends ModuleController {

    public enum Algorithm {
        KNAPSACK, CHANGE, FIB, LCS, SEQALIGN
    }

    public static Algorithm currentAlgorithm;

    @FXML
    private ComboBox<String> modeCombo;
    @FXML
    private VBox inputBox;
    @FXML
    private Button randomBtn, computeBtn;
    @FXML
    private Pane vizPane;
    @FXML
    private Button startBtn, playBtn, pauseBtn, prevBtn, nextBtn, goBackBtn;
    @FXML
    private Label statusLabel;
    @FXML
    private TextArea storyArea;
    @FXML
    private Label titleLabel;

    private Map<String, String> fullCodes = new HashMap<>();
    private Map<String, String[]> codeLines = new HashMap<>();

    // Visualization components
    private VBox codeArea;
    private List<Label> codeLinesLabels;
    private VBox stackPanel;
    private GridPane tableOutput;
    private Label tableAnswerLabel;

    // Step structure
    private static class Step {
        int row, col;
        List<int[]> deps;
        String message;
        int highlightLine;
        List<String> stackSnapshot;
        // Code lines for reference

        Step(int r, int c, List<int[]> d, String msg, int line, List<String> stack) {
            row = r;
            col = c;
            deps = d;
            message = msg;
            highlightLine = line;
            stackSnapshot = new ArrayList<>(stack);
        }
    }

    private List<Step> steps = new ArrayList<>();
    private int currentStep = -1;
    private Timeline playTimeline;
    private Deque<String> callStack = new ArrayDeque<>();
    private Map<String, Label> tableCellMap = new HashMap<>();

    // Input fields
    private TextField weightsField, valuesField, capacityField;
    private TextField amountField, coinsField, nField;
    private TextField str1Field, str2Field;

    @Override
    protected void initialize() {
        super.initialize();
        modeCombo.getItems().addAll("Memoization", "Table");
        modeCombo.setValue("Table");
        randomBtn.setOnAction(e -> randomize());
        computeBtn.setOnAction(e -> compute());
        setupControlButtons();
        configureForAlgorithm();
    }

    private void setupControlButtons() {
        startBtn.setOnAction(e -> startAnimation());
        playBtn.setOnAction(e -> playAnimation());
        pauseBtn.setOnAction(e -> pauseAnimation());
        prevBtn.setOnAction(e -> prevStep());
        nextBtn.setOnAction(e -> nextStep());
        goBackBtn.setOnAction(e -> SceneManager.switchScene("modules/dp.fxml"));
    }

    // --- Visualization Layout ---
    private void createVisualizationLayout(String mode) {
        VBox layout = new VBox(20);
        layout.setStyle("-fx-padding:20; -fx-background-color:white;");

        // Code area
        codeArea = new VBox(4);
        codeArea.setStyle("-fx-font-family:'Courier New'; -fx-font-size:18;");
        layout.getChildren().add(codeArea);

        // Stack area
        if (!mode.equals("Table")) {
            stackPanel = new VBox(6);
            stackPanel.setStyle("-fx-font-family:'Courier New'; -fx-font-size:18;");
            layout.getChildren().add(stackPanel);
        }

        // Table area
        // if (mode.equals("Memoization") || mode.equals("Table")) {
        // tableOutput = new GridPane();
        // tableOutput.setStyle("-fx-hgap:5; -fx-vgap:5;");
        // layout.getChildren().add(tableOutput);
        // tableAnswerLabel = new Label();
        // tableAnswerLabel.setStyle("-fx-font-size:20; -fx-font-weight:bold;");
        // layout.getChildren().add(tableAnswerLabel);
        // }

        if (mode.equals("Memoization") || mode.equals("Table")) {
            tableOutput = new GridPane();
            tableOutput.setStyle("-fx-hgap:5; -fx-vgap:5;");
            tableOutput.setPadding(new Insets(10));

            tableAnswerLabel = new Label();
            tableAnswerLabel.setStyle("-fx-font-size:20; -fx-font-weight:bold;");

            // ✅ Wrap tableOutput in a ScrollPane
            ScrollPane tableScroll = new ScrollPane(tableOutput);
            tableScroll.setFitToWidth(true);
            tableScroll.setFitToHeight(true);
            tableScroll.setPrefViewportWidth(600);
            tableScroll.setPrefViewportHeight(400);

            layout.getChildren().addAll(tableScroll, tableAnswerLabel);
        }

        vizPane.getChildren().add(layout);
    }

    // --- Step Recording & Redraw ---
    private void recordStep(int r, int c, List<int[]> deps, String msg, int line, List<String> stack) {
        steps.add(new Step(r, c, deps, msg, line, stack));
    }

    /**/
    // if below code is changed ,that will affect sequence alignment
    private void redrawCurrentStep() {
        if (currentStep < 0 || currentStep >= steps.size())
            return;
        Step step = steps.get(currentStep);
        statusLabel.setText(step.message);
        if (codeLinesLabels != null) {
            for (int i = 0; i < codeLinesLabels.size(); i++) {
                Label lbl = codeLinesLabels.get(i);
                if (i == step.highlightLine) {
                    lbl.setStyle("-fx-font-family:'Courier New'; -fx-font-size:18; -fx-background-color:yellow;");
                } else {
                    lbl.setStyle("-fx-font-family:'Courier New'; -fx-font-size:18;");
                }
            }
        }
        // Reset all cells to white
        for (Label cell : tableCellMap.values()) {
            cell.setStyle(
                    "-fx-background-color:white; -fx-border-color:black; -fx-font-size:18; -fx-alignment:center;");
        }

        // Highlight current cell orange and show value
        Label cur = tableCellMap.get((step.row + 1) + "," + (step.col + 1));
        if (cur != null) {
            cur.setStyle(
                    "-fx-background-color:orange; -fx-border-color:black; -fx-font-size:18; -fx-alignment:center;");
            if (step.message.contains("=")) {
                cur.setText(step.message.split("=")[1].trim());
            }
        }

        // Highlight dependencies blue
        if (step.deps != null) {
            for (int[] dep : step.deps) {
                Label depCell = tableCellMap.get((dep[0] + 1) + "," + (dep[1] + 1));
                if (depCell != null) {
                    depCell.setStyle(
                            "-fx-background-color:lightblue; -fx-border-color:black; -fx-font-size:18; -fx-alignment:center;");
                }
            }
        }

        // Final answer cell green
        if (currentStep == steps.size() - 1 && cur != null) {
            cur.setStyle(
                    "-fx-background-color:lightgreen; -fx-border-color:black; -fx-font-size:18; -fx-alignment:center;");
        }
    }

    // --- Animation Controls ---
    private void startAnimation() {
        currentStep = 0;
        redrawCurrentStep();
    }

    private void playAnimation() {
        if (steps.isEmpty())
            return;
        playTimeline = new Timeline();
        for (int i = 0; i < steps.size(); i++) {
            int idx = i;
            KeyFrame kf = new KeyFrame(Duration.seconds(1.0 * i), e -> {
                currentStep = idx;
                redrawCurrentStep();
            });
            playTimeline.getKeyFrames().add(kf);
        }
        playTimeline.play();
    }

    private void pauseAnimation() {
        if (playTimeline != null)
            playTimeline.pause();
    }

    private void prevStep() {
        if (currentStep > 0) {
            currentStep--;
            redrawCurrentStep();
        }
    }

    private void nextStep() {
        if (currentStep < steps.size() - 1) {
            currentStep++;
            redrawCurrentStep();
        }
    }

    // --- Fibonacci Implementation ---
    private void computeFib(String mode) {
        int n = Integer.parseInt(nField.getText().trim());

        if (mode.equals("Memoization")) {
            callStack.clear();
            steps.clear();
            setupFibTable(n);
            int[] memo = new int[n + 1];
            Arrays.fill(memo, -1);

            int res = fibMemo(n, memo);
            tableAnswerLabel.setText("Fibonacci Number: " + res);
            statusLabel.setText("Fibonacci Number = " + res);
            startAnimation();
        } else { // ✅ Table mode
            steps.clear();
            setupFibTable(n);
            int[] dp = new int[n + 1];
            for (int i = 0; i <= n; i++) {
                if (i <= 1)
                    dp[i] = i;
                else
                    dp[i] = dp[i - 1] + dp[i - 2];

                List<int[]> deps = new ArrayList<>();
                if (i - 1 >= 0)
                    deps.add(new int[] { 0, i - 1 });
                if (i - 2 >= 0)
                    deps.add(new int[] { 0, i - 2 });

                // Record step to update this cell later
                recordStep(0, i, deps, "dp[" + i + "] = " + dp[i], 4, new ArrayList<>(callStack));
            }

            tableAnswerLabel.setText("Fibonacci Number: " + dp[n]);
            startAnimation();
        }
    }

    /**/

    // Recursive Fibonacci
    private int fibRec(int n) {
        callStack.push("fib(" + n + ")");
        recordStep(-1, -1, null, "call fib(" + n + ")", 0, new ArrayList<>(callStack));

        if (n <= 1) {
            recordStep(-1, -1, null, "return " + n, 1, new ArrayList<>(callStack));
            callStack.pop();
            return n;
        }

        int a = fibRec(n - 1);
        int b = fibRec(n - 2);
        int res = a + b;

        recordStep(-1, -1, null, "return " + res, 4, new ArrayList<>(callStack));
        callStack.pop();
        return res;
    }

    // Memoization Fibonacci
    private int fibMemo(int n, int[] memo) {
        callStack.push("fib(" + n + ")");
        recordStep(-1, -1, null, "call fib(" + n + ")", 0, new ArrayList<>(callStack));

        if (n <= 1) {
            memo[n] = n;
            recordStep(0, n, null, "memo[" + n + "] = " + n, 1, new ArrayList<>(callStack));
            callStack.pop();
            return n;
        }

        if (memo[n] != -1) {
            recordStep(0, n, null, "use memo[" + n + "] = " + memo[n], 2, new ArrayList<>(callStack));
            callStack.pop();
            return memo[n];
        }

        int res = fibMemo(n - 1, memo) + fibMemo(n - 2, memo);
        memo[n] = res;
        recordStep(0, n, null, "store memo[" + n + "] = " + res, 3, new ArrayList<>(callStack));
        callStack.pop();
        return res;
    }

    // --- Knapsack Implementation ---

    private void computeKnapsack(String mode) {
        String[] ws = weightsField.getText().split("[,\\s]+");
        String[] vs = valuesField.getText().split("[,\\s]+");
        int cap = Integer.parseInt(capacityField.getText().trim());
        int n = Math.min(ws.length, vs.length);
        int[] w = new int[n];
        int[] v = new int[n];
        for (int i = 0; i < n; i++) {
            w[i] = Integer.parseInt(ws[i].trim());
            v[i] = Integer.parseInt(vs[i].trim());
        }

        if (mode.equals("Memoization")) {
            callStack.clear();
            steps.clear();
            int[][] memo = new int[n][cap + 1];
            for (int[] row : memo)
                Arrays.fill(row, -1);

            setupKnapsackTable(n, cap);

            int res = knapMemo(w, v, cap, 0, memo);
            // Backtrack to find selected items
            List<Integer> selected = new ArrayList<>();
            int idx = 0, c = cap;
            while (idx < n && c > 0) {
                if (idx < n - 1 && memo[idx][c] == memo[idx + 1][c]) {
                    idx++;
                } else if (w[idx] <= c) {
                    selected.add(idx);
                    c -= w[idx];
                    idx++;
                } else {
                    idx++;
                }
            }
            String items = selected.stream().map(String::valueOf).collect(Collectors.joining(","));
            tableAnswerLabel.setText("Selected Items: " + items);
            statusLabel.setText("Selected Items = " + items);
            startAnimation();
        } else { // ✅ Table mode
            steps.clear();
            setupKnapsackTable(n, cap);

            int[][] dp = new int[n + 1][cap + 1];
            for (int j = 0; j <= cap; j++) {
                dp[0][j] = 0;
                recordStep(0, j, null, "dp[0][" + j + "] = 0", 1, new ArrayList<>(callStack));
            }
            for (int i = 1; i <= n; i++) {
                for (int j = 0; j <= cap; j++) {
                    if (w[i - 1] > j)
                        dp[i][j] = dp[i - 1][j];
                    else
                        dp[i][j] = Math.max(dp[i - 1][j], v[i - 1] + dp[i - 1][j - w[i - 1]]);

                    List<int[]> deps = new ArrayList<>();
                    deps.add(new int[] { i - 1, j });
                    if (w[i - 1] <= j)
                        deps.add(new int[] { i - 1, j - w[i - 1] });

                    recordStep(i, j, deps, "dp[" + i + "][" + j + "] = " + dp[i][j], 4, new ArrayList<>(callStack));
                }
            }

            List<Integer> selected = new ArrayList<>();
            int ii = n, jj = cap;
            while (ii > 0 && jj > 0) {
                if (dp[ii][jj] == dp[ii - 1][jj]) {
                    ii--;
                } else {
                    selected.add(ii - 1);
                    jj -= w[ii - 1];
                    ii--;
                }
            }
            Collections.reverse(selected);
            String items = selected.stream().map(String::valueOf).collect(Collectors.joining(","));
            tableAnswerLabel.setText("Selected Items: " + items);
            statusLabel.setText("Selected Items = " + items);
            startAnimation();
        }
    }

    // Recursive Knapsack
    private int knapRec(int[] w, int[] v, int cap, int idx) {
        callStack.push("knap(idx=" + idx + ",cap=" + cap + ")");
        recordStep(-1, -1, null, "call knap idx=" + idx, 0, new ArrayList<>(callStack));

        if (idx == w.length || cap == 0) {
            recordStep(-1, -1, null, "return 0", 1, new ArrayList<>(callStack));
            callStack.pop();
            return 0;
        }

        if (w[idx] > cap) {
            int res = knapRec(w, v, cap, idx + 1);
            callStack.pop();
            return res;
        }

        int take = v[idx] + knapRec(w, v, cap - w[idx], idx + 1);
        int skip = knapRec(w, v, cap, idx + 1);
        int res = Math.max(take, skip);

        recordStep(-1, -1, null, "return " + res, 4, new ArrayList<>(callStack));
        callStack.pop();
        return res;
    }

    // Memoization Knapsack
    private int knapMemo(int[] w, int[] v, int cap, int idx, int[][] memo) {
        callStack.push("knap(" + idx + "," + cap + ")");
        recordStep(-1, -1, null, "call knap memo idx=" + idx, 0, new ArrayList<>(callStack));

        if (idx == w.length || cap == 0) {
            recordStep(-1, -1, null, "return 0", 1, new ArrayList<>(callStack));
            callStack.pop();
            return 0;
        }

        if (memo[idx][cap] != -1) {
            recordStep(idx, cap, null, "use memo[" + idx + "][" + cap + "] = " + memo[idx][cap], 2,
                    new ArrayList<>(callStack));
            callStack.pop();
            return memo[idx][cap];
        }

        if (w[idx] > cap) {
            int res = knapMemo(w, v, cap, idx + 1, memo);
            memo[idx][cap] = res;
            recordStep(idx, cap, null, "store memo[" + idx + "][" + cap + "] = " + res, 3, new ArrayList<>(callStack));
            callStack.pop();
            return res;
        }

        int take = v[idx] + knapMemo(w, v, cap - w[idx], idx + 1, memo);
        int skip = knapMemo(w, v, cap, idx + 1, memo);
        int res = Math.max(take, skip);
        memo[idx][cap] = res;

        recordStep(idx, cap, null, "store memo[" + idx + "][" + cap + "] = " + res, 3, new ArrayList<>(callStack));
        callStack.pop();
        return res;
    }
    // --- Coin Change Implementation ---

    private void computeChange(String mode) {
        int amt = Integer.parseInt(amountField.getText().trim());
        String[] cs = coinsField.getText().split("[,\\s]+");
        int m = cs.length;
        int[] coins = new int[m];
        for (int i = 0; i < m; i++)
            coins[i] = Integer.parseInt(cs[i].trim());

        if (mode.equals("Memoization")) {
            // ... keep your memoization logic
        } else { // ✅ Table mode
            steps.clear();
            setupChangeTable(amt);

            int[] dp = new int[amt + 1];
            Arrays.fill(dp, Integer.MAX_VALUE / 2);
            dp[0] = 0;

            // Record steps instead of filling immediately
            for (int a = 1; a <= amt; a++) {
                for (int c : coins) {
                    if (c <= a)
                        dp[a] = Math.min(dp[a], 1 + dp[a - c]);
                }

                List<int[]> deps = new ArrayList<>();
                for (int c : coins) {
                    if (c <= a)
                        deps.add(new int[] { 0, a - c });
                }

                // Step will update cell later
                recordStep(0, a, deps, "dp[" + a + "] = " + dp[a], 4, new ArrayList<>(callStack));
            }

            List<Integer> coinsUsed = new ArrayList<>();
            int aaa = amt;
            while (aaa > 0) {
                for (int c : coins) {
                    if (c <= aaa && dp[aaa] == dp[aaa - c] + 1) {
                        coinsUsed.add(c);
                        aaa -= c;
                        break;
                    }
                }
            }
            String coinStr = coinsUsed.stream().map(String::valueOf).collect(Collectors.joining(","));
            tableAnswerLabel.setText("Coins Used: " + coinStr);
            statusLabel.setText("Coins Used = " + coinStr);
            startAnimation();
        }
    }

    // Recursive Coin Change
    private int changeRec(int[] coins, int amt) {
        callStack.push("change(" + amt + ")");
        recordStep(-1, -1, null, "call change amt=" + amt, 0, new ArrayList<>(callStack));

        if (amt == 0) {
            recordStep(-1, -1, null, "return 0", 1, new ArrayList<>(callStack));
            callStack.pop();
            return 0;
        }

        int res = Integer.MAX_VALUE / 2;
        for (int c : coins) {
            if (c <= amt) {
                int sub = changeRec(coins, amt - c);
                res = Math.min(res, sub + 1);
            }
        }

        recordStep(-1, -1, null, "return " + res, 4, new ArrayList<>(callStack));
        callStack.pop();
        return res;
    }

    // Memoization Coin Change
    private int changeMemo(int[] coins, int amt, int[] memo) {
        callStack.push("change(" + amt + ")");
        recordStep(-1, -1, null, "call change memo amt=" + amt, 0, new ArrayList<>(callStack));

        if (amt == 0) {
            recordStep(-1, -1, null, "return 0", 1, new ArrayList<>(callStack));
            callStack.pop();
            return 0;
        }

        if (memo[amt] != -1) {
            recordStep(0, amt, null, "use memo[" + amt + "] = " + memo[amt], 2, new ArrayList<>(callStack));
            callStack.pop();
            return memo[amt];
        }

        int res = Integer.MAX_VALUE / 2;
        for (int c : coins) {
            if (c <= amt) {
                res = Math.min(res, 1 + changeMemo(coins, amt - c, memo));
            }
        }
        memo[amt] = res;

        recordStep(0, amt, null, "store memo[" + amt + "] = " + res, 3, new ArrayList<>(callStack));
        callStack.pop();
        return res;
    }
    // --- LCS Implementation ---

    private void computeLCS(String mode) {
        String a = str1Field.getText();
        String b = str2Field.getText();
        int n = a.length(), m = b.length();

        if (mode.equals("Memoization")) {
            callStack.clear();
            steps.clear();
            int[][] memo = new int[n + 1][m + 1];
            for (int[] row : memo)
                Arrays.fill(row, -1);

            setupLCSTable(a, b);

            int res = lcsMemo(a, b, n, m, memo);
            // Backtrack to build LCS
            StringBuilder lcs = new StringBuilder();
            int iii = n, jjj = m;
            while (iii > 0 && jjj > 0) {
                if (a.charAt(iii - 1) == b.charAt(jjj - 1)) {
                    lcs.append(a.charAt(iii - 1));
                    iii--;
                    jjj--;
                } else if (iii > 0 && memo[iii - 1][jjj] > memo[iii][jjj - 1]) {
                    iii--;
                } else {
                    jjj--;
                }
            }
            lcs.reverse();
            tableAnswerLabel.setText("LCS: " + lcs.toString());
            statusLabel.setText("LCS = " + lcs.toString());
            startAnimation();
        } else { // Table mode
            steps.clear();
            setupLCSTable(a, b);
            int[][] dp = new int[n + 1][m + 1];

            for (int i = 0; i <= n; i++) {
                dp[i][0] = 0;
                recordStep(i, 0, null, "dp[" + i + "][0] = 0", 1, new ArrayList<>(callStack));
            }
            for (int j = 0; j <= m; j++) {
                dp[0][j] = 0;
                recordStep(0, j, null, "dp[0][" + j + "] = 0", 1, new ArrayList<>(callStack));
            }

            for (int i = 1; i <= n; i++) {
                for (int j = 1; j <= m; j++) {
                    if (a.charAt(i - 1) == b.charAt(j - 1))
                        dp[i][j] = dp[i - 1][j - 1] + 1;
                    else
                        dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);

                    List<int[]> deps = new ArrayList<>();
                    deps.add(new int[] { i - 1, j });
                    deps.add(new int[] { i, j - 1 });
                    deps.add(new int[] { i - 1, j - 1 });

                    recordStep(i, j, deps, "dp[" + i + "][" + j + "] = " + dp[i][j], 4, new ArrayList<>(callStack));
                }
            }

            StringBuilder lcs = new StringBuilder();
            int iii = n, jjj = m;
            while (iii > 0 && jjj > 0) {
                if (a.charAt(iii - 1) == b.charAt(jjj - 1)) {
                    lcs.append(a.charAt(iii - 1));
                    iii--;
                    jjj--;
                } else if (dp[iii - 1][jjj] > dp[iii][jjj - 1]) {
                    iii--;
                } else {
                    jjj--;
                }
            }
            lcs.reverse();
            tableAnswerLabel.setText("LCS: " + lcs.toString());
            statusLabel.setText("LCS = " + lcs.toString());
            startAnimation();
        }

        // for (int i = 1; i <= n; i++) {
        // for (int j = 1; j <= m; j++) {
        // if (a.charAt(i - 1) == b.charAt(j - 1)) dp[i][j] = dp[i - 1][j - 1] + 1;
        // else dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
        //
        // Label cell = new Label(String.valueOf(dp[i][j]));
        // cell.setPrefSize(70, 70);
        // cell.setStyle("-fx-border-color:black; -fx-font-size:18;
        // -fx-alignment:center;");
        // tableOutput.add(cell, j, i);
        // tableCellMap.put(i + "," + j, cell);
        //
        // List<int[]> deps = new ArrayList<>();
        // deps.add(new int[]{i - 1, j});
        // deps.add(new int[]{i, j - 1});
        // deps.add(new int[]{i - 1, j - 1});
        // recordStep(i, j, deps, "dp[" + i + "][" + j + "] = " + dp[i][j], 4, new
        // ArrayList<>(callStack));
        // }
        // }
        // tableAnswerLabel.setText("Result: " + dp[n][m]);
        // startAnimation();
        // }
    }

    // Recursive LCS
    private int lcsRec(String a, String b, int i, int j) {
        callStack.push("lcs(" + i + "," + j + ")");
        recordStep(-1, -1, null, "call lcs(" + i + "," + j + ")", 0, new ArrayList<>(callStack));

        if (i == 0 || j == 0) {
            recordStep(-1, -1, null, "return 0", 1, new ArrayList<>(callStack));
            callStack.pop();
            return 0;
        }

        int res;
        if (a.charAt(i - 1) == b.charAt(j - 1)) {
            res = 1 + lcsRec(a, b, i - 1, j - 1);
        } else {
            res = Math.max(lcsRec(a, b, i - 1, j), lcsRec(a, b, i, j - 1));
        }

        recordStep(-1, -1, null, "return " + res, 4, new ArrayList<>(callStack));
        callStack.pop();
        return res;
    }

    // Memoization LCS
    private int lcsMemo(String a, String b, int i, int j, int[][] memo) {
        callStack.push("lcs(" + i + "," + j + ")");
        recordStep(-1, -1, null, "call lcs memo(" + i + "," + j + ")", 0, new ArrayList<>(callStack));

        if (i == 0 || j == 0) {
            recordStep(-1, -1, null, "return 0", 1, new ArrayList<>(callStack));
            callStack.pop();
            return 0;
        }

        if (memo[i][j] != -1) {
            recordStep(i, j, null, "use memo[" + i + "][" + j + "] = " + memo[i][j], 2, new ArrayList<>(callStack));
            callStack.pop();
            return memo[i][j];
        }

        int res;
        if (a.charAt(i - 1) == b.charAt(j - 1)) {
            res = 1 + lcsMemo(a, b, i - 1, j - 1, memo);
        } else {
            res = Math.max(lcsMemo(a, b, i - 1, j, memo), lcsMemo(a, b, i, j - 1, memo));
        }
        memo[i][j] = res;

        recordStep(i, j, null, "store memo[" + i + "][" + j + "] = " + res, 3, new ArrayList<>(callStack));
        callStack.pop();
        return res;
    }
    // --- Sequence Alignment Implementation ---

    private void setupSeqAlignTable(String a, String b) {
        tableOutput.getChildren().clear();
        tableCellMap.clear();

        int n = a.length();
        int m = b.length();

        // Empty corner
        Label corner = new Label("");
        corner.setPrefSize(70, 70);
        tableOutput.add(corner, 0, 0);

        // Column headers (string b)
        for (int j = 0; j < m; j++) {
            Label header = new Label(String.valueOf(b.charAt(j)));
            header.setPrefSize(70, 70);
            header.setStyle(
                    "-fx-font-weight:bold; -fx-font-size:20; -fx-alignment:center; -fx-background-color:lightgray;");
            tableOutput.add(header, j + 2, 0);
        }

        // Row headers (string a)
        for (int i = 0; i < n; i++) {
            Label header = new Label(String.valueOf(a.charAt(i)));
            header.setPrefSize(70, 70);
            header.setStyle(
                    "-fx-font-weight:bold; -fx-font-size:20; -fx-alignment:center; -fx-background-color:lightgray;");
            tableOutput.add(header, 0, i + 2);
        }

        // Initialize DP cells
        for (int i = 0; i <= n; i++) {
            for (int j = 0; j <= m; j++) {
                Label cell = new Label("");
                cell.setPrefSize(70, 70);
                cell.setStyle(
                        "-fx-border-color:black; -fx-font-size:18; -fx-alignment:center; -fx-background-color:white;");
                tableOutput.add(cell, j + 1, i + 1);
                tableCellMap.put((i + 1) + "," + (j + 1), cell);
            }
        }
    }

    private void setupMemoTable(String a, String b) {
        tableOutput.getChildren().clear();
        tableCellMap.clear();

        int n = a.length();
        int m = b.length();

        // Empty corner
        Label corner = new Label("");
        corner.setPrefSize(70, 70);
        tableOutput.add(corner, 0, 0);

        // Column headers (string b)
        for (int j = 0; j < m; j++) {
            Label header = new Label(String.valueOf(b.charAt(j)));
            header.setPrefSize(70, 70);
            header.setStyle(
                    "-fx-font-weight:bold; -fx-font-size:20; -fx-alignment:center; -fx-background-color:lightgray;");
            tableOutput.add(header, j + 2, 0);
        }

        // Row headers (string a)
        for (int i = 0; i < n; i++) {
            Label header = new Label(String.valueOf(a.charAt(i)));
            header.setPrefSize(70, 70);
            header.setStyle(
                    "-fx-font-weight:bold; -fx-font-size:20; -fx-alignment:center; -fx-background-color:lightgray;");
            tableOutput.add(header, 0, i + 2);
        }

        // Initialize memo cells
        for (int i = 0; i <= n; i++) {
            for (int j = 0; j <= m; j++) {
                Label cell = new Label(" ");
                cell.setPrefSize(70, 70);
                cell.setStyle(
                        "-fx-border-color:black; -fx-font-size:18; -fx-alignment:center; -fx-background-color:white;");
                tableOutput.add(cell, j + 1, i + 1);
                tableCellMap.put((i + 1) + "," + (j + 1), cell);
            }
        }
    }

    private void setupFibTable(int n) {
        tableOutput.getChildren().clear();
        tableCellMap.clear();

        // Row header
        Label rowHeader = new Label("i");
        rowHeader.setPrefSize(70, 70);
        rowHeader.setStyle(
                "-fx-font-weight:bold; -fx-font-size:20; -fx-alignment:center; -fx-background-color:lightgray;");
        tableOutput.add(rowHeader, 0, 1);

        // Column headers
        for (int i = 0; i <= n; i++) {
            Label header = new Label(String.valueOf(i));
            header.setPrefSize(70, 70);
            header.setStyle(
                    "-fx-font-weight:bold; -fx-font-size:20; -fx-alignment:center; -fx-background-color:lightgray;");
            tableOutput.add(header, i + 1, 0);
        }

        // Initialize cells
        for (int i = 0; i <= n; i++) {
            Label cell = new Label("");
            cell.setPrefSize(70, 70);
            cell.setStyle(
                    "-fx-border-color:black; -fx-font-size:18; -fx-alignment:center; -fx-background-color:white;");
            tableOutput.add(cell, i + 1, 1);
            tableCellMap.put("1," + (i + 1), cell);
        }
    }

    private void setupKnapsackTable(int n, int cap) {
        tableOutput.getChildren().clear();
        tableCellMap.clear();

        // Empty corner
        Label corner = new Label("");
        corner.setPrefSize(70, 70);
        tableOutput.add(corner, 0, 0);

        // Column headers (capacity)
        for (int j = 0; j <= cap; j++) {
            Label header = new Label(String.valueOf(j));
            header.setPrefSize(70, 70);
            header.setStyle(
                    "-fx-font-weight:bold; -fx-font-size:20; -fx-alignment:center; -fx-background-color:lightgray;");
            tableOutput.add(header, j + 1, 0);
        }

        // Row headers (items)
        for (int i = 0; i < n; i++) {
            Label header = new Label("Item " + i);
            header.setPrefSize(70, 70);
            header.setStyle(
                    "-fx-font-weight:bold; -fx-font-size:20; -fx-alignment:center; -fx-background-color:lightgray;");
            tableOutput.add(header, 0, i + 1);
        }

        // Initialize cells
        for (int i = 0; i < n; i++) {
            for (int j = 0; j <= cap; j++) {
                Label cell = new Label(" ");
                cell.setPrefSize(70, 70);
                cell.setStyle(
                        "-fx-border-color:black; -fx-font-size:18; -fx-alignment:center; -fx-background-color:white;");
                tableOutput.add(cell, j + 1, i + 1);
                tableCellMap.put((i + 1) + "," + (j + 1), cell);
            }
        }
    }

    private void setupChangeTable(int amt) {
        tableOutput.getChildren().clear();
        tableCellMap.clear();

        // Row header
        Label rowHeader = new Label("Amount");
        rowHeader.setPrefSize(70, 70);
        rowHeader.setStyle(
                "-fx-font-weight:bold; -fx-font-size:20; -fx-alignment:center; -fx-background-color:lightgray;");
        tableOutput.add(rowHeader, 0, 1);

        // Column headers
        for (int a = 0; a <= amt; a++) {
            Label header = new Label(String.valueOf(a));
            header.setPrefSize(70, 70);
            header.setStyle(
                    "-fx-font-weight:bold; -fx-font-size:20; -fx-alignment:center; -fx-background-color:lightgray;");
            tableOutput.add(header, a + 1, 0);
        }

        // Initialize cells
        for (int a = 0; a <= amt; a++) {
            Label cell = new Label("");
            cell.setPrefSize(70, 70);
            cell.setStyle(
                    "-fx-border-color:black; -fx-font-size:18; -fx-alignment:center; -fx-background-color:white;");
            tableOutput.add(cell, a + 1, 1);
            tableCellMap.put("1," + (a + 1), cell);
        }
    }

    private void setupLCSTable(String a, String b) {
        tableOutput.getChildren().clear();
        tableCellMap.clear();

        int n = a.length();
        int m = b.length();

        // Empty corner
        Label corner = new Label("");
        corner.setPrefSize(70, 70);
        tableOutput.add(corner, 0, 0);

        // Column headers (string b)
        for (int j = 0; j < m; j++) {
            Label header = new Label(String.valueOf(b.charAt(j)));
            header.setPrefSize(70, 70);
            header.setStyle(
                    "-fx-font-weight:bold; -fx-font-size:20; -fx-alignment:center; -fx-background-color:lightgray;");
            tableOutput.add(header, j + 2, 0);
        }

        // Row headers (string a)
        for (int i = 0; i < n; i++) {
            Label header = new Label(String.valueOf(a.charAt(i)));
            header.setPrefSize(70, 70);
            header.setStyle(
                    "-fx-font-weight:bold; -fx-font-size:20; -fx-alignment:center; -fx-background-color:lightgray;");
            tableOutput.add(header, 0, i + 2);
        }

        // Initialize cells
        for (int i = 0; i <= n; i++) {
            for (int j = 0; j <= m; j++) {
                Label cell = new Label("");
                cell.setPrefSize(70, 70);
                cell.setStyle(
                        "-fx-border-color:black; -fx-font-size:18; -fx-alignment:center; -fx-background-color:white;");
                tableOutput.add(cell, j + 1, i + 1);
                tableCellMap.put((i + 1) + "," + (j + 1), cell);
            }
        }
    }

    private void computeSeqAlign(String mode) {
        String a = str1Field.getText();
        String b = str2Field.getText();
        int n = a.length(), m = b.length();

        if (mode.equals("Memoization")) {
            callStack.clear();
            steps.clear();

            int[][] memo = new int[n + 1][m + 1];
            for (int[] row : memo)
                Arrays.fill(row, Integer.MIN_VALUE / 2);

            setupMemoTable(a, b);

            // // Initialize first column
            // for (int i = 0; i <= n; i++) {
            // memo[i][0] = -i;
            // Label cell = tableCellMap.get((i+1) + "," + 1);
            // if (cell != null) cell.setText("");
            // //if (cell != null) cell.setText(String.valueOf(memo[i][0]));
            // recordStep(i, 0, null, "memo[" + i + "][0] = " + memo[i][0], 1, new
            // ArrayList<>(callStack));
            // }
            //
            // // Initialize first row
            // for (int j = 0; j <= m; j++) {
            // memo[0][j] = -j;
            // Label cell = tableCellMap.get(1 + "," + (j+1));
            // //if (cell != null) cell.setText(String.valueOf(memo[0][j]));
            // if (cell != null) cell.setText("");
            // recordStep(0, j, null, "memo[0][" + j + "] = " + memo[0][j], 1, new
            // ArrayList<>(callStack));
            // }

            int res = seqMemo(a, b, n, m, memo);
            // Backtrack to build aligned sequences
            StringBuilder alignA = new StringBuilder();
            StringBuilder alignB = new StringBuilder();
            int iiii = n, jjjj = m;
            while (iiii > 0 || jjjj > 0) {
                if (iiii > 0 && jjjj > 0 && memo[iiii][jjjj] == memo[iiii - 1][jjjj - 1]
                        + (a.charAt(iiii - 1) == b.charAt(jjjj - 1) ? 1 : -1)) {
                    alignA.append(a.charAt(iiii - 1));
                    alignB.append(b.charAt(jjjj - 1));
                    iiii--;
                    jjjj--;
                } else if (iiii > 0 && memo[iiii][jjjj] == memo[iiii - 1][jjjj] - 1) {
                    alignA.append(a.charAt(iiii - 1));
                    alignB.append('-');
                    iiii--;
                } else if (jjjj > 0) {
                    alignA.append('-');
                    alignB.append(b.charAt(jjjj - 1));
                    jjjj--;
                }
            }
            alignA.reverse();
            alignB.reverse();
            tableAnswerLabel.setText("Aligned Sequences:\n" + alignA + "\n" + alignB);
            statusLabel.setText("Aligned Sequences = " + alignA + " / " + alignB);
            startAnimation();
        }

        else {
            steps.clear();
            setupSeqAlignTable(a, b);

            int[][] dp = new int[n + 1][m + 1];

            // --- Fill first column ---
            for (int i = 0; i <= n; i++) {
                dp[i][0] = -i;
                recordStep(i, 0, null, "dp[" + i + "][0] = " + dp[i][0], 1, new ArrayList<>(callStack));
            }

            // --- Fill first row ---
            for (int j = 0; j <= m; j++) {
                dp[0][j] = -j;
                recordStep(0, j, null, "dp[0][" + j + "] = " + dp[0][j], 1, new ArrayList<>(callStack));
            }

            // --- Fill rest ---
            for (int i = 1; i <= n; i++) {
                for (int j = 1; j <= m; j++) {
                    int match = dp[i - 1][j - 1] + (a.charAt(i - 1) == b.charAt(j - 1) ? 1 : -1);
                    int del = dp[i - 1][j] - 1;
                    int ins = dp[i][j - 1] - 1;
                    dp[i][j] = Math.max(match, Math.max(del, ins));

                    List<int[]> deps = Arrays.asList(
                            new int[] { i - 1, j - 1 },
                            new int[] { i - 1, j },
                            new int[] { i, j - 1 });

                    recordStep(i, j, deps, "dp[" + i + "][" + j + "] = " + dp[i][j], 4, new ArrayList<>(callStack));
                }
            }

            StringBuilder alignA = new StringBuilder();
            StringBuilder alignB = new StringBuilder();
            int iiii = n, jjjj = m;
            while (iiii > 0 || jjjj > 0) {
                if (iiii > 0 && jjjj > 0 && dp[iiii][jjjj] == dp[iiii - 1][jjjj - 1]
                        + (a.charAt(iiii - 1) == b.charAt(jjjj - 1) ? 1 : -1)) {
                    alignA.append(a.charAt(iiii - 1));
                    alignB.append(b.charAt(jjjj - 1));
                    iiii--;
                    jjjj--;
                } else if (iiii > 0 && dp[iiii][jjjj] == dp[iiii - 1][jjjj] - 1) {
                    alignA.append(a.charAt(iiii - 1));
                    alignB.append('-');
                    iiii--;
                } else if (jjjj > 0) {
                    alignA.append('-');
                    alignB.append(b.charAt(jjjj - 1));
                    jjjj--;
                }
            }
            alignA.reverse();
            alignB.reverse();
            tableAnswerLabel.setText("Aligned Sequences:\n" + alignA + "\n" + alignB);
            statusLabel.setText("Aligned Sequences = " + alignA + " / " + alignB);
            startAnimation();
        }
    }

    // Recursive Sequence Alignment
    private int seqRec(String a, String b, int i, int j) {
        callStack.push("seq(" + i + "," + j + ")");
        recordStep(-1, -1, null, "call seq(" + i + "," + j + ")", 0, new ArrayList<>(callStack));

        if (i == 0) {
            recordStep(-1, -1, null, "return " + (-j), 1, new ArrayList<>(callStack));
            callStack.pop();
            return -j;
        }
        if (j == 0) {
            recordStep(-1, -1, null, "return " + (-i), 1, new ArrayList<>(callStack));
            callStack.pop();
            return -i;
        }

        int match = seqRec(a, b, i - 1, j - 1) + (a.charAt(i - 1) == b.charAt(j - 1) ? 1 : -1);
        int del = seqRec(a, b, i - 1, j) - 1;
        int ins = seqRec(a, b, i, j - 1) - 1;
        int res = Math.max(match, Math.max(del, ins));

        recordStep(-1, -1, null, "return " + res, 4, new ArrayList<>(callStack));
        callStack.pop();
        return res;
    }

    // Memoization Sequence Alignment
    private int seqMemo(String a, String b, int i, int j, int[][] memo) {
        callStack.push("seq(" + i + "," + j + ")");
        recordStep(-1, -1, null, "call seq memo(" + i + "," + j + ")", 0, new ArrayList<>(callStack));

        if (memo[i][j] != Integer.MIN_VALUE / 2) {
            recordStep(i, j, null, "use memo[" + i + "][" + j + "] = " + memo[i][j], 2, new ArrayList<>(callStack));
            callStack.pop();
            return memo[i][j];
        }

        if (i == 0) {
            memo[i][j] = -j;
            recordStep(i, j, null, "store memo[" + i + "][" + j + "] = " + memo[i][j], 3, new ArrayList<>(callStack));
            callStack.pop();
            return memo[i][j];
        }
        if (j == 0) {
            memo[i][j] = -i;
            recordStep(i, j, null, "store memo[" + i + "][" + j + "] = " + memo[i][j], 3, new ArrayList<>(callStack));
            callStack.pop();
            return memo[i][j];
        }

        int match = seqMemo(a, b, i - 1, j - 1, memo) + (a.charAt(i - 1) == b.charAt(j - 1) ? 1 : -1);
        int del = seqMemo(a, b, i - 1, j, memo) - 1;
        int ins = seqMemo(a, b, i, j - 1, memo) - 1;
        int res = Math.max(match, Math.max(del, ins));
        memo[i][j] = res;

        recordStep(i, j, null, "store memo[" + i + "][" + j + "] = " + res, 3, new ArrayList<>(callStack));
        callStack.pop();
        return res;
    }
    // --- Utility Methods ---

    private String getStackText() {
        StringBuilder sb = new StringBuilder();
        for (String s : callStack) {
            sb.append(s).append("\n");
        }
        return sb.toString();
    }

    private void updateControls() {
        startBtn.setDisable(false);
        playBtn.setDisable(false);
        pauseBtn.setDisable(false);
        prevBtn.setDisable(false);
        nextBtn.setDisable(false);
    }

    private void configureForAlgorithm() {
        if (currentAlgorithm == null)
            return;
        switch (currentAlgorithm) {
            case KNAPSACK -> {
                titleLabel.setText("0/1 Knapsack");
                storyArea.setText(
                        "Given weights and values of n items, along with a capacity, find the maximum total value you can obtain.");
                buildKnapsackInputs();
            }
            case CHANGE -> {
                titleLabel.setText("Coin Change");
                storyArea.setText(
                        "Given an amount and coin denominations, compute the minimum number of coins needed to make the amount.");
                buildChangeInputs();
            }
            case FIB -> {
                titleLabel.setText("Fibonacci");
                storyArea.setText("Compute the nth Fibonacci number using recursion, memoization, or DP table.");
                buildFibInputs();
            }
            case LCS -> {
                titleLabel.setText("Longest Common Subsequence");
                storyArea.setText("Given two strings, find the length of their longest common subsequence.");
                buildTwoStringInputs();
            }
            case SEQALIGN -> {
                titleLabel.setText("Sequence Alignment");
                storyArea.setText("Align two strings to maximize matches and penalize mismatches/gaps.");
                buildTwoStringInputs();
            }
        }
        randomize(); // auto-fill inputs with random values
    }

    private void randomize() {
        Random rnd = new Random();
        if (currentAlgorithm == null)
            return;

        switch (currentAlgorithm) {
            case KNAPSACK -> {
                int n = rnd.nextInt(4) + 3;
                List<Integer> w = new ArrayList<>();
                List<Integer> v = new ArrayList<>();
                for (int i = 0; i < n; i++) {
                    w.add(rnd.nextInt(10) + 1);
                    v.add(rnd.nextInt(20) + 1);
                }
                weightsField.setText(join(w));
                valuesField.setText(join(v));
                capacityField.setText(String.valueOf(rnd.nextInt(n * 5) + 5));
            }
            case CHANGE -> {
                int amt = rnd.nextInt(20) + 5;
                amountField.setText(String.valueOf(amt));
                int m = rnd.nextInt(4) + 3;
                List<Integer> coins = new ArrayList<>();
                for (int i = 0; i < m; i++)
                    coins.add(rnd.nextInt(10) + 1);
                coinsField.setText(join(coins));
            }
            case FIB -> {
                nField.setText(String.valueOf(rnd.nextInt(6) + 5));
            }
            case LCS, SEQALIGN -> {
                int len1 = rnd.nextInt(4) + 3;
                int len2 = rnd.nextInt(4) + 3;
                str1Field.setText(randomString(len1));
                str2Field.setText(randomString(len2));
            }
        }
    }

    // Generate a random string of given length using letters A–E
    private String randomString(int len) {
        StringBuilder sb = new StringBuilder();
        Random r = new Random();
        for (int i = 0; i < len; i++) {
            sb.append((char) ('A' + r.nextInt(5))); // produces A–E
        }
        return sb.toString();
    }

    private void compute() {
        vizPane.getChildren().clear();
        statusLabel.setText("");
        steps.clear();
        callStack.clear();
        tableCellMap.clear();
        currentStep = -1;
        if (playTimeline != null)
            playTimeline.stop();

        String mode = modeCombo.getValue();
        createVisualizationLayout(mode);
        loadCodeForAlgorithm();

        if (currentAlgorithm == null)
            return;
        try {
            switch (currentAlgorithm) {
                case KNAPSACK -> computeKnapsack(mode);
                case CHANGE -> computeChange(mode);
                case FIB -> computeFib(mode);
                case LCS -> computeLCS(mode);
                case SEQALIGN -> computeSeqAlign(mode);
            }
        } catch (Exception ex) {
            showAlert("Invalid input");
            ex.printStackTrace();
        }
        updateControls(); // enable Start/Play/Pause/Next/Prev
    }

    // Build input fields for Knapsack
    private void buildKnapsackInputs() {
        inputBox.getChildren().clear();
        weightsField = new TextField();
        weightsField.setPromptText("weights (comma separated)");
        valuesField = new TextField();
        valuesField.setPromptText("values (comma separated)");
        capacityField = new TextField();
        capacityField.setPromptText("capacity");
        inputBox.getChildren().addAll(
                new Label("Weights"), weightsField,
                new Label("Values"), valuesField,
                new Label("Capacity"), capacityField);
    }

    // Build input fields for Coin Change
    private void buildChangeInputs() {
        inputBox.getChildren().clear();
        amountField = new TextField();
        amountField.setPromptText("amount");
        coinsField = new TextField();
        coinsField.setPromptText("denominations (comma separated)");
        inputBox.getChildren().addAll(
                new Label("Amount"), amountField,
                new Label("Coins"), coinsField);
    }

    // Build input fields for Fibonacci
    private void buildFibInputs() {
        inputBox.getChildren().clear();
        nField = new TextField();
        nField.setPromptText("n (e.g. 10)");
        inputBox.getChildren().addAll(
                new Label("n"), nField);
    }

    // Build input fields for two-string problems (LCS, Sequence Alignment)
    private void buildTwoStringInputs() {
        inputBox.getChildren().clear();
        str1Field = new TextField();
        str1Field.setPromptText("string 1");
        str2Field = new TextField();
        str2Field.setPromptText("string 2");
        inputBox.getChildren().addAll(
                new Label("String 1"), str1Field,
                new Label("String 2"), str2Field);
    }

    private String join(List<Integer> list) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0)
                sb.append(",");
            sb.append(list.get(i));
        }
        return sb.toString(); // <-- important: return String, not List
    }

    private void displayCode(String[] lines) {
        codeArea.getChildren().clear();
        codeLinesLabels = new ArrayList<>();
        for (String line : lines) {
            Label label = new Label(line);
            label.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 11; -fx-text-fill: #333;");
            codeLinesLabels.add(label);
            codeArea.getChildren().add(label);
        }
    }

    private void loadCodeForAlgorithm() {
        if (currentAlgorithm == null || codeArea == null)
            return;

        String name = switch (currentAlgorithm) {
            case KNAPSACK -> "knapsack";
            case CHANGE -> "change";
            case FIB -> "fib";
            case LCS -> "lcs";
            case SEQALIGN -> "seqalign";
        };

        InputStream is = getClass().getResourceAsStream("/codes/" + name + ".txt");
        if (is != null) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String code = br.lines().collect(Collectors.joining("\n"));
                fullCodes.put(name, code);
                codeLines.put(name, code.split("\n"));
                displayCode(code.split("\n")); // <-- initializes codeLinesLabels
            } catch (IOException ex) {
                displayCode(new String[] { "// Code file not found" });
            }
        } else {
            displayCode(new String[] { "// Code file not found" });
        }
    }

}
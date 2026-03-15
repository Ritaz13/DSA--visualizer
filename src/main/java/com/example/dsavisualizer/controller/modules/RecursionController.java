package com.example.dsavisualizer.controller.modules;

import com.example.dsavisualizer.controller.ModuleController;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class RecursionController extends ModuleController {

    private ChoiceBox<String> opChoice;
    @FXML
    private TextField inputField;
    @FXML
    private Button startBtn, pauseBtn, stepBtn, clearBtn;
    private Slider speedSlider;
    @FXML
    private Pane callStackPane;
    @FXML
    private Pane codePane;
    @FXML
    private Label returnLabel;

    private List<Event> events = new ArrayList<>();
    private int eventIndex = 0;
    private Timeline player;
    @FXML
    private Button showCodeBtn;
    @FXML
    private Button copyCodeBtn;

    private String codeContent = "";
    private Deque<String> callStack = new ArrayDeque<>();
    private int highlightedLineIndex = -1;

    @Override
    protected void initialize() {
        super.initialize();
        titleLabel.setText("Recursion");
        storyArea.setText(
                """
                        Imagine you're cutting a big cake into equal slices.
                        You don't cut the whole cake at once. Instead, you cut it into two halves.
                        Then you take one half and cut it again into two smaller halves.
                        You keep repeating this process until the piece is small enough (the base case) that you don't need to cut anymore.""");

        buildControls();

        startBtn.setOnAction(e -> start());
        pauseBtn.setOnAction(e -> pause());
        stepBtn.setOnAction(e -> step());
        clearBtn.setOnAction(e -> clear());

        showCodeBtn.setOnAction(e -> toggleCodeArea());
        copyCodeBtn.setOnAction(e -> copyCode());

        updateCode();
        renderCode();
    }

    private void copyCode() {
        javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
        javafx.scene.input.ClipboardContent cc = new javafx.scene.input.ClipboardContent();
        cc.putString(codeContent);
        clipboard.setContent(cc);
    }

    private void loadCodeFile() {
        try (InputStream is = getClass().getResourceAsStream("/codes/recursion.txt")) {
            if (is != null) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append('\n');
                    }
                    codeContent = sb.toString(); // For codeArea
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void updateCode() {
        String op = opChoice.getValue();
        if ("Factorial".equals(op)) {
            codeContent = """
                    public static int factorial(int n) {
                        if (n <= 1) return 1;
                        return n * factorial(n - 1);
                    }
                    """;
        } else if ("Fibonacci".equals(op)) {
            codeContent = """
                    public static int fibonacci(int n) {
                        if (n <= 1) return n;
                        return fibonacci(n - 1) + fibonacci(n - 2);
                    }
                    """;
        } else if ("Reverse String".equals(op)) {
            codeContent = """
                    public static String reverse(String s) {
                        if (s.length() <= 1) return s;
                        return reverse(s.substring(1)) + s.charAt(0);
                    }
                    """;
        }
        renderCode();
        if (codeArea != null) {
            codeArea.setText(codeContent);
        }
    }

    private void toggleCodeArea() {
        // Code is always shown in codePane
    }

    private void renderCode() {
        if (codePane == null)
            return;

        codePane.getChildren().clear();
        VBox codeBox = new VBox(0);
        codeBox.setPadding(new Insets(10));
        codeBox.setStyle("-fx-background-color: #1e1e1e;");

        String[] lines = codeContent.trim().split("\n");
        for (int i = 0; i < lines.length; i++) {
            HBox lineBox = new HBox(4);
            lineBox.setAlignment(Pos.TOP_LEFT);
            lineBox.setPadding(new Insets(2, 0, 2, 0));
            lineBox.setId("codeline_" + i);

            Text lineNum = new Text(String.format("%2d ", i + 1));
            lineNum.setFont(Font.font("Courier New", 13));
            lineNum.setFill(Color.web("#666666"));

            Text lineText = new Text(lines[i]);
            lineText.setFont(Font.font("Courier New", 13));
            lineText.setFill(Color.web("#d4d4d4"));
            lineText.setId("line_" + i);

            lineBox.getChildren().addAll(lineNum, lineText);
            codeBox.getChildren().add(lineBox);
        }

        codePane.getChildren().add(codeBox);
    }

    private void highlightCodeLine(int lineIndex) {
        highlightedLineIndex = lineIndex;
        if (codePane == null || codePane.getChildren().isEmpty())
            return;

        VBox codeBox = (VBox) codePane.getChildren().get(0);
        for (Node node : codeBox.getChildren()) {
            if (node instanceof HBox) {
                HBox lineBox = (HBox) node;
                if (lineBox.getId() != null && lineBox.getId().equals("codeline_" + lineIndex)) {
                    lineBox.setStyle("-fx-background-color: #FFD700; -fx-background-radius: 3;");
                } else if (lineBox.getId() != null && lineBox.getId().startsWith("codeline_")) {
                    lineBox.setStyle("-fx-background-color: transparent;");
                }
            }
        }
    }

    private void renderCallStack() {
        if (callStackPane == null)
            return;

        callStackPane.getChildren().clear();
        VBox stackBox = new VBox(8);
        stackBox.setPadding(new Insets(10));
        stackBox.setStyle("-fx-background-color: #f9f9f9;");

        if (callStack.isEmpty()) {
            Label emptyLabel = new Label("Call Stack is empty");
            emptyLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #999; -fx-font-family: 'Courier New';");
            stackBox.getChildren().add(emptyLabel);
        } else {
            List<String> stackList = new ArrayList<>(callStack);
            Collections.reverse(stackList);

            for (int i = 0; i < stackList.size(); i++) {
                String call = stackList.get(i);
                Color bgColor = Color.WHITE;
                Color textColor = Color.BLACK;

                HBox callBox = new HBox(4);
                callBox.setAlignment(Pos.CENTER_LEFT);
                callBox.setPadding(new Insets(10, 12, 10, 12));
                String style = "-fx-background-color: " + toHexColor(bgColor) +
                        "; -fx-border-radius: 4; -fx-background-radius: 4;";
                if (i == stackList.size() - 1) { // current
                    style += "-fx-background-color: yellow;";
                }
                callBox.setStyle(style);

                Label indent = new Label(generateIndent(i));
                indent.setFont(Font.font("Courier New", 13));
                indent.setTextFill(textColor);

                Label callLabel = new Label(call);
                callLabel.setFont(Font.font("Courier New", 14));
                callLabel.setTextFill(textColor);
                callLabel.setStyle("-fx-font-weight: bold;");

                callBox.getChildren().addAll(indent, callLabel);
                stackBox.getChildren().add(callBox);
            }
        }

        callStackPane.getChildren().add(stackBox);
    }

    private String generateIndent(int level) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            sb.append("   ");
        }
        if (level > 0) {
            sb.append("├─ ");
        }
        return sb.toString();
    }

    private void buildControls() {
        if (moduleControls == null)
            return;
        moduleControls.getChildren().clear();

        VBox controlsBox = new VBox(10);
        controlsBox.setAlignment(Pos.CENTER_LEFT);

        // ChoiceBox
        opChoice = new ChoiceBox<>();
        opChoice.getItems().addAll("Factorial", "Fibonacci", "Reverse String");
        opChoice.setValue("Factorial");
        opChoice.setStyle("-fx-font-size: 15; -fx-padding: 6 12;");
        opChoice.valueProperty().addListener((obs, oldV, newV) -> updateCode());

        Label opLabel = new Label("Operation:");
        opLabel.setStyle("-fx-font-size: 15; -fx-font-weight: bold;");

        // Speed slider
        speedSlider = new Slider(100, 1000, 500);
        speedSlider.setPrefWidth(250);
        speedSlider.setStyle("-fx-font-size: 12;");

        Label speedLabel = new Label("Speed:");
        speedLabel.setStyle("-fx-font-size: 15; -fx-font-weight: bold;");

        controlsBox.getChildren().addAll(opLabel, opChoice, speedLabel, speedSlider);

        moduleControls.getChildren().add(controlsBox);

        pauseBtn.setDisable(true);

        player = new Timeline();
        player.setCycleCount(Timeline.INDEFINITE);
        player.getKeyFrames().add(new KeyFrame(Duration.millis(speedSlider.getValue()), ev -> step()));

        speedSlider.valueProperty().addListener((obs, oldV, newV) -> {
            if (player != null) {
                boolean wasRunning = player.getStatus() == Timeline.Status.RUNNING;
                player.stop();
                double speedFactor = newV.doubleValue();
                double delay = 2000 / speedFactor;
                player.getKeyFrames().setAll(new KeyFrame(Duration.millis(delay), ev -> step()));
                if (wasRunning)
                    player.play();
            }
        });
    }

    private void start() {
        if (!prepareEvents())
            return;
        pauseBtn.setDisable(false);
        startBtn.setDisable(true);
        stepBtn.setDisable(true);
        clearBtn.setDisable(true);
        player.play();
    }

    private void pause() {
        player.stop();
        pauseBtn.setDisable(true);
        startBtn.setDisable(false);
        stepBtn.setDisable(false);
        clearBtn.setDisable(true);
    }

    private void step() {
        if (events == null || events.isEmpty())
            return;
        if (eventIndex < events.size()) {
            Event ev = events.get(eventIndex++);
            applyEvent(ev);
        } else if (eventIndex == events.size()) {
            showFinalAnswer();
            pauseBtn.setDisable(true);
            startBtn.setDisable(false);
            stepBtn.setDisable(false);
        } else {
            stepBtn.setDisable(false);
            Event lastEv = events.get(events.size() - 1);
            applyEvent(lastEv);
        }
    }

    private void clear() {
        events.clear();
        eventIndex = 0;
        callStack.clear();
        renderCallStack();
        startBtn.setDisable(false);
        stepBtn.setDisable(false);
        pauseBtn.setDisable(true);
        if (player != null)
            player.stop();
    }

    private boolean prepareEvents() {
        clear();
        String op = opChoice.getValue();

        if (op.equals("Reverse String")) {
            String s = inputField.getText();
            if (s == null)
                return false;
            if (s.length() > 10)
                s = s.substring(0, 10);
            buildReverseEvents(s, events);
        } else {
            String in = inputField.getText();
            if (in == null || in.trim().isEmpty())
                return false;
            int n;
            try {
                n = Integer.parseInt(in.trim());
            } catch (NumberFormatException ex) {
                return false;
            }
            if (n < 0)
                return false;
            if (n > 20)
                n = 20;

            if (op.equals("Factorial")) {
                buildFactorialEvents(n, events);
            } else if (op.equals("Fibonacci")) {
                buildFibonacciEvents(n, events);
            }
        }
        return !events.isEmpty();
    }

    private void applyEvent(Event ev) {
        Platform.runLater(() -> {
            switch (ev.type) {
                case ENTER:
                    callStack.push(ev.label);
                    highlightCodeLine(ev.lineIndex);
                    renderCallStack();
                    if (statusLabel != null) {
                        statusLabel.setText("Called: " + ev.label);
                    }
                    if (returnLabel != null) {
                        returnLabel.setText("");
                    }
                    break;
                case RETURN:
                    if (!callStack.isEmpty()) {
                        callStack.pop();
                    }
                    highlightCodeLine(ev.lineIndex);
                    renderCallStack();
                    if (statusLabel != null) {
                        statusLabel.setText("Returns: " + ev.result);
                    }
                    if (returnLabel != null) {
                        returnLabel.setText("Return: " + ev.result);
                    }
                    if (callStack.isEmpty()) {
                        // Final result
                        statusLabel.setText("Final Answer: " + ev.result);
                    }
                    break;
                case INFO:
                    callStack.clear();
                    renderCallStack();
                    highlightCodeLine(-1); // clear highlight
                    VBox infoBox = new VBox(10);
                    infoBox.setPadding(new Insets(15));
                    infoBox.setStyle(
                            "-fx-background-color: #e6f0ff; -fx-border-color: #1976D2; -fx-border-radius: 4; -fx-background-radius: 4;");

                    Label resultLabel = new Label("Final Result:");
                    resultLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #1976D2;");

                    if (ev.result instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<Object> list = (List<Object>) ev.result;
                        HBox seqBox = new HBox(8);
                        seqBox.setAlignment(Pos.CENTER_LEFT);
                        seqBox.setPadding(new Insets(10));
                        for (Object o : list) {
                            Label v = new Label(String.valueOf(o));
                            v.setFont(Font.font("Courier New", 14));
                            v.setStyle("-fx-font-size: 14; -fx-border-color: #444; -fx-padding: 8 12; "
                                    + "-fx-background-color: #FFE082; -fx-border-radius: 4; -fx-background-radius: 4; -fx-font-weight: bold;");
                            seqBox.getChildren().add(v);
                        }
                        infoBox.getChildren().addAll(resultLabel, seqBox);
                    } else {
                        Label ansLabel = new Label(String.valueOf(ev.result));
                        ansLabel.setFont(Font.font("Courier New", 18));
                        ansLabel.setStyle("-fx-font-size: 18; -fx-text-fill: #1976D2; -fx-font-weight: bold;");
                        infoBox.getChildren().addAll(resultLabel, ansLabel);
                    }

                    if (statusLabel != null) {
                        statusLabel.setText("Complete!");
                    }
                    if (returnLabel != null) {
                        returnLabel.setText("");
                    }
                    break;
            }
        });
    }

    private long buildFactorialEvents(int n, List<Event> ev) {
        ev.add(new Event(EventType.ENTER, "fact(" + n + ")"));
        long res;
        if (n <= 1) {
            res = 1;
            ev.add(new Event(EventType.RETURN, "fact(" + n + ")", res));
        } else {
            long sub = buildFactorialEvents(n - 1, ev);
            res = n * sub;
            ev.add(new Event(EventType.RETURN, "fact(" + n + ")", res));
        }
        return res;
    }

    private long buildFibonacciEvents(int n, List<Event> ev) {
        ev.add(new Event(EventType.ENTER, "fib(" + n + ")"));
        long res;
        if (n <= 1) {
            res = n;
            ev.add(new Event(EventType.RETURN, "fib(" + n + ")", res));
        } else {
            long a = buildFibonacciEvents(n - 1, ev);
            long b = buildFibonacciEvents(n - 2, ev);
            res = a + b;
            ev.add(new Event(EventType.RETURN, "fib(" + n + ")", res));
        }
        return res;
    }

    private String buildReverseEvents(String s, List<Event> ev) {
        ev.add(new Event(EventType.ENTER, "rev(\"" + s + "\")"));
        String res;
        if (s.length() <= 1) {
            res = s;
            ev.add(new Event(EventType.RETURN, "rev(\"" + s + "\")", res));
        } else {
            String sub = buildReverseEvents(s.substring(1), ev);
            res = sub + s.charAt(0);
            ev.add(new Event(EventType.RETURN, "rev(\"" + s + "\")", res));
        }
        return res;
    }

    private void showFinalAnswer() {
        callStack.clear();
        renderCallStack();
    }

    private enum EventType {
        ENTER, RETURN, INFO
    }

    private String toHexColor(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    private static class Event {
        EventType type;
        String label;
        Object result;
        int lineIndex;

        Event(EventType type, String label) {
            this.type = type;
            this.label = label;
            this.lineIndex = -1;
        }

        Event(EventType type, String label, Object result) {
            this.type = type;
            this.label = label;
            this.result = result;
            this.lineIndex = -1;
        }

        Event(EventType type, String label, Object result, int lineIndex) {
            this.type = type;
            this.label = label;
            this.result = result;
            this.lineIndex = lineIndex;
        }
    }
}

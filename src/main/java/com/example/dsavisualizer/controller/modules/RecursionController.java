package com.example.dsavisualizer.controller.modules;

import com.example.dsavisualizer.controller.ModuleController;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class RecursionController extends ModuleController {

    private ChoiceBox<String> opChoice;
    private TextField inputField;
    private Button startBtn, pauseBtn, stepBtn, clearBtn;
    private Slider speedSlider;

    private List<Event> events = new ArrayList<>();
    private int eventIndex = 0;
    private Timeline player;

    @Override
    public void initialize() {
        super.initialize();

        setContent(
                "Recursion",
                "Visualize recursion (factorial, fibonacci, reverse string)",
                """
                        Imagine you’re cutting a big cake into equal slices.
                        
                        You don’t cut the whole cake at once. Instead, you cut it into two halves.
                        
                        Then you take one half and cut it again into two smaller halves.
                        
                        You keep repeating this process until the piece is small enough (the base case) that you don’t need to cut anymore."""
        );

        buildControls();

        // recursion module doesn't need generic push/pop controls
        if (pushBtn != null) pushBtn.setVisible(false);
        if (popBtn != null) popBtn.setVisible(false);
        if (pushField != null) pushField.setVisible(false);
    }

    private void buildControls() {
        if (moduleControls == null) return;
        moduleControls.getChildren().clear();

        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        opChoice = new ChoiceBox<>();
        opChoice.getItems().addAll("Factorial", "Fibonacci", "Reverse String");
        opChoice.setValue("Factorial");

        inputField = new TextField();
        inputField.setPromptText("n or string");
        inputField.setPrefWidth(120);

        startBtn = new Button("Start");
        pauseBtn = new Button("Pause");
        stepBtn = new Button("Step");
        clearBtn = new Button("Clear");

        speedSlider = new Slider(100, 2000, 500); // ms per step - lower=faster, higher=slower
        speedSlider.setPrefWidth(200);

        row.getChildren().addAll(new Label("Operation:"), opChoice,
                new Label("Input:"), inputField,
                startBtn, pauseBtn, stepBtn, clearBtn,
                new Label("Speed:"), speedSlider);

        moduleControls.getChildren().add(row);

        startBtn.setOnAction(e -> start());
        pauseBtn.setOnAction(e -> pause());
        stepBtn.setOnAction(e -> step());
        clearBtn.setOnAction(e -> clear());

        pauseBtn.setDisable(true);

        player = new Timeline();
        player.setCycleCount(Timeline.INDEFINITE);
        player.getKeyFrames().add(new KeyFrame(Duration.millis(speedSlider.getValue()), ev -> {
            step();
        }));

        // adapt speed when slider changes
        speedSlider.valueProperty().addListener((obs, oldV, newV) -> {
            if (player != null) {
                player.stop();
                player.getKeyFrames().setAll(new KeyFrame(Duration.millis(newV.doubleValue()), ev -> step()));
                if (!pauseBtn.isDisabled() && player != null && player.getStatus() == Timeline.Status.RUNNING) player.play();
            }
        });
    }

    private void start() {
        if (!prepareEvents()) return;
        pauseBtn.setDisable(false);
        startBtn.setDisable(true);
        stepBtn.setDisable(true);
        player.play();
    }

    private void pause() {
        player.stop();
        pauseBtn.setDisable(true);
        startBtn.setDisable(false);
        stepBtn.setDisable(false);
    }

    private void step() {
        if (events == null || events.isEmpty() || eventIndex >= events.size()) {
            player.stop();
            pauseBtn.setDisable(true);
            startBtn.setDisable(false);
            stepBtn.setDisable(false);
            return;
        }

        Event ev = events.get(eventIndex++);
        applyEvent(ev);
    }

    // Reverse events list so first function calls appear first (bottom-up visualization)
    @Override
    protected void setContent(String title, String desc, String story) {
        super.setContent(title, desc, story);
    }

    private void clear() {
        events.clear();
        eventIndex = 0;
        vizArea.getChildren().clear();
        startBtn.setDisable(false);
        stepBtn.setDisable(false);
        pauseBtn.setDisable(true);
        if (player != null) player.stop();
    }

    private boolean prepareEvents() {
        clear();
        String op = opChoice.getValue();
        if (op.equals("Reverse String")) {
            String s = inputField.getText();
            if (s == null) return false;
            if (s.length() > 10) s = s.substring(0, 10); // limit
            buildReverseEvents(s, events);
        } else {
            String in = inputField.getText();
            if (in == null || in.trim().isEmpty()) return false;
            int n;
            try { n = Integer.parseInt(in.trim()); }
            catch (NumberFormatException ex) { return false; }
            if (n < 0) return false;
            if (n > 20) n = 20; // limit to avoid explosion

            if (op.equals("Factorial")) buildFactorialEvents(n, events);
            else if (op.equals("Fibonacci")) {
                // Find the largest Fibonacci index <= input value
                int idxForValue = findFibIndexForValue(n);
                if (idxForValue == -1) {
                    // input is not a Fibonacci value; find previous Fibonacci number
                    idxForValue = findPrevFibIndex(n);
                }
                if (idxForValue >= 0) {
                    buildFibonacciEvents(idxForValue, events);
                    // Always show the sequence up to that index
                    java.util.List<Long> seq = new java.util.ArrayList<>();
                    seq.add(0L);
                    if (idxForValue >= 1) seq.add(1L);
                    for (int i = 2; i <= idxForValue; i++) {
                        seq.add(seq.get(i - 1) + seq.get(i - 2));
                    }
                    events.add(new Event(EventType.INFO, "sequence", seq));
                }
            }
        }
        eventIndex = 0;
        return !events.isEmpty();

    }

    // helper: return index i if fib(i) == value, else -1
    private int findFibIndexForValue(int value) {
        if (value < 0) return -1;
        if (value == 0) return 0;
        long a = 0, b = 1;
        for (int i = 1; i <= 50; i++) {
            if (b == value) return i;
            long c = a + b;
            a = b;
            b = c;
            if (b > value && c > Integer.MAX_VALUE) break;
        }
        return -1;
    }

    // helper: find the largest Fibonacci index where fib(i) <= value
    private int findPrevFibIndex(int value) {
        if (value < 0) return -1;
        if (value == 0) return 0;
        long a = 0, b = 1;
        int lastIdx = 0;
        for (int i = 1; i <= 50; i++) {
            if (b <= value) {
                lastIdx = i;
                long c = a + b;
                a = b;
                b = c;
            } else {
                break;
            }
        }
        return lastIdx;
    }

    private void applyEvent(Event ev) {
        Platform.runLater(() -> {
            switch (ev.type) {
                case ENTER:
                    Label lbl = makeFrameLabel(ev.label);
                    vizArea.getChildren().add(lbl); // append to end, not beginning
                    break;
                case RETURN:
                    if (!vizArea.getChildren().isEmpty()) {
                        Label top = (Label) vizArea.getChildren().get(vizArea.getChildren().size() - 1);
                        top.setText(top.getText() + " => " + ev.result);
                        top.setStyle(top.getStyle() + " -fx-background-color: lightgreen;");
                    }
                    break;
                case INFO:
                    // For INFO, show the provided data (e.g., Fibonacci sequence) clearly
                    vizArea.getChildren().clear();
                    if (ev.result instanceof java.util.List) {
                        @SuppressWarnings("unchecked")
                        java.util.List<Object> list = (java.util.List<Object>) ev.result;
                        HBox seqBox = new HBox(8);
                        seqBox.setPadding(new Insets(8));
                        for (Object o : list) {
                            Label v = new Label(String.valueOf(o));
                            v.setStyle("-fx-border-color:#444; -fx-padding:6; -fx-background-color:lightblue; -fx-border-radius:4; -fx-background-radius:4;");
                            seqBox.getChildren().add(v);
                        }
                        vizArea.getChildren().add(seqBox);
                    } else {
                        Label info = new Label(String.valueOf(ev.result));
                        info.setStyle("-fx-border-color:#444; -fx-padding:6; -fx-background-color:lightblue; -fx-border-radius:4; -fx-background-radius:4;");
                        vizArea.getChildren().add(info);
                    }
                    break;
            }
        });
    }

    private Label makeFrameLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font(14));
        l.setPadding(new Insets(8));
        l.setStyle("-fx-border-color: #333; -fx-background-color: lightyellow; -fx-border-radius:4; -fx-background-radius:4;");
        return l;
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

    private enum EventType { ENTER, RETURN, INFO }

    private static class Event {
        EventType type;
        String label;
        Object result; // number or string or list

        Event(EventType type, String label) {
            this.type = type; this.label = label;
        }

        Event(EventType type, String label, Object result) {
            this.type = type; this.label = label; this.result = result;
        }
    }
}

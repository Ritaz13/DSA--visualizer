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
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RecursionController extends ModuleController {

    private ChoiceBox<String> opChoice;
    @FXML
    private TextField inputField;
    @FXML
    private Button startBtn, pauseBtn, stepBtn, clearBtn;
    private Slider speedSlider;
    @FXML
    private VBox vizArea;

    private List<Event> events = new ArrayList<>();
    private int eventIndex = 0;
    private Timeline player;
    @FXML private Button showCodeBtn;
    @FXML private Button copyCodeBtn;
    @FXML private TextArea codeArea;


    @Override
    protected void initialize() {
        super.initialize();
        titleLabel.setText("Recursion");
        storyArea.setText("""
            Imagine you’re cutting a big cake into equal slices.
            You don’t cut the whole cake at once. Instead, you cut it into two halves.
            Then you take one half and cut it again into two smaller halves.
            You keep repeating this process until the piece is small enough (the base case) that you don’t need to cut anymore.""");

        buildControls();
        if (vizArea != null) {
            vizArea.setPrefSize(800, 400);
            vizArea.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            VBox.setVgrow(vizArea, Priority.ALWAYS);
            vizArea.setStyle("-fx-border-color: gray; " + "-fx-padding: 10; " + "-fx-background-color: white;");
        }
        startBtn.setOnAction(e -> start());
        pauseBtn.setOnAction(e -> pause());
        stepBtn.setOnAction(e -> step());
        clearBtn.setOnAction(e -> clear());

        showCodeBtn.setOnAction(e -> toggleCodeArea());

        copyCodeBtn.setOnAction(e -> copyCode());
        loadCodeFile();

    }
    private void copyCode() {
        String code = codeArea.getText();
        javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
        javafx.scene.input.ClipboardContent cc = new javafx.scene.input.ClipboardContent();
        cc.putString(code);
        clipboard.setContent(cc);
//        showAlert("Code copied to clipboard!");
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
                    codeArea.setText(sb.toString());
                    codeArea.setVisible(false);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    private void toggleCodeArea() {
        codeArea.setVisible(!codeArea.isVisible());
    }
    private void buildControls() {
        if (moduleControls == null) return;
        moduleControls.getChildren().clear();

        VBox controlsBox = new VBox(10);
        controlsBox.setAlignment(Pos.CENTER_LEFT);

        // ChoiceBox
        opChoice = new ChoiceBox<>();
        opChoice.getItems().addAll("Factorial", "Fibonacci", "Reverse String");
        opChoice.setValue("Factorial");
        opChoice.setStyle("-fx-font-size: 16; -fx-padding: 6 12;");

        Label opLabel = new Label("Operation:");
        opLabel.setStyle("-fx-font-size: 16;");

        // Speed slider
        speedSlider = new Slider(100, 1000, 500);
        speedSlider.setPrefWidth(250);
        speedSlider.setStyle("-fx-font-size: 16;");

        Label speedLabel = new Label("Speed:");
        speedLabel.setStyle("-fx-font-size: 16;");

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
                if (wasRunning) player.play();
            }
        });
    }


    /*private void buildControls() {
        if (moduleControls == null) return;
        moduleControls.getChildren().clear();
        VBox controlsBox = new VBox(10);
        controlsBox.setAlignment(Pos.CENTER_LEFT);


        opChoice = new ChoiceBox<>();
        opChoice.getItems().addAll("Factorial", "Fibonacci", "Reverse String");
        opChoice.setValue("Factorial");
        opChoice.setStyle("-fx-font-size: 16; -fx-padding: 6 12;");

        inputField = new TextField();
        inputField.setPromptText("n or string");
        inputField.setPrefWidth(150);
        inputField.setStyle("-fx-font-size: 16; -fx-padding: 6 12;");

        Label opLabel = new Label("Operation:");
        opLabel.setStyle("-fx-font-size: 16;");

        speedSlider = new Slider(100, 1000, 500);
        speedSlider.setPrefWidth(250);
        speedSlider.setStyle("-fx-font-size: 16;");

        row.getChildren().addAll(
                new Label("Operation:") {{
                    setStyle("-fx-font-size: 16;");
                }},
                opChoice,
                new Label("Input:") {{
                    setStyle("-fx-font-size: 16;");
                }},
                inputField,
                startBtn, pauseBtn, stepBtn, clearBtn,
                new Label("Speed:") {{
                    setStyle("-fx-font-size: 16;");
                }},
                speedSlider
        );

        moduleControls.getChildren().add(row);


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
                if (wasRunning) player.play();
            }
        });
    }
*/
    private void start() {
        if (!prepareEvents()) return;
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

        if (events == null || events.isEmpty()) return;
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
        if (vizArea != null) vizArea.getChildren().clear();
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
            if (s.length() > 10) s = s.substring(0, 10);
            buildReverseEvents(s, events);
        } else {
            String in = inputField.getText();
            if (in == null || in.trim().isEmpty()) return false;
            int n;
            try {
                n = Integer.parseInt(in.trim());
            } catch (NumberFormatException ex) {
                return false;
            }
            if (n < 0) return false;
            if (n > 20) n = 20;

            if (op.equals("Factorial")) buildFactorialEvents(n, events);
            else if (op.equals("Fibonacci")) {
                int idxForValue = findFibIndexForValue(n);
                if (idxForValue == -1) idxForValue = findPrevFibIndex(n);
                if (idxForValue >= 0) {
                    buildFibonacciEvents(idxForValue, events);
                    List<Long> seq = new ArrayList<>();
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
            } else break;
        }
        return lastIdx;
    }

    private void applyEvent(Event ev) {
        Platform.runLater(() -> {
            switch (ev.type) {
                case ENTER:
                    Label lbl = makeFrameLabel(ev.label);
                    lbl.setWrapText(true);//new

                    HBox.setHgrow(lbl, Priority.NEVER);
                    vizArea.getChildren().add(lbl);
                    break;
                case RETURN:
                    if (!vizArea.getChildren().isEmpty()) {
                        int lastIndex = vizArea.getChildren().size() - 1;
                        Label top = (Label) vizArea.getChildren().get(lastIndex);

                        top.setWrapText(true);

                        top.setText(top.getText() + " => " + ev.result);
                        top.setStyle(top.getStyle() + " -fx-background-color: lightgreen;");
                    }
                    break;
                case INFO:
                    vizArea.getChildren().clear();
                    if (ev.result instanceof java.util.List) {
                        @SuppressWarnings("unchecked")
                        java.util.List<Object> list = (java.util.List<Object>) ev.result;
                        HBox seqBox = new HBox(8);
                        seqBox.setPadding(new Insets(8));
                        for (Object o : list) {
                            Label v = new Label(String.valueOf(o));
                            v.setWrapText(true);
                            v.setStyle("-fx-font-size: 18; -fx-border-color:#444; -fx-padding:6; "
                                    + "-fx-background-color:lightblue; -fx-border-radius:4; -fx-background-radius:4;");
                            seqBox.getChildren().add(v);
                        }
                        vizArea.getChildren().add(seqBox);
                    } else {
                        Label info = new Label(String.valueOf(ev.result));
                        info.setWrapText(true);


                        info.setStyle("-fx-border-color:#444; -fx-padding:6; -fx-background-color:lightblue; "
                                + "-fx-border-radius:4; -fx-background-radius:4;");
                        vizArea.getChildren().add(info);
                    }
                    break;
            }
        });
    }


    private Label makeFrameLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font(18));
        l.setPadding(new Insets(8));
        l.setWrapText(true);
        l.setMaxWidth(Double.MAX_VALUE);
        l.setStyle("-fx-border-color: #333; -fx-background-color: lightyellow; "
                + "-fx-border-radius:4; -fx-background-radius:4;");
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

    private void showFinalAnswer() {
        vizArea.getChildren().clear();

        String op = opChoice.getValue();
        Event last = events.get(events.size() - 1);

        if (op.equals("Factorial") || op.equals("Reverse String")) {
            Label finalLbl = new Label("Final Answer: " + last.result);
            finalLbl.setStyle("-fx-font-size: 18; -fx-text-fill: navy; -fx-font-weight: bold; "
                    + "-fx-padding: 10; -fx-background-color: #e6f0ff; "
                    + "-fx-border-color:#333; -fx-border-radius:4; -fx-background-radius:4;");
            vizArea.getChildren().add(finalLbl);
        } else if (op.equals("Fibonacci")) {
            Event infoEvent = null;
            for (Event ev : events) {
                if (ev.type == EventType.INFO) {
                    infoEvent = ev;
                    break;
                }
            }
            if (infoEvent != null && infoEvent.result instanceof java.util.List) {
                @SuppressWarnings("unchecked")
                java.util.List<Object> list = (java.util.List<Object>) infoEvent.result;
                HBox seqBox = new HBox(8);
                seqBox.setPadding(new Insets(8));
                for (Object o : list) {
                    Label v = new Label(String.valueOf(o));
                    v.setStyle("-fx-font-size: 18; -fx-text-fill: navy; -fx-font-weight: bold; "
                            + "-fx-border-color:#444; -fx-padding:6; -fx-background-color:#e6f0ff; "
                            + "-fx-border-radius:4; -fx-background-radius:4;");
                    seqBox.getChildren().add(v);
                }
                vizArea.getChildren().add(seqBox);
            }
        }
    }

    private enum EventType {ENTER, RETURN, INFO}

    private static class Event {
        EventType type;
        String label;
        Object result;

        Event(EventType type, String label) {
            this.type = type;
            this.label = label;
        }

        Event(EventType type, String label, Object result) {
            this.type = type;
            this.label = label;
            this.result = result;
        }
    }
}
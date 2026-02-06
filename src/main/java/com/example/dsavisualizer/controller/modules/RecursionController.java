package com.example.dsavisualizer.controller.modules;

import com.example.dsavisualizer.controller.ModuleController;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
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

        if (pushBtn != null) pushBtn.setVisible(false);
        if (popBtn != null) popBtn.setVisible(false);
        if (pushField != null) pushField.setVisible(false);

        if (vizArea != null) {
            vizArea.setPrefSize(800, 400);
            vizArea.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            VBox.setVgrow(vizArea, Priority.ALWAYS);
            vizArea.setStyle("-fx-border-color: gray; " + "-fx-padding: 10; " + "-fx-background-color: white;");


        }
//        if (vizArea != null) {
//            vizArea.setPrefWidth(400);
//            vizArea.setMaxWidth(400); // 👉 width fixed 400px
//            vizArea.setPrefHeight(400); // default height
//            vizArea.setMaxHeight(Double.MAX_VALUE); // 👉 vertically expand করবে
//            //VBox.setVgrow(vizArea, Priority.ALWAYS); // parent VBox অনুযায়ী নিচ পর্যন্ত যাবে
//
//            vizArea.setStyle("-fx-border-color: gray; -fx-padding: 10; -fx-background-color: white;");
//        }


    }

    private void buildControls() {
        if (moduleControls == null) return;
        moduleControls.getChildren().clear();

        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        opChoice = new ChoiceBox<>();
        opChoice.getItems().addAll("Factorial", "Fibonacci", "Reverse String");
        opChoice.setValue("Factorial");
        opChoice.setStyle("-fx-font-size: 16; -fx-padding: 6 12;");

        inputField = new TextField();
        inputField.setPromptText("n or string");
        inputField.setPrefWidth(150);
        inputField.setStyle("-fx-font-size: 16; -fx-padding: 6 12;");

        startBtn = new Button("Start");
        startBtn.setStyle("-fx-font-size: 16; -fx-padding: 8 16;");
        pauseBtn = new Button("Pause");
        pauseBtn.setStyle("-fx-font-size: 16; -fx-padding: 8 16;");
        stepBtn = new Button("Step");
        stepBtn.setStyle("-fx-font-size: 16; -fx-padding: 8 16;");
        clearBtn = new Button("Clear");
        clearBtn.setStyle("-fx-font-size: 16; -fx-padding: 8 16;");

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

        startBtn.setOnAction(e -> start());
        pauseBtn.setOnAction(e -> pause());
        stepBtn.setOnAction(e -> step());
        clearBtn.setOnAction(e -> clear());

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
            Event lastEv = events.getLast();
            applyEvent(lastEv);
        }
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
                        Label top = (Label) vizArea.getChildren().getLast();

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
        Event last = events.getLast();

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

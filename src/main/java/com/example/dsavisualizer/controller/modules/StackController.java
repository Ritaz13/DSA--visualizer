package com.example.dsavisualizer.controller.modules;

import com.example.dsavisualizer.controller.ModuleController;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.Optional;

public class StackController extends ModuleController {

    @FXML private TextField inputField;
    @FXML private Button pushBtn;
    @FXML private Button popBtn;
    @FXML private Button peekBtn;
    @FXML private Button topBtn;
    @FXML private Button clearBtn;
    @FXML private VBox vizArea;
    @FXML private TextArea codeArea;
    @FXML private Button showCodeBtn;
    @FXML private Button copyCodeBtn;
    @FXML private TextArea storyArea;
    @FXML private TextField parenField;
    @FXML private Button checkParenBtn;
    @FXML private TextField convertField;
    @FXML private Button toPostfixBtn;
    @FXML private Button toPrefixBtn;
    @FXML private TextArea conversionResult;
    @FXML private TextArea stepResultArea;

    private final Stack<Integer> stack = new Stack<>();

    @Override
    protected void initialize() {
        super.initialize();
        titleLabel.setText("Stack");
        storyArea.setText("Stack is LIFO (Last In First Out).\nPush adds to top, Pop removes from top.\nThink of a stack of plates.");
        pushBtn.setOnAction(e -> pushElement());
        popBtn.setOnAction(e -> popElement());
        peekBtn.setOnAction(e -> peekElement());
        topBtn.setOnAction(e -> showTop());
        clearBtn.setOnAction(e -> clearStack());
        showCodeBtn.setOnAction(e -> toggleCode());
        copyCodeBtn.setOnAction(e -> copyCode());
        
        checkParenBtn.setOnAction(e -> checkParentheses());
        toPostfixBtn.setOnAction(e -> convertToPostfix());
        toPrefixBtn.setOnAction(e -> convertToPrefix());
        
        loadCodeFile();
    }

    protected void toggleCode() { codeArea.setVisible(!codeArea.isVisible()); }

    private void copyCode() {
        String code = codeArea.getText();
        javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
        javafx.scene.input.ClipboardContent cc = new javafx.scene.input.ClipboardContent();
        cc.putString(code);
        clipboard.setContent(cc);
        showAlert("Code copied to clipboard!");
    }

    private void loadCodeFile() {
        try (InputStream is = getClass().getResourceAsStream("/codes/stack.txt")) {
            if (is != null) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line).append('\n');
                    codeArea.setText(sb.toString());
                    codeArea.setVisible(false);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void pushElement() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;
        try {
            int val = Integer.parseInt(text);
            stack.push(val);
            renderStack();
            inputField.clear();
        } catch (NumberFormatException e) {
            showAlert("Enter a valid number");
        }
    }

    private void popElement() {
        if (stack.isEmpty()) {
            showAlert("Stack underflow");
            return;
        }
        stack.pop();
        renderStack();
    }

    private void peekElement() {
        if (stack.isEmpty()) {
            showAlert("Stack is empty");
            return;
        }
        showAlert("Top element: " + stack.peek());
    }

    private void showTop() { peekElement(); }

    private void clearStack() {
        stack.clear();
        renderStack();
    }

    private void renderStack() {
        vizArea.getChildren().clear();
        
        VBox stackBox = new VBox();
        stackBox.setSpacing(5);
        stackBox.setStyle("-fx-alignment: CENTER;");
        
        for (int i = stack.size() - 1; i >= 0; i--) {
            Label lbl = new Label(String.valueOf(stack.get(i)));
            lbl.setStyle("-fx-border-color:black; -fx-border-width:2; -fx-padding:15 20; -fx-background-color:#4CAF50; -fx-font-size:16; -fx-font-weight:bold; -fx-text-fill:white;");
            stackBox.getChildren().add(lbl);
        }
        
        vizArea.getChildren().add(stackBox);
    }

    protected void showAlert(String msg) {
        if (statusLabel != null) {
            statusLabel.setText(msg);
        }
    }
    private void checkParentheses() {
        String expr = parenField.getText().trim();
        if (expr.isEmpty()) {
            showAlert("Enter expression with parentheses");
            return;
        }

        Stack<Character> st = new Stack<>();
        List<Stack<Character>> snapshots = new ArrayList<>();
        List<String> stepDescriptions = new ArrayList<>();
        boolean valid = true;
        int step = 0;

        for (char c : expr.toCharArray()) {
            if (c == '(' || c == '[' || c == '{') {
                st.push(c);
                stepDescriptions.add("Step " + (++step) + ": Push '" + c + "' -> Stack: " + st);
            } else if (c == ')' || c == ']' || c == '}') {
                if (st.isEmpty()) {
                    valid = false;
                    stepDescriptions.add("Step " + (++step) + ": Error - Pop from empty stack");
                    break;
                }
                char top = st.pop();
                if ((c == ')' && top != '(') || (c == ']' && top != '[') || (c == '}' && top != '{')) {
                    valid = false;
                    stepDescriptions.add("Step " + (++step) + ": Error - Mismatch '" + top + "' with '" + c + "'");
                    break;
                }
                stepDescriptions.add("Step " + (++step) + ": Pop '" + top + "' (matches '" + c + "') -> Stack: " + st);
            }
            snapshots.add((Stack<Character>) st.clone());
        }

        if (!st.isEmpty()) {
            valid = false;
            stepDescriptions.add("Step " + (++step) + ": Error - Stack not empty at end");
        }

        stepDescriptions.add("Result: " + (valid ? "✓ Valid" : "✗ Invalid"));

        // Animate snapshots with step descriptions
        animateSnapshots(snapshots, stepDescriptions);

        showAlert(valid ? "✓ Valid Parentheses" : "✗ Invalid Parentheses");
    }private void convertToPostfix() {
        String expr = convertField.getText().trim();
        if (expr.isEmpty()) {
            showAlert("Enter infix expression");
            return;
        }

        Stack<Character> st = new Stack<>();
        List<Stack<Character>> snapshots = new ArrayList<>();
        List<String> stepDescriptions = new ArrayList<>();
        StringBuilder result = new StringBuilder();
        int step = 0;

        for (char c : expr.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                result.append(c);
                stepDescriptions.add("Step " + (++step) + ": Operand '" + c + "' -> Output: " + result);
            } else if (c == '(') {
                st.push(c);
                stepDescriptions.add("Step " + (++step) + ": Push '(' -> Stack: " + st);
            } else if (c == ')') {
                while (!st.isEmpty() && st.peek() != '(') {
                    result.append(st.pop());
                }
                if (!st.isEmpty()) st.pop();
                stepDescriptions.add("Step " + (++step) + ": Pop until '(' -> Output: " + result + ", Stack: " + st);
            } else {
                while (!st.isEmpty() && getPrecedence(st.peek()) >= getPrecedence(c)) {
                    result.append(st.pop());
                }
                st.push(c);
                stepDescriptions.add("Step " + (++step) + ": Push '" + c + "' -> Output: " + result + ", Stack: " + st);
            }
            snapshots.add((Stack<Character>) st.clone());
        }

        while (!st.isEmpty()) {
            result.append(st.pop());
            snapshots.add((Stack<Character>) st.clone());
        }
        stepDescriptions.add("Step " + (++step) + ": Pop remaining -> Output: " + result);
        stepDescriptions.add("Final Postfix: " + result);

        animateSnapshots(snapshots, stepDescriptions);
    }
    private void convertToPrefix() {
        String expr = convertField.getText().trim();
        if (expr.isEmpty()) {
            showAlert("Enter infix expression");
            return;
        }

        // Reverse input and swap parentheses
        String reversed = new StringBuilder(expr)
                .reverse()
                .toString()
                .replace('(', '\u0001')
                .replace(')', '(')
                .replace('\u0001', ')');

        Stack<Character> st = new Stack<>();
        List<Stack<Character>> snapshots = new ArrayList<>();
        List<String> stepDescriptions = new ArrayList<>();
        StringBuilder result = new StringBuilder();
        stepDescriptions.add("Step 1: Reverse input: " + reversed);

        int step = 2;
        for (char c : reversed.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                result.append(c);
                stepDescriptions.add("Step " + (step++) + ": Operand '" + c + "' -> Output: " + result);
            } else if (c == '(') {
                st.push(c);
                stepDescriptions.add("Step " + (step++) + ": Push '(' -> Stack: " + st);
            } else if (c == ')') {
                while (!st.isEmpty() && st.peek() != '(') {
                    result.append(st.pop());
                }
                if (!st.isEmpty()) st.pop();
                stepDescriptions.add("Step " + (step++) + ": Pop until '(' -> Output: " + result + ", Stack: " + st);
            } else {
                while (!st.isEmpty() && getPrecedence(st.peek()) >= getPrecedence(c)) {
                    result.append(st.pop());
                }
                st.push(c);
                stepDescriptions.add("Step " + (step++) + ": Push '" + c + "' -> Output: " + result + ", Stack: " + st);
            }
            snapshots.add((Stack<Character>) st.clone());
        }

        while (!st.isEmpty()) {
            result.append(st.pop());
            snapshots.add((Stack<Character>) st.clone());
        }

        String prefix = new StringBuilder(result).reverse().toString();
        stepDescriptions.add("Step " + (step++) + ": Reverse result");
        stepDescriptions.add("Final Prefix: " + prefix);

        animateSnapshots(snapshots, stepDescriptions);
    }
    private void animateSnapshots(List<Stack<Character>> snapshots, List<String> stepDescriptions) {
        vizArea.getChildren().clear();
        Timeline timeline = new Timeline();
        int index = 0;

        for (int i = 0; i < snapshots.size(); i++) {
            final Stack<Character> snapshot = (Stack<Character>) snapshots.get(i).clone();
            final String stepText = stepDescriptions.get(i);

            KeyFrame frame = new KeyFrame(Duration.seconds(index + 1), e -> {
                renderCharStack(snapshot, stepText);
            });
            timeline.getKeyFrames().add(frame);
            index++;
        }

        timeline.setCycleCount(1);
        timeline.play();
    }

    private void renderCharStack(Stack<Character> st, String stepText) {
        vizArea.getChildren().clear();

        HBox container = new HBox(30); // stack + step text side by side
        container.setStyle("-fx-alignment: CENTER;"); // ✅
        // Stack visualization (vertical)
        VBox stackBox = new VBox();
        stackBox.setSpacing(10);
        stackBox.setStyle("-fx-alignment: CENTER;");

        for (int i = st.size() - 1; i >= 0; i--) {
            Label lbl = new Label(String.valueOf(st.get(i)));
            lbl.setStyle(
                    "-fx-border-color:black; -fx-border-width:2; " +
                            "-fx-padding:15 20; -fx-background-color:#2196F3; " +
                            "-fx-font-size:16; -fx-font-weight:bold; -fx-text-fill:white;"
            );
            stackBox.getChildren().add(lbl);
        }

        // Step description
        Label stepLabel = new Label(stepText);
        stepLabel.setWrapText(true);
        stepLabel.setStyle("-fx-font-size: 20; -fx-text-fill: darkblue; -fx-padding: 10;");

        container.getChildren().addAll(stackBox, stepLabel);
        vizArea.getChildren().add(container);
        vizArea.setStyle("-fx-alignment: CENTER;");
    }




    // Parentheses matching

    private int getPrecedence(char c) {
        if (c == '+' || c == '-') return 1;
        if (c == '*' || c == '/') return 2;
        if (c == '^') return 3;
        return 0;
    }
}
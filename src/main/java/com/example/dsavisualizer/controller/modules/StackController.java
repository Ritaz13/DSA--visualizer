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
        for (int i = stack.size() - 1; i >= 0; i--) {
            Label lbl = new Label(String.valueOf(stack.get(i)));
            lbl.setStyle("-fx-border-color:black; -fx-border-width:2; -fx-padding:10; -fx-background-color:#ffcccc; -fx-font-size:14; -fx-font-weight:bold;");
            vizArea.getChildren().add(lbl);
        }
    }

    protected void showAlert(String msg) {
        if (statusLabel != null) {
            statusLabel.setText(msg);
        }
    }
    
    // Parentheses matching
    private void checkParentheses() {
        String expr = parenField.getText().trim();
        if (expr.isEmpty()) {
            showAlert("Enter expression with parentheses");
            return;
        }
        
        Stack<Character> st = new Stack<>();
        StringBuilder steps = new StringBuilder("Step-by-step:\n\n");
        boolean valid = true;
        int step = 0;
        
        for (char c : expr.toCharArray()) {
            if (c == '(' || c == '[' || c == '{') {
                st.push(c);
                steps.append("Step ").append(++step).append(": Push '").append(c).append("' -> Stack: ").append(st).append("\n");
            } else if (c == ')' || c == ']' || c == '}') {
                if (st.isEmpty()) {
                    valid = false;
                    steps.append("Step ").append(++step).append(": Error - Pop from empty stack\n");
                    break;
                }
                char top = st.pop();
                if ((c == ')' && top != '(') || 
                    (c == ']' && top != '[') || 
                    (c == '}' && top != '{')) {
                    valid = false;
                    steps.append("Step ").append(++step).append(": Error - Mismatch '").append(top).append("' with '").append(c).append("'\n");
                    break;
                }
                steps.append("Step ").append(++step).append(": Pop '").append(top).append("' (matches '").append(c).append("') -> Stack: ").append(st).append("\n");
            }
        }
        
        if (!st.isEmpty()) {
            valid = false;
            steps.append("Step ").append(++step).append(": Error - Stack not empty at end\n");
        }
        
        steps.append("\nResult: ").append(valid ? "✓ Valid" : "✗ Invalid");
        if (conversionResult != null) {
            conversionResult.setText(steps.toString());
        }
        showAlert(valid ? "✓ Valid Parentheses" : "✗ Invalid Parentheses");
    }
    
    // Infix to Postfix conversion
    private void convertToPostfix() {
        String expr = convertField.getText().trim();
        if (expr.isEmpty()) {
            showAlert("Enter infix expression");
            return;
        }
        
        String postfix = infixToPostfixWithSteps(expr);
        if (conversionResult != null) {
            conversionResult.setText(postfix);
        }
    }
    
    // Infix to Prefix conversion
    private void convertToPrefix() {
        String expr = convertField.getText().trim();
        if (expr.isEmpty()) {
            showAlert("Enter infix expression");
            return;
        }
        
        String prefix = infixToPrefixWithSteps(expr);
        if (conversionResult != null) {
            conversionResult.setText(prefix);
        }
    }
    
    private String infixToPostfixWithSteps(String expr) {
        Stack<Character> st = new Stack<>();
        StringBuilder result = new StringBuilder();
        StringBuilder steps = new StringBuilder("Step-by-step conversion:\n\n");
        int step = 0;
        
        for (char c : expr.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                result.append(c);
                steps.append("Step ").append(++step).append(": '").append(c).append("' is operand -> Output: ").append(result).append("\n");
            } else if (c == '(') {
                st.push(c);
                steps.append("Step ").append(++step).append(": Push '").append(c).append("' -> Stack: ").append(st).append("\n");
            } else if (c == ')') {
                while (!st.isEmpty() && st.peek() != '(') {
                    result.append(st.pop());
                }
                if (!st.isEmpty()) st.pop();
                steps.append("Step ").append(++step).append(": Pop until '(' -> Output: ").append(result).append(", Stack: ").append(st).append("\n");
            } else {
                while (!st.isEmpty() && getPrecedence(st.peek()) >= getPrecedence(c)) {
                    result.append(st.pop());
                }
                st.push(c);
                steps.append("Step ").append(++step).append(": Push '").append(c).append("' -> Output: ").append(result).append(", Stack: ").append(st).append("\n");
            }
        }
        
        while (!st.isEmpty()) {
            result.append(st.pop());
        }
        steps.append("Step ").append(++step).append(": Pop remaining -> Output: ").append(result).append("\n");
        steps.append("\nFinal Postfix: ").append(result);
        
        return steps.toString();
    }
    
    private String infixToPrefixWithSteps(String expr) {
        Stack<Character> st = new Stack<>();
        StringBuilder result = new StringBuilder();
        StringBuilder steps = new StringBuilder("Step-by-step conversion (Infix to Prefix):\n\n");
        
        // Reverse and convert
        String reversed = new StringBuilder(expr)
            .reverse()
            .toString()
            .replace('(', '\u0001')
            .replace(')', '(')
            .replace('\u0001', ')');
        
        steps.append("Step 1: Reverse input: ").append(reversed).append("\n");
        steps.append("Step 2: Convert to postfix...\n");
        
        int step = 3;
        Stack<Character> st2 = new Stack<>();
        
        for (char c : reversed.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                result.append(c);
                steps.append("Step ").append(step++).append(": '").append(c).append("' is operand -> Output: ").append(result).append("\n");
            } else if (c == '(') {
                st2.push(c);
                steps.append("Step ").append(step++).append(": Push '").append(c).append("' -> Stack: ").append(st2).append("\n");
            } else if (c == ')') {
                while (!st2.isEmpty() && st2.peek() != '(') {
                    result.append(st2.pop());
                }
                if (!st2.isEmpty()) st2.pop();
                steps.append("Step ").append(step++).append(": Pop until '(' -> Output: ").append(result).append("\n");
            } else {
                while (!st2.isEmpty() && getPrecedence(st2.peek()) >= getPrecedence(c)) {
                    result.append(st2.pop());
                }
                st2.push(c);
                steps.append("Step ").append(step++).append(": Push '").append(c).append("' -> Stack: ").append(st2).append("\n");
            }
        }
        
        while (!st2.isEmpty()) {
            result.append(st2.pop());
        }
        
        String prefix = new StringBuilder(result).reverse().toString();
        steps.append("Step ").append(step).append(": Reverse result\n\n");
        steps.append("Final Prefix: ").append(prefix);
        
        return steps.toString();
    }
    private String infixToPostfix(String expr) {
        Stack<Character> st = new Stack<>();
        StringBuilder result = new StringBuilder();
        
        for (char c : expr.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                result.append(c);
            } else if (c == '(') {
                st.push(c);
            } else if (c == ')') {
                while (!st.isEmpty() && st.peek() != '(') {
                    result.append(st.pop());
                }
                if (!st.isEmpty()) st.pop();
            } else {
                while (!st.isEmpty() && getPrecedence(st.peek()) >= getPrecedence(c)) {
                    result.append(st.pop());
                }
                st.push(c);
            }
        }
        
        while (!st.isEmpty()) {
            result.append(st.pop());
        }
        
        return result.toString();
    }
    
    private String infixToPrefix(String expr) {
        // Reverse and convert ) to ( and vice versa
        String reversed = new StringBuilder(expr)
            .reverse()
            .toString()
            .replace('(', '\u0001')
            .replace(')', '(')
            .replace('\u0001', ')');
        
        String postfix = infixToPostfix(reversed);
        return new StringBuilder(postfix).reverse().toString();
    }
    
    private int getPrecedence(char c) {
        if (c == '+' || c == '-') return 1;
        if (c == '*' || c == '/') return 2;
        if (c == '^') return 3;
        return 0;
    }
}
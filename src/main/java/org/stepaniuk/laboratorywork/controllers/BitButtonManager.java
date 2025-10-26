package org.stepaniuk.laboratorywork.controllers;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

import java.util.*;

/**
 * Керує створенням, відображенням та інтерактивністю
 * динамічного списку кнопок-бітів.
 */
public class BitButtonManager {

    private final HBox codeWordBox;
    private final Runnable onAnalysisCallback; // Метод, який треба викликати (напр. handleAnalysis)
    private final List<Button> codeWordButtons = new ArrayList<>();

    private Map<Integer, List<Integer>> bitRelationships = new HashMap<>();

    /**
     * @param codeWordBox      Контейнер HBox з FXML, куди додавати кнопки.
     * @param onAnalysisCallback Метод, який буде викликано після зміни біта (lambda `this::handleAnalysis`).
     */
    public BitButtonManager(HBox codeWordBox, Runnable onAnalysisCallback) {
        this.codeWordBox = codeWordBox;
        this.onAnalysisCallback = onAnalysisCallback;
    }

    /**
     * Встановлює нову мапу зв'язків для логіки підсвічування.
     */
    public void setRelationships(Map<Integer, List<Integer>> relationships) {
        this.bitRelationships = (relationships != null) ? relationships : Collections.emptyMap();
    }

    /**
     * Повністю очищує та перебудовує HBox з кнопками для бітів.
     */
    public void buildCodeWordUI(int bitCount) {
        codeWordButtons.clear();
        codeWordBox.getChildren().clear();

        for (int i = 0; i < bitCount; i++) {
            Button bitButton = createBitButton(i);
            codeWordButtons.add(bitButton);
            codeWordBox.getChildren().add(bitButton);
        }
    }

    /**
     * Створює стилізовану кнопку для біта.
     */
    private Button createBitButton(int index) {
        Button bitButton = new Button("0");
        bitButton.getStyleClass().add("bit-button");

        // Обробник кліку (для симуляції помилки)
        bitButton.setOnAction(e -> handleBitToggle(bitButton));
        // Обробники наведення миші
        bitButton.setOnMouseEntered(e -> handleBitHover(index));
        bitButton.setOnMouseExited(e -> clearBitHighlights());

        return bitButton;
    }

    /**
     * Оновлює текст на кнопках-бітах.
     */
    public void displayEncodedWord(String encodedWord) {
        for (int i = 0; i < encodedWord.length() && i < codeWordButtons.size(); i++) {
            codeWordButtons.get(i).setText(String.valueOf(encodedWord.charAt(i)));
        }
    }

    /**
     * Повертає поточне слово, зчитане з кнопок.
     */
    public String getCurrentCodeWord() {
        StringBuilder word = new StringBuilder();
        codeWordButtons.forEach(btn -> word.append(btn.getText()));
        return word.toString();
    }

    // --- Логіка обробки подій кнопок ---

    private void handleBitToggle(Button bitButton) {
        bitButton.setText(bitButton.getText().equals("0") ? "1" : "0");
        // Викликаємо зворотний зв'язок до MainController
        if (onAnalysisCallback != null) {
            onAnalysisCallback.run();
        }
    }

    private void handleBitHover(int index) {
        Set<Integer> indicesToHighlight = collectRelatedBitIndices(index);
        applyHighlightToBits(indicesToHighlight);
    }

    private void clearBitHighlights() {
        codeWordButtons.forEach(btn -> btn.getStyleClass().remove("highlight-bit"));
    }

    private Set<Integer> collectRelatedBitIndices(int index) {
        Set<Integer> indices = new HashSet<>();
        // Підсвічуємо групу контрольного біта
        if (bitRelationships.containsKey(index)) {
            indices.addAll(bitRelationships.get(index));
        }
        // Підсвічуємо всі групи, що містять цей біт даних
        bitRelationships.values().stream()
                .filter(group -> group.contains(index))
                .forEach(indices::addAll);
        return indices;
    }

    // --- Логіка стилізації кнопок ---

    private void applyHighlightToBits(Set<Integer> indices) {
        indices.forEach(i -> {
            if (i < codeWordButtons.size()) {
                codeWordButtons.get(i).getStyleClass().add("highlight-bit");
            }
        });
    }

    public void highlightErrors(String current, String corrected) {
        for (int i = 0; i < codeWordButtons.size(); i++) {
            Button btn = codeWordButtons.get(i);
            boolean hasError = i < current.length() && i < corrected.length()
                    && current.charAt(i) != corrected.charAt(i);
            updateButtonErrorState(btn, hasError);
        }
    }

    public void clearErrorHighlights() {
        codeWordButtons.forEach(btn -> btn.getStyleClass().remove("error-bit"));
    }

    private void updateButtonErrorState(Button button, boolean hasError) {
        if (hasError) {
            if (!button.getStyleClass().contains("error-bit")) {
                button.getStyleClass().add("error-bit");
            }
        } else {
            button.getStyleClass().remove("error-bit");
        }
    }
}

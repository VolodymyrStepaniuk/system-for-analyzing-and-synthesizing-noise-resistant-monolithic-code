package org.stepaniuk.laboratorywork.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.stepaniuk.laboratorywork.algorithms.IErrorCorrectionCode;
import org.stepaniuk.laboratorywork.algorithms.hamming.DynamicHammingCode;
import org.stepaniuk.laboratorywork.algorithms.hamming.HammingCode;
import org.stepaniuk.laboratorywork.algorithms.hamming.HammingGeneratorPlaceholder;
import org.stepaniuk.laboratorywork.algorithms.repetition.RepetitionCode;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    private static final int MAX_K_VALUE = 32;

    // --- FXML Поля ---
    @FXML private ComboBox<IErrorCorrectionCode> algorithmSelector;
    @FXML private TextField inputField;
    @FXML private Button encodeButton;
    @FXML private HBox codeWordBox;
    @FXML private Label statusLabel;
    @FXML private Label correctedDataLabel;
    @FXML private Label correctedWordLabel;
    @FXML private VBox mainControlsBox;
    @FXML private HBox generatorBox;
    @FXML private TextField kInput;
    @FXML private Button generateButton;

    // --- Внутрішні поля ---
    private IErrorCorrectionCode currentAlgorithm;
    private BitButtonManager bitButtonManager;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Створюємо менеджер, передаємо йому HBox та метод,
        // який потрібно викликати після кліку на біт (this::handleAnalysis)
        bitButtonManager = new BitButtonManager(codeWordBox, this::handleAnalysis);

        initializeAlgorithmSelector();
        algorithmSelector.setOnAction(e -> onAlgorithmSelected());
        algorithmSelector.getSelectionModel().selectFirst();
        onAlgorithmSelected();
    }

    private void initializeAlgorithmSelector() {
        algorithmSelector.getItems().addAll(
                new HammingCode(),
                new RepetitionCode(),
                new HammingGeneratorPlaceholder()
        );
        algorithmSelector.setConverter(new StringConverter<>() {
            @Override public String toString(IErrorCorrectionCode code) { return (code == null) ? "" : code.getName(); }
            @Override public IErrorCorrectionCode fromString(String string) { return null; }
        });
    }

    // --- 1. Керування станом (Вибір режиму) ---

    private void onAlgorithmSelected() {
        IErrorCorrectionCode selected = algorithmSelector.getValue();
        if (selected == null) return;

        if (isGeneratorMode(selected)) {
            switchToGeneratorMode();
        } else {
            switchToAlgorithmMode(selected);
        }
    }

    private boolean isGeneratorMode(IErrorCorrectionCode algorithm) {
        return algorithm instanceof HammingGeneratorPlaceholder;
    }

    private void switchToGeneratorMode() {
        setUIVisibility(false, true);
        currentAlgorithm = null;
        clearResults();
    }

    private void switchToAlgorithmMode(IErrorCorrectionCode algorithm) {
        setUIVisibility(true, false);
        setCurrentAlgorithm(algorithm);
    }

    private void setUIVisibility(boolean showMainControls, boolean showGenerator) {
        mainControlsBox.setVisible(showMainControls);
        mainControlsBox.setManaged(showMainControls);
        generatorBox.setVisible(showGenerator);
        generatorBox.setManaged(showGenerator);
    }

    private void setCurrentAlgorithm(IErrorCorrectionCode algorithm) {
        currentAlgorithm = algorithm;
        // Передаємо нові налаштування менеджеру бітів
        bitButtonManager.setRelationships(algorithm.getBitRelationships());
        bitButtonManager.buildCodeWordUI(algorithm.getCodeWordLength());

        updateInputFieldPrompt();
        clearResults();
    }

    // --- 2. Обробники подій (Event Handlers) ---

    @FXML
    protected void handleGenerate() {
        try {
            int k = parseAndValidateKValue();
            DynamicHammingCode dynamicCode = new DynamicHammingCode(k);

            setCurrentAlgorithm(dynamicCode);
            setUIVisibility(true, false);
            inputField.setText("");
            algorithmSelector.getSelectionModel().selectLast();

        } catch (NumberFormatException e) {
            showAlert("Помилка генерації", "Будь ласка, введіть коректне число 'k'.");
        } catch (Exception e) {
            showAlert("Помилка генерації", e.getMessage());
        }
    }

    @FXML
    protected void handleEncode() {
        if (currentAlgorithm == null) return;
        String dataWord = inputField.getText();

        if (!isValidDataWord(dataWord)) {
            showInvalidInputAlert();
            return;
        }
        encodeAndDisplay(dataWord);
    }

    // --- 3. Основна логіка (Core Logic) ---

    private void encodeAndDisplay(String dataWord) {
        try {
            String encodedWord = currentAlgorithm.encode(dataWord);
            bitButtonManager.displayEncodedWord(encodedWord); // Делегуємо
            handleAnalysis();
        } catch (Exception ex) {
            showAlert("Помилка кодування", ex.getMessage());
        }
    }

    private void handleAnalysis() {
        if (currentAlgorithm == null) return;

        String currentWord = bitButtonManager.getCurrentCodeWord(); // Делегуємо
        IErrorCorrectionCode.CodeCheckResult result = currentAlgorithm.checkAndCorrect(currentWord);

        displayAnalysisResults(result);
        bitButtonManager.highlightErrors(currentWord, result.correctedWord()); // Делегуємо
    }

    // --- 4. Оновлення UI та Валідація ---

    private void displayAnalysisResults(IErrorCorrectionCode.CodeCheckResult result) {
        statusLabel.setText(result.status());
        correctedDataLabel.setText(result.extractedData());
        correctedWordLabel.setText(result.correctedWord());
    }

    private void clearResults() {
        statusLabel.setText("...");
        correctedDataLabel.setText("...");
        correctedWordLabel.setText("...");
        bitButtonManager.clearErrorHighlights(); // Делегуємо
    }

    private void updateInputFieldPrompt() {
        inputField.setPromptText(currentAlgorithm.getDataWordLength() + " біт(а)");
    }

    private int parseAndValidateKValue() {
        int k = Integer.parseInt(kInput.getText());
        if (k <= 0) throw new IllegalArgumentException("Кількість інформаційних біт 'k' має бути > 0.");
        if (k > MAX_K_VALUE) throw new IllegalArgumentException("Значення 'k' занадто велике (макс. " + MAX_K_VALUE + ").");
        return k;
    }

    private boolean isValidDataWord(String dataWord) {
        return dataWord.length() == currentAlgorithm.getDataWordLength()
                && dataWord.matches("[01]+");
    }

    private void showInvalidInputAlert() {
        showAlert("Помилка вводу",
                "Інформаційне слово для '" + currentAlgorithm.getName() +
                        "' повинно складатися рівно з " + currentAlgorithm.getDataWordLength() + " бітів.");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        styleAlert(alert);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void styleAlert(Alert alert) {
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/org/stepaniuk/laboratorywork/styles.css").toExternalForm());
        dialogPane.getStyleClass().add("main-container");
    }
}

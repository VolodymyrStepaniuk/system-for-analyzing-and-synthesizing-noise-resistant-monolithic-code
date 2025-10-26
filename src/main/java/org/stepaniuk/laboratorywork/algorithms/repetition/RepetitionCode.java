package org.stepaniuk.laboratorywork.algorithms.repetition;

import org.stepaniuk.laboratorywork.algorithms.IErrorCorrectionCode;

import java.util.List;
import java.util.Map;

public class RepetitionCode implements IErrorCorrectionCode {

    @Override
    public String getName() {
        return "Код з потрійним повторенням";
    }

    @Override
    public int getDataWordLength() {
        return 4;
    }

    @Override
    public int getCodeWordLength() {
        return 12;
    }

    @Override
    public String encode(String dataWord) {
        if (dataWord == null || dataWord.length() != getDataWordLength() || !dataWord.matches("[01]+")) {
            throw new IllegalArgumentException("Інформаційне слово повинно складатися з 4 бітів.");
        }
        StringBuilder sb = new StringBuilder();
        for (char c : dataWord.toCharArray()) {
            sb.append(c).append(c).append(c); // Повторюємо кожен біт тричі
        }
        return sb.toString();
    }

    @Override
    public CodeCheckResult checkAndCorrect(String potentiallyCorruptedWord) {
        StringBuilder correctedData = new StringBuilder();
        StringBuilder correctedWord = new StringBuilder();
        int errorsFound = 0;

        // Аналізуємо слово блоками по 3 біти
        for (int i = 0; i < getCodeWordLength(); i += 3) {
            String block = potentiallyCorruptedWord.substring(i, i + 3);

            // "Мажоритарне голосування"
            int ones = 0;
            for(char c : block.toCharArray()) {
                if(c == '1') ones++;
            }

            char correctedBit = (ones >= 2) ? '1' : '0';
            correctedData.append(correctedBit);
            correctedWord.append(correctedBit).append(correctedBit).append(correctedBit);

            // Підрахунок помилок для статусу
            for(char c : block.toCharArray()){
                if(c != correctedBit) errorsFound++;
            }
        }

        String status;
        if (errorsFound == 0) {
            status = "Помилок немає";
        } else if (errorsFound == 1) {
            status = "Виявлено та виправлено 1 помилку";
        } else {
            status = String.format("Виявлено та виправлено %d помилок", errorsFound);
        }

        return new CodeCheckResult(status, correctedWord.toString(), correctedData.toString());
    }

    /**
     * Тут "групи" - це просто трійки бітів.
     * Ключем виступає індекс першого біта в групі.
     */
    @Override
    public Map<Integer, List<Integer>> getBitRelationships() {
        return Map.of(
                0, List.of(0, 1, 2),    // 1-й блок
                3, List.of(3, 4, 5),    // 2-й блок
                6, List.of(6, 7, 8),    // 3-й блок
                9, List.of(9, 10, 11)   // 4-й блок
        );
    }
}

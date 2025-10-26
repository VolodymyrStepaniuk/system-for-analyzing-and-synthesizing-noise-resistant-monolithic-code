package org.stepaniuk.laboratorywork.algorithms.hamming;

import org.stepaniuk.laboratorywork.algorithms.IErrorCorrectionCode;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Реалізація коду Гемінга.
 */
public class HammingCode implements IErrorCorrectionCode {

    @Override
    public String getName() {
        return "Код Гемінга";
    }

    @Override
    public int getDataWordLength() {
        return 4;
    }

    @Override
    public int getCodeWordLength() {
        return 7;
    }

    @Override
    public String encode(String dataWord) {
        if (dataWord == null || dataWord.length() != getDataWordLength() || !dataWord.matches("[01]+")) {
            throw new IllegalArgumentException("Інформаційне слово повинно складатися з 4 бітів.");
        }

        int[] dataBits = dataWord.chars().map(c -> c - '0').toArray();
        int d1 = dataBits[0]; // Позиція 3
        int d2 = dataBits[1]; // Позиція 5
        int d3 = dataBits[2]; // Позиція 6
        int d4 = dataBits[3]; // Позиція 7

        int p1 = (d1 + d2 + d4) % 2; // 1, 3, 5, 7
        int p2 = (d1 + d3 + d4) % 2; // 2, 3, 6, 7
        int p3 = (d2 + d3 + d4) % 2; // 4, 5, 6, 7

        int[] codeWord = {p1, p2, d1, p3, d2, d3, d4};
        return IntStream.of(codeWord)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining());
    }

    @Override
    public CodeCheckResult checkAndCorrect(String potentiallyCorruptedWord) {
        int[] bits = potentiallyCorruptedWord.chars().map(c -> c - '0').toArray();
        int p1 = bits[0], p2 = bits[1], d1 = bits[2], p3 = bits[3], d2 = bits[4], d3 = bits[5], d4 = bits[6];

        int s1 = (p1 + d1 + d2 + d4) % 2;
        int s2 = (p2 + d1 + d3 + d4) % 2;
        int s3 = (p3 + d2 + d3 + d4) % 2;

        int errorPosition = s3 * 4 + s2 * 2 + s1;
        String status;
        String correctedWordStr;

        if (errorPosition == 0) {
            status = "Помилок немає";
            correctedWordStr = potentiallyCorruptedWord;
        } else {
            status = "Помилка на позиції " + errorPosition;
            char[] correctedChars = potentiallyCorruptedWord.toCharArray();
            int errorIndex = errorPosition - 1;
            correctedChars[errorIndex] = (correctedChars[errorIndex] == '0' ? '1' : '0');
            correctedWordStr = new String(correctedChars);
        }

        // Витягуємо виправлені дані
        char[] finalBits = correctedWordStr.toCharArray();
        String extractedData = "" + finalBits[2] + finalBits[4] + finalBits[5] + finalBits[6];

        return new CodeCheckResult(status, correctedWordStr, extractedData);
    }

    /**
     * p1 (індекс 0) перевіряє біти 1, 3, 5, 7 (індекси 0, 2, 4, 6)
     * p2 (індекс 1) перевіряє біти 2, 3, 6, 7 (індекси 1, 2, 5, 6)
     * p3 (індекс 3) перевіряє біти 4, 5, 6, 7 (індекси 3, 4, 5, 6)
     */
    @Override
    public Map<Integer, List<Integer>> getBitRelationships() {
        return Map.of(
                0, List.of(0, 2, 4, 6), // Група S1 (p1, d1, d2, d4)
                1, List.of(1, 2, 5, 6), // Група S2 (p2, d1, d3, d4)
                3, List.of(3, 4, 5, 6)  // Група S3 (p3, d2, d3, d4)
        );
    }

}

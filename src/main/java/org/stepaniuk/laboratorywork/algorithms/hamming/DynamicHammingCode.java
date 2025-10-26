package org.stepaniuk.laboratorywork.algorithms.hamming;

import org.stepaniuk.laboratorywork.algorithms.IErrorCorrectionCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Динамічна реалізація коду Гемінга для будь-якої
 * кількості інформаційних біт 'k'.
 */
public class DynamicHammingCode implements IErrorCorrectionCode {

    private final int k; // Кількість інформаційних біт
    private final int r; // Кількість контрольних біт
    private final int n; // Загальна довжина (n = k + r)
    private final Map<Integer, List<Integer>> relationships;

    /**
     * Конструктор, що ініціалізує та обчислює всі параметри.
     * @param k Кількість інформаційних біт.
     */
    public DynamicHammingCode(int k) {
        if (k <= 0) {
            throw new IllegalArgumentException("Кількість біт 'k' має бути > 0");
        }
        if (k > 32) {
            throw new IllegalArgumentException("Значення 'k' занадто велике (макс. 32).");
        }

        this.k = k;
        this.r = calculateR(k);
        this.n = k + r;
        this.relationships = buildRelationships();
    }

    /**
     * Обчислює 'r' за формулою 2^r >= k + r + 1
     */
    private int calculateR(int k) {
        int r = 0;
        while ((1 << r) < (k + r + 1)) {
            r++;
        }
        return r;
    }

    /**
     * Динамічно будує мапу зв'язків для getBitRelationships()
     */
    private Map<Integer, List<Integer>> buildRelationships() {
        Map<Integer, List<Integer>> rel = new HashMap<>();
        for (int i = 0; i < r; i++) {
            int parityPos = (1 << i); // Позиція 1, 2, 4, 8...
            int parityIndex = parityPos - 1; // Індекс 0, 1, 3, 7...

            List<Integer> group = new ArrayList<>();
            for (int j = 1; j <= n; j++) {
                // Перевіряємо, чи j-та позиція має '1' в i-му біті
                if (((j >> i) & 1) == 1) {
                    group.add(j - 1); // Додаємо 0-індексовану позицію
                }
            }
            rel.put(parityIndex, group);
        }
        return rel;
    }

    @Override
    public String getName() {
        return String.format("Гемінг (%d, %d)", n, k);
    }

    @Override
    public int getDataWordLength() { return k; }

    @Override
    public int getCodeWordLength() { return n; }

    @Override
    public Map<Integer, List<Integer>> getBitRelationships() {
        return relationships;
    }

    @Override
    public String encode(String dataWord) {
        if (dataWord.length() != k) {
            throw new IllegalArgumentException("Невірна довжина даних");
        }

        int[] codeWord = new int[n];
        char[] data = dataWord.toCharArray();
        int dataIdx = 0;
        int parityIdx = 0;

        // 1. Розставляємо біти даних, залишаючи '0' на контрольних позиціях
        for (int i = 0; i < n; i++) {
            if (i == ((1 << parityIdx) - 1)) {
                codeWord[i] = 0; // Контрольний біт (поки 0)
                parityIdx++;
            } else {
                codeWord[i] = data[dataIdx] - '0';
                dataIdx++;
            }
        }

        // 2. Обчислюємо контрольні біти
        for (int pIndex : relationships.keySet()) {
            int xorSum = 0;
            for (int bitIndex : relationships.get(pIndex)) {
                if (bitIndex != pIndex) { // Не враховуємо сам контрольний біт
                    xorSum ^= codeWord[bitIndex];
                }
            }
            codeWord[pIndex] = xorSum;
        }

        StringBuilder sb = new StringBuilder(n);
        for (int bit : codeWord) {
            sb.append(bit);
        }
        return sb.toString();
    }

    @Override
    public CodeCheckResult checkAndCorrect(String potentiallyCorruptedWord) {
        int[] bits = potentiallyCorruptedWord.chars().map(c -> c - '0').toArray();
        int syndrome = 0;

        // Обчислюємо синдром
        List<Integer> pIndices = new ArrayList<>(relationships.keySet());
        Collections.sort(pIndices); // Важливо: 0, 1, 3, 7...

        int pBitPower = 0;
        for (int pIndex : pIndices) {
            int xorSum = 0;
            for (int bitIndex : relationships.get(pIndex)) {
                xorSum ^= bits[bitIndex];
            }
            if (xorSum != 0) {
                syndrome += (1 << pBitPower);
            }
            pBitPower++;
        }

        int errorPosition = syndrome;
        String status;
        String correctedWordStr;
        char[] correctedChars = potentiallyCorruptedWord.toCharArray();

        if (errorPosition == 0) {
            status = "Помилок немає";
            correctedWordStr = potentiallyCorruptedWord;
        } else {
            status = "Помилка на позиції " + errorPosition;
            int errorIndex = errorPosition - 1;
            if (errorIndex < correctedChars.length) {
                correctedChars[errorIndex] = (correctedChars[errorIndex] == '0' ? '1' : '0');
            }
            correctedWordStr = new String(correctedChars);
        }

        // Витягуємо виправлені дані
        String extractedData = extractData(correctedWordStr);
        return new IErrorCorrectionCode.CodeCheckResult(status, correctedWordStr, extractedData);
    }

    /**
     * Динамічно витягує інформаційні біти з виправленого слова.
     */
    private String extractData(String correctedWord) {
        StringBuilder extracted = new StringBuilder(k);
        char[] bits = correctedWord.toCharArray();
        int parityIdx = 0;
        for (int i = 0; i < n; i++) {
            if (i == ((1 << parityIdx) - 1)) {
                parityIdx++; // Це контрольний біт, ігноруємо
            } else {
                extracted.append(bits[i]); // Це біт даних
            }
        }
        return extracted.toString();
    }
}

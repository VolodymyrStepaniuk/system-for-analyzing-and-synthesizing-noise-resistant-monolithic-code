package org.stepaniuk.laboratorywork.algorithms.hamming;

import org.stepaniuk.laboratorywork.algorithms.IErrorCorrectionCode;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Це фіктивний клас-заповнювач, який ми використовуємо в ComboBox
 * для представлення опції "Згенерувати власний код".
 */
public class HammingGeneratorPlaceholder implements IErrorCorrectionCode {

    @Override
    public String getName() {
        return "Генератор коду Гемінга...";
    }

    @Override
    public int getDataWordLength() { return 0; }

    @Override
    public int getCodeWordLength() { return 0; }

    @Override
    public String encode(String dataWord) { return ""; }

    @Override
    public IErrorCorrectionCode.CodeCheckResult checkAndCorrect(String codeWord) {
        return new CodeCheckResult("N/A", "", "");
    }

    @Override
    public Map<Integer, List<Integer>> getBitRelationships() {
        return Collections.emptyMap();
    }
}

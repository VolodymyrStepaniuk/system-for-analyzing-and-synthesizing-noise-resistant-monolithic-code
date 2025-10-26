package org.stepaniuk.laboratorywork.algorithms;

import java.util.List;
import java.util.Map;

public interface IErrorCorrectionCode {

    /**
     * Результат перевірки та виправлення кодового слова.
     *
     * @param status        Текстовий опис результату (напр., "Помилок немає", "Помилка на позиції 5")
     * @param correctedWord Виправлене повне кодове слово.
     * @param extractedData Виправлене інформаційне повідомлення, витягнуте з кодового слова.
     */
    record CodeCheckResult(String status, String correctedWord, String extractedData) {}

    /**
     * @return Назва алгоритму, яка буде відображатися у списку (ComboBox).
     */
    String getName();

    /**
     * @return Кількість біт, яку очікує інформаційне слово.
     */
    int getDataWordLength();

    /**
     * @return Кількість біт у повному кодовому слові.
     */
    int getCodeWordLength();

    /**
     * Кодує інформаційне слово у завадостійке кодове слово.
     *
     * @param dataWord Вхідне інформаційне слово.
     * @return Згенероване кодове слово.
     */
    String encode(String dataWord);

    /**
     * Аналізує, знаходить та виправляє помилки у кодовому слові.
     *
     * @param codeWord Потенційно пошкоджене кодове слово.
     * @return Об'єкт CodeCheckResult з результатами аналізу.
     */
    CodeCheckResult checkAndCorrect(String codeWord);

    /**
     * Повертає мапу зв'язків між бітами.
     * Key: Індекс контрольного біта (p1, p2...).
     * Value: Список індексів ВСІХ бітів, які цей контрольний біт перевіряє.
     */
    Map<Integer, List<Integer>> getBitRelationships();
}
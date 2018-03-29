package ru.spbau.mit.dictionary;

import com.sun.rowset.internal.Row;

public class DictionaryImplementation implements Dictionary {

    final double minRatioSize = 0.5;
    final double maxRationSize = 3;
    private int curRowNumber = 5;
    private int numberOfContainedElements;
    private RowNode[] rows;

    DictionaryImplementation() {
        numberOfContainedElements = 0;
        rows = new RowNode[curRowNumber];
        setNotNullRowNodes(rows);
    }

    @Override
    public int size() {
        return numberOfContainedElements;
    }

    @Override
    public boolean contains(String key) {
        // получим хэш от ключа и вычислим, в какой строке будет храниться элемент
        int rowNumber = key.hashCode() % curRowNumber;
        RowNode neededRow = rows[rowNumber];

        // вызовем сначала функцию, которая возвращает первый элемент списка,
        // а потом - функцию, которая проверяет наличие элемента
        boolean result = false;
        try {
            result = neededRow.getHead().contains(key);
        } catch (Exception e) {
            return result;
        }

        return result;
    }

    @Override
    public String get(String key) {
        // получим хэш от ключа и вычислим, в какой строке будет храниться элемент
        int rowNumber = key.hashCode() % curRowNumber;
        RowNode neededRow = rows[rowNumber];

        // вызовем сначала функцию, которая возвращает первый элемент списка,
        // а потом - функцию, которая, если нашла, возвращает хранимое значение,
        // иначе возвращает null
        String returnedValue = null;
        try {
            returnedValue = neededRow.getHead().find(key);
        } catch (Exception e) {
            return returnedValue;
        }

        return returnedValue;
    }

    @Override
    public String put(String key, String value) {
        // получим хэш от ключа и вычислим, в какой строке будет храниться элемент
        int rowNumber = key.hashCode() % curRowNumber;
        RowNode neededRow = rows[rowNumber];

        String returnedValue = null;
        ListNode head  = neededRow.getHead();
        if (head == null) {
            neededRow.setHead(new ListNode(key, value));
            numberOfContainedElements++;
        } else if (head.contains(key)) {
            returnedValue = head.findAndReplace(key, value);
        } else {
            head.add(key, value);
            numberOfContainedElements++;
        }

        // проверка на необходимость rehash-a
        if (getCurrentRatioSize() > maxRationSize) {
            rehash(maxRationSize);
        }

        return returnedValue;
    }

    @Override
    public String remove(String key) {
        // получим хэш от ключа и вычислим, в какой строке хранится элемент
        int rowNumber = key.hashCode() % curRowNumber;
        RowNode neededRow = rows[rowNumber];

        String returnedValue = null;
        ListNode head = neededRow.getHead();
        try {
            if (head.contains(key)) {
                // костыль; если с нужным ключом оказался первый элемент списка, то удаляем его здесь
                if (head.getKey() == key) {
                    returnedValue = head.getValue();
                    neededRow.setHead(head.getNext());
                } else {
                    returnedValue = head.findAndRemove(key);
                }

                numberOfContainedElements--;
            }
        } catch (Exception e) {
            return returnedValue;
        }

        // проверка на необходимость rehash-a
        if (getCurrentRatioSize() < minRatioSize) {
            rehash(minRatioSize);
        }

        return returnedValue;
    }

    @Override
    public void clear() {
        numberOfContainedElements = 0;
        rows = new RowNode[curRowNumber];
        setNotNullRowNodes(rows);

    }

    private void rehash(double newRation) {
        System.out.format("Rehash was called. New ratio = %.1f.\n", newRation);
        final int oldRowNumber = curRowNumber;
        final RowNode[] oldRows = this.rows;

        curRowNumber = (int)(curRowNumber * newRation);
        rows = new RowNode[curRowNumber];
        setNotNullRowNodes(rows);
        numberOfContainedElements = 0;

        String[] keys;
        String[] values;
        for (int i = 0; i < oldRowNumber; i++) {
            ListNode head =  oldRows[i].getHead();
            if (head != null) {
                keys = head.getKeysOfAllList();
                values = head.getValuesOfAllList();

                int oldListSize = keys.length;
                for (int j = 0; j < oldListSize; j++) {
                    put(keys[j], values[j]);
                }
            }
        }

    }

    private double getCurrentRatioSize() {
        return numberOfContainedElements / curRowNumber;
    }

    private void setNotNullRowNodes(RowNode[] rows) {
        final int size = rows.length;
        for (int i = 0; i < size; i++) {
            rows[i] = new RowNode();
        }
    }
}

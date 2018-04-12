package ru.spbau.mit.trie;

public class Node {

    private Node[] next;
    private boolean isTerminal;
    private int numberWordsAfter;

    public Node() {
        next = new Node[52];
        isTerminal = false;
        numberWordsAfter = 0;
    }

    public Node(String nodeInString, Node[] children) {
        // первым символом каждого элемента будет [
        String[] fields = nodeInString.split("]");
        // уберем [
        for (int i = 0; i < fields.length; i++) {
            fields[i] = fields[i].substring(1);
        }

        isTerminal = Boolean.parseBoolean(fields[0]);
        numberWordsAfter = Integer.parseInt(fields[1]);
        next = children;
    }

    public int getIndex(char letter) {
        if (letter >= 'a' && letter <= 'z') {
            return letter - 'a';
        } else if (letter >= 'A' && letter <= 'Z') {
            return letter - 'A';
        }
        return -1;
    }

    public int getNumberWordsAfter() {
        return numberWordsAfter;
    }

    public Node[] getNext() {
        return next;
    }

    public Node getNextNeededNode(char letter) {
        return next[getIndex(letter)];
    }

    public boolean isTerminal() {
        return isTerminal;
    }

    public void addNextNewLetter(char letter) {
        next[getIndex(letter)] = new Node();
        return;
    }

    public void increaseNumberWordsAfter() {
        numberWordsAfter++;
        return;
    }

    public void decreaseNumberWordsAfter() {
        numberWordsAfter--;
        return;
    }

    public void setTerminal() {
        isTerminal = true;
    }

    public void unsetTerminal() {
        isTerminal = false;
    }

    public boolean checkWasteLetters() {
        if (numberWordsAfter == 0) {
            next = new Node[52];
            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        String tmp;
        tmp = "[" + Boolean.toString(isTerminal) + "]";
        result.append(tmp);
        tmp = "[" + Integer.toString(numberWordsAfter) + "]";
        result.append(tmp);
        result.append("[");
        for (int i = 0; i < 52; i++) {
            if (next[i] != null) {
                tmp = Integer.toString(i) + ",";
                result.append(tmp);
            }
        }
        result.append("]");
        return result.toString();
    }
}

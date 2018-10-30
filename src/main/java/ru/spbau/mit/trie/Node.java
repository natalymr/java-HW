package ru.spbau.mit.trie;

public class Node {

    public Node() {
        next = new Node[52];
        isTerminal = false;
        numberWordsAfter = 0;
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

    final Node nodeByChar(final char letter) {
        final int indexSearchedNode = getIndex(letter);
        return next[indexSearchedNode];
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

    private int numberWordsAfter;
    private Node[] next;
    private boolean isTerminal;
}

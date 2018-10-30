package ru.spbau.mit.trie;

public class TrieImplementation implements Trie {

    private final Node root;

    TrieImplementation() {
        root = new Node();
    }

    @Override
    public boolean add(String element) {
        if (contains(element)) {
            return false;
        }

        Node curNode = root;
        int lenElement = element.length();
        for (int i = 0; i < lenElement; i++) {

            char letter = element.charAt(i);
            if (curNode.getNextNeededNode(letter) == null) {
                curNode.addNextNewLetter(letter);
            }

            curNode.increaseNumberWordsAfter();
            if (i == lenElement - 1) {
                curNode.setTerminal();
                break;
            }
            curNode = curNode.getNextNeededNode(letter);
        }
        return true;
    }

    @Override
    public boolean contains(String element) {
        Node lastLetter = findLastLetterNode(element);

        if (lastLetter == null) {
            return false;
        } else {
            return lastLetter.isTerminal();
        }
    }

    @Override
    public boolean remove(String element) {
        Node lastLetterNode = findLastLetterNode(element);
        if (lastLetterNode == null || !lastLetterNode.isTerminal()) {
            return false;
        }

        Node curNode = root;
        int lenElement = element.length();
        for (int i = 0; i < lenElement; i++) {
            char letter = element.charAt(i);

            curNode.decreaseNumberWordsAfter();
            curNode = curNode.getNextNeededNode(letter);

            if (i == lenElement - 1) {
                curNode.unsetTerminal();
            }
        }

        curNode = root.getNextNeededNode(element.charAt(0));
        for (int i = 0; i < lenElement; i++) {
            char letter = element.charAt(i);

            if (curNode.checkWasteLetters()) {
                break;
            }
            curNode = curNode.getNextNeededNode(letter);
        }

        return true;
    }

    @Override
    public int size() {
        return root.getNumberWordsAfter();
    }

    @Override
    public int howManyStartsWithPrefix(String prefix) {
        Node prefixLastLetterNode = findLastLetterNode(prefix);

        if (prefixLastLetterNode == null) {
            return 0;
        }

        return prefixLastLetterNode.getNumberWordsAfter();
    }


    private Node findLastLetterNode(String element) {
        Node curNode = root;

        int lenElement = element.length();
        for (int i = 0; i < lenElement - 1; i++) {
            char letter = element.charAt(i);

            if (curNode.getNextNeededNode(letter) == null) {
                return null;

            }

            curNode = curNode.getNextNeededNode(letter);
        }
        return curNode;
    }

}

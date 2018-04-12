package ru.spbau.mit.trie;

import java.io.*;
import java.util.LinkedList;

public class TrieImplementation implements Trie, StreamSerializable {

    private Node root;

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

    @Override
    public void serialize(OutputStream out) throws IOException {
        DataOutputStream outData = new DataOutputStream(out);
        String thisToString = this.toString();
        outData.writeChars(thisToString);
    }

    @Override
    public void deserialize(InputStream in) throws IOException {
        DataInputStream inData = new DataInputStream(in);
        StringBuilder trieInString = new StringBuilder();
        char tmp;
        try {
            while (true) {
                tmp = inData.readChar();
                trieInString.append(tmp);
            }
        } catch (EOFException e) {}

        fromString(trieInString.toString());
    }


    @Override
    public String toString() {
        LinkedList<Node> dequeue = new LinkedList<>();
        dequeue.add(root);

        StringBuilder result = new StringBuilder();
        while (!dequeue.isEmpty()) {
            // pop
            Node curNode = dequeue.getFirst();
            dequeue.removeFirst();
            // append
            result.append(curNode.toString());
            result.append(";");
            // push children
            Node[] curNodeNext = curNode.getNext();
            for (int i = 0; i < 52; i++) {
                if (curNodeNext[i] != null) {
                    dequeue.add(curNodeNext[i]);
                }
            }
        }

        return result.toString();
    }

    public void fromString(String trieInString) {
        String[] nodesArrayInString = trieInString.split(";");
        LinkedList<Node> dequeue = new LinkedList<>();

        int numOfNodes = nodesArrayInString.length;
        for (int i = numOfNodes - 1; i >= 0; i--) {
            String[] fields = nodesArrayInString[i].split("]");

            Node[] children = new Node[52];
            if (fields[2].toCharArray().length != 1) {
                String tmp = fields[2].substring(1);

                String[] indicesOdChildenInString = tmp.split(",");
                int[] indicesOfChildrenInInt = new int[indicesOdChildenInString.length];
                for (int j = 0; j < indicesOdChildenInString.length; j++) {
                    indicesOfChildrenInInt[j] = Integer.parseInt(indicesOdChildenInString[j]);
                }

                for (int j = indicesOfChildrenInInt.length - 1; j >= 0; j--) {
                    Node curChildren = dequeue.getFirst();
                    dequeue.removeFirst();

                    children[indicesOfChildrenInInt[j]] = curChildren;
                }
            }
            dequeue.add(new Node(nodesArrayInString[i], children));
            if (i == 0) {
                root = dequeue.getLast();
            }
        }
    }
}

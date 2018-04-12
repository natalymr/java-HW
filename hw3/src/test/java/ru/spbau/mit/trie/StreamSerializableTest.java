package ru.spbau.mit.trie;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import static junit.framework.TestCase.*;

public class StreamSerializableTest {

    @Test
    public void testToString() {
        TrieImplementation testedTrie = new TrieImplementation();

        assertTrue(testedTrie.add("a"));
        assertEquals("[true][1][0,];[false][0][];", testedTrie.toString());

        assertTrue(testedTrie.add("ab"));
        assertEquals("[true][2][0,];[true][1][1,];[false][0][];", testedTrie.toString());


        assertTrue(testedTrie.add("abc"));
        assertEquals("[true][3][0,];[true][2][1,];[true][1][2,];[false][0][];", testedTrie.toString());

        assertTrue(testedTrie.add("ade"));
        assertEquals("[true][4][0,];[true][3][1,3,];[true][1][2,];[true][1][4,];[false][0][];[false][0][];",
                     testedTrie.toString());

        assertTrue(testedTrie.add("afg"));
        assertEquals("[true][5][0,];[true][4][1,3,5,];[true][1][2,];[true][1][4,];" +
                              "[true][1][6,];[false][0][];[false][0][];[false][0][];",
                                testedTrie.toString());
    }


    @Test
    public void testFromString() {
        TrieImplementation testedTrie = new TrieImplementation();

        testedTrie.fromString("[true][5][0,];[true][4][1,3,5,];[true][1][2,];[true][1][4,];" +
                                        "[true][1][6,];[false][0][];[false][0][];[false][0][];");
        assertTrue(testedTrie.contains("a"));
        assertTrue(testedTrie.contains("ab"));
        assertTrue(testedTrie.contains("abc"));
        assertTrue(testedTrie.contains("ade"));
        assertTrue(testedTrie.contains("afg"));
    }

    @Test
    public void testSerializeDeserialize() {
        TrieImplementation testedTrie = new TrieImplementation();
        testedTrie.add("qwe");
        testedTrie.add("qrt");
        testedTrie.add("asd");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            testedTrie.serialize(outputStream);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        try {
            testedTrie.deserialize(inputStream);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        assertTrue(testedTrie.contains("qwe"));
        assertTrue(testedTrie.contains("qrt"));
        assertTrue(testedTrie.contains("asd"));
    }
}

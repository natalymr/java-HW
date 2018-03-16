package ru.spbau.mit.trie;

import org.junit.Test;
import static junit.framework.TestCase.*;

public class TrieImplementationTest {

    @Test
    public void test1() {
        TrieImplementation testTrie = new TrieImplementation();

        assertTrue(testTrie.add("qwerty"));
        assertTrue(testTrie.contains("qwerty"));
        assertEquals(1, testTrie.size());


        assertTrue(testTrie.remove("qwerty"));
        assertEquals(0, testTrie.size());
        assertFalse(testTrie.contains("qwerty"));
    }

    @Test
    public void test2() {
        TrieImplementation testTrie = new TrieImplementation();

        assertTrue(testTrie.add("test"));
        assertTrue(testTrie.contains("test"));

        assertEquals(1, testTrie.howManyStartsWithPrefix("te"));

        assertTrue(testTrie.remove("test"));
        assertEquals(0, testTrie.size());

        assertEquals(0, testTrie.howManyStartsWithPrefix("te"));
    }

    @Test
    public void test3() {
        TrieImplementation testTrie = new TrieImplementation();

        assertTrue(testTrie.add("qaz"));
        assertFalse(testTrie.add("qaz"));

        assertEquals(1, testTrie.size());
        assertEquals(1, testTrie.howManyStartsWithPrefix("q"));
    }

    @Test
    public void test4() {
        TrieImplementation testTrie = new TrieImplementation();

        assertTrue(testTrie.add("qaz"));
        assertTrue(testTrie.add("qazaq"));

        assertEquals(2, testTrie.size());
        assertTrue(testTrie.remove("qaz"));
        assertFalse(testTrie.contains("qaz"));
        assertEquals(1, testTrie.size());
    }
}

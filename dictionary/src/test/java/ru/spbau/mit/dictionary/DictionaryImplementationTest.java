package ru.spbau.mit.dictionary;

import org.junit.Test;
import static junit.framework.TestCase.*;

public class DictionaryImplementationTest {

    @Test
    public void SizePutRemoveTest() {
        DictionaryImplementation testedDictionary = new DictionaryImplementation();

        assertEquals(0, testedDictionary.size());
        assertEquals(null, testedDictionary.put("key", "value"));
        assertEquals(1, testedDictionary.size());
        assertEquals("value", testedDictionary.put("key", "newValue"));
        assertEquals(1, testedDictionary.size());
        assertEquals("newValue", testedDictionary.remove("key"));
        assertEquals(0, testedDictionary.size());
        assertEquals(null, testedDictionary.remove("key"));
    }

    @Test
    public void PutGetTest() {
        DictionaryImplementation testedDictionary = new DictionaryImplementation();

        assertEquals(null, testedDictionary.put("key", "value"));
        assertEquals("value", testedDictionary.get("key"));
        assertEquals(null, testedDictionary.get("anKey"));
        assertEquals("value", testedDictionary.put("key", "newValue"));
    }

    @Test
    public void RehashTest() {
        DictionaryImplementation testedDictionary = new DictionaryImplementation();

        String[] keys = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p",
                         "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};

        String[] values = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p",
                           "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};

        int testSize = keys.length;
        for (int i = 0; i < testSize; i++) {
            assertEquals(null, testedDictionary.put(keys[i], values[i]));
            assertEquals(i + 1, testedDictionary.size());
        }

        for (int i = 0; i < testSize; i++) {
            assertEquals(values[i], testedDictionary.remove(keys[i]));
            assertEquals(testSize - i - 1, testedDictionary.size());
        }
    }
}

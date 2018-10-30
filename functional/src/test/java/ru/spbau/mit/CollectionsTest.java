package ru.spbau.mit;

import org.junit.Test;

import javax.swing.text.html.HTMLDocument;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class CollectionsTest {

    public static class Sq extends Function1<Integer, Integer> {
        public Integer apply(Integer input) {
            return (input * input);
        }
    }

    Sq sq = new Sq();

    @Test
    public void testMap() {

        LinkedList<Integer> data = new LinkedList<>();
        for (int i = 0; i < 20; i++) {
            data.add(i);
        }

        Collection<Integer> toTest = Collections.map(sq, data);
        Iterator<Integer> it = toTest.iterator();

        for (int i = 0; i < 20; i++) {
            int tmp = it.next();
            assertEquals(tmp, i * i);
        }
    }

    class DivByThree extends Predicate<Integer>{
        public boolean apply(Integer x){
            return  x % 3 == 0;
        }
    }

    DivByThree divByThree = new DivByThree();

    @Test
    public void testFilter() {

        LinkedList<Integer> data = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            data.add(i);
        }

        Collection<Integer> toTest = Collections.filter(divByThree, data);
        Iterator<Integer> it = toTest.iterator();

        ArrayList<Integer> expected = new ArrayList<>();
        expected.add(0);
        expected.add(3);
        expected.add(6);
        expected.add(9);

        for (int i = 0; i < 3; i++) {
            int tmp = it.next();
            int tmp1 = expected.get(i);
            assertEquals(tmp, tmp1);
        }
    }

    class LessTen extends Predicate<Integer> {
        public boolean apply(Integer in) {
            return in < 10;
        }
    }

    LessTen lessTen = new LessTen();

    @Test
    public void testTakeWhile() {
        LinkedList<Integer> data = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            data.add(i);
        }

        Collection<Integer> toTest = Collections.takeWhile(lessTen, data);
        Iterator<Integer> it = toTest.iterator();

        for (int i = 0; i < 9; i++) {
            int tmp = it.next();
            assertEquals(tmp, i);
        }
    }

    @Test
    public void testTakeUnless() {
        LinkedList<Integer> data = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            data.add(i);
        }

        Collection<Integer> toTest = Collections.takeUnless(lessTen, data);
        Iterator<Integer> it = toTest.iterator();

        assertFalse(it.hasNext());
    }

    static class Plus extends Function2<Integer, Integer, Integer>{
        public Integer apply(Integer f, Integer s) {
            return f + s;
        }
    }

    Plus plus = new Plus();

    @Test
    public void testFoldr() {
        LinkedList<Integer> data = new LinkedList<>();
        for (int i = 0; i < 5; i++) {
            data.add(i);
        }

        int result = Collections.foldr(plus, 0, data);
        int expected = 10;

        assertEquals(expected, result);
    }

    @Test
    public void testFoldl() {
        LinkedList<Integer> data = new LinkedList<>();
        for (int i = 0; i < 5; i++) {
            data.add(i);
        }

        int result = Collections.foldl(plus, 0, data);
        int expected = 10;

        assertEquals(expected, result);
    }
}

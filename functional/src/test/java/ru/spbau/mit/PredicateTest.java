package ru.spbau.mit;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PredicateTest {
    class ThrowException extends Predicate<Object>{
        public boolean apply(Object in){
            throw new NullPointerException();
        }
    }

    class StartWithA extends Predicate<String>{
        public boolean apply(String in){
            return in.startsWith("A");
        }
    }

    class StartWithB extends Predicate<String>{
        public boolean apply(String in){
            return in.startsWith("B");
        }
    }
    
    class DivByThree extends Predicate<Integer>{
        public boolean apply(Integer x){
            return  x % 3 == 0;
        }
    }

    class DivBySix extends Predicate<Integer>{
        public boolean apply(Integer x){
            return  x % 6 == 0;
        }
    }

    class PowerOfTwo extends Predicate<Integer>{
        public boolean apply(Integer x){
            while(x % 2 == 0) {
                x /= 2;
            }
            return x == 1;
        }
    }

    StartWithA sA = new StartWithA();
    StartWithB sB = new StartWithB();
    DivByThree div3 = new DivByThree();
    DivBySix div6 = new DivBySix();
    PowerOfTwo p2 = new PowerOfTwo();
    ThrowException exc = new ThrowException();

    @Test
    public void testSimple() {
        assertTrue(sA.apply("A"));
        assertFalse(sA.apply("B"));
        assertFalse(sB.apply("A"));
        assertTrue(sB.apply("B"));
        assertTrue(div3.apply(9));
        assertFalse(div6.apply(9));
        assertFalse(div3.apply(5));
        assertTrue(div6.apply(12));
        assertTrue(p2.apply(4));
        assertFalse(p2.apply(6));
    }

    @Test
    public void testNot() {
        Predicate<String> notStartWithA = sA.not();
        assertTrue(notStartWithA.apply("asdf"));
        assertFalse(notStartWithA.apply("Asdf"));
    }

    @Test
    public void testAnd() {
        Predicate<Integer> div3AndDiv6 = div3.and(div6);
        assertTrue(div3AndDiv6.apply(12));
        assertFalse(div3AndDiv6.apply(9));
    }

    @Test
    public void testOr() {
        Predicate<String> sAOrSB = sA.or(sB);
        assertTrue(sAOrSB.apply("A"));
        assertTrue(sAOrSB.apply("B"));
        assertFalse(sAOrSB.apply("a"));
        assertFalse(sAOrSB.apply("b"));
        assertFalse(sAOrSB.apply("acb"));
    }

    @Test
    public void testPredicateLaziness() {
        Predicate<Object> lazyTrue = Predicate.ALWAYS_TRUE.or(exc);
        assertEquals(true, lazyTrue.apply(new Object()));
        Predicate<Object> lazyFalse = Predicate.ALWAYS_FALSE.and(exc);
        assertEquals(false, lazyFalse.apply(new Object()));
    }
}
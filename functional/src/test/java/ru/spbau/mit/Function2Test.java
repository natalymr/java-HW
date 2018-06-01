package ru.spbau.mit;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Function2Test {
    static class Plus extends Function2<Integer, Integer, Integer>{
        public Integer apply(Integer f, Integer s){
            return f + s;
        }
    }

    static class Minus extends Function2<Integer, Integer, Integer>{
        public Integer apply(Integer f, Integer s){
            return f - s;
        }
    }

    static class Multiply extends Function2<Integer, Integer, Integer>{
        public Integer apply(Integer f, Integer s){
            return f * s;
        }
    }

    static class Division extends Function2<Double, Double, Double>{
        public Double apply(Double f, Double s){
            return f / s;
        }
    }

    class Sqrt extends Function1<Integer, Double> {
        public Double apply(Integer input){
            return Math.sqrt(input);
        }
    }

    Sqrt sqrt = new Sqrt();
    Plus plus = new Plus();
    Minus minus = new Minus();
    Multiply mult = new Multiply();
    Division div = new Division();

    @Test
    public void testSimple(){
        assertEquals(7, (long)plus.apply(2, 5));
        assertEquals(-3, (long)minus.apply(2, 5));
        assertEquals(10, (long)mult.apply(2, 5));
        assertEquals(0.4, div.apply(2.0, 5.0), 0.001);
    }

    @Test
    public void testBind() {
        Function1<Integer, Integer> plus1 = plus.bind1(1);
        Function1<Integer, Integer> minus1 = minus.bind2(1);
        Function1<Integer, Integer> mult0 = mult.bind1(0);
        Function1<Double, Double> div3 = div.bind2(3.0);
        assertEquals(3, (long)plus1.apply(2));
        assertEquals(1, (long)minus1.apply(2));
        assertEquals(0, (long)mult0.apply( 5));
        assertEquals(2.0, div3.apply(6.0), 0.001);
    }

    @Test
    public void testCurry() {
        Function1<Integer, Function1<Integer, Integer>> curr = plus.curry();
        assertEquals(7, curr.apply(2).apply(5).intValue());
        assertEquals(3, curr.apply(-2).apply(5).intValue());
    }

    @Test
    public void testCompose() {
        Function2<Integer, Integer, Double> sqrtAndPlus = plus.compose(sqrt);
        Function2<Integer, Integer, Double> sqrtAndMinus = minus.compose(sqrt);
        assertEquals(sqrtAndPlus.apply(12, 13), 5.0, 0.001);
        assertEquals(sqrtAndMinus.apply(5, 5), 0, 0.001);
    }
}
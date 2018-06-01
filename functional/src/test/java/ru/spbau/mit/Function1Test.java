
package ru.spbau.mit;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Function1Test {

    class Sqrt extends Function1<Double, Double> {
        public Double apply(Double input){
            return Math.sqrt(input);
        }
    }

    class Sq extends Function1<Double, Double> {
        public Double apply(Double input){
            return (input * input);
        }
    }

    Sqrt sqrt = new Sqrt();
    Sq sq = new Sq();

    @Test
    public void testApplt() {
        assertEquals(25, sq.apply(5.0), 0.001);
        assertEquals(25, sq.apply(-5.0),  0.001);
        assertEquals(5, sqrt.apply(25.0),  0.001);
        assertEquals(Math.sqrt(1234), (double)sqrt.apply(1234.0),  0.001);
    }

    @Test
    public void testCompose() {
        Function1<Double, Double> sqrtAndSq = sq.compose(sqrt);
        assertEquals(10, sqrtAndSq.apply(10.0),  0.001);
        Function1<Double, Double> sqAndSqrt = sqrt.compose(sq);
        assertEquals(2, sqAndSqrt.apply(2.0),  0.001);
        assertEquals(1, sqrtAndSq.apply(-1.0),  0.001);
        assertTrue(Double.isNaN(sqAndSqrt.apply(-1.0)));


        //sqrtAndSq.apply(-1.0);

    }
}
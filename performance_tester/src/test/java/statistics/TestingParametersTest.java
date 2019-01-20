package statistics;

import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

public class TestingParametersTest {

    @Test
    public void testIteratorForM() {
        TestingParameters parameters = new TestingParameters(5, 10, 15, 20, VaryingParameter.M,
                                                        5, 7, 1);
        Iterator<ParametersPerIteration> iterator = parameters.iterator();

        assertEquals(true, iterator.hasNext());
        ParametersPerIteration first = iterator.next();
        assertEquals(5, first.getM());
        assertEquals(10, first.getN());
        assertEquals(15, first.getDelay());

        assertEquals(true, iterator.hasNext());
        ParametersPerIteration second = iterator.next();
        assertEquals(6, second.getM());
        assertEquals(10, second.getN());
        assertEquals(15, second.getDelay());

        assertEquals(true, iterator.hasNext());
        ParametersPerIteration third = iterator.next();
        assertEquals(7, third.getM());
        assertEquals(10, third.getN());
        assertEquals(15, third.getDelay());

        assertEquals(false, iterator.hasNext());
    }

    @Test
    public void testIteratorForN() {
        TestingParameters parameters = new TestingParameters(5, 10, 15, 20, VaryingParameter.N,
            10, 12, 1);
        Iterator<ParametersPerIteration> iterator = parameters.iterator();

        assertEquals(true, iterator.hasNext());
        ParametersPerIteration first = iterator.next();
        assertEquals(5, first.getM());
        assertEquals(10, first.getN());
        assertEquals(15, first.getDelay());

        assertEquals(true, iterator.hasNext());
        ParametersPerIteration second = iterator.next();
        assertEquals(5, second.getM());
        assertEquals(11, second.getN());
        assertEquals(15, second.getDelay());

        assertEquals(true, iterator.hasNext());
        ParametersPerIteration third = iterator.next();
        assertEquals(5, third.getM());
        assertEquals(12, third.getN());
        assertEquals(15, third.getDelay());

        assertEquals(false, iterator.hasNext());
    }
}
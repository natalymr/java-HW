package statistics;


import java.io.Serializable;
import java.util.Iterator;

public class TestingParameters implements Iterable<ParametersPerIteration>, Serializable {
    private final int m;
    private final int n;
    private final int delay;
    private final int x;
    private final VaryingParameter varyingParameter;
    private final int minVP;
    private final int maxVP;
    private final int stepVP;

    public TestingParameters(int m, int n, int delay, int x, VaryingParameter varyingParameter,
                             int minVP, int maxVP, int stepVP) {
        this.m = m;
        this.n = n;
        this.delay = delay;
        this.x = x;
        this.varyingParameter = varyingParameter;
        this.minVP = minVP;
        this.maxVP = maxVP;
        this.stepVP = stepVP;
    }

    public int getX() {
        return x;
    }

    public int getDelay() {
        return delay;
    }

    public VaryingParameter getVaryingParameter() {
        return varyingParameter;
    }

    @Override
    public Iterator<ParametersPerIteration> iterator() {
        Iterator<ParametersPerIteration> it = new Iterator<ParametersPerIteration>() {
            private int currentValue = minVP;

            @Override
            public boolean hasNext() {
                return currentValue <= maxVP;
            }

            @Override
            public ParametersPerIteration next() {
                ParametersPerIteration result = null;
                switch (varyingParameter) {
                    case M: {
                        result = new ParametersPerIteration(currentValue, n, delay, currentValue);
                        break;
                    }
                    case N: {
                        result = new ParametersPerIteration(m, currentValue, delay, currentValue);
                        break;
                    }
                    case delay: {
                        result = new ParametersPerIteration(m, n, currentValue, currentValue);
                        break;
                    }
                }
                currentValue += stepVP;

                return result;
            }
        };

        return it;
    }

    public int getN() {
        return n;
    }

    public int getM() {
        return m;
    }
}

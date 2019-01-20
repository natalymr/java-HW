package statistics;

public class ParametersPerIteration {
    private final int m;
    private final int n;
    private final int delay;

    private final int varyingParameterCurrentValue;

    ParametersPerIteration(int m, int n, int delay, int varyingParameterCurrentValue) {
        this.m = m;
        this.n = n;
        this.delay = delay;
        this.varyingParameterCurrentValue = varyingParameterCurrentValue;
    }

    public int getM() {
        return m;
    }

    public int getN() {
        return n;
    }

    public int getDelay() {
        return delay;
    }

    public int getVaryingParameterCurrentValue() {
        return varyingParameterCurrentValue;
    }
}

package statistics;

import java.io.Serializable;

/* POJO */
public class StatisticsResultPerIteration implements Serializable {
    private int currentValueOfVaryingParameter;
    private final AverageValues averageValues;

    public StatisticsResultPerIteration(int currentValueOfVaryingParameter, AverageValues averageValues) {
        this.currentValueOfVaryingParameter = currentValueOfVaryingParameter;
        this.averageValues = averageValues;
    }

    public int getCurrentValueOfVaryingParameter() {
        return currentValueOfVaryingParameter;
    }

    public AverageValues getAverageValues() {
        return averageValues;
    }
}

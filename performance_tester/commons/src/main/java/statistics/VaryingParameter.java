package statistics;

public enum VaryingParameter {
    M("M"),
    N("N"),
    delay("delay");

    private final String value;
    private VaryingParameter(String value) {
        this.value = value;
    }

    public String getVaryingParameterInString() {
        return value;
    }
}

package server;

public enum ServerType {
    threadPerClient("threadPerClient"),
    sortInThreadPool("sortInThreadPool"),
    nonBlockingServer("nonBlockingServer");

    private final String type;

    private ServerType(String type) {
        this.type = type;
    }

    public String getTypeInString() {
        return type;
    }
}

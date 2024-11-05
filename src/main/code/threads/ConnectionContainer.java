package main.code.threads;

public interface ConnectionContainer {
    public long lastKeepAliveMessageTime();
    public int keepAliveTimeout();
}

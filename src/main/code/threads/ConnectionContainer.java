package main.code.threads;

public interface ConnectionContainer {
    public long lastKeepAliveMessageTime();
    public void setLastKeepMessageTime();
    public int keepAliveTimeout();
    public ConnectionManager getConnectionManager();
    public void shutdown();
    public void informDisconnect();
    public STATE getConnectionState();
    public void setState(STATE state);
    public String getIdentifier();

    public static enum STATE {
        IDLE, CONNECT, ACTIVE, OPEN_SENT, OPEN_CONFIRM, ESTABLISHED, SHUT_DOWN
    }
}

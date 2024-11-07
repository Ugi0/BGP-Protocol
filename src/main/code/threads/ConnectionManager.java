package main.code.threads;

import static main.Main.printDebug;

import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import messages.ControlMessage;
import messages.Message;

public class ConnectionManager implements Runnable {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    ScheduledFuture<?> future;
    private OutputStream stream;
    private byte[] keepaliveMessage;
    private int timeout;
    private String address;

    private boolean killed = false;

    private ConnectionContainer parent;

    public ConnectionManager(OutputStream stream, ConnectionContainer parent, String ipAddress) {
        this.stream = stream;
        this.parent = parent;
        address = ipAddress;
    }

    public ConnectionManager(OutputStream stream, ConnectionContainer parent) {
        this(stream, parent, null);
    }

    public String getAddress() {
        return this.address;
    }

    /**
     * Set the message that the connection should send after a given timeout
     * This message can't be changed for a given connection after creation
     * @param keepaliveMessage
     * @param timeout
     */
    public void setKeepAliveMessage(ControlMessage keepaliveMessage, int timeout) {
        this.timeout = timeout;
        this.keepaliveMessage = keepaliveMessage.toBytes();

        future = scheduler.scheduleWithFixedDelay(this, timeout, timeout, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        if (killed) return;
        if (parent.lastKeepAliveMessageTime() + parent.keepAliveTimeout() < TimeUnit.MILLISECONDS.toSeconds( System.currentTimeMillis())) {
            printDebug("Timed out");
            kill();
            parent.informDisconnect();
        }
        try {
            stream.write(keepaliveMessage);
            stream.flush();
            printDebug(String.format("Wrote byte %s to the input stream", keepaliveMessage));
        } catch (SocketException e) {
            printDebug("Socket has been closed");
            kill();
            parent.informDisconnect();
        } catch (IOException e) {
            printDebug("Socket write Error");
            e.printStackTrace();
            kill();
        }
    }

    /**
     * Write a message to the underlying connection
     * @param message
     */
    public void writeToStream(Message message) {
        if (killed) return;
        printDebug("Writing to stream: " + message);
        try {
            stream.write(message.toBytes());
            stream.flush();
            if (future != null && !scheduler.isShutdown()) {
                future.cancel(true); //Reset keepalive timer
                future = scheduler.scheduleWithFixedDelay(this, timeout, timeout, TimeUnit.SECONDS);
            }
        } catch (SocketException e) {
            printDebug("Socket has been closed");
            kill();
            parent.informDisconnect();
        } catch (IOException e) {
            printDebug("Socket write Error");
            e.printStackTrace();
            kill();
        }
    }

    public void kill () {
        if (killed) return;
        scheduler.shutdown();
        killed = true;
        parent.shutdown();
    }
}

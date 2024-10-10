package main.code.threads;

import static main.Main.printDebug;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ConnectionManager implements Runnable {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    ScheduledFuture<?> future;
    private OutputStream stream;
    private byte[] keepaliveMessage;
    private int timeout;

    public ConnectionManager(OutputStream stream) {
        this.stream = stream;
    }

    public void setKeepAliveMessage(byte[] keepaliveMessage, int timeout) {
        this.timeout = timeout;
        this.keepaliveMessage = keepaliveMessage;

        future = scheduler.scheduleWithFixedDelay(this, timeout, timeout, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        try {
            stream.write(keepaliveMessage);
            stream.flush();
            printDebug(String.format("Wrote byte %s to the input stream", keepaliveMessage));
        } catch (IOException e) {
            printDebug("Socket write Error");
            e.printStackTrace();
        }
    }

    public void writeToStream(byte[] message) {
        try {
            stream.write(message);
            stream.flush();
            if (future != null) {
                future.cancel(true); //Reset keepalive timer
                future = scheduler.scheduleWithFixedDelay(this, timeout, timeout, TimeUnit.SECONDS);
            }
        } catch (IOException e) {
            printDebug("Socket write Error");
            e.printStackTrace();
        }
    }

    public void kill() {
        scheduler.shutdown();
    }
}

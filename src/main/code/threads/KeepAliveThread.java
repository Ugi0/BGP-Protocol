package main.code.threads;

import static main.Main.printDebug;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class KeepAliveThread implements Runnable {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private OutputStream stream;
    private byte message;

    public KeepAliveThread(OutputStream stream, byte message) {
        this.stream = stream;
        this.message = message;

        scheduler.scheduleWithFixedDelay(this, 0, 5, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        try {
            stream.write(message); //Write the object byte array here
            stream.flush();
            printDebug(String.format("Wrote byte %s to the input stream", message));
        } catch (IOException e) {
            printDebug("Socket write Error");
            e.printStackTrace();
        }
    }

    public void kill() {
        scheduler.shutdown();
    }
}

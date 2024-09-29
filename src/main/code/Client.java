// A simple Client Server Protocol .. Client for Echo Server
package main.code;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import main.code.threads.KeepAliveThread;

import java.io.InputStream;
import java.io.OutputStream;

import static main.Main.*;

public class Client extends Thread {
    private int port;
    Socket socket = null;
    InputStream inputStream = null;
    OutputStream outputStream = null;
    KeepAliveThread keepAliveThread;

    public Client(int port) {
        this.port = port;
    }

    public void run() {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            printDebug(String.format("Attempting to connect to port %s", port));
            socket = new Socket("localhost", port); // You can use static final constant PORT_NUM
            printDebug(String.format("Client connected to port %s", port));
            socket.setKeepAlive(false);
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
        }
        catch (IOException e){
            e.printStackTrace();
            if (e.getLocalizedMessage().equals("Connection refused: connect")) {
                printDebug("Port not yet open");
                return;
            }
            e.printStackTrace();
            printDebug("IO Exception");
            return;
        }

        //Start a timer thread that will send a keepalive message every 20 seconds
        keepAliveThread = new KeepAliveThread(outputStream, (byte) 2);

        //Listen to messages from the server, should only be keep alive messages
        byte[] buff = new byte[200]; //200 bytes for keepalive message, change this value when it's known how many bytes an keepalive message should be
        String message = "";
        int i = 0;
        try {
            while (true) {
                inputStream.read(buff);
                while (buff[i] != 0x0) {
                    message += buff[i];
                    i++;
                }
                printDebug(String.format("Client read %s in the stream", message));
                message = "";
                i = 0;
            }
        }
        catch(IOException e){
            e.printStackTrace();
            printDebug("Socket read Error");
        }
        finally{
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            printDebug("Connection Closed");

        }

    }

    @Override
    public void interrupt() {
        keepAliveThread.kill();
    }
}
// A simple Client Server Protocol .. Client for Echo Server
package main.code;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import main.code.threads.ConnectionManager;
import messages.Keepalive;
import messages.Message;
import messages.Open;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;

import static main.Main.*;

public class Client extends Thread {
    private String ipAdd;
    private Integer ownAS;
    Socket socket = null;
    InputStream inputStream = null;
    OutputStream outputStream = null;
    ConnectionManager connectionManager;

    public Client(String ipAdd, Integer AS) {
        this.ipAdd=ipAdd;
        this.ownAS = AS;
    }

    public void run() {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            InetAddress addr = InetAddress.getByName(ipAdd);
            printDebug(String.format("%s attempting to connect to address %s", ownAS, addr));
            socket = new Socket(addr, 8080); // Port number can be freely chosen as long as it matches the server port.
            printDebug(String.format("%s client connected to address %s", ownAS, addr));
            socket.setKeepAlive(false);
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
        }
        catch (IOException e){
            e.printStackTrace();
            if (e.getLocalizedMessage().equals(String.format("%s connection refused: connect", ownAS))) {
                printDebug("Connection not yet open");
                return;
            }
            e.printStackTrace();
            printDebug("IO Exception");
            return;
        }

        //Start a timer thread that will send a keepalive message every 20 seconds
        connectionManager = new ConnectionManager(outputStream);

        Open openMessage = new Open(ownAS, 20, 0, 0, 0);
        connectionManager.writeToStream(openMessage);

        byte[] buff = new byte[Message.MAX_MESSAGE_LENGTH];
        try {
            while (true) {
                inputStream.read(buff);
                Class<? extends Message> clazz = Message.classFromMessage(buff);
                Message message = clazz.getConstructor(byte[].class).newInstance(buff);

                printDebug(String.format("%s client read %s in the stream", ownAS, message));

                handleMessage(message);
            }
        } catch (IOException e) {
            printDebug(String.format("IO Error/ Client %s terminated abruptly", getName()));
        } catch(NullPointerException e){
            printDebug(String.format("Client %s Closed", getName()));
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | SecurityException | IllegalArgumentException | InvocationTargetException e) {
            printDebug(String.format("Client %s couldn't parse the message", getName()));
            e.printStackTrace();
        } finally{
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
            printDebug(String.format("Client %s connection Closed", ownAS));

        }

    }

    /**
     * Handle client receiving a message
     * @param received
     */
    private void handleMessage(Message received) {
        //Client shouldn't receive anything else than keepalive messages
        if (received instanceof Open) {
            Open message = (Open) received;
            
            connectionManager.setKeepAliveMessage(new Keepalive(), message.getHoldTime());

            connectionManager.writeToStream(new Keepalive());
        }  
    }

    @Override
    public void interrupt() {
        connectionManager.kill();
    }
}
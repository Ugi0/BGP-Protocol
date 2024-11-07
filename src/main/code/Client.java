// A simple Client Server Protocol .. Client for Echo Server
package main.code;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import main.code.threads.ConnectionContainer;
import main.code.threads.ConnectionManager;
import messages.Keepalive;
import messages.Message;
import messages.Notification;
import messages.ControlMessage;
import messages.Open;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;

import static main.Main.*;

public class Client extends Thread implements ConnectionContainer {
    private String ipAdd;
    private Integer ownAS;
    Socket socket = null;
    InputStream inputStream = null;
    OutputStream outputStream = null;
    ConnectionManager connectionManager;

    long lastMessageTime = TimeUnit.MILLISECONDS.toSeconds( System.currentTimeMillis());

    Router parent;

    public STATE state = STATE.IDLE;

    public Client(String ipAdd, Integer AS, Router parent) {
        this.parent = parent;
        this.ipAdd=ipAdd;
        this.ownAS = AS;
    }

    public void run() {

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
        connectionManager = new ConnectionManager(outputStream, this, ipAdd);
        parent.addToConnections(connectionManager);

        Open openMessage = new Open(ownAS, 20, 0, 0, 0);
        connectionManager.writeToStream(openMessage);
        state = STATE.OPEN_SENT;

        try {
            while (true) {
                byte[] buff = new byte[ControlMessage.MAX_MESSAGE_LENGTH];
                inputStream.read(buff);
                int index = 0;
                
                while (true) {
                    byte[] newArray = new byte[ControlMessage.MAX_MESSAGE_LENGTH];
                    System.arraycopy(buff, index, newArray, 0, ControlMessage.MAX_MESSAGE_LENGTH - index);

                    Class<? extends Message> clazz = Message.classFromMessage(newArray);
                    if (clazz == null) break;

                    Message message = clazz.getConstructor(byte[].class).newInstance(newArray);

                    index += message.getLength();

                    printDebug(String.format("%s client read %s in the stream", ownAS, message));

                    handleMessage(message);
                }
            }
        } catch (IOException e) {
            printDebug(String.format("IO Error/ Client %s terminated abruptly", getName()));
            e.printStackTrace();
        } catch(NullPointerException e){
            printDebug(String.format("Client %s Closed", getName()));
            e.printStackTrace();
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
            shutdown();
        }

    }

    /**
     * Handle client receiving a message
     * @param received
     */
    private void handleMessage(Message received) {
        setLastKeepMessageTime();
        if (received instanceof Keepalive) {
            if (state == STATE.OPEN_SENT) state = STATE.OPEN_CONFIRM;
        } else if (received instanceof Open) {
            if (state != STATE.OPEN_CONFIRM) return;
            Open message = (Open) received;
            
            connectionManager.setKeepAliveMessage(new Keepalive(), message.getHoldTime());

            connectionManager.writeToStream(new Keepalive());

            state = STATE.ESTABLISHED;

            parent.getServer().handleRoutingTableChange(message, this);
        } else if (received instanceof Notification) {
            Notification message = (Notification) received;
            if (message.getError() == Notification.ErrorCode.Cease.getValue()) {
                connectionManager.kill();

                parent.removeFromRoutingTable(ipAdd);
            }
        } else {
            if (state != STATE.ESTABLISHED) return;
            parent.getServer().handleMessage(received, this);
        }
    }

    @Override
    public long lastKeepAliveMessageTime() {
        return lastMessageTime;
    }

    @Override
    public void setLastKeepMessageTime() {
        lastMessageTime = TimeUnit.MILLISECONDS.toSeconds( System.currentTimeMillis());
    }

    @Override
    public int keepAliveTimeout() {
        return 60;
    }

    @Override
    public void shutdown() {
        printDebug(String.format("Client %s is shutting down", ownAS));
        connectionManager.kill();
    }

    @Override
    public void informDisconnect() {
        parent.getServer().removeFromRoutingTable(ipAdd);
    }

    public void killGracefully() {
        connectionManager.writeToStream(new Notification(Notification.ErrorCode.Cease.getValue(), 0, null));
        connectionManager.kill();
    }

    @Override
    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    @Override
    public STATE getConnectionState() {
        return state;
    }

    @Override
    public void setState(STATE state) {
        this.state = state;
    }
}
package main.code.threads;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import messages.ControlMessage;
import messages.Message;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;

import main.code.Server;

import static main.Main.*;

public class ServerThread extends Thread implements ConnectionContainer {  
    InputStream inputStream = null;
    OutputStream outputStream = null;
    Socket socket=null;
    ConnectionManager connectionManager;

    long lastMessageTime = TimeUnit.MILLISECONDS.toSeconds( System.currentTimeMillis());

    Server parent;

    public ServerThread(Socket s, Server parent) {
        this.parent = parent;
        socket = s;
        try {
            s.setSoTimeout(60000); //milliseconds, timeouts after 1min
        }catch (IOException e){
            printDebug(("server thread timed out"));
        }
    }

    public void run() {
        try {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();

        } catch(IOException e){
            printDebug("IO error in server thread");
        }

        connectionManager = new ConnectionManager(outputStream, this);
        parent.parent.addToConnections(connectionManager);

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

                    printDebug(String.format("%s client read %s in the stream", parent.AS, message));

                    parent.handleMessage(message, connectionManager);
                }
                lastMessageTime = TimeUnit.MILLISECONDS.toSeconds( System.currentTimeMillis());
            }
        } catch (IOException e) {
            printDebug(String.format("IO Error/ Server %s terminated abruptly", getName()));
            e.printStackTrace();
        } catch(NullPointerException e){
            printDebug(String.format("Server %s Closed", getName()));
            e.printStackTrace();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException | SecurityException e) {
            printDebug(String.format("Server %s couldn't parse the message", getName()));
            e.printStackTrace();
        }


        finally {
            try {
                printDebug("Server connection Closing..");
                if (inputStream != null){
                    inputStream.close(); 
                    printDebug(" Socket Input Stream Closed");
                }

                if (outputStream != null){
                    outputStream.close();
                    printDebug("Socket Out Closed");
                }
                if (socket!=null){
                    socket.close();
                    printDebug("Socket Closed");
                }

                }
            catch (IOException ie) {
                printDebug("Socket Close Error");
            }
        }//end finally
    }

    @Override
    public void interrupt() {
        connectionManager.kill();
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public byte[] getSocketAddress() {
        return socket.getInetAddress().getAddress();
    }

    @Override
    public long lastKeepAliveMessageTime() {
        return lastMessageTime;
    }

    @Override
    public int keepAliveTimeout() {
        return 60;
    }

    @Override
    public void handleConnectionDeath() {
        
    }
}
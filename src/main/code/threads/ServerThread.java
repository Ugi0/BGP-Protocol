package main.code.threads;

import java.io.IOException;
import java.net.Socket;
import java.io.InputStream;
import java.io.OutputStream;

import static main.Main.*;

public class ServerThread extends Thread {  
    InputStream inputStream = null;
    OutputStream outputStream = null;
    Socket socket=null;
    KeepAliveThread keepAliveThread;

    public ServerThread(Socket s) {
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

        //Start a timer thread that will send a keepalive message every 20 seconds
        keepAliveThread = new KeepAliveThread(outputStream, (byte) 1);

        byte[] buff = new byte[2000];
        String message = "";
        int i = 0;
        try {
            while (true) {
                inputStream.read(buff);
                while (buff[i] != 0x0) {
                    message += buff[i];
                    i++;
                }
                printDebug(String.format("Server read %s in the stream", message));
                message = "";
                i = 0;
            }
        } catch (IOException e) {
            printDebug(String.format("IO Error/ Client %s terminated abruptly", getName()));
        } catch(NullPointerException e){
            printDebug(String.format("Client %s Closed", getName()));
        }


        finally {    
            try {
                printDebug("Connection Closing..");
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
        keepAliveThread.kill();
    }
}
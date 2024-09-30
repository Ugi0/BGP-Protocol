// echo server
package main.code;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import main.code.threads.ServerThread;

import static main.Main.*;

public class Server extends Thread {
    private int port;

    Socket socket = null;
    ServerSocket serverSocket = null;

    public Server(int portNum) {
        port = portNum;
    }

    public void run() {
        try {
            serverSocket = new ServerSocket(port); // can also use static final PORT_NUM , when defined
            printDebug(String.format("Router Listening on port %s", port));
        } catch(IOException e) {
            e.printStackTrace();
            printDebug("Server error");
            return;
        }

        while(true) {
            try {
                socket = serverSocket.accept();
                printDebug("connection Established");
    
                ServerThread st = new ServerThread(socket);
                st.start();

            } catch(Exception e) {
                e.printStackTrace();
                printDebug("Connection Error");
            }
        }

    }

    @Override
    public void interrupt() {
        try {
            socket.close();
        } catch (IOException ignored) {}
        try {
            serverSocket.close();
        } catch (IOException ignored) {}
        super.interrupt();
    }

}
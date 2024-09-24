// echo server
package main.code;

import java.io.IOException;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import main.code.threads.ServerThread;

import static main.Main.*;

public class Router extends Thread {
    private int port;
    private Integer[] connectionPorts;
    private Socket[] connections;

    Socket socket = null;
    ServerSocket serverSocket = null;

    public Router(int portNum) {
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

        int i = 0;
        for (Integer port : connectionPorts) {
            try (Socket socket = new Socket("localhost", port)) {
                printDebug(String.format("Connected to port %s", port));
                connections[i] = socket;
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                out.write(null); //Send message as bytes
        
                DataInputStream in = new DataInputStream(socket.getInputStream());
                in.readFully(null); //Read answer into a buffer
                
            } catch(IOException e) {
                e.printStackTrace();
                printDebug(String.format("Couldn't connect to port %s", port));
            }
            i++;
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

    public void setConnections(Integer[] ports) {
        connectionPorts = ports;
        connections = new Socket[ports.length];
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
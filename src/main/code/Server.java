package main.code;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import main.code.threads.ServerThread;

import static main.Main.*;

public class Server extends Thread {
    private String ip;

    Socket socket = null;
    ServerSocket serverSocket = null;

    public Server(String ipAdd) {
        ip=ipAdd;
    }

    public void run() {
        try {
            InetAddress addr = InetAddress.getByName(ip);
            serverSocket = new ServerSocket(8080,50,addr); // Make sure the client port is the same
            printDebug(String.format("Router using address %s", addr));
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
package main.code;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import main.code.threads.ServerThread;
import messages.Keepalive;
import messages.Message;
import messages.Open;

import java.util.List;
import java.util.ArrayList;

import static main.Main.*;

public class Server extends Thread {
    private String ip;

    Socket socket = null;
    ServerSocket serverSocket = null;

    List<ServerThread> connections;

    public Server(String ipAdd) {
        connections = new ArrayList<>();
        ip=ipAdd;
    }

    public void run() {
        try {
            InetAddress addr = InetAddress.getByName(ip);
            serverSocket = new ServerSocket(8080,50, addr); // Make sure the client port is the same
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
    
                ServerThread st = new ServerThread(socket, this);
                st.start();

            } catch(Exception e) {
                e.printStackTrace();
                printDebug("Connection Error");
            }
        }

    }

    public void handleMessage(Message received, ServerThread source) {
        if (received instanceof Open) {
            Open message = (Open) received;
            
            source.getConnectionManager().setKeepAliveMessage(new Keepalive().toBytes(), message.getHoldTime());

            source.getConnectionManager().writeToStream(new Open(0, 20, 0, 0, 0).toBytes());
            
            //Handle open message
            //Send a open message back to the sender and set up keepAlive messages

        }   
        //TODO handle receiving message
        //Message should be checked for instance of different message classes
        //Write response to source
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
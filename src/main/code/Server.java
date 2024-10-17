package main.code;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import main.code.threads.ServerThread;
import routing.Route;
import routing.RoutingInformationBase;
import routing.RoutingInformationBase.RouteUpdateType;
import messages.Keepalive;
import messages.Message;
import messages.Notification;
import messages.Open;
import messages.Update;

import java.util.List;

import java.util.ArrayList;

import static main.Main.*;

public class Server extends Thread {
    private String ip;

    Socket socket = null;
    ServerSocket serverSocket = null;

    List<ServerThread> connections;

    RoutingInformationBase routingTable;

    public Server(String ipAdd) {
        connections = new ArrayList<>();
        ip=ipAdd;

        byte[] ipArr = IpAddToIntArray(ipAdd);

        routingTable = new RoutingInformationBase(ipArr, ipArr[2]);
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

    /**
     * Handle server receiving a message
     * @param received
     * @param source
     */
    public void handleMessage(Message received, ServerThread source) {
        printDebug("Server received" + received);
        if (received instanceof Open) {
            Open message = (Open) received;
            
            source.getConnectionManager().setKeepAliveMessage(new Keepalive(), message.getHoldTime());

            source.getConnectionManager().writeToStream(new Open(0, 20, 0, 0, 0));

            handleRoutingTableChange(message, source);

        } else if (received instanceof Update) {
            handleRoutingTableChange((Update) received, source);
        } else if (received instanceof Notification) {
            //Some error happened
            //Either close connection or resend 
        }
    }

    /**
     * Handle changing of the server routing table when Open message is received
     * @param message
     * @param source
     */
    private void handleRoutingTableChange(Open message, ServerThread source) {
        //TODO Handle changing routing table
        routingTable.updateRoute(
            new Route(source.getSocketAddress(), new ArrayList<>(message.getAS()), source.getSocketAddress()),
            RouteUpdateType.ADD);

        //if (null) { //if routing table changed - change this
        //    handleSendingToConnections(null);
        //}
    }

    /**
     * Handle changing of the server routing table when Update message is received
     * @param message
     * @param source
     */
    private void handleRoutingTableChange(Update message, ServerThread source) {
        //TODO Handle changing routing table

        routingTable.updateRoute(
            new Route(message.getSource(), message.getAS(), message.getNextHop()),
            RouteUpdateType.ADD);

        //if (null) { //if routing table changed - change this
        //    handleSendingToConnections(null);
        //}

    }

    /**
     * Handle sending update messages to connected peers
     * This should only be sent if routing table has actually changed to not cause infinite loops
     * @param message
     */
    private void handleSendingToConnections(Message message) {
        //connections.forEach(e -> {
        //    e.getConnectionManager().writeToStream(message);
        //});
    }

    private byte[] IpAddToIntArray(String addr) {
        byte[] arr = new byte[4];
        int index = 0;
        for (String part : addr.split("\\.")) {
            arr[index++] = Byte.valueOf(part);
        }
        return arr;
    }

    public void printRoutingTable() {
        routingTable.print();
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
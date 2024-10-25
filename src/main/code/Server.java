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
import messages.Update.AttributeTypes;
import messages.Update.PathAttribute;

import java.util.List;

import java.util.ArrayList;
import java.util.Arrays;

import static main.Main.*;

public class Server extends Thread {
    private String ip;

    public byte AS;

    Socket socket = null;
    ServerSocket serverSocket = null;

    List<ServerThread> connections;

    RoutingInformationBase routingTable;

    Router parent;

    public Server(String ipAdd, Router parent) {
        connections = new ArrayList<>();
        this.parent = parent;
        ip=ipAdd;

        byte[] ipArr = IpAddToIntArray(ipAdd);

        AS = ipArr[2];

        routingTable = new RoutingInformationBase(ipArr, AS);
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
                connections.add(st);
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
        if (received instanceof Open) {
            Open message = (Open) received;
            
            source.getConnectionManager().setKeepAliveMessage(new Keepalive(), message.getHoldTime());

            source.getConnectionManager().writeToStream(new Open(AS, 20, 0, 0, 0));

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
        if(routingTable.updateRoute(
            new Route(source.getSocketAddress(), new ArrayList<>(Arrays.asList(message.getAS())), source.getSocketAddress()),
            RouteUpdateType.ADD)) {

            handleSendingToConnections(
                new Update(null, 
                new ArrayList<>(Arrays.asList(
                    new PathAttribute(AttributeTypes.AS_Path.getValue(), PathAttribute.initialLength)
                        .setValue(new byte[]{AS}),
                    new PathAttribute(AttributeTypes.Next_Hop.getValue(), PathAttribute.initialLength)
                        .setValue(new byte[]{AS}),
                    new PathAttribute(AttributeTypes.Origin.getValue(), PathAttribute.initialLength)
                        .setValue(new byte[]{AS})
                )), 
                null)
            );
        }
    }

    /**
     * Handle changing of the server routing table when Update message is received
     * @param message
     * @param source
     */
    private void handleRoutingTableChange(Update message, ServerThread source) {
        if(routingTable.updateRoute(
            new Route(message.getSource(), message.getAS(), message.getNextHop()),
            RouteUpdateType.ADD)) {

            handleSendingToConnections(message.addToAS(AS));
        }

    }

    /**
     * Handle sending update messages to connected peers
     * This should only be sent if routing table has actually changed to not cause infinite loops
     * @param message
     */
    private void handleSendingToConnections(Message message) {
        Arrays.asList(parent.getClients()).forEach(e -> 
            e.connectionManager.writeToStream(message)
        );
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
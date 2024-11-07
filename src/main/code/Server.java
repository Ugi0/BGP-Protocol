package main.code;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import main.code.threads.ConnectionContainer;
import main.code.threads.ConnectionManager;
import main.code.threads.ServerThread;
import main.code.threads.ConnectionContainer.STATE;
import routing.Route;
import routing.RoutingInformationBase;
import messages.Keepalive;
import messages.Message;
import messages.IpPacket;
import messages.Notification;
import messages.Open;
import messages.Update;
import messages.Update.AttributeTypes;
import messages.Update.PathAttribute;
import messages.Update.RouteInformation;

import java.util.List;

import java.util.ArrayList;
import java.util.Arrays;
import java.io.ByteArrayOutputStream;

import static main.Main.*;

public class Server extends Thread {
    private String ip;

    public byte AS;

    private byte[] socketAddress;

    ServerSocket serverSocket = null;

    List<ServerThread> connections;

    RoutingInformationBase routingTable;

    public Router parent;

    public Server(String ipAdd, Router parent) {
        connections = new ArrayList<>();
        this.parent = parent;
        ip=ipAdd;

        socketAddress = IpAddToIntArray(ipAdd);

        AS = socketAddress[2];

        routingTable = new RoutingInformationBase(socketAddress, AS);
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
                Socket socket = serverSocket.accept();
                printDebug("connection Established");
    
                ServerThread st = new ServerThread(socket, this);
                connections.add(st);
                st.start();

            } catch(SocketException e){
                printDebug("Can't accept connection, server is closed");
                break;
            }
            
            catch(Exception e) {
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
    public void handleMessage(Message received, ConnectionContainer source) {
        if (received instanceof Keepalive) {
            if (source.getConnectionState() == STATE.OPEN_SENT) source.setState(STATE.ESTABLISHED);
        } else if (received instanceof Open) {
            if (source.getConnectionState() != STATE.IDLE) return;
            Open message = (Open) received;
            
            source.getConnectionManager().setKeepAliveMessage(new Keepalive(), message.getHoldTime());

            source.getConnectionManager().writeToStream(new Keepalive());
            source.getConnectionManager().writeToStream(new Open(AS, 20, 0, 0, 0));

            source.setState(STATE.OPEN_SENT);

            handleRoutingTableChange(message, source);

        } else if (received instanceof Notification) {
            Notification message = (Notification) received;

            if (message.getError() == Notification.ErrorCode.Cease.getValue()) {
                source.shutdown();
            }
        } else if (received instanceof Update) {
            if (source.getConnectionState() != STATE.ESTABLISHED) return;

            handleRoutingTableChange((Update) received);
        } else if (received instanceof IpPacket) {
            if (source.getConnectionState() != STATE.ESTABLISHED) return;

            IpPacket message = (IpPacket) received;

            if (message.getDestination().equals(parent.getRouterAddress())) {
                //Received a packet meant for me
                System.out.println(String.format("Router %s received message %s", AS, message.getData()));
            } else {
                if (message.verifyCheckSum()) {
                    printDebug("Message checksum was not valid");
                    return;
                }
                if (message.getTimeToLive() == 0) return;
                message.decreateTimeToLive();
                System.out.println(String.format("Router %s received message which it will pass on", AS));
                //Pass it to the next hop
                String nextHop = routingTable.getTable().getNextHop(message.getDestination());

                for (ConnectionManager connection : parent.getConnections()) {
                    if (connection.getAddress().equals(nextHop)) {
                        connection.writeToStream(message);
                    }
                }
            }
        }
    }

    /**
     * Handle changing of the server routing table when Open message is received
     * @param message
     * @param source
     */
    public void handleRoutingTableChange(Open message, ConnectionContainer source) {
        byte[] connectedAddress = new byte[]{127,0,(byte) message.getAS(),0};
        if(routingTable.addRoute(
            new Route(connectedAddress, new ArrayList<>(Arrays.asList(message.getAS())), connectedAddress))) {

            synchronized(routingTable.getAdvertisedRoutes()) {
                for (Route route : routingTable.getAdvertisedRoutes()) {
                    source.getConnectionManager().writeToStream(new Update(null, 
                        new ArrayList<>(Arrays.asList(
                            new PathAttribute(AttributeTypes.AS_Path.getValue(), route.AS_PATH.size())
                                .setValue(route.AS_PATH.stream().collect(() -> new ByteArrayOutputStream(), (baos, i) -> baos.write((byte) i.intValue()), (baos1, baos2) -> {}).toByteArray()),
                            new PathAttribute(AttributeTypes.Next_Hop.getValue(), 4)
                                .setValue(new byte[]{127, 0, AS, 0}),
                            new PathAttribute(AttributeTypes.Origin.getValue(), 4)
                                .setValue(route.destinationAddress)
                        )), 
                        null));
                }
            }

            handleSendingToConnections(
                new Update(null, 
                new ArrayList<>(Arrays.asList(
                    new PathAttribute(AttributeTypes.AS_Path.getValue(), 2)
                        .setValue(new byte[]{(byte) message.getAS(), AS}),
                    new PathAttribute(AttributeTypes.Next_Hop.getValue(), 4)
                        .setValue(new byte[]{127, 0, AS, 0}),
                    new PathAttribute(AttributeTypes.Origin.getValue(), 
                    4)
                        .setValue(new byte[]{127, 0, (byte) message.getAS(), 0})
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
    private void handleRoutingTableChange(Update message) {
        for (RouteInformation route : message.getWithdrawnRoutes()) {
            List<Route> removedRoutes = routingTable.removeRoute(route.getPrefixes().get(0));

            List<RouteInformation> removedRouteInformations = new ArrayList<>();
            for (Route r : removedRoutes) {
                RouteInformation rf = new RouteInformation(r.AS_PATH.size());
                for (Integer AS : r.AS_PATH) {
                    rf.addToPrefix(AS.byteValue());
                }
                removedRouteInformations.add(rf);
            }

            handleSendingToConnections(new Update(removedRouteInformations, null, null));
        }

        if(routingTable.addRoute(
            new Route(message.getSource(), message.getAS(), message.getNextHop()))) {

            handleSendingToConnections(message.addToAS(AS).setNextHop(socketAddress));
        }

    }

    /**
     * Handle sending update messages to connected peers
     * This should only be sent if routing table has actually changed to not cause infinite loops
     * @param message
     */
    public void handleSendingToConnections(Update message)  {
        synchronized(parent.getConnections()) {

            parent.getConnections().forEach(e -> {
                if (e != null) {
                    e.writeToStream(message);
                }
            }
            );
        }
    }

    public void removeFromRoutingTable(String ipAddr) {
        Integer removedAS = Integer.valueOf(routingTable.getAS(ipAddr));
        List<Route> removedRoutes = routingTable.removeRoute(removedAS);

        List<RouteInformation> removedRouteInformations = new ArrayList<>();
        for (Route r : removedRoutes) {
            RouteInformation rf = new RouteInformation(r.AS_PATH.size());
            for (Integer AS : r.AS_PATH) {
                rf.addToPrefix(AS.byteValue());
            }
            removedRouteInformations.add(rf);
        }

        handleSendingToConnections(new Update(removedRouteInformations, null, null));
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

    public void printStates() {
        System.out.println("Server connections:");
        for (ServerThread connection : connections) {
            System.out.println(connection.state);
        }
    }

    public void shutdown() {
        printDebug(String.format("Server %s is shutting down", AS));
        routingTable.empty();
        for (ServerThread connection : connections) {
            connection.shutdown();
        }
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ignored) {}
        }
        interrupt();
    }

    public void killGracefully() {
        printDebug(String.format("Server %s is shutting down gracefully", AS));
        routingTable.empty();
        for (ServerThread connection : connections) {
            connection.getConnectionManager().writeToStream(new Notification(Notification.ErrorCode.Cease.getValue(), 0, null));
            connection.shutdown();
        }
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ignored) {}
        }
        interrupt();
    }

}
package main.code;

import static main.Main.printDebug;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import main.code.threads.ConnectionManager;

public class Router {
    private String serverAddess;
    private Server server;
    public final Integer ownAS;
    private String[] clientAddresses;
    private Client[] clients;

    private List<ConnectionManager> connections;

    public Router(String address, String[] connectionAddresses, Integer ownAS) {
        serverAddess = address;
        this.ownAS = ownAS;
        this.clientAddresses = connectionAddresses;
        clients = new Client[connectionAddresses.length];

        connections = Collections.synchronizedList(new ArrayList<>());

        createServerThread();
        createClientThreads();
    }

    public void addToConnections(ConnectionManager connection) {
        connections.add(connection);
    }

    public List<ConnectionManager> getConnections() {
        return this.connections;
    }

    private void createServerThread() {
        printDebug(String.format("Starting server thread on address %s", serverAddess));
        server = new Server(serverAddess, this);
        server.start();
    }

    private void createClientThreads() {
        int i = 0;
        for (String ip : clientAddresses) {
            clients[i] = new Client(ip, ownAS, this);
            i++;
        }
        for (int j = 0; j < i; j++) {
            clients[j].start();
        }    
    }

    public Client[] getClients() {
        return clients;
    }

    public Server getServer() {
        return server;
    }

    public void printRoutingTable() {
        server.printRoutingTable();
    }

    public void removeFromRoutingTable(String ipAddr) {
        server.removeFromRoutingTable(ipAddr);
    }

    public void kill() {
        server.interrupt();
        for (Client client : clients) {
            client.interrupt();
        }
    }

}

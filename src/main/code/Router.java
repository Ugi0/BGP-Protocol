package main.code;

import static main.Main.printDebug;

public class Router {
    private String serverAddess;
    private Server server;
    private String[] connectionAddresses;
    private Client[] connections;

    public Router(String address, String[] connectionAddresses) {
        serverAddess = address;
        this.connectionAddresses = connectionAddresses;
        connections = new Client[connectionAddresses.length];

        createServerThread();
        createClientThreads();
    }

    private void createServerThread() {
        printDebug(String.format("Starting server thread on address %s", serverAddess));
        server = new Server(serverAddess);
        server.start();
    }

    private void createClientThreads() {
        int i = 0;
        for (String ip : connectionAddresses) {
            connections[i] = new Client(ip);
            i++;
        }
        for (int j = 0; j < i; j++) {
            connections[j].start();
        }    
    }

    public void kill() {
        server.interrupt();
        for (Client client : connections) {
            client.interrupt();
        }
    }

}

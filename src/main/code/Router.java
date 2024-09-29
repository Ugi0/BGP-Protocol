package main.code;

import static main.Main.printDebug;

public class Router {
    private int serverPort;
    private Server server;
    private Integer[] connectionPorts;
    private Client[] connections;

    public Router(int portNum, Integer[] connectionPorts) {
        serverPort = portNum;
        this.connectionPorts = connectionPorts;
        connections = new Client[connectionPorts.length];

        createServerThread();
        createClientThreads();
    }

    private void createServerThread() {
        printDebug(String.format("Starting server thread on port %s", serverPort));
        server = new Server(serverPort);
        server.start();
    }

    private void createClientThreads() {
        int i = 0;
        for (Integer port : connectionPorts) {
            connections[i] = new Client(port);
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

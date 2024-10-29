package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream; 

import main.code.Router;

public class Main {
    public static boolean debug = true;
    private static int routerCount = 0;
    private static HashMap<Integer, Integer[]> connections = new HashMap<>();
    private static List<Router> routers;
    public static void main(String[] args) {

        routers = new ArrayList<>();

        parseConfig();

        for (int i = 1; i <= routerCount; i++) {
            final int ownAS = i;
            routers.add(new Router(String.format("127.0.%s.0", ownAS), 
                Arrays.stream(connections.get(ownAS)).map(e -> String.format("127.0.%s.0", e)).toArray(String[]::new), 
                ownAS)
            );
        }

        Scanner scanner = new Scanner(System.in);
        try {
            while (true) {
                String command = scanner.nextLine();
                handleCommand(command);
            }
        } finally {
            scanner.close();
            System.out.println("Stopping threads");
            for (Router router : routers) {
                router.kill();
            }
        }
    }

    private static Integer[] parseInt(String value) {
        if (value.strip().equals("")) {
            return new Integer[0];
        }
        return Stream.of(value.split(",")).map(e -> Integer.parseInt(e.strip())).toArray(Integer[]::new);
    }

    public static void printDebug(Object message) {
        if (debug) {
            System.out.println(message.toString());
        }
    }

    /**
     * Handle command instructions from terminal
     * @param command
     */
    private static void handleCommand(String command) {
        command = command.toLowerCase();
        String[] stringParts = command.split(" ");
        switch (stringParts[0]) {
            case "get":
                if (stringParts.length == 1) return;
                switch (stringParts[1]) {
                    case "routing":
                        for (Router router : routers) {
                            router.printRoutingTable();
                        }
                        break;
                
                    default:
                        printDebug(stringParts[1]);
                        break;
                }
                break;
        
            default:
                printDebug(stringParts[0]);
                break;
        }
        //TODO
    }

    private static void parseConfig() {
        boolean connectionsMarker = false;
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader("src/main/.config"));
            String line = reader.readLine();                                               
			while (line != null) {
                if (line.isEmpty() || line.startsWith("#")) {
                    line = reader.readLine();
                    continue;
                }
                String[] temp = line.split(":");
                switch (temp[0]) {
                    case "Routers":
                        routerCount = Integer.parseInt(temp[1].strip());
                        break;
                    case "PortConnections":
                        connectionsMarker = true;
                        break;
                
                    default:
                        if (connectionsMarker) {
                            if (temp.length != 1) {
                                connections.put(Integer.parseInt(temp[0]), parseInt(temp[1]));
                            } else {
                                connections.put(Integer.parseInt(temp[0]), new Integer[0]);
                            }
                        }
                        break;
                }
                line = reader.readLine();
			}
			reader.close();
        } catch (IOException e) {
            printDebug("Failed to parse config");
            e.printStackTrace();
        }
    }
}
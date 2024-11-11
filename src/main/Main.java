package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import config.Config;
import main.code.Router;
import main.code.Visualizer;
import messages.IpPacket;

public class Main {
    public static boolean debug = false;
    private static List<Router> routers;
    private static Visualizer visualizer;
    public static void main(String[] args) {

        new Config();
        visualizer = new Visualizer(Config.connections);
        visualizer.printIntro();
        visualizer.printMap();
        

        routers = new ArrayList<>();

        for (int i = 1; i <= Config.routerCount; i++) {
            final int ownAS = i;
            routers.add(new Router(String.format("127.0.%s.0", ownAS), 
                Config.connections.get(ownAS).stream().map(e -> String.format("127.0.%s.0", e)).toArray(String[]::new), 
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
            case "visualize": 
                //DEBUG
                System.out.println("hashmap: ");
                for (HashMap.Entry<Integer, List<Integer>> entry : Config.connections.entrySet()) {
                    Integer key = entry.getKey();
                    List<Integer> valueArray = entry.getValue();
                    System.out.println("Key: " + key + ", Value: " + String.join(", ", valueArray.toString()));
                }

                visualizer.printMap();
                break;
            case "get":
                if (stringParts.length == 1) return;
                switch (stringParts[1]) {
                    case "routing":
                        for (Router router : routers) {
                            router.printRoutingTable();
                        }
                        break;
                    case "states":
                        for (Router router : routers) {
                            router.printStates();
                        }
                    default:
                        printDebug("Invalid argument. Type \"help\" for the list of commands.");
                }
                break;
            
            case "shutdown":
                if (stringParts.length == 1){
                    System.out.println("Missing an argument. Type \"help\" for the list of commands.");
                }
                if (stringParts.length == 2){
                    try {
                        int routerNumber = Integer.parseInt(stringParts[1]);
                        routers.stream().filter(e -> e.ownAS == routerNumber).forEach(e -> e.kill());
                        visualizer.shutRouter(routerNumber);
                    }
                    catch (NumberFormatException e){
                        printDebug("Invalid argument. Type \"help\" for the list of commands.");
                    }
                }

                if (stringParts.length==3){
                    switch (stringParts[1]) {
                        case "gracefully":
                            try {
                                int routerNumber = Integer.parseInt(stringParts[2]);
                                routers.stream().filter(e -> e.ownAS == routerNumber).forEach(e -> e.killGracefully());
                                visualizer.shutRouter(routerNumber);
                            }
                            catch (NumberFormatException e) {
                                printDebug("Invalid argument. Type \"help\" for the list of commands.");
                            }
                    }
                }
                break;

            case "help":
                if (stringParts.length==1) {
                    System.out.println("Available commands:\n");
                    System.out.printf("%-30s %s%n","get routing","- Prints routing tables.");
                    System.out.printf("%-30s %s%n","visualize","- Prints router map");
                    System.out.printf("%-30s %s%n","shutdown (number)","- kill router without notification.");
                    System.out.printf("%-30s %s%n","shutdown gracefully (number)","- kill router with notification.\n");
                } else {
                    System.out.println("Invalid argument. Type \"help\" for the list of commands");
                }
                break;

            case "send":
                String message = stringParts[1];
                byte[] source = new byte[4];
                byte[] destination = new byte[4];
                String sourceString = stringParts[2];
                String destinationString = stringParts[3];

                int index = 0;
                for (String s : sourceString.split("\\.")) {
                    source[index++] = Integer.valueOf(s).byteValue();
                }
                index = 0;
                for (String s : destinationString.split("\\.")) {
                    destination[index++] = Integer.valueOf(s).byteValue();
                }

                for (Router r : routers) {
                    if (r.getRouterAddress().equals(sourceString)) {
                        r.getServer().handleMessage(
                            new IpPacket(source, destination, message.getBytes()), 
                            null);
                    }
                }
                break;
        
            default:
                printDebug("Invalid command. Type \"help\" for the list of commands");
                break;
        }
    }

}
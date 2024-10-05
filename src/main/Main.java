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
    public static void main(String[] args) {
        BufferedReader reader;
        int routerCount = 0;
        HashMap<Integer, Integer[]> connections = new HashMap<>();
        boolean connectionsMarker = false;

        List<Router> routers = new ArrayList<>();

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

            for (int i = 0; i < routerCount; i++) {
                routers.add(new Router(String.format("127.0.%s.0", i+1), 
                    Arrays.stream(connections.get(i+1)).map(e -> String.format("127.0.%s.0", e)).toArray(String[]::new)));
            }

            Scanner scanner = new Scanner(System.in);
            try {
                while (true) {
                    //System.out.println("Command: ");
                    String command = scanner.nextLine();
                    //TODO handle commands somehow here
                }
            } finally {
                scanner.close();
                System.out.println("Stopping threads");
                for (Router router : routers) {
                    router.kill();
                }
            }
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    private static Integer[] parseInt(String value) {
        if (value.strip().equals("")) {
            return new Integer[0];
        }
        return Stream.of(value.split(",")).map(e -> Integer.parseInt(e.strip())).toArray(Integer[]::new);
    }

    public static void printDebug(String message) {
        if (debug) {
            System.out.println(message);
        }
    }
}
package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

import main.code.Router;

public class Main {
    public static boolean debug = true;
    private static int portRangeStart = 0;
    public static String[] addressList = {"127.0.0.1","127.0.0.2","127.0.0.3"};
    public static void main(String[] args) {
        BufferedReader reader;
        int routerCount = 0;
        HashMap<Integer, Integer[]> portConnections = new HashMap<>();
        boolean portConnectionsMarker = false;

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
                    
                    case "PortRangeStart":
                        portRangeStart = Integer.parseInt(temp[1].strip());
                        break;
                    case "PortConnections":
                        portConnectionsMarker = true;
                        break;
                
                    default:
                        if (portConnectionsMarker) {
                            if (temp.length != 1) {
                                portConnections.put(Integer.parseInt(temp[0]), parseInt(temp[1]));
                            } else {
                                portConnections.put(Integer.parseInt(temp[0]), new Integer[0]);
                            }
                        }
                        break;
                }
                line = reader.readLine();
			}
			reader.close();

            for (int i = 0; i < routerCount; i++) {
                routers.add(new Router(addressList[i], addressList)); //Assume port is free
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
        return Stream.of(value.split(",")).map(e -> portRangeStart - 1 + Integer.parseInt(e.strip())).toArray(Integer[]::new);
    }

    public static void printDebug(String message) {
        if (debug) {
            System.out.println(message);
        }
    }
}
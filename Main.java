import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) {
        BufferedReader reader;
        int routerCount = 0;
        int portRangeStart = 0;
        HashMap<Integer, Integer[]> portConnections = new HashMap<>();
        boolean portConnectionsMarker = false;

        List<Router> routers = new ArrayList<>();

		try {
			reader = new BufferedReader(new FileReader(".config"));
			String line = reader.readLine();
			while (line != null) {
                if (line.isEmpty()) {
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
                                portConnections.put(Integer.parseInt(temp[0]), Stream.of(temp[1].split(",")).map(e -> Integer.parseInt(e.strip())).toArray(Integer[]::new));
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
                routers.add(new Router(portRangeStart+i)); //Assume port is free
                routers.get(i).setConnections(portConnections.get(i));
            }
            for (Router router : routers) {
                router.start();
            }

            Scanner scanner = new Scanner(System.in);
            try {
                while (true) {
                    System.out.println("Command: ");
                    String command = scanner.nextLine();
                    //TODO handle commands somehow here
                }
            } finally {
                scanner.close();
                System.out.println("Stopping threads");
                for (Router router : routers) {
                    router.interrupt();
                }
            }
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
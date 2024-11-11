package config;

import static main.Main.printDebug;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Config {
    public static int routerCount;
    public static HashMap<Integer, List<Integer>> connections = new HashMap<>();
    public static int timeout;

    public Config() {
        parseConfig();
    }

    private void parseConfig() {
        boolean connectionsMarker = false;
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader("config/.config"));
            String line = reader.readLine();                                               
			while (line != null) {
                if (line.isEmpty() || line.startsWith("#")) {
                    line = reader.readLine();
                    continue;
                }
                if (line.startsWith("Routers")) {
                    routerCount = Integer.parseInt(line.split(":")[1].strip());
                    line = reader.readLine();
                    continue;
                }
                if (line.startsWith("Timeout")) {
                    timeout = Integer.parseInt(line.split(":")[1].strip());
                    line = reader.readLine();
                    continue;
                }
                if (line.startsWith("PortConnections")) {
                    connectionsMarker = true;
                    line = reader.readLine();
                    continue;
                }
                String[] temp = line.split("-");

                if (connectionsMarker) {
                    if (connections.containsKey(Integer.parseInt(temp[0]))) {
                        connections.get(Integer.parseInt(temp[0])).add(Integer.valueOf(temp[1]));
                    } else {
                        connections.put(Integer.parseInt(temp[0]), new ArrayList<>());
                        connections.get(Integer.parseInt(temp[0])).add(Integer.valueOf(temp[1]));
                    }
                    if (connections.containsKey(Integer.parseInt(temp[1]))) {
                        connections.get(Integer.parseInt(temp[1])).add(Integer.valueOf(temp[0]));
                    } else {
                        connections.put(Integer.parseInt(temp[1]), new ArrayList<>());
                        connections.get(Integer.parseInt(temp[1])).add(Integer.valueOf(temp[0]));
                    }
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

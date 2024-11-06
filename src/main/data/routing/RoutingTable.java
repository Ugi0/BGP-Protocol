package routing;

import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;
import java.nio.ByteBuffer;

public class RoutingTable{
	private ConcurrentHashMap<ByteBuffer, Byte[]> addresses; //destinationAddress as key and nextHop as value
	
	public RoutingTable(){
		addresses = new ConcurrentHashMap<>();
	}
	
	public String getNextHop(String destinationAddress) {
		Byte[] destination = new Byte[4];
		int index = 0;
		for (String s : destinationAddress.split("\\.")) {
			destination[index++] = Byte.valueOf(Integer.valueOf(s).byteValue());
		}
		Byte[] nextHop = addresses.get(ByteBuffer.wrap(toPrimitive(destination)));
		StringBuilder nextHopString = new StringBuilder();
        for (int i = 0; i < nextHop.length; i++) {
            // Convert byte to an unsigned integer and append to StringBuilder
            nextHopString.append(nextHop[i] & 0xFF);  
            if (i < nextHop.length - 1) {
                nextHopString.append(".");  // Add dots between the numbers
            }
        }
		return nextHopString.toString();
	}
	
	protected void addRoute(Route route) {
		addresses.put(
			ByteBuffer.wrap(route.destinationAddress),
			toByteArray(route.nextHop)
		);
	}

	public void print() {
		System.out.println(String.format("Routingtable addresses: %s", 
			addresses.entrySet().stream()
			.map(e -> String.format("%s: %s", Route.byteArrayToString(e.getKey().array()), Route.byteArrayToString(e.getValue()))).collect(Collectors.toList()).toString())
		);
	}
	
	protected boolean removeRoute(Route route) {
		boolean tableChanged = false;
		if (addresses.containsKey(ByteBuffer.wrap(route.destinationAddress))) {
			addresses.remove(ByteBuffer.wrap(route.destinationAddress));
			tableChanged = true;
		}
		return tableChanged;
	}

	private Byte[] toByteArray(byte[] bytes) {
		Byte[] byteObjects = new Byte[bytes.length];
		int i = 0;
		for (byte b : bytes) {
			byteObjects[i++] = b;
		}
		return byteObjects;
	}

	private static byte[] toPrimitive(Byte[] byteObjectArray) {
        byte[] byteArray = new byte[byteObjectArray.length];
        for (int i = 0; i < byteObjectArray.length; i++) {
            byteArray[i] = byteObjectArray[i];
        }
        return byteArray;
    }
	
}
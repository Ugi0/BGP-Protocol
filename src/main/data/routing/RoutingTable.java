package routing;

import java.util.HashMap;
import java.util.stream.Collectors;

public class RoutingTable{
	private HashMap<Byte[], Byte[]> addresses; //destinationAddress as key and nextHop as value
	
	public RoutingTable(){
		addresses = new HashMap<>();
	}
	
	public Byte[] getNextHop(byte[] destinationAddress) {
		Byte[] nextHop = addresses.get(
			toByteArray(destinationAddress)
		);
		return nextHop;
	}
	
	protected void addRoute(Route route) {
		addresses.put(
			toByteArray(route.destinationAddress),
			toByteArray(route.nextHop)
		);
	}

	public void print() {
		System.out.println(String.format("Routingtable addresses: %s", 
			addresses.entrySet().stream()
			.map(e -> String.format("%s: %s", Route.byteArrayToString(e.getKey()), Route.byteArrayToString(e.getValue()))).collect(Collectors.toList()).toString())
		);
	}
	
	protected boolean removeRoute(Route route) {
		boolean tableChanged = false;
		if (addresses.containsKey(toByteArray(route.destinationAddress))) {
			addresses.remove(toByteArray(route.destinationAddress));
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
	
}
package main.data.routing;

import java.util.HashMap;

public class RoutingTable{
	private HashMap<Integer, Integer> addresses; //destinationAddress as key and nextHop as value
	
	public RoutingTable(){
		addresses = new HashMap<>();
	}
	
	public int getNextHop(int destinationAddress) {
		int nextHop = addresses.get(destinationAddress);
		return nextHop;
	}
	
	protected void addRoute(Route route) {
		addresses.put(route.destinationAddress, route.nextHop);
	}
	
	protected void removeRoute(Route route) {
		addresses.remove(route.destinationAddress);
	}
	
}
package routing;

import java.util.HashMap;

public class RoutingTable{
	private HashMap<Integer, Integer> addresses; //destinationAddress as key and nextHop as value
	
	public RoutingTable(RoutingInformationBase base){
		addresses = new HashMap<>();
		for (int i = 0; i < base.LocRIB.size(); i++) {
			addresses.put(base.LocRIB.get(i).destinationAddress, base.LocRIB.get(i).nextHop);
		}
	}
	
	public int getNextHop(int destinationAddress) {
		int nextHop = addresses.get(destinationAddress);
		return nextHop;
	}
	
	//update should be only those routes that are added in LocRib???
	protected void updateRoute(Route route, int type) {
		if (type == 0) {
			removeRoute(route);
		}
		else if (type == 1){
			addRoute(route);
		}
	}
	
	private addRoute(Route route) {
		addresses.put(route.destinationAddress, route.nextHop);
	}
	
	private removeRoute(Route route) {
		addresses.remove(route.destinationAddress);
	}
	
}
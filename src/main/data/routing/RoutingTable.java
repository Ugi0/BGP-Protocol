package routing;

import java.util.HashMap;
import java.util.stream.IntStream;

public class RoutingTable{
	private HashMap<Integer[], Integer[]> addresses; //destinationAddress as key and nextHop as value
	
	public RoutingTable(){
		addresses = new HashMap<>();
	}
	
	public Integer[] getNextHop(int destinationAddress) {
		Integer[] nextHop = addresses.get(
			IntStream.of( destinationAddress ).boxed().toArray( Integer[]::new )
		);
		return nextHop;
	}
	
	protected void addRoute(Route route) {
		addresses.put(
			IntStream.of( route.destinationAddress ).boxed().toArray( Integer[]::new ),
			IntStream.of( route.nextHop ).boxed().toArray( Integer[]::new )
		);
	}
	
	protected void removeRoute(Route route) {
		addresses.remove(IntStream.of( route.destinationAddress ).boxed().toArray( Integer[]::new ));
	}
	
}
package routing;
import java.util.ArrayList;
import java.util.List;

public class RoutingInformationBase{
	List<Route> AdjRIBsIn; //All routes received with OPEN or UPDATE messages
	List<Route> LocRIB; //Only best routes (shortest AS_PATH) filtered from AdjRIBsIn
	List<Route> AdjRIBsOut; //Routes which are advertised to neighbourgs
	
	public RoutingInformationBase() {
		AdjRIBsIn = new ArrayList<Route>();
		LocRIB = new ArrayList<Route>();
		AdjRIBsOut = new ArrayList<Route>();
	}
	
	private void addRoute(Route route) {
		AdjRIBsIn.add(route);
	}
	
	private void removeRoute(Route route) {
		AdjRIBsIn.remove(route);
		LocRIB.remove(route);
		AdjRIBsOut.remove(route);
	}
	
	protected void updateRoute(Route route, int type) {
		//Type 0 means removing and 1 means adding the route
		if (type == 0) {
			removeRoute(route);
		}
		else if (type == 1){
			filter(route); //Filtering first so the new route is not compared with itself
			addRoute(route);
		}
	}
	
	private void filter(Route route) {
		//decision process if the route should be added to LocRIB and AdjRIBsOut or only to AdjRIBsIn
		//comparing AS_PATH length with every route in AdjRIBsIn with same destination
		//If new route is better than already existing one, need to remove the old one and add new one
		Route bestRoute = null;
		ArrayList<Route> oldRoutes = new ArrayList<Route>();;
		int sameDestinations = 0;
		for (int i = 0; i < AdjRIBsIn.size(); i++) {
			if (AdjRIBsIn.get(i).destinationAddress == route.destinationAddress) {
				sameDestinations =+ 1;
				if (AdjRIBsIn.get(i).AS_PATH.length > route.AS_PATH.length) {
					bestRoute = route;
					oldRoutes.add(AdjRIBsIn.get(i));
				} else {
					bestRoute = AdjRIBsIn.get(i);
				}
			}
		}
		if (sameDestinations == 0) {
			LocRIB.add(route);
			AdjRIBsOut.add(route);
		}
		else if (bestRoute.equals(route)) {
			for (int j = 0; j < oldRoutes.size(); j++) {
				removeRoute(oldRoutes.get(j));
			}
			LocRIB.add(route);
			AdjRIBsOut.add(route);
		}
	}
}
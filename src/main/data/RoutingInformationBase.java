package main.data
import java.util.ArrayList;

public class RoutingInformationBase{
	ArrayList<Route> AdjRIBsIn; //All routes received with OPEN or UPDATE messages
	ArrayList<Route> LocRIB; //Only best routes (shortest AS_PATH) filtered from AdjRIBsIn
	ArrayList<Route> AdjRIBsOut; //Routes which are advertised to neighbourgs
	
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
		elif (type == 1){
			filter(route); //Filtering first so the new route is not compared with itself
			addRoute(route);
		}
	}
	
	private void filter(Route route) {
		//decision process if the route should be added to LocRIB and AdjRIBsOut or only to AdjRIBsIn
		//comparing AS_PATH length with every route in AdjRIBsIn with same destination
		//If new route is better than already existing one, need to remove the old one and add new one
		Route bestRoute;
		ArrayList<Route> oldRoutes = new ArrayList<Route>();;
		int sameDestinations = 0;
		for (int i = 0; i < AdjRIBsIn.size(); i++) {
			if (AdjRIBsIn[i].destinationAddress.equals(route.destinationAddress)) {
				sameDestinations =+ 1;
				if (AdjRIBsIn[i].AS_PATH.length > route.AS_PATH.length)
					bestRoute = route;
					oldRoutes.add(AdjRIBsIn[i]);
				else
					bestRoute = AdjRIBsIn[i];
			}
		}
		if (sameDestinations == 0) {
			LocRIB.add(route);
			AdjRIBsOut.add(route);
		}
		else if (bestRoute.equals(route)) {
			for (int j = 0; j < oldRoutes.size(); i++) {
				removeRoute(oldRoutes[i]);
			}
			LocRIB.add(route);
			AdjRIBsOut.add(route);
		}
	}
}
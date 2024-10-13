package main.data.routing;
import java.util.ArrayList;
import java.util.List;

public class RoutingInformationBase{
	int ownASN;
	List<Route> AdjRIBsIn; //All routes received with OPEN or UPDATE messages
	List<Route> LocRIB; //Only best routes (shortest AS_PATH) filtered from AdjRIBsIn
	List<Route> AdjRIBsOut; //Routes which are advertised to neighbours
	RoutingTable routingTable;
	
	public RoutingInformationBase(int ownAddress, int ownASN) {
		this.ownASN = ownASN;
		AdjRIBsIn = new ArrayList<Route>();
		LocRIB = new ArrayList<Route>();
		AdjRIBsOut = new ArrayList<Route>();
		routingTable = new RoutingTable();
		//Adds the route to the router itself since it is the only one known before connecting
		ArrayList<Integer> defaultPath = new ArrayList<Integer>();
		defaultPath.add(ownASN);
		Route defaultRoute = new Route(ownAddress, defaultPath, ownAddress);
		AdjRIBsIn.add(defaultRoute);
		LocRIB.add(defaultRoute);
		AdjRIBsOut.add(defaultRoute);
		routingTable.addRoute(defaultRoute);
	}
	
	private void addRoute(Route route) {

		for (int ASN : route.AS_PATH) {
			if (ASN == ownASN) {
				return;
			}
		}
		route.AS_PATH.addFirst(ownASN);
		filter(route);
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
        for (Route r : AdjRIBsIn) {
            if (r.destinationAddress == route.destinationAddress) {
                sameDestinations = +1;
                if (r.AS_PATH.size() > route.AS_PATH.size()) {
                    bestRoute = route;
                    oldRoutes.add(r);
                } else {
                    bestRoute = r;
                }
            }
        }
		if (sameDestinations == 0) {
			LocRIB.add(route);
			AdjRIBsOut.add(route);
			routingTable.addRoute(route);
		}
		else if (bestRoute.equals(route)) {
            for (Route oldRoute : oldRoutes) {
                removeRoute(oldRoute);
				routingTable.removeRoute(oldRoute);
            }
			LocRIB.add(route);
			AdjRIBsOut.add(route);
			routingTable.addRoute(route);
		}
	}
}
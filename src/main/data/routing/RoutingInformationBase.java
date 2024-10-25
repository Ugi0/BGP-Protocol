package routing;
import java.util.ArrayList;
import java.util.List;

public class RoutingInformationBase{
	int ownASN;
	List<Route> AdjRIBsIn; //All routes received with OPEN or UPDATE messages
	List<Route> LocRIB; //Only best routes (shortest AS_PATH) filtered from AdjRIBsIn
	List<Route> AdjRIBsOut; //Routes which are advertised to neighbours
	RoutingTable routingTable;
	
	public RoutingInformationBase(byte[] ownAddress, int ownASN) {
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

	public void print() {
		System.out.println(String.format("ownASN: %s", ownASN));
		System.out.println(String.format("AdjRIBsIn: %s", AdjRIBsIn.toString()));
		System.out.println(String.format("LocRIB: %s", LocRIB.toString()));
		System.out.println(String.format("AdjRIBsOut: %s", AdjRIBsOut.toString()));
		routingTable.print();
	}
	
	private boolean addRoute(Route route) {

		boolean tableChanged;
		for (int ASN : route.AS_PATH) {
			if (ASN == ownASN) {
				return false;
			}
		}
		route.AS_PATH.add(ownASN);
		tableChanged = filter(route);
		AdjRIBsIn.add(route);
		return tableChanged;
	}
	
	private boolean removeRoute(Route route) {
		boolean tableChanged;
		if (AdjRIBsIn.contains(route)){
			AdjRIBsIn.remove(route);
		}
		if (LocRIB.contains(route)){
			LocRIB.remove(route);
		}
		if (AdjRIBsOut.contains(route)) {
			AdjRIBsOut.remove(route);
		}
		tableChanged = routingTable.removeRoute(route);
		return tableChanged;
	}
	
	public boolean updateRoute(Route route, RouteUpdateType type) {
		if (type.equals(RouteUpdateType.REMOVE)) {
			return removeRoute(route);
		}
		else if (type.equals(RouteUpdateType.ADD)){
			return addRoute(route);
		}
		return false;
	}

	public enum RouteUpdateType {
		ADD, REMOVE
	}
	
	private boolean filter(Route route) {
		//decision process if the route should be added to LocRIB and AdjRIBsOut or only to AdjRIBsIn
		//comparing AS_PATH length with every route in AdjRIBsIn with same destination
		//If new route is better than already existing one, need to remove the old one and add new one
		Route bestRoute = null;
		ArrayList<Route> oldRoutes = new ArrayList<Route>();
		int sameDestinations = 0;
		boolean tableChanged = false;
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
			tableChanged = true;
		}
		else if (bestRoute.equals(route)) {
            for (Route oldRoute : oldRoutes) {
                removeRoute(oldRoute);
				routingTable.removeRoute(oldRoute);
            }
			LocRIB.add(route);
			AdjRIBsOut.add(route);
			routingTable.addRoute(route);
			tableChanged = true;
		}
		return tableChanged;
	}
}
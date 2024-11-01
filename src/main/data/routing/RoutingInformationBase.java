package routing;

import java.util.Arrays;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class RoutingInformationBase{
	int ownASN;
	List<Route> AdjRIBsIn; //All routes received with OPEN or UPDATE messages
	List<Route> LocRIB; //Only best routes (shortest AS_PATH) filtered from AdjRIBsIn

						//Shouldn't advertised routes always be the optimal routes in LocRIB??
	List<Route> AdjRIBsOut; //Routes which are advertised to neighbours
	RoutingTable routingTable;
	
	public RoutingInformationBase(byte[] ownAddress, int ownASN) {
		this.ownASN = ownASN;
		AdjRIBsIn = Collections.synchronizedList(new ArrayList<Route>());
		LocRIB = Collections.synchronizedList(new ArrayList<Route>());
		AdjRIBsOut = Collections.synchronizedList(new ArrayList<Route>());
		routingTable = new RoutingTable();
		//Adds the route to the router itself since it is the only one known before connecting
		//Does the router need to know how to route to itself?
		
		//ArrayList<Integer> defaultPath = new ArrayList<Integer>();
		//defaultPath.add(ownASN);
		//Route defaultRoute = new Route(ownAddress, defaultPath, ownAddress);
		//AdjRIBsIn.add(defaultRoute);
		//LocRIB.add(defaultRoute);
		//AdjRIBsOut.add(defaultRoute);
		//routingTable.addRoute(defaultRoute);
	}

	public List<Route> getAdvertisedRoutes() {
		return LocRIB;
	}

	public void print() {
		System.out.println(String.format("ownASN: %s", ownASN));
		//System.out.println(String.format("AdjRIBsIn: %s", AdjRIBsIn.toString()));
		//System.out.println(String.format("LocRIB: %s", LocRIB.toString()));
		//System.out.println(String.format("AdjRIBsOut: %s", AdjRIBsOut.toString()));
		routingTable.print();
	}

	public RoutingTable getTable() {
		return routingTable;
	}
	
	private boolean addRoute(Route route) {
		boolean tableChanged;
		//printDebug(String.format("%s %s", route.destinationAddress[2], ownASN));
		if (route.destinationAddress[2] == ownASN) return false;
 		for (int ASN : route.AS_PATH) {
			if (ASN == ownASN) {
				return false;
			}
		}

		route.AS_PATH.add(ownASN);
		tableChanged = filter(route);
		if (!AdjRIBsIn.contains(route)) {
			AdjRIBsIn.add(route);
		}
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

		//There are 3 cases:
		//Route destination does not appear in LocRIB
		//Route destination is in LocRIB but the AS_PATH is not shorter
		//Route destination is in LocRIB and the AS_PATH is shorter

		synchronized(LocRIB){
			for (Route r : LocRIB) {
				if (Arrays.equals(r.destinationAddress, route.destinationAddress)) {
					if (route.AS_PATH.size() < r.AS_PATH.size()) {
						//Route destination is in LocRIB and the AS_PATH is shorter
						r.AS_PATH = route.AS_PATH;
						r.nextHop = route.nextHop;
						routingTable.removeRoute(r);
						routingTable.addRoute(route);
						return true;
						
					} else {
						//Route destination is in LocRIB but the AS_PATH is not shorter
						return false;
					}
				}
			}

			//Route destination does not appear in LocRIB
			LocRIB.add(route);
			routingTable.addRoute(route);
			return true;
		}
	}
}
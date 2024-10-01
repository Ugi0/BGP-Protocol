package main.data

public class RoutingTable{
	private HashMap<int, int> addresses; //destinationAddress as key and nextHop as value
	
	//Is it enough to have just these two in routing table, or add other attributes here
	//RoutingTable class should be used when actually sending the packet
	//Default could be added
	
	public RoutingTable(RoutingInformationBase base){
		addresses = new HashMap<>();
		for (int i = 0; i < base.LocRIB.size(); i++) {
			addresses.put(base.LocRIB[i].destinationAddress, base.LocRIB[i].nextHop);
		}
	}
	
	//Methods for updating and getting the nextHop with destinationAddress
	
}
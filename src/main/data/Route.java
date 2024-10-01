package main.data

public class Route{
	private int destinationAddress; //IP address, can also be named as network or prefix
	private int[] AS_PATH;
	private int nextHop; //IP address of the next hop in AS path
	//private int origin;
	//private int localPref;
	//private int MED;
	
	public Route(int destAddress, int[]AS_PATH, int nextHop) {
		this.destinationAddress = destAddress;
		this.AS_PATH = AS_PATH;
		this.nextHop = nextHop;
	}
}
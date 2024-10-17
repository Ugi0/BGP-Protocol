package routing;

import java.util.ArrayList;

public class Route{
	public int[] destinationAddress; //IP address, can also be named as network or prefix
	public ArrayList<Integer> AS_PATH;
	public int[] nextHop; //IP address of the next hop in AS path
	//private int origin;
	//private int localPref;
	//private int MED;
	
	public Route(int destinationAddress[], ArrayList<Integer> AS_PATH, int nextHop[]) {
		this.destinationAddress = destinationAddress;
		this.AS_PATH = AS_PATH;
		this.nextHop = nextHop;
	}
}
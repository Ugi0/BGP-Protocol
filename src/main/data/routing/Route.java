package routing;

import java.util.List;
import java.util.Arrays;

public class Route{
	public byte[] destinationAddress; //IP address, can also be named as network or prefix
	public List<Integer> AS_PATH;
	public byte[] nextHop; //IP address of the next hop in AS path
	//private int origin;
	//private int localPref;
	//private int MED;
	
	public Route(byte[] destinationAddress, List<Integer> AS_PATH, byte[] nextHop) {
		this.destinationAddress = destinationAddress;
		this.AS_PATH = AS_PATH;
		this.nextHop = nextHop;
	}

	@Override
	public String toString() {
		return String.format("destination: %s, nextHop: %s, AS_PATH: %s", 
		byteArrayToString(destinationAddress), 
		byteArrayToString(nextHop), 
		Arrays.toString(AS_PATH.toArray()));
	}

	public static String byteArrayToString(byte[] bytes) {
		return String.format("%s.%s.%s.%s", String.valueOf(bytes[0]), String.valueOf(bytes[1]), String.valueOf(bytes[2]), String.valueOf(bytes[3]));
	}
	public static String byteArrayToString(Byte[] bytes) {
		return String.format("%s.%s.%s.%s", String.valueOf(bytes[0]), String.valueOf(bytes[1]), String.valueOf(bytes[2]), String.valueOf(bytes[3]));
	}
}
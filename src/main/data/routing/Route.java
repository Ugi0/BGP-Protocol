package routing;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

public class Route {
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

		if (this.destinationAddress == null) this.destinationAddress = new byte[0];
		if (this.AS_PATH == null) this.AS_PATH = new ArrayList<>();
		if (this.nextHop == null) this.nextHop = new byte[0];
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

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Route)) return false;
		Route r = (Route) o;
		if (Arrays.compare(r.nextHop, this.nextHop) != 0) {
			return false;
		}
		if (Arrays.compare(r.destinationAddress, this.destinationAddress) != 0) {
			return false;
		}
		if (!r.AS_PATH.equals(this.AS_PATH)) {
			return false;
		}
		return true;
	}
}
package messages;

import java.util.ArrayList;
import java.util.List;

public class Update extends Message {
    private int withdrawnRoutLen;
    private List<RouteInformation> withdrawnRoutes; //this field shows all the prefixes that should be removed from the BGP table
    private int totPathAttrLen;
    private List<PathAttribute> totPathAttr; //Each path attribute is a triple <attribute type, attribute length, attribute value>
                                            //Attribute Type is a two-octet field that consists of the Attribute Flags octet followed by the Attribute Type Code octet.
                                            // https://www.freesoft.org/CIE/RFC/1771/9.htm
    private List<RouteInformation> networkReachabilityInform;

    /*
     *+-----------------------------------------------------+
      |   Withdrawn Routes Length (2 octets)                |
      +-----------------------------------------------------+
      |   Withdrawn Routes (variable)                       |
      +-----------------------------------------------------+
      |   Total Path Attribute Length (2 octets)            |
      +-----------------------------------------------------+
      |   Path Attributes (variable)                        |
      +-----------------------------------------------------+
      |   Network Layer Reachability Information (variable) |
      +-----------------------------------------------------+
     */

    public Update(byte[] message) {
        super(message);
        withdrawnRoutes = new ArrayList<>();
        totPathAttr = new ArrayList<>();

        withdrawnRoutLen = getValue(2);
        int index = 0;
        while (index < withdrawnRoutLen) {
            int length = getValue(1);
            RouteInformation route = new RouteInformation(length);
            for (int i = 0; i < length; i++) {
                route.addToPrefix((byte) getValue(1));
            }
            withdrawnRoutes.add(route);
            index += length;
        }
        totPathAttrLen = getValue(2);
        index = 0;
        while (index < totPathAttrLen) {
            PathAttribute pathAttr = new PathAttribute(getValue(2), getValue(1));
            pathAttr.setValue(getValue(pathAttr.getLength()));
            totPathAttr.add(pathAttr);

            index += 3 + pathAttr.getLength();
        }
        int networkReachLength = super.length - 23 - totPathAttrLen - withdrawnRoutLen;
        index = 0;
        while (index < networkReachLength) {
            int length = getValue(1);
            RouteInformation route = new RouteInformation(length);
            for (int i = 0; i < length; i++) {
                route.addToPrefix((byte) getValue(1));
            }
            networkReachabilityInform.add(route);
            index += length;
        }
    }

    private class RouteInformation {
        int length;
        List<Integer> prefix;

        public RouteInformation(int length) {
            this.length = length;
            this.prefix = new ArrayList<>();
        }

        public void addToPrefix(byte value) {
            prefix.add(Integer.valueOf(value));
        }

        public List<Byte> toBytes() {
            List<Byte> bytes = new ArrayList<>();
            bytes.add(Integer.valueOf(length).byteValue());
            for (int i = 0; i< length; i++) {
                bytes.add(prefix.get(i).byteValue());
            }
            return bytes;
        }
    }
    
    private class PathAttribute {
        int AttrFlags;
        int AttrType;
        int AttrLength;
        int value;
        public PathAttribute(int AttrType, int AttrLength) {
            this.AttrFlags = (AttrType << 8) & 0xFF;
            this.AttrType = AttrType & 0xFF;
            this.AttrLength = AttrLength;
        }
        public void setValue(int value) {
            this.value = value;
        }
        public int getLength() {
            return AttrLength;
        }
        public List<Byte> toBytes() {
            List<Byte> bytes = new ArrayList<>();
            bytes.add(Integer.valueOf(AttrFlags).byteValue());
            bytes.add(Integer.valueOf(AttrType).byteValue());
            bytes.add(Integer.valueOf(AttrLength).byteValue());
            bytes.add(Integer.valueOf(value).byteValue());
            return bytes;
        }
    }

    @Override
    byte[] contentToBytes() {
        List<Byte> bytes = new ArrayList<>();
        bytes.add(Integer.valueOf(withdrawnRoutes.stream().mapToInt(e -> e.length).sum()).byteValue());
        for (RouteInformation withDrawnRoute : withdrawnRoutes) {
            bytes.addAll(withDrawnRoute.toBytes());
        }
        bytes.add(Integer.valueOf(totPathAttr.stream().mapToInt(e -> 3 + e.getLength()).sum()).byteValue());
        for (PathAttribute pathAttribute : totPathAttr) {
            bytes.addAll(pathAttribute.toBytes());
        }
        for (RouteInformation networkReachabilityInf : networkReachabilityInform) {
            bytes.addAll(networkReachabilityInf.toBytes());
        }

        byte[] byteArr = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            byteArr[i] = bytes.get(i);
        }
        return byteArr;
    }

    enum AttributeTypes {
        Origin(1), AS_Path(2),
        Next_Hop(3), Multi_Exit_Disc(4),
        Local_Pref(5), Atomic_Aggregate(6),
        Aggregator(7), Community(8),
        Originator_ID(9), Cluster_List(10),
        MP_REACH_NLRI(14), MP_UNREACH_NLRI(15),
        Extended_Communities(16);

        private int intValue;

        AttributeTypes(int value) {
            intValue = value;
        }

        public int getValue() {
            return intValue;
        }
    }
}

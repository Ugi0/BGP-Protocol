package messages;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.Arrays;

public class Update extends ControlMessage {
    private int withdrawnRoutLen;
    private List<RouteInformation> withdrawnRoutes; //this field shows all the prefixes that should be removed from the BGP table
    private int totPathAttrLen;
    private List<PathAttribute> totPathAttr; //Each path attribute is a triple <attribute type, attribute length, attribute value>
                                            //Attribute Type is a two-octet field that consists of the Attribute Flags octet followed by the Attribute Type Code octet.
                                            // https://www.freesoft.org/CIE/RFC/1771/9.htm
    private List<RouteInformation> networkReachabilityInform;

    private static final int WithdrawnRoutesLength = 2;
    private static final int TotalPathAttributesLength = 2;

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

     public Update(List<RouteInformation> removedRoutes, List<PathAttribute> pathAttributes, List<RouteInformation> networkReachability) {
        if (removedRoutes == null) removedRoutes = new ArrayList<>();
        if (pathAttributes == null) pathAttributes = new ArrayList<>();
        if (networkReachability == null) networkReachability = new ArrayList<>(); 

        withdrawnRoutLen = removedRoutes.stream().collect(Collectors.summingInt(e -> 1 + e.length));
        withdrawnRoutes = removedRoutes;

        totPathAttrLen = pathAttributes.stream().collect(Collectors.summingInt(e -> 3 + e.getLength()));
        totPathAttr = pathAttributes;

        networkReachabilityInform = networkReachability;
     }

    public Update(byte[] message) {
        super(message);
        withdrawnRoutes = new ArrayList<>();
        totPathAttr = new ArrayList<>();
        networkReachabilityInform = new ArrayList<>();

        withdrawnRoutLen = getValue(2);
        int index = 0;
        while (index < withdrawnRoutLen) {
            int length = getValue(1);
            RouteInformation route = new RouteInformation(length);
            for (int i = 0; i < length; i++) {
                route.addToPrefix((byte) getValue(1));
            }
            withdrawnRoutes.add(route);
            index += length + 1;
        }
        totPathAttrLen = getValue(2);
        index = 0;
        while (index < totPathAttrLen) {
            PathAttribute pathAttr = new PathAttribute(getValue(2), getValue(1));
            pathAttr.setValue(getBytes(pathAttr.getLength()));
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
            index += length + 1;
        }
    }

    public List<RouteInformation> getWithdrawnRoutes() {
        return this.withdrawnRoutes;
    }

    public Update addToAS(byte AS) {
        List<Integer> path = getAS();
        path.add(Integer.valueOf(AS));

        setAS(path);
        return this;
    }

    public static class RouteInformation {
        int length;
        List<Integer> prefix;

        public RouteInformation(int length) {
            this.length = length;
            this.prefix = new ArrayList<>();
        }

        public void addToPrefix(byte value) {
            prefix.add(Integer.valueOf(value));
        }

        public List<Integer> getPrefixes() {
            return this.prefix;
        }

        public List<Byte> toBytes() {
            List<Byte> bytes = new ArrayList<>();
            if (length != 0) {
                bytes.add(Integer.valueOf(length).byteValue());
            }
            for (int i = 0; i< length; i++) {
                bytes.add(prefix.get(i).byteValue());
            }
            return bytes;
        }
    }

    public byte[] getNextHop() {
        Optional<PathAttribute> optionalSource = totPathAttr.stream().filter(e -> e.AttrType == AttributeTypes.Next_Hop.getValue()).findFirst();
        if (optionalSource.isPresent()) {
            return optionalSource.get().value;
        }
        return null;
    }

    public Update setNextHop(byte[] value) {
        Optional<PathAttribute> optionalSource = totPathAttr.stream().filter(e -> e.AttrType == AttributeTypes.Next_Hop.getValue()).findFirst();
        if (optionalSource.isPresent()) {
            optionalSource.get().setValue(value);
            optionalSource.get().setLength(value.length);
        }
        return this;
    }

    public byte[] getSource() {
        Optional<PathAttribute> optionalSource = totPathAttr.stream().filter(e -> e.AttrType == AttributeTypes.Origin.getValue()).findFirst();
        if (optionalSource.isPresent()) {
            return optionalSource.get().value;
        }
        return null;
    }

    public List<Integer> getAS() {
        Optional<PathAttribute> optionalSource = totPathAttr.stream().filter(e -> e.AttrType == AttributeTypes.AS_Path.getValue()).findFirst();
        if (optionalSource.isPresent()) {
            List<Integer> res = new ArrayList<>();
            for (int i = 0; i < optionalSource.get().value.length; i++) {
                res.add(Integer.valueOf(optionalSource.get().value[i]));
            }
            return res;
        }
        return null;
    }

    public void setAS(List<Integer> AS) {
        OptionalInt optionalSource = IntStream.range(0, totPathAttr.size())
            .filter(i -> totPathAttr.get(i).AttrType == AttributeTypes.AS_Path.getValue()).findFirst();
        if (optionalSource.isPresent()) {
            byte[] bytes = new byte[AS.size()];
            int index = 0;
            for (Integer value : AS) {
                bytes[index] = value.byteValue();
                index++;
            }
            totPathAttr.get(optionalSource.getAsInt()).setValue(bytes);
            totPathAttr.get(optionalSource.getAsInt()).setLength(AS.size());
        }
    }
    
    public static class PathAttribute {
        
        int AttrFlags;
        int AttrType;
        int AttrLength;
        byte[] value;
        public PathAttribute(int type, int AttrLength) {
            this.AttrFlags = (AttrType << 8) & 0xFF;
            this.AttrType = type & 0xFF;
            this.AttrLength = AttrLength;
        }
        public PathAttribute setValue(byte[] value) {
            this.value = value;
            return this;
        }
        public void setLength(int value) {
            this.AttrLength = value;
        }
        public int getLength() {
            return AttrLength;
        }
        public List<Byte> toByteList() {
            List<Byte> bytes = new ArrayList<>();
            bytes.add(Integer.valueOf(AttrFlags).byteValue());
            bytes.add(Integer.valueOf(AttrType).byteValue());
            bytes.add(Integer.valueOf(AttrLength).byteValue());
            for (Byte b : value) {
                bytes.add(b);
            }

            return bytes;
        }

        public byte[] toBytes() {
            List<Byte> bytes = toByteList();
            byte[] byteArr = new byte[bytes.size()];
            for (int i = 0; i < bytes.size(); i++) {
                byteArr[i] = bytes.get(i);
            }
            return byteArr;
        }
    }

    @Override
    byte[] contentToBytes() {
        List<Byte> bytes = new ArrayList<>();
        addToByteList(Integer.valueOf(withdrawnRoutes.stream().mapToInt(e -> 1 + e.length).sum()), WithdrawnRoutesLength, bytes);
        for (RouteInformation withDrawnRoute : withdrawnRoutes) {
            bytes.addAll(withDrawnRoute.toBytes());
        }
        addToByteList(Integer.valueOf(totPathAttr.stream().mapToInt(e -> 3 + e.getLength()).sum()), TotalPathAttributesLength, bytes);
        for (PathAttribute pathAttribute : totPathAttr) {
            bytes.addAll(pathAttribute.toByteList());
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

    public static enum AttributeTypes {
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

        public static AttributeTypes getType(int value) {
            return Arrays.asList(values()).stream().filter(e -> e.getValue() == value).findFirst().get();
        }

        public int getValue() {
            return intValue;
        }
    }
}

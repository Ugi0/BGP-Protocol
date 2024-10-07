package messages;

public class Update extends Message {
    private int withdrawnRoutLen;
    private int[] withdrawnRoutes;
    private int totPathAttrLen;
    private PathAttribute[] totPathAttr;
    private int networkReachabilityInform;

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
        withdrawnRoutLen = getValue(2);
        int index = 0;
        for (int rounds = 0; rounds < withdrawnRoutLen; rounds++) {
            int length = getValue(1);
            withdrawnRoutes[index] = getValue(length);
            index++;
        }
        totPathAttrLen = getValue(2);
        index = 0;
        for (int rounds = 0; rounds < totPathAttrLen; rounds++) {
            totPathAttr[index] = new PathAttribute(getValue(1), getValue(1));
        }
        //TODO Network layer reachability information
    }
    
    private class PathAttribute {
        int AttrFlags;
        int AttrTypeCode;
        public PathAttribute(int AttrFlags, int AttrTypeCode) {
            this.AttrFlags = AttrFlags;
            this.AttrTypeCode = AttrTypeCode;
        }
    }

    @Override
    byte[] contentToBytes() {
        // TODO
        throw new UnsupportedOperationException("Unimplemented method 'contentToBytes'");
    }
}

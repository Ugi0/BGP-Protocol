package main.data;

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

    public Update(int[] message) {
        super(message);
        int i = HEADER_SIZE;
        withdrawnRoutLen = getValue(i, 2);
        i += 2;
        int index = 0;
        for (int rounds = 0; rounds < withdrawnRoutLen; rounds++) {
            int length = message[i];
            i++;
            withdrawnRoutes[index] = getValue(i, length);
            index++;
        }
        totPathAttrLen = getValue(i, 2);
        i += 2;
        index = 0;
        for (int rounds = 0; rounds < totPathAttrLen; rounds++) {
            totPathAttr[index] = new PathAttribute(message[i], message[i+1]);
            i += 2;
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
}

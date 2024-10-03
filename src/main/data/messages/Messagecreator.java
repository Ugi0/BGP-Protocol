package messages;
//TODO: all variables are not necessarily int, but maybe string as well? in that case make the changes accordingly
public class Messagecreator {

    //Message types
    private static final int TYPE_OPEN = 1;
    private static final int TYPE_UPDATE = 2;
    private static final int TYPE_NOTIFICATION = 3;
    private static final int TYPE_KEEPALIVE = 4;
    private static final int TYPE_ROUTEREFRESH = 5;

    // for header
    private static final int HEADER_SIZE = 19;
    private static final int HEADER_MARKER_SIZE = 16;
    private static final int HEADER_LENGTH_SIZE = 2;
    private static final int MAX_MESSAGE_LENGTH = 4096;
    private static final int MIN_MESSAGE_LENGTH = 19;

    //For open message
    private static final int OPEN_MESSAGE_SIZE_NO_PARM = 10; 
    private static final int DEFAULT_BGP_VERSION = 4;
    private static final int VERSION_START = 0;
    private static final int MYAS_START = 1;
    private static final int HOLD_TIME_START = 3;
    private static final int BGP_IDENTIFIER_START = 5;
    private static final int OPT_PARAM_LEN_START = 9;
    private static final int OPT_PARAM_START = 10;

    //For update message
    private static final int WITHDRAWN_ROUTES_LENGTH_SIZE = 2;
    //Withdrawn_routes = variable
    private static final int PATH_ATTRIBUTE_LENGTH_SIZE = 2;
    //Path_attributes = variable
    //Network_layer_reachability_information(NLRI) = variable


    public static int[] createHeader (int length, int type){
        
        if (length > MAX_MESSAGE_LENGTH || length < MIN_MESSAGE_LENGTH) {
            throw new Error("invalid message length");
        }
        if (type > 5 || type < 1) {
            throw new Error("invalid type");
        }
        
        //Marker creation
        int[] header = new int[HEADER_SIZE];
        for (int i = 0; i < HEADER_MARKER_SIZE; i++) {
            header[i] = 255;
        }
        // Length creation
        for (int i = HEADER_MARKER_SIZE; i < HEADER_MARKER_SIZE + HEADER_LENGTH_SIZE; i++) {
            header[i] = (length >> (8 * (HEADER_LENGTH_SIZE - 1 - i))) & 0xFF;
        }
        //Type creation
        header[HEADER_MARKER_SIZE+2] = type;

        return header;
    };

    public static int[] createOpen(int version, int myAS, int holdtime, int BGPidentifier, int optParamLen, int optParam){
        //TODO: is optional param length length in bits, octets or how?, for now assuming octets
        
        int[] openMessage = new int[OPEN_MESSAGE_SIZE_NO_PARM + optParamLen];
        openMessage[VERSION_START] = version; //or DEFAULTBGPVERSION, we can use that instead and remove the version altogether
        for (int i = MYAS_START; i < HOLD_TIME_START; i++) {
            openMessage[i] = (myAS >> (8 * (HOLD_TIME_START-MYAS_START - 1 - i))) & 0xFF;
        }
        for (int i = HOLD_TIME_START; i < BGP_IDENTIFIER_START; i++) {
            openMessage[i] = (holdtime >> (8 * (BGP_IDENTIFIER_START-HOLD_TIME_START - 1 - i))) & 0xFF;
        }
        for (int i = BGP_IDENTIFIER_START; i < OPT_PARAM_LEN_START; i++) {
            openMessage[i] = (BGPidentifier >> (8 * (OPT_PARAM_LEN_START-BGP_IDENTIFIER_START - 1 - i))) & 0xFF;
        }
        for (int i = OPT_PARAM_LEN_START; i < OPT_PARAM_START; i++) {
            openMessage[i] = (optParamLen >> (8 * (OPT_PARAM_START-OPT_PARAM_LEN_START - 1 - i))) & 0xFF;
        }
        //TODO? do we need optional parameters? Also, should this be big-endian?
        if (optParamLen > 0) {
            for (int i = OPT_PARAM_START; i < OPT_PARAM_START + optParamLen; i++) {
                openMessage[i] = (optParam >> (8 * (optParamLen - 1 - i))) & 0xFF;
            }
        } 

        int[] openMessageWithHeader = combineArrays(createHeader(HEADER_SIZE+openMessage.length,TYPE_OPEN),openMessage);
        return openMessageWithHeader;
    };

    public static int[] createUpdate(){
        //TODO
        int[] updateMessage = new int[0];
        return updateMessage;
    };

    public static int[] createNotification(int error, int errorSub, int data){ //TODO specify errors somewhere
        int significantBits = 32 - Integer.numberOfLeadingZeros(data);
        int dataOctets = (significantBits + 7) / 8;
        int[] notificationMessage = new int[2 + dataOctets];
        notificationMessage[0] = error;
        notificationMessage[1] = errorSub;
        if (data != 0){
            for (int i = 2; i < 2 + dataOctets; i++) {
                notificationMessage[i] = (data >> (8 * (dataOctets - 1 - i))) & 0xFF;
            }
        }

        int[] notificationMessageWithHeader = combineArrays(createHeader(HEADER_SIZE+notificationMessage.length,TYPE_NOTIFICATION),notificationMessage);
        return notificationMessageWithHeader;
    };

    public static int[] createKeepalive(){
        int[] keepaliveMessage = createHeader(0,TYPE_KEEPALIVE);
        return keepaliveMessage;
    };

    public static int[] createRouteRefresh(){
        //TODO
        int[] routeRefreshMessage = new int[0];
        return routeRefreshMessage;
    };

    private static int[] combineArrays(int[] array1, int[] array2) {
        // CHAT-GPT code:
        int[] result = new int[array1.length + array2.length];
        System.arraycopy(array1, 0, result, 0, array1.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);

        return result;
    }

}

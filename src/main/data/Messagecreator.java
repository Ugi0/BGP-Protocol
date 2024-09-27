package main.data;

public class Messagecreator {
    // for header
    private static final int HEADER_SIZE = 19;
    private static final int HEADER_MARKER_SIZE = 16;
    private static final int HEADER_LENGTH_SIZE = 2;
    private static final int MAX_MESSAGE_LENGTH = 65535;

    //For open message
    private static final int OPEN_MESSAGE_SIZE_NO_PARM = 10; 
    private static final int DEFAULT_BGP_VERSION = 4;
    private static final int VERSION_START = 0;
    private static final int MYAS_START = 1;
    private static final int HOLD_TIME_START = 5;
    private static final int BGP_IDENTIFIER_START = 7;
    private static final int OPT_PARAM_LEN_START = 11;
    private static final int OPT_PARAM_START = 12;


    public int[] createHeader (int length, int type){
        
        if (length > MAX_MESSAGE_LENGTH || length < 0) {
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

    //TODO does not add header to the message yet.
    public int[] createOpen(int version, int myAS, int holdtime, int BGPidentifier, int optParamLen, int optParam){
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
        //TODO? do we need optional parameters?
        if (optParamLen >0) {
            for (int i = OPT_PARAM_START; i < OPT_PARAM_START + optParamLen; i++) {
                openMessage[i] = (optParam >> (8 * (optParamLen - 1 - i))) & 0xFF;
            }
        } 
        return openMessage;
    };

    public int[] createUpdate(){
        //TODO
        int[] updateMessage = new int[0];
        return updateMessage;
    };

    public int[] createNotification(){
        //TODO
        int[] NotificationMessage = new int[0];
        return NotificationMessage;
    };

    public int[] createKeepalive(){
        //TODO
        int[] keepaliveMessage = new int[0];
        return keepaliveMessage;
    };

    public int[] createRouteRefresh(){
        //TODO
        int[] routeRefreshMessage = new int[0];
        return routeRefreshMessage;
    };




}

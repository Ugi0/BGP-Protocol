package messages;

public class Open extends Message {

    /*
     * 0                   1                   2                   3
       0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
       +-+-+-+-+-+-+-+-+
       |    Version    |
       +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
       |     My Autonomous System      |
       +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
       |           Hold Time           |
       +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
       |                         BGP Identifier                        |
       +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
       | Opt Parm Len  |
       +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
       |                                                               |
       |             Optional Parameters (variable)                    |
       |                                                               |
       +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-
     */

    private static final int OPEN_MESSAGE_SIZE_NO_PARM = 10; 
    private static final int DEFAULT_BGP_VERSION = 4;
    private static final int VERSION_START = 0;
    private static final int MYAS_START = 1;
    private static final int HOLD_TIME_START = 3;
    private static final int BGP_IDENTIFIER_START = 5;
    private static final int OPT_PARAM_LEN_START = 9;
    private static final int OPT_PARAM_START = 10;
    
    
    private int version;
    private int AS;
    private int holdTime;
    private int identifier;
    private int OptParamLen;
    private int OptParams;
    private byte[] OpenMessage;
    

    public Open() {
      super();
      //TODO default open message parameters
    }

    public Open(int[] message) {
        super(message);
        
        version = getValue(1);
        AS = getValue(2);
        holdTime = getValue(2);
        identifier = getValue(4); //e.g 1.1.1.1
        OptParamLen = getValue(1); //Assume OptParam is never used, so this value is always 0
        OptParams = getValue(OptParamLen);
    }

    public Open(int myAS, int holdtime, int BGPidentifier, int optParamLen, int optParam){
      version = DEFAULT_BGP_VERSION;
      AS = myAS;
      holdTime = holdtime;
      identifier = BGPidentifier;
      OptParamLen = optParamLen;
      OptParams = optParam;
      type = TYPE_OPEN;

      OpenMessage = toBytes(); 
    };

    @Override
    byte[] contentToBytes() {
      byte[] openMessage = new byte[OPEN_MESSAGE_SIZE_NO_PARM + OptParamLen];
      openMessage[VERSION_START] = (byte) version;
      for (int i = MYAS_START; i < HOLD_TIME_START; i++) {
          openMessage[i] = (byte) ((AS >> (8 * (HOLD_TIME_START-MYAS_START - 1 - i))) & 0xFF);
      }
      for (int i = HOLD_TIME_START; i < BGP_IDENTIFIER_START; i++) {
          openMessage[i] = (byte) ((holdTime >> (8 * (BGP_IDENTIFIER_START-HOLD_TIME_START - 1 - i))) & 0xFF);
      }
      for (int i = BGP_IDENTIFIER_START; i < OPT_PARAM_LEN_START; i++) {
          openMessage[i] = (byte) ((identifier >> (8 * (OPT_PARAM_LEN_START-BGP_IDENTIFIER_START - 1 - i))) & 0xFF);
      }
      for (int i = OPT_PARAM_LEN_START; i < OPT_PARAM_START; i++) {
          openMessage[i] = (byte) ((OptParamLen >> (8 * (OPT_PARAM_START-OPT_PARAM_LEN_START - 1 - i))) & 0xFF);
      }
      //TODO? do we need optional parameters? Also, should this be big-endian?
      if (OptParamLen > 0) {
          for (int i = OPT_PARAM_START; i < OPT_PARAM_START + OptParamLen; i++) {
              openMessage[i] = (byte) ((OptParams >> (8 * (OptParamLen - 1 - i))) & 0xFF);
          }
      } 

      return openMessage;
    }

}

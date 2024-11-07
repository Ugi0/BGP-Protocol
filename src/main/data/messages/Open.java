package messages;

import java.util.ArrayList;
import java.util.List;

public class Open extends ControlMessage {

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

    private static final int DEFAULT_BGP_VERSION = 4;
    private static final int VERSION_LENGTH = 1;
    private static final int MYAS_LENGTH = 2;
    private static final int HOLD_TIME_LENGTH = 2;
    private static final int BGP_IDENTIFIER_LENGTH = 4;
    private static final int OPT_PARAM_LEN = 1;
    
    
    private int version;
    private int AS;
    private int holdTime;
    private int identifier;
    private int OptParamLen;
    @SuppressWarnings("unused")
    private int OptParams;

    public Open(byte[] message) {
        super(message);
        
        version = getValue(1);
        AS = getValue(2);
        holdTime = getValue(2);
        identifier = getValue(4); //e.g 1.1.1.1
        OptParamLen = getValue(1); //Assume OptParam is never used, so this value is always 0
        OptParams = getValue(OptParamLen);
    }

    public Open(int myAS, int holdtime, int BGPidentifier, int optParamLen, int optParam){
      super();

      this.version = DEFAULT_BGP_VERSION;
      this.AS = myAS;
      this.holdTime = holdtime;
      this.identifier = BGPidentifier;
      this.OptParamLen = optParamLen;
      this.OptParams = optParam;
      this.type = TYPE_OPEN;

      this.message = toBytes(); 
    }

    @Override
    byte[] contentToBytes() {
      List<Byte> bytes = new ArrayList<>();

      addToByteList(getVersion(), VERSION_LENGTH, bytes);
      addToByteList(getAS(), MYAS_LENGTH, bytes);
      addToByteList(getHoldTime(), HOLD_TIME_LENGTH, bytes);
      addToByteList(getIdentifier(), BGP_IDENTIFIER_LENGTH, bytes);
      addToByteList(OptParamLen, OPT_PARAM_LEN, bytes);

      byte[] byteArr = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            byteArr[i] = bytes.get(i);
        }
        return byteArr;
    }

    public int getVersion() {
      return version;
    }

    public int getAS() {
      return AS;
    }

    public int getHoldTime() {
      return holdTime;
    }

    public int getIdentifier() {
      return identifier;
    }

}

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

    private int version;
    private int AS;
    private int holdTime;
    private int identifier;
    private int OptParamLen;
    private int OptParams;

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

    @Override
    byte[] contentToBytes() {
      // TODO
      throw new UnsupportedOperationException("Unimplemented method 'contentToBytes'");
    }

}

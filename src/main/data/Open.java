package main.data;

public class Open extends Message {
    private int version;
    private int AS;
    private int holdTime;
    private int identifier;
    private int OptParamLen;
    private int OptParams;

    public Open(int[] message) {
        super(message);
        
        int i = HEADER_SIZE;
        version = message[i];
        AS = ((message[i+1] & 0xFF << 8) | (message[i+2] & 0xFF));
        holdTime = ((message[i+3] & 0xFF << 8) | (message[i+4] & 0xFF));
        identifier = ((message[i+5] & 0xFF << 24) | (message[i+6] & 0xFF << 16) | (message[i+7] & 0xFF << 8) | (message[i+8] & 0xFF));
        OptParamLen = message[i+9];
        //TODO Handle optional parameters. How does Param. type identify the type??
    }
    
}

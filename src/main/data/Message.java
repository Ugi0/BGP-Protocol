package main.data;

public class Message {
    protected static final int HEADER_SIZE = 19;
    private static final int HEADER_MARKER_SIZE = 16;
    private static final int HEADER_LENGTH_SIZE = 2;
    private static final int HEADER_TYPE_SIZE = 1;
    private static final int MAX_MESSAGE_LENGTH = 65535;

    private int[] message;
    protected int type;
    protected int length;

    public Message(int[] message) {
        this.message = message;

        int index = 0;
        for (int i = 0; i<HEADER_MARKER_SIZE; i++) {
            if (message[index] != 255) {
                throw new Error();
            }
        }
        length = getValue(HEADER_MARKER_SIZE, 2);
        type = message[HEADER_MARKER_SIZE+2];
    }

    protected int getValue(int startIndex, int octects) {
        int res = 0;
        for (int i = 0; i< octects; i++) {
            res = res << 8;
            res = res | (message[startIndex+i] & 0xFF);
        }
        return res;
    }

    public int[] createHeader(int length, int type){
        
        if (length > MAX_MESSAGE_LENGTH || length < 0) {
            throw new Error("invalid message length");
        }
        if (type > 5 || type < 1) {
            throw new Error("invalid type");
        }
        
        //Marker creation
        int[] header =new int[HEADER_SIZE];
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
}

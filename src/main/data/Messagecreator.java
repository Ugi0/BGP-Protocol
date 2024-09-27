package main.data;

public class Messagecreator {
    protected static final int HEADER_SIZE = 19;
    private static final int HEADER_MARKER_SIZE = 16;
    private static final int HEADER_LENGTH_SIZE = 2;
    private static final int MAX_MESSAGE_LENGTH = 65535;

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

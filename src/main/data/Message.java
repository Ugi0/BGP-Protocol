package main.data;

public class Message {
    protected static final int HEADER_SIZE = 19;
    private static final int HEADER_MARKER_SIZE = 16;

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
        length = getValue(HEADER_MARKER_SIZE+1, 2);
        type = message[HEADER_MARKER_SIZE+3];
    }

    protected int getValue(int startIndex, int octects) {
        int res = 0;
        for (int i = 0; i< octects; i++) {
            res = res << 8;
            res = res | (message[startIndex+i] & 0xFF);
        }
        return res;
    }
}

package main.data;

public class Message {
    protected static final int HEADER_SIZE = 19;
    private static final int HEADER_MARKER_SIZE = 16;

    protected int type;
    protected int length;

    public Message(int[] message) {

        int index = 0;
        for (int i = 0; i<HEADER_MARKER_SIZE; i++) {
            if (message[index] != 255) {
                throw new Error();
            }
        }
        length = ((message[HEADER_MARKER_SIZE+1] & 0xFF << 8) | (message[HEADER_MARKER_SIZE+2] & 0xFF));
        type = message[HEADER_MARKER_SIZE+3];
    }
}

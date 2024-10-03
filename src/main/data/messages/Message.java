package messages;

import java.nio.ByteBuffer;

public abstract class Message {
    protected static final int HEADER_SIZE = 19;
    private static final int HEADER_MARKER_SIZE = 16;

    private int[] message;
    protected int type;
    protected int length;
    private int index;

    public Message() {
        //TODO default header parameters
    }

    public Message(int[] message) {
        this.message = message;

        int index = 0;
        for (int i = 0; i<HEADER_MARKER_SIZE; i++) {
            if (message[index] != 255) {
                throw new Error();
            }
        }
        index = HEADER_MARKER_SIZE;
        length = getValue(2);
        type = getValue(1);
    }

    protected int getValue(int octects) {
        int res = 0;
        for (int i = 0; i< octects; i++) {
            res = res << 8;
            res = res | (message[index+i] & 0xFF);
        }
        index += octects;
        return res;
    }

    private byte[] headerToBytes() {
        byte[] bytes = new byte[HEADER_SIZE];
        for (int i = 0; i< HEADER_MARKER_SIZE; i++) {
            bytes[i] = (byte) 255;
        }
        bytes[17] = (byte) (length << 8);
        bytes[18] = (byte) length;
        bytes[19] = (byte) type;

        return bytes;
    }

    protected byte[] toBytes() {
        byte[] headerBytes = headerToBytes();
        byte[] contentBytes = contentToBytes();
        byte[] combined = new byte[headerBytes.length + contentBytes.length];
        ByteBuffer buff = ByteBuffer.wrap(combined);
        buff.put(headerBytes);
        buff.put(contentBytes);

        return buff.array();
    }

    abstract byte[] contentToBytes();
}

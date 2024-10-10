package messages;

import static main.Main.printDebug;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public abstract class Message {
    public static final int HEADER_SIZE = 19;
    public static final int HEADER_MARKER_SIZE = 16;
    public static final int MAX_MESSAGE_LENGTH = 1500;

    //Message types
    protected static final int TYPE_OPEN = 1;
    protected static final int TYPE_UPDATE = 2;
    protected static final int TYPE_NOTIFICATION = 3;
    protected static final int TYPE_KEEPALIVE = 4;
    protected static final int TYPE_ROUTEREFRESH = 5;

    protected byte[] message;
    protected int type;
    protected int length;
    private int index;

    public Message() {
    }

    public Message(byte[] message) {
        this.message = message;

        for (int i = 0; i<HEADER_MARKER_SIZE; i++) {
            if (getValue(1) != 255) {
                throw new Error();
            }
        }
        index = HEADER_MARKER_SIZE;
        length = getValue(2);
        type = getValue(1);
    }

    public static Class<? extends Message> classFromMessage(byte[] message) {
        for (int i = 0; i<HEADER_MARKER_SIZE; i++) {
            if ((message[i] & 0xFF) != 255) {
                throw new Error();
            }
        }
        int type = message[HEADER_MARKER_SIZE+2];

        switch (type) {
            case 1:
                return Open.class;
            case 2:
                return Update.class;
            case 3:
                return Notification.class;
            case 4:
                return Keepalive.class;
            default:
                return null;
        }
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
            bytes[i] = Integer.valueOf(255).byteValue();
        }
        bytes[16] = (byte) (length << 8);
        bytes[17] = (byte) length;
        bytes[18] = (byte) classToType(getClass());

        return bytes;
    }

    protected void addToByteList(int value, int octets, List<Byte> bytes) {
        for (int i = 0; i < octets; i++) {
            bytes.add(Integer.valueOf(value >> 8 * (octets-i-1)).byteValue());
        }
    }

    public byte[] toBytes() { 
        byte[] contentBytes = contentToBytes();
        length = contentBytes.length + HEADER_SIZE;
        byte[] headerBytes = headerToBytes();
        
        byte[] combined = new byte[headerBytes.length + contentBytes.length];
        ByteBuffer buff = ByteBuffer.wrap(combined);
        buff.put(headerBytes);
        buff.put(contentBytes);

        return buff.array();
    }

    abstract byte[] contentToBytes();

    private int classToType(Class<? extends Message> clazz) {
        return ClassType.valueOf(clazz.getSimpleName()).num;
    }

    private enum ClassType {
        Open(1), Update(2),
        Notification(3), Keepalive(4),
        RouterRefresh(5);

        int num;
        ClassType(int num) {
            this.num = num;
        }
    }
}

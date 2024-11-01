package messages;

import java.nio.ByteBuffer;

public abstract class ControlMessage extends Message {
    public static final int HEADER_SIZE = 19;
    public static final int HEADER_MARKER_SIZE = 16;
    public static final int MAX_MESSAGE_LENGTH = 1500;
    public static final int MIN_MESSAGE_LENGTH = 19;

    //Message types
    protected static final int TYPE_OPEN = 1;
    protected static final int TYPE_UPDATE = 2;
    protected static final int TYPE_NOTIFICATION = 3;
    protected static final int TYPE_KEEPALIVE = 4;
    protected static final int TYPE_ROUTEREFRESH = 5;

    protected int type;
    protected int length;

    public ControlMessage() {
    }

    public ControlMessage(byte[] message) {
        super(message);

        for (int i = 0; i<HEADER_MARKER_SIZE; i++) {
            if (getValue(1) != 255) {
                throw new Error();
            }
        }
        index = HEADER_MARKER_SIZE;
        length = getValue(2);
        type = getValue(1);

        byte[] messageCopy = new byte[length];
        System.arraycopy(message, 0, messageCopy, 0, length);
        message = messageCopy;
    }

    public int getLength() {
        return this.length;
    }

    /**
     * Change the header of the message to bytes
     * @return
     */
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

    /**
     * Change the whole message to bytes
     * @return
     */
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

    /**
     * Change the part of the message after the header to bytes
     * <p>
     * DO NOT USE THIS DIRECTLY USE {@link #toBytes()} INSTEAD
     * </p>
     * @return
     */
    abstract byte[] contentToBytes();

    private int classToType(Class<? extends ControlMessage> clazz) {
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

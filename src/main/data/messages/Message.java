package messages;

import java.util.List;

public abstract class Message {
    protected byte[] message;
    protected int index;

    public Message() {}
    public Message(byte[] message) {
        this.message = message;
    }
    public static Class<? extends Message> classFromMessage(byte[] message) {
        for (int i = 0; i<ControlMessage.HEADER_MARKER_SIZE; i++) {
            if ((message[i] & 0xFF) != 255) {
                throw new Error();
            }
        }
        int type = message[ControlMessage.HEADER_MARKER_SIZE+2];

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
                break;
        }
        if (message[0] == (IpPacket.DEFAULT_VERSION << 4 | IpPacket.DEFAULT_IHL) && message[1] == (IpPacket.DEFAULT_DSCP << 4 | IpPacket.DEFAULT_ECN)) {
            return IpPacket.class;
        }
        return null;
    }
    public abstract int getLength();
    public abstract byte[] toBytes();

    /**
     * Get next {@code octects} bytes from the message and combine those to a single int.
     * Should only be used when Message is constructed using {@code byte[]}
     * @param octects
     * @return
     */
    protected int getValue(int octects) {
        int res = 0;
        for (int i = 0; i< octects; i++) {
            res = res << 8;
            res = res | (message[index+i] & 0xFF);
        }
        index += octects;
        return res;
    }

    protected byte[] getBytes(int octects) {
        byte[] bytes = new byte[octects];
        for (int i = 0; i< octects; i++) {
            bytes[i] = message[index+i];
        }
        index += octects;
        return bytes;
    }
    /**
     * Add the given {@code value} to given {@code bytes} List, filling with zeros until {@code octects} bytes are added.
     * @param value
     * @param octets
     * @param bytes
     */
    protected void addToByteList(int value, int octets, List<Byte> bytes) {
        for (int i = 0; i < octets; i++) {
            bytes.add(Integer.valueOf(value >> 8 * (octets-i-1)).byteValue());
        }
    }
}

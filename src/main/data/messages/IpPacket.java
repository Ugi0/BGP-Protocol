package messages;

import java.util.ArrayList;
import java.util.List;

public class IpPacket extends Message {
    //https://en.wikipedia.org/wiki/IPv4#Packet_structure
    //(bits) (value)
    //4 version, 4 IHL
    //6 DSCP 2 ECN
    //16 Total length
    //16 Identification
    //3 flags 15 fragment Offset
    //8 Time to Live
    //8 protocol
    //16 Header checksum
    //Source address
    //Destination address
    //Options - Not often used
    //Data 
    public static int HEADER_LENGTH;
    public static int DEFAULT_VERSION;
    public static int DEFAULT_IHL;
    public static int DEFAULT_DSCP;
    public static int DEFAULT_ECN;
    public static int DEFAULT_IDENTIFICATION;
    public static int DEFAULT_FLAGS;
    public static int DEFAULT_FRAGMENT_OFFSET;
    public static int DEFAULT_TIMETOLIVE;
    public static int DEFAULT_PROTOCOL;
    private int version;
    private int IHL;
    private int DSCP;
    private int ECN;
    private int TotalLen;
    private int Identification;
    private int flags;
    private int fragmentOffset;
    private int timeToLive;
    private int protocol;
    private int headerChecksum;
    private byte[] source;
    private byte[] destination;
    private byte[] data;

    public IpPacket(byte[] message) {
        super(message);

        int value = getValue(1);
        version = value >> 4;
        IHL = value & 0xF;
        value = getValue(1);
        DSCP = value >> 6;
        ECN = value & 0x2;
        TotalLen = getValue(2);
        Identification = getValue(2);
        value = getValue(2);
        flags = value >> 14;
        fragmentOffset = value & (2^14);
        timeToLive = getValue(1);
        protocol = getValue(1);
        headerChecksum = getValue(2);
        source = getBytes(4);
        destination = getBytes(4);
        data = getBytes(TotalLen - HEADER_LENGTH);
    }

    public IpPacket(byte[] source, byte[] destination, byte[] data) {
        version = DEFAULT_VERSION;
        IHL = DEFAULT_IHL;
        DSCP = DEFAULT_DSCP;
        Identification = DEFAULT_IDENTIFICATION;
        flags = DEFAULT_FLAGS;
        fragmentOffset = DEFAULT_FRAGMENT_OFFSET;
        timeToLive = DEFAULT_TIMETOLIVE;
        protocol = DEFAULT_PROTOCOL;

        this.source = source;
        this.destination = destination;
        this.data = data;
    }

    @Override
    public int getLength() {
        return TotalLen;
    }

    private void makeCheckSum() {
        int ans = 0;
        ans += version << 12;
        ans += IHL << 8;
        ans += DSCP << 2;
        ans += ECN;
        ans += TotalLen;
        ans += Identification;
        ans += flags << 15;
        ans += fragmentOffset;
        ans += timeToLive << 8;
        ans += protocol;
        
        headerChecksum = (ans >> 16) + (ans & 0xFFFF);
    }

    private boolean verifyCheckSum() {
        int ans = 0;
        ans += version << 12;
        ans += IHL << 8;
        ans += DSCP << 2;
        ans += ECN;
        ans += TotalLen;
        ans += Identification;
        ans += flags << 15;
        ans += fragmentOffset;
        ans += timeToLive << 8;
        ans += protocol;
        ans += headerChecksum;
        
        return ans == 0xFFFF;
    }

    @Override
    public byte[] toBytes() {
        List<Byte> bytes = new ArrayList<>();

        makeCheckSum();
        TotalLen = HEADER_LENGTH + data.length;

        addToByteList((version << 4) | IHL, 1, bytes);
        addToByteList((DSCP << 6) | ECN, 1, bytes);
        addToByteList(TotalLen, 2, bytes);
        addToByteList(Identification, 2, bytes);
        addToByteList((flags << 14) | fragmentOffset, 2, bytes);
        addToByteList(timeToLive, 1, bytes);
        addToByteList(protocol, 1, bytes);
        addToByteList(headerChecksum, 2, bytes);

        for (byte b : source) {
            bytes.add(Byte.valueOf(b));
        }
        for (byte b : destination) {
            bytes.add(Byte.valueOf(b));
        }

        byte[] byteArr = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            byteArr[i] = bytes.get(i);
        }
        return byteArr;
    }
}

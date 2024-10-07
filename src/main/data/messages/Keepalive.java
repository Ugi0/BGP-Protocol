package messages;

public class Keepalive extends Message {

    private byte[] keepaliveMessage;

    public Keepalive(int[] message) {
        super(message);
    }

    public Keepalive(){
        type = TYPE_KEEPALIVE;

        keepaliveMessage = toBytes();
    }

    @Override
    byte[] contentToBytes() {
        return new byte[0];
    }  
}

package messages;

public class Keepalive extends Message {


    public Keepalive(byte[] message) {
        super(message);
    }

    public Keepalive(){
        type = TYPE_KEEPALIVE;

        message = toBytes();
    }

    @Override
    byte[] contentToBytes() {
        return new byte[0];
    }  
}

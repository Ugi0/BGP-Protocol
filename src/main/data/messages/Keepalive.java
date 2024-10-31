package messages;

public class Keepalive extends ControlMessage {


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

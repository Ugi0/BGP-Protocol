package messages;

public class Keepalive extends Message {

    public Keepalive(int[] message) {
        super(message);
    }

    @Override
    byte[] contentToBytes() {
        // TODO
        throw new UnsupportedOperationException("Unimplemented method 'contentToBytes'");
    }
    
}

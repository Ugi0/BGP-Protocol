package messages;

public class Notification extends Message {

    /*
     * 0                   1                   2                   3
      0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
      | Error code    | Error subcode |   Data (variable)             |
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     */
    /*
     * Error Code       Symbolic Name               Reference

              1         Message Header Error             Section 6.1

              2         OPEN Message Error               Section 6.2

              3         UPDATE Message Error             Section 6.3

              4         Hold Timer Expired               Section 6.5

              5         Finite State Machine Error       Section 6.6

              6         Cease  
     */
    /*
     * Message Header Error subcodes:

               1 - Connection Not Synchronized.
               2 - Bad Message Length.
               3 - Bad Message Type.

      OPEN Message Error subcodes:

               1 - Unsupported Version Number.
               2 - Bad Peer AS.
               3 - Bad BGP Identifier.
               4 - Unsupported Optional Parameter.
               5 - [Deprecated - see Appendix A].
               6 - Unacceptable Hold Time.

      UPDATE Message Error subcodes:

               1 - Malformed Attribute List.
               2 - Unrecognized Well-known Attribute.
               3 - Missing Well-known Attribute.
               4 - Attribute Flags Error.
               5 - Attribute Length Error.
               6 - Invalid ORIGIN Attribute.
               7 - [Deprecated - see Appendix A].
               8 - Invalid NEXT_HOP Attribute.
               9 - Optional Attribute Error.
              10 - Invalid Network Field.
              11 - Malformed AS_PATH.
     */

    private int error;
    private int errorSub;
    private int data;

    public Notification(byte[] message) {
        super(message);
        //TODO Auto-generated constructor stub
    }

    public Notification(int error, int errorSub, int data){ //TODO specify errors somewhere
        this.error = error;
        this.errorSub = errorSub;
        this.data = data;
        type = TYPE_NOTIFICATION;

        message = toBytes();         
    };

    @Override
    byte[] contentToBytes() {
        int significantBits = 32 - Integer.numberOfLeadingZeros(data);
        int dataOctets = (significantBits + 7) / 8;
        byte[] notificationMessage = new byte[2 + dataOctets];
        notificationMessage[0] = (byte) error;
        notificationMessage[1] = (byte) errorSub;
        if (data != 0){
            for (int i = 2; i < 2 + dataOctets; i++) {
                notificationMessage[i] = (byte) ((data >> (8 * (dataOctets - 1 - i))) & 0xFF);
            }
        }
        return notificationMessage;
    }
    
}

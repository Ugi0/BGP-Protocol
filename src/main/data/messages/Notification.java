package messages;

import java.util.ArrayList;
import java.util.List;

public class Notification extends ControlMessage {

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
    private byte[] data;

    public Notification(byte[] message) {
        super(message);
        
        error = getValue(1);
        errorSub = getValue(1);

        int dataLength = getValue(1);
        data = getBytes(dataLength);

    }

    public Notification(int error, int errorSub, byte[] data){
        this.error = error;
        this.errorSub = errorSub;
        if (this.data == null) { 
            data = new byte[0];
        } else {
            this.data = data;
        }
        type = TYPE_NOTIFICATION;

        message = toBytes();      
    };

    public int getError() {
        return this.error;
    }

    @Override
    byte[] contentToBytes() {
        List<Byte> bytes = new ArrayList<>();

        if (data == null) data = new byte[0];

        addToByteList(error, 1, bytes);
        addToByteList(errorSub, 1, bytes);
        addToByteList(data.length, 1, bytes);

        for (byte b : data) {
            bytes.add(b);
        }

        byte[] byteArr = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            byteArr[i] = bytes.get(i);
        }
        return byteArr;
    }

    public static enum ErrorCode {
        HeaderError(1), OpenError(2),
        UpdateError(3), TimerExpired(4),
        StateMachineError(5), Cease(6);

        int value;

        ErrorCode(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }
    
}

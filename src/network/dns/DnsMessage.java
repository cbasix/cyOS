package network.dns;

public class DnsMessage {
    public static final int QR_QUERY = 0;
    public static final int QR_RESPONSE = 1;

    public static final int OPCODE_QUERY = 0;
    public static final int OPCODE_IQUERY = 1;

    public static final int RCODE_NO_ERROR = 0;
    public static final int RCODE_FORMAT_ERROR = 1;
    public static final int RCODE_NOT_IMPLEMENTED = 4;

    public DnsMessage(){}

    public static DnsMessage fromBytes(byte[] data){
        // todo implement
        return null;
    }

    public DnsMessage addQuestion(String name){

        return this;
    }

    public DnsMessage addResponse(ARecord aRecord){

        return this;
    }


}

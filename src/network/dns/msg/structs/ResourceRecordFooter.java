package network.dns.msg.structs;

public class ResourceRecordFooter extends STRUCT {
    public static final int SIZE = 10;

    public short type;
    public short class_;
    public int ttl;
    public short rdlength;
    @SJC(count = 0)
    public byte[] rdata;
}

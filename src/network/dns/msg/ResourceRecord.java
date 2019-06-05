package network.dns.msg;

public class ResourceRecord {
    public String name;
    public int type;
    public int class_;
    public int ttl;
    public byte[] rdata;

    public ResourceRecord(String name, int type, int class_, int ttl, byte[] rdata) {
        this.name = name;
        this.type = type;
        this.class_ = class_;
        this.ttl = ttl;
        this.rdata = rdata;
    }
}

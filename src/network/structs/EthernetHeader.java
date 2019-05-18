package network.structs;

public class EthernetHeader extends STRUCT {
    public static final int SIZE = 14;
    
    @SJC(count = 6)
    public byte[] destMac;
    @SJC(count = 6)
    public byte[] srcMac;
    public short type;
}

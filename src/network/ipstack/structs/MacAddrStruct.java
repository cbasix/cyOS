package network.ipstack.structs;

public class MacAddrStruct extends STRUCT{
    @SJC(count = 6)
    public byte[] mac;
}

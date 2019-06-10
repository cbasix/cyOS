package network.address;

import io.LowlevelLogging;
import network.ipstack.structs.MacAddrStruct;

// mac is saved in network byte order !!!

public class MacAddress {
    public static final int MAC_LEN = 6;

    public byte[] addr;

    public MacAddress(){}

    public MacAddress(byte[] data){
        addr = data;
    }

    public static MacAddress fromBytes(byte[] data){
        if (data.length != MAC_LEN){
            LowlevelLogging.debug("Got invalid Mac Addr");
            return null;
        }

        byte[] copy = new byte[MAC_LEN];
        for(int i = 0; i < MAC_LEN; i++){
            copy[i] = data[i];
        }

        return new MacAddress(copy);
    }

    public static MacAddress zeroAddr(){
        byte[] data = new byte[MAC_LEN];
        return new MacAddress(data);
    }

    public static MacAddress fromStruct(MacAddrStruct data){
        byte[] copy = new byte[MAC_LEN];
        for(int i = 0; i < MAC_LEN; i++){
            copy[i] = data.mac[i];
        }

        return new MacAddress(copy);
    }

    public void writeTo(MacAddrStruct data){
        for (int i = 0; i < MAC_LEN; i++) {
            data.mac[i] = addr[i];
        }
    }

    public byte[] toBytes(){
        return addr;
        /*byte[] copy = new byte[MAC_LEN];
        for(int i = 0; i < MAC_LEN; i++){
            copy[i] = addr[i];
        }
        return copy;*/
    }

    public long toLong() {
        long l = 0;
        for(int i = 0; i < MAC_LEN; i++){
            l |= (addr[i] << i); // todo check correct endianness?
        }
        return l;
    }

    public String toString(){
        String macStr = "";

        for (int i = 0; i < MAC_LEN; i++) {
            macStr = String.concat(macStr, String.hexFrom(addr[i]));

            if(i != MAC_LEN - 1) {
                macStr = String.concat(macStr, ":");
            }
        }
        return  macStr;
    }

    private static MacAddress broadcastMac;
    public static MacAddress getBroadcastAddr(){
        if (broadcastMac == null){
            byte[] data = new byte[MAC_LEN];

            for (int i = 0; i < MAC_LEN; i++){
                data[i] = (byte)0xFF;
            }

            broadcastMac = new MacAddress(data);
        }

        return broadcastMac;
    }
}

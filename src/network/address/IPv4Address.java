package network.address;

import io.Color;
import io.LowlevelLogging;
import io.LowlevelOutput;

public class IPv4Address {
    public static final int IPV4_LEN = 4;

    public int addr;

    public IPv4Address(){}

    public IPv4Address(int data){
        addr = data;
    }

    public IPv4Address(byte[] ipBytes) {
        if (ipBytes.length != 4){
            LowlevelLogging.debug("invalid length for ip address");
            return;
        }
        for (int i = 0; i < IPV4_LEN; i++) {
            //LowlevelOutput.printStr(String.hexFrom(ipBytes[i]), 2, 2+2*i, Color.PINK);
            addr |= ((ipBytes[i] & 0xFF) << (8*(IPV4_LEN-1-i)));
        }
        //addr = ipBytes[0] | ((int)ipBytes[] << 8) | (45 << 16) | (ipBytes[3] << 24);
        //addr = 0x12345678;

    }

    public static IPv4Address getBroadcastAddr() {
        return new IPv4Address(0xFFFFFFFF);
    }

    // todo fix
    public byte[] toBytes(){
        byte[] copy = new byte[IPV4_LEN];
        for(int i = 0; i < IPV4_LEN; i++){
            copy[i] = (byte)((addr >> IPV4_LEN - 1 -i) & 0xFF);
        }
        return copy;
    }

    public int toInt() {
        return addr;
    }

    public String toString(){
        String ipStr = "";

        for (int i = 1; i <= IPV4_LEN; i++) {
            ipStr = String.concat(ipStr, String.from(((addr >>> ((IPV4_LEN*8 - i*8))) & 0xFF)));

            if(i != IPV4_LEN) {
                ipStr = String.concat(ipStr, ".");
            }
        }
        return  ipStr;
    }

    public boolean equals(IPv4Address other) {
        return other.toInt() == this.toInt();
    }
}

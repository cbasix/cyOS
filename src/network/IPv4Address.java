package network;

import io.LowlevelLogging;

public class IPv4Address {
    public static final int IPV4_LEN = 4;

    public int addr;

    public IPv4Address(){}

    public IPv4Address(int data){
        addr = data;
    }


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

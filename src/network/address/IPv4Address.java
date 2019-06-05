package network.address;

import arithmetics.Unsigned;
import io.LowlevelLogging;

public class IPv4Address {
    public static final int IPV4_LEN = 4;

    public int addr;
    public int netmask;

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
            addr |= ((ipBytes[i] & 0xFF) << (8*(IPV4_LEN-1-i)));
        }

    }

    public IPv4Address setNetmask(int netmask){
        this.netmask = netmask;
        return this;
    }

    public IPv4Address setNetmaskCidr(int onesCount){

        for (int i = 0; i < IPV4_LEN*8; i++) {
            this.netmask |= ((i < onesCount ? 1 : 0) << (IPV4_LEN*8 - 1 - i));
        }
        return this;
    }

    public IPv4Address setNetmask(byte[] netmaskBytes){
        if (netmaskBytes.length != 4){
            LowlevelLogging.debug("invalid length for ip netmask");
            return null;
        }

        for (int i = 0; i < IPV4_LEN; i++) {
            this.netmask |= ((netmaskBytes[i] & 0xFF) << (8*(IPV4_LEN-1-i)));
        }

        return this;

    }

    public IPv4Address getBroadcastAddr() {
        if (netmask == 0){

            LowlevelLogging.debug("BroadcastAddr calc not possible without netmask");
            return null;
        }
        return new IPv4Address((addr & netmask) | ~netmask);
    }

    public static IPv4Address getGlobalBreadcastAddr(){
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

        if(netmask != 0){
            ipStr = String.concat(ipStr, "/", String.from(getNetmaskSlash()));
        }

        return  ipStr;
    }

    public int getNetmaskSlash(){
        for (int i = 0; i < IPV4_LEN*8; i++){
            if((netmask & (1 << IPV4_LEN*8 - 1 - i)) == 0){
                return i;
            }

        }
        return IPV4_LEN*8;
    }

    public boolean equals(IPv4Address other) {
        return other.toInt() == this.toInt();
    }

    public boolean isInSameNetwork(IPv4Address other){
        if (this.netmask != other.netmask && this.netmask != 0 && other.netmask != 0){
            return false;
        }

        // get the one not zero
        int mask = Unsigned.isGreaterThan(this.netmask, other.netmask) ? this.netmask : other.netmask;

        return (this.addr & mask) == (other.addr & mask);
    }

    public boolean isBroadcastAddr() {
        return addr == getBroadcastAddr().toInt();
    }

    public static IPv4Address fromString(String ipStr){
        int netmaskCidr = 0;

        if (ipStr.countOccurences('/') == 1){
            String[] split = ipStr.split('/');
            ipStr = split[0];
            String netmString = split[1];
            netmaskCidr = netmString.toInt();
        }

        String[] ipParts = ipStr.split('.');
        if (ipParts.length != 4){
            LowlevelLogging.debug("Not a valid IP. Must contain three dots");
            return null;
        }

        byte[] ipBytes = new byte[4];
        for (int i = 0; i < ipParts.length; i++){
            int val = ipParts[i].toInt();
            if (val > 255){
                LowlevelLogging.debug("Not a valid IP. Values must be less than 255");
                return null;
            }

            ipBytes[i] = (byte) val;
        }

        return new IPv4Address(ipBytes).setNetmaskCidr(netmaskCidr);
    }
}

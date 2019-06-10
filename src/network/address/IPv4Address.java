package network.address;

import arithmetics.ByteArray;
import datastructs.ArrayList;
import io.Color;
import io.LowlevelLogging;
import io.LowlevelOutput;
import kernel.Kernel;
import network.dhcp.msg.IPv4AddrStruct;

public class IPv4Address {
    public static final int IPV4_LEN = 4;
    private static IPv4Address globalBroadcast;

    public byte[] addr;
    public byte[] netmask;

    private IPv4Address(){}


    public IPv4Address(byte[] ipBytes) {
        if (ipBytes.length != 4){
            LowlevelLogging.debug("invalid length for ip address");
            Kernel.stop(); // todo remove
            return;
        }

        addr = ipBytes;
    }

    public static IPv4Address fromBytes(byte[] ipBytes){
        if (ipBytes == null){
            return null;
        } else {
            return new IPv4Address(ipBytes);
        }
    }

    public static IPv4Address fromStruct(IPv4AddrStruct ips){
        byte[] addr = new byte[IPV4_LEN];
        for (int i = 0; i < IPV4_LEN; i++) {
            addr[i] = ips.ip[i];
        }
        return new IPv4Address(addr);
    }

    public void writeTo(IPv4AddrStruct ips) {
        for (int i = 0; i < IPV4_LEN; i++) {
            ips.ip[i] = this.addr[i];
        }
    }

    public void writeNetmaskTo(IPv4AddrStruct ips) {
        for (int i = 0; i < IPV4_LEN; i++) {
            ips.ip[i] = this.netmask[i];
        }
    }


    public IPv4Address setNetmaskCidr(int onesCount){
        netmask = new byte[IPV4_LEN];
        int currentPos = 0;
        for (int i = 0; i < IPV4_LEN; i++) {
            for (int b = 7; b >= 0; b--) {
                this.netmask[i] |= ((currentPos < onesCount ? 1 : 0) << (7 - (currentPos % 8)));
                currentPos++;
            }
        }

        return this;
    }

    public IPv4Address setNetmask(byte[] netmaskBytes){
        if (netmaskBytes.length != 4){
            LowlevelLogging.debug("invalid length for ip netmask");
            return null;
        }

        this.netmask = netmaskBytes;

        return this;

    }

    public IPv4Address getBroadcastAddr() {
        if (netmask == null){

            LowlevelLogging.debug("BroadcastAddr calc not possible without netmask");
            return null;
        }

        byte[] broadcast = new byte[IPV4_LEN];
        for(int i = 0; i < IPV4_LEN; i++){
            broadcast[i] = (byte) (0xff & ((addr[i] & netmask[i]) | ~netmask[i]));
        }

        return new IPv4Address(broadcast);
    }

    public static IPv4Address getGlobalBreadcastAddr(){
        if (globalBroadcast == null) {
            globalBroadcast = IPv4Address.fromString("255.255.255.255");
        }
        return globalBroadcast;
    }

    // todo fix
    public byte[] toBytes(){
        return addr;
    }

    public String toString(){
        String ipStr = "";

        for (int i = 0; i < IPV4_LEN; i++) {
            ipStr = String.concat(ipStr, String.from(addr[i] & 0xFF));

            if(i != IPV4_LEN - 1) {
                ipStr = String.concat(ipStr, ".");
            }
        }

        if(netmask != null){
            ipStr = String.concat(ipStr, "/", String.from(getNetmaskCidr()));
        }

        return  ipStr;
    }

    public int getNetmaskCidr(){
        if (this.netmask == null){
            return 0;
        }
        return ByteArray.countStartOnes(this.netmask);
    }

    public boolean equals(IPv4Address other) {
        return ByteArray.equals(this.addr, other.addr);
    }

    public boolean isInSameNetwork(IPv4Address other){
        if (this.netmask != null && other.netmask != null && !ByteArray.equals(this.netmask, other.netmask)){
            return false;
        }

        // get the one not zero
        byte[] mask = this.getNetmaskCidr() > other.getNetmaskCidr() ? this.netmask : other.netmask;

        /*LowlevelOutput.printStr(String.from(this.getNetmaskCidr()), 2, 2, Color.PINK);
        LowlevelLogging.debug(IPv4Address.fromBytes(mask).toString(), " mask ", String.concat(
                String.hexFrom(mask[0]), String.hexFrom(mask[1]), String.hexFrom(mask[2]), String.hexFrom(mask[3])));*/

        return ByteArray.equals(
                ByteArray.and(this.addr, mask),
                ByteArray.and(other.addr, mask)
        );
    }

    public boolean isBroadcastAddr() {
        return ByteArray.equals(addr, getBroadcastAddr().toBytes());
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

        IPv4Address result = new IPv4Address(ipBytes);
        return result.setNetmaskCidr(netmaskCidr);
    }

    public IPv4Address copy(){
        byte[] newAddr = ByteArray.copy(this.addr);
        return new IPv4Address(newAddr);
    }
}

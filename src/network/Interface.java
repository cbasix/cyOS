package network;

import datastructs.ArrayList;
import io.LowlevelLogging;
import network.address.IPv4Address;
import network.ipstack.Ip;


public class Interface {

    public Nic nic;

    public ArrayList ownAddresses;

    public Interface(Nic nic) {
        ownAddresses = new ArrayList();
        //ownAddresses._add(new IPv4Address(ZERO_ADDR)); // placeholder 0.0.0.0 is used as sender addr in broadcast
        ownAddresses._add(IPv4Address.fromString("127.0.0.1").setNetmaskCidr(8)); // link local 127.0.0.1

        this.nic = nic;
    }

    public void addAddress(IPv4Address ip){
        if (ip.getNetmaskCidr() == 0){
            LowlevelLogging.debug("trying to add own ip without netmask");
            return;
        }
        ownAddresses._add(ip);
    }

    public ArrayList getAddresses(){
        return ownAddresses;
    }

    public boolean hasIp(IPv4Address targetIp) {
        for(int addrNo = 0; addrNo < ownAddresses.size(); addrNo++){
            IPv4Address ownIp = (IPv4Address) ownAddresses._get(addrNo);
            if (targetIp.equals(ownIp)){
                return true;
            }
        }
        return false;
    }

    /** last one added is default */
    public IPv4Address getDefaultIp(){
        return (IPv4Address) ownAddresses._get(ownAddresses.size()-1);
    }

    public void removeAddress(IPv4Address ip) {
        // we need the object from the ownAdd List for the == comparation in "remove"
        IPv4Address o = getLocalIpForTarget(ip);
        this.ownAddresses.remove(o);

    }

    public IPv4Address getLocalIpForTarget(IPv4Address ip) {

        //return getDefaultIp();
        for (int i = 0; i < ownAddresses.size(); i++){
            IPv4Address myAddr = (IPv4Address) ownAddresses._get(i);
            if (myAddr.isInSameNetwork(ip)){
                return myAddr;
            }
        }

        // no ip found and target is broadcast -> send with "undefined" ip as sender
        if (IPv4Address.getGlobalBreadcastAddr().equals(ip)){
            return IPv4Address.fromString("0.0.0.0");
        }

        return null;
    }
}

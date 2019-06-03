package network.dhcp;

import datastructs.ArrayList;
import network.address.IPv4Address;

public class DhcpCache {
    public static final int AVAILABLE = 0;
    public static final int RESERVED = 0;
    public static final int IN_USE = 0;

    public class Entry {
        IPv4Address ip;
        int status;
        int leaseTime; // unused for now

        public Entry(IPv4Address ip, int status) {
            this.ip = ip;
            this.status = status;
        }
    }

    ArrayList cache;

    public DhcpCache (){
        cache = new ArrayList();
    }

    public int getStatus(IPv4Address ip){
        Entry e = get(ip);
        if (e == null){
            return AVAILABLE;
        } else {
            return e.status;
        }
    }

    public void setStatus (IPv4Address ip, int status){
        put(ip, status);
    }


    private void put(IPv4Address ip, int status){
        Entry e = get(ip);
        if (e != null){
            e.status = status;
        } else {
            cache._add(new Entry(ip, status));
        }
    }

    private Entry get (IPv4Address ip){
        for (int i = 0; i < cache.size(); i++){
            Entry e = (Entry) cache._get(i);
            if (e.ip.toInt() == ip.toInt()){
                return e;
            }
        }
        return null;
    }


}

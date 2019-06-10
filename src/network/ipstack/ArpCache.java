package network.ipstack;

import datastructs.ArrayList;
import network.address.IPv4Address;
import network.address.MacAddress;

public class ArpCache {
    private ArrayList cache;

    public ArpCache(){
        cache = new ArrayList();
    }

    public ArrayList getList() {
        return cache;
    }

    public static class Entry{
        public IPv4Address ip;
        public MacAddress mac;
        //public int pendingRequests;
    }

    public Entry getEntryFromCache(IPv4Address ip){
        for (int entryNo = 0; entryNo < cache.size(); entryNo++){
            Entry entry = (Entry) cache._get(entryNo);
            if (entry.ip.equals(ip)){
                // may return null if no arp request completed yet
                return entry;
            }
        }
        return null;
    }

    public MacAddress getMac(IPv4Address ip){
        Entry e = getEntryFromCache(ip);
        if (e == null){
            return null;
        } else {
            return e.mac;
        }
    }

    public boolean contains(IPv4Address ip){
        return getEntryFromCache(ip) != null;
    }

    public void put(MacAddress mac, IPv4Address ip){
        if(!contains(ip)){
            Entry e = new Entry();
            e.ip = ip;
            e.mac = mac;
            cache._add(e);
        } else {
            Entry e = getEntryFromCache(ip);
            e.mac = mac;
        }
    }
}

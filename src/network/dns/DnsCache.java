package network.dns;

import datastructs.ArrayList;
import network.address.IPv4Address;

public class DnsCache {

    /** Only supports A-Records */

    ArrayList cache;

    public DnsCache(){
        cache = new ArrayList();
    }

    public void add(ARecord a){
        cache._add(a);
    }

    public ARecord get(String name){
        for (int i = 0; i < cache.size(); i++){
            ARecord a = (ARecord) cache._get(i);
            if(a.name.equals(name)){
                return a;
            }
        }
        return null;
    }
}

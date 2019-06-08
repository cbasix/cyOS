package network.dns;

import network.address.IPv4Address;

public class ARecord {
    String name;
    IPv4Address ip;

    public ARecord(String name, IPv4Address ip) {
        this.name = name;
        this.ip = ip;
    }

    public String getName() {
        return name;
    }

    public IPv4Address getIp() {
        return ip;
    }
}

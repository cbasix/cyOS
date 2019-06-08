package network.ipstack;

import network.address.IPv4Address;
import network.ipstack.binding.BindingsManager;

public class NetworkStack {
    public BindingsManager bindingsManager = new BindingsManager();
    public Udp udpLayer = new Udp(bindingsManager);
    public Ip ipLayer = new Ip();
    public Arp arpLayer = new Arp();
    public Ethernet ethernetLayer = new Ethernet();
    private IPv4Address dnsServer = IPv4Address.fromString("127.0.0.1");


    public NetworkStack(){
        ipLayer.setArpLayer(arpLayer);
        ipLayer.setEthernetLayer(ethernetLayer);
        ipLayer.setUdpLayer(udpLayer);

        udpLayer.setIpLayer(ipLayer);
    }

    public void setDnsServer(IPv4Address ip){
        dnsServer = ip;
    }

    public IPv4Address getDnsServer() {
        return dnsServer;
    }
}

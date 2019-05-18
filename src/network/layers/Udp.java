package network.layers;

import kernel.Kernel;
import network.structs.UdpHeader;

public class Udp {

    public void sendDatagram(int ipAddress, int port, byte[] data){
        Kernel.networkManager.stack.ipLayer.getBuffer(data.length + UdpHeader.SIZE);
    }
}

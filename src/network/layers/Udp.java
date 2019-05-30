package network.layers;

import kernel.Kernel;
import network.layers.abstracts.TransportLayer;
import network.structs.UdpHeader;

public class Udp extends TransportLayer {

    public void sendDatagram(int ipAddress, int port, byte[] data){
        Kernel.networkManager.stack.ipLayer.getBuffer(data.length + UdpHeader.SIZE);
    }
}

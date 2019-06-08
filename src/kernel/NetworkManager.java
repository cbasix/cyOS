package kernel;

import datastructs.ArrayList;
import network.ipstack.NetworkStack;
import network.Nic;

public class NetworkManager {
    public ArrayList nics;
    public NetworkStack stack = new NetworkStack();

   /* public void addInterface(Nic nic){
        nics._add(nic);
    }

    public void getInterfaceForTarget(){

    }*/

    public void receive() {
        byte[] data = Kernel.networkManager.nic.receive();
        if (data != null){
            stack.ethernetLayer.receive(data);
        }
    }
}

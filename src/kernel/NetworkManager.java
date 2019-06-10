package kernel;

import datastructs.ArrayList;
import io.LowlevelLogging;
import network.Interface;
import network.address.IPv4Address;
import network.ipstack.NetworkStack;
import network.Nic;

public class NetworkManager {
    private IPv4Address defaultGateway = IPv4Address.fromString("127.0.0.5");
    private ArrayList interfaces = new ArrayList();
    public NetworkStack stack = new NetworkStack();

    public NetworkManager(){

    }

    public int addInterfaceFor(Nic nic){
        interfaces._add(new Interface(nic));
        return interfaces.size() - 1;
    }

    public boolean receive() {
        boolean received = false;

        for (int i = 0; i < interfaces.size(); i++) {
            byte[] data = ((Interface) interfaces._get(i)).nic.receive();
            if (data != null) {
                stack.ethernetLayer.receive(i, data);
                received = true;
            }
        }
        return received;
    }

    public void setDefaultGateway(IPv4Address ip){
        defaultGateway = ip;
    }

    public IPv4Address getDefaultGateway() {
        return defaultGateway;
    }

    public boolean hasLocalIp(IPv4Address targetIp) {
        for (int interfaceNo = 0; interfaceNo < interfaces.size(); interfaceNo++){
            Interface interf = (Interface) interfaces._get(interfaceNo);

            if (interf.hasIp(targetIp)){
                return true;
            }
        }

        return false;
    }

    public Interface getInterfaceForTarget(IPv4Address targetIp){
        for (int interfaceNo = 0; interfaceNo < interfaces.size(); interfaceNo++){
            Interface interf = (Interface) interfaces._get(interfaceNo);

             IPv4Address adress = interf.getLocalIpForTarget(targetIp);
             if (adress != null){
                 return interf;
             }
        }
        return null;
    }


    public Interface getInterface(int interfaceNo) {
        if (interfaceNo >= interfaces.size()){
            LowlevelLogging.debug("Asking for non existent interface: ", String.from(interfaceNo));
            Kernel.stop();
        }
        return (Interface) interfaces._get(interfaceNo);
    }

    public int getInterfaceNoForTarget(IPv4Address targetIp) {
        for (int interfaceNo = 0; interfaceNo < interfaces.size(); interfaceNo++){
            Interface interf = (Interface) interfaces._get(interfaceNo);

            IPv4Address adress = interf.getLocalIpForTarget(targetIp);
            if (adress != null){
                return interfaceNo;
            }
        }
        return -1;
    }

    public ArrayList getInterfaces() {
        return interfaces;
    }
}

package network.ipstack.binding;

import datastructs.ArrayList;
import io.LowlevelLogging;
import network.address.IPv4Address;
import network.ipstack.abstracts.TransportLayer;

public class BindingsManager extends PackageReceiver{
    public static final int ALL_INTERFACES = -2;
    public static final int UNSET_INTERFACE = -1;

    private final ArrayList bindings;
    private short randomAssignedPort = 1024;

    public int getUnusedPort(int interfaceNo, TransportLayer t) {
        if (randomAssignedPort < 1024) {randomAssignedPort = 1024;}

        randomAssignedPort++;
        while (null != getBindingFor(interfaceNo, t, randomAssignedPort)){
            randomAssignedPort++;
        }
        return (int)randomAssignedPort;
    }

    public static class Binding {


        int interfaceNo;
        TransportLayer transport;
        int port;
        PackageReceiver receiver;

        public Binding(int interfaceNo, TransportLayer transport, int port, PackageReceiver receiver) {
            this.interfaceNo = interfaceNo;
            this.transport = transport;
            this.port = port;
            this.receiver = receiver;
        }
    }

    public BindingsManager(){
         bindings = new ArrayList();
    }

    /**
     *  binds given receiver to specified port on  all ip addresses
     *
     *  returns true if bind was successful
     */
    public boolean bind(int interfaceNo, TransportLayer transport, int port, PackageReceiver receiver){
        if(getBindingFor(interfaceNo, transport, port) == null){
            bindings._add(new Binding(interfaceNo, transport, port, receiver));
            return true;

        } else {
            LowlevelLogging.debug("Port already bound. Canceling the old bind.  ");
            Binding b = getBindingFor(interfaceNo, transport, port);
            b.receiver = receiver;
            return false;
        }
    }

    public void unbind(int interfaceNo, TransportLayer transport, int port, PackageReceiver receiver){
        Binding b;
        do {
            b = getBindingFor(interfaceNo, transport, port);
            if (b != null && b.receiver == receiver) {
                bindings.remove(b);
            }
        } while (b != null);
    }

    private Binding getBindingFor(int interfaceNo, TransportLayer transport, int port){
        for (int i = 0; i < bindings.size(); i++){
            Binding b = (Binding) bindings._get(i);
            if (b.transport == transport && b.port == port && (b.interfaceNo == interfaceNo || b.interfaceNo == ALL_INTERFACES || interfaceNo == ALL_INTERFACES)){
                return b;
            }
        }
        return null;
    }

    @Override
    public void receive(int interfaceNo, TransportLayer transport, IPv4Address senderIp, int senderPort, int receiverPort,  byte[] data) {
        Binding b = getBindingFor(interfaceNo, transport, receiverPort);
        if (b != null){

            b.receiver.receive(interfaceNo, transport, senderIp, senderPort, receiverPort, data);

        } else {
            LowlevelLogging.debug(String.concat("Bindings manager got package for not bound port: ", String.from(receiverPort), "   "));
        }

    }
}

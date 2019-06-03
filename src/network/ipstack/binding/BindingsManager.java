package network.ipstack.binding;

import datastructs.ArrayList;
import io.LowlevelLogging;
import network.address.IPv4Address;
import network.ipstack.abstracts.TransportLayer;

public class BindingsManager extends PackageReceiver{

    private final ArrayList bindings;

    public static class Binding {
        TransportLayer transport;
        int port;
        PackageReceiver receiver;

        public Binding(TransportLayer transport, int port, PackageReceiver receiver) {
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
    public boolean bind(TransportLayer transport, int port, PackageReceiver receiver){
        if(getBindingFor(transport, port) == null){
            bindings._add(new Binding(transport, port, receiver));
            return true;

        } else {
            LowlevelLogging.debug("Port already bound. Canceling the old bind.  ");
            Binding b = getBindingFor(transport, port);
            b.receiver = receiver;
            return false;
        }
    }

    private Binding getBindingFor(TransportLayer transport, int port){
        for (int i = 0; i < bindings.size(); i++){
            Binding b = (Binding) bindings._get(i);
            if (b.transport == transport && b.port == port){
                return b;
            }
        }
        return null;
    }


    @Override
    public void receive(TransportLayer transport, IPv4Address senderIp, int senderPort, int receiverPort,  byte[] data) {
        Binding b = getBindingFor(transport, receiverPort);
        if (b != null){
            b.receiver.receive(transport, senderIp, senderPort, receiverPort, data);
        } else {
            LowlevelLogging.debug(String.concat("Bindings manager got package for not bound port: ", String.from(receiverPort)));
        }
    }
}

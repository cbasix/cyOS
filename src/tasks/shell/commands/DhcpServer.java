package tasks.shell.commands;

import datastructs.RingBuffer;
import kernel.Kernel;
import network.address.IPv4Address;
import tasks.LogEvent;

public class DhcpServer extends Command{

    private network.dhcp.DhcpServer server
            ;

    @Override
    public String getCmd() {
        return "dhcpserver";
    }

    @Override
    public void execute(RingBuffer shellOutput, String[] args) {


        if (args.length < 2){
            shellOutput.push(new LogEvent("use subcomand start or stop"));
            return;
        }

        if (args[1].equals("start")) {
            Kernel.networkManager.stack.ipLayer.addAddress(IPv4Address.fromString("192.168.100.1").setNetmaskCidr(24));
            server = new network.dhcp.DhcpServer();

        } else if (args[1].equals("stop")) {
            if (server != null){
                server.stop();
            }
            Kernel.networkManager.stack.ipLayer.removeAddress(IPv4Address.fromString("192.168.100.1"));
        }
    }
}

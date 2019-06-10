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
        return "dhcps";
    }

    @Override
    public void execute(RingBuffer shellOutput, String[] args) {


        if (args.length < 2){
            shellOutput.push(new LogEvent("assuming start 0"));
            server = new network.dhcp.DhcpServer();
            server.startListenOn(Kernel.networkManager.getInterface(0).getDefaultIp());
            return;
        }

        if (args[1].equals("start")) {
            if (args.length < 3){
                shellOutput.push(new LogEvent("specify interface to start the server on"));
                return;
            }
            int interfaceNo = args[2].toInt();

            server = new network.dhcp.DhcpServer();
            server.startListenOn(Kernel.networkManager.getInterface(interfaceNo).getDefaultIp());

        } else if (args[1].equals("stop")) {
            if (server != null){
                server.stop();
            }
        }
    }
}

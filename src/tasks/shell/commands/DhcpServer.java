package tasks.shell.commands;

import datastructs.RingBuffer;

public class DhcpServer extends Command{

    @Override
    public String getCmd() {
        return "dhcpserver";
    }

    @Override
    public void execute(RingBuffer shellOutput, String[] args) {
        new network.dhcp.DhcpServer();
    }
}

package tasks.shell.commands;

import datastructs.RingBuffer;

public class DhcpClient extends Command{

    @Override
    public String getCmd() {
        return "dhcpclient";
    }

    @Override
    public void execute(RingBuffer shellOutput, String[] args) {
        new network.dhcp.DhcpClient();
    }
}

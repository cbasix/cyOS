package tasks.shell.commands;

import datastructs.RingBuffer;
import tasks.LogEvent;

public class DhcpClient extends Command{

    private network.dhcp.DhcpClient client;

    @Override
    public String getCmd() {
        return "dhcpclient";
    }

    @Override
    public void execute(RingBuffer shellOutput, String[] args) {
        if (args.length < 2){
            shellOutput.push(new LogEvent("use subcomand start or stop"));
            return;
        }

        if (args[1].equals("start")) {
            client = new network.dhcp.DhcpClient();

        } else if (args[1].equals("stop")) {
            if (client != null){
                client.stop();
            }
        }
    }
}

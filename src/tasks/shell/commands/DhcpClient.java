package tasks.shell.commands;

import datastructs.RingBuffer;
import tasks.LogEvent;

public class DhcpClient extends Command{

    private network.dhcp.DhcpClient client;

    @Override
    public String getCmd() {
        return "dhcp";
    }

    @Override
    public void execute(RingBuffer shellOutput, String[] args) {
        if (args.length < 2){
            shellOutput.push(new LogEvent("assuming start 0"));
            client = new network.dhcp.DhcpClient(0);
            return;
        }

        if (args[1].equals("start")) {
            if (args.length < 3){
                shellOutput.push(new LogEvent("specify interface to start the server on"));
                return;
            }
            int interfaceNo = args[2].toInt();

            client = new network.dhcp.DhcpClient(interfaceNo);

        } else if (args[1].equals("stop")) {
            if (client != null){
                client.stop();
            }
        }
    }
}

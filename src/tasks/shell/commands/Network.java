package tasks.shell.commands;

import datastructs.ArrayList;
import datastructs.RingBuffer;
import kernel.Kernel;
import kernel.NetworkManager;
import network.*;
import network.address.IPv4Address;
import network.ipstack.ArpCache;
import network.ipstack.Ip;
import network.ipstack.NetworkStack;
import network.ipstack.binding.BindingsManager;
import tasks.LogEvent;


public class Network extends Command{

    @Override
    public String getCmd() {
        return "net";
    }

    @Override
    public void execute(RingBuffer shellMessageBuffer, String[] args) {
        NetworkStack stack = Kernel.networkManager.stack;
        NetworkManager netManager = Kernel.networkManager;

        if(netManager.getInterfaces().size() == 0){
            shellMessageBuffer.push(new LogEvent("No interfaces found"));
            return;
        }

        if (args.length > 1) {
            if (args[1].equals("receive")) {
                doReceive(stack);


            } if (args[1].equals("gateway")) {
                if (args.length < 3){
                    shellMessageBuffer.push(new LogEvent("Please provide gateway ip"));
                    return;
                }
                netManager.setDefaultGateway(IPv4Address.fromString(args[2]));


            } else if (args[1].equals("dnsserver")) {
                if (args.length < 3){
                    shellMessageBuffer.push(new LogEvent("Please provide dnsserver ip"));
                    return;
                }
                stack.setDnsServer(IPv4Address.fromString(args[2]));


            } else if (args[1].equals("ip")) {
                if (args.length < 4){
                    shellMessageBuffer.push(new LogEvent("Please provide interface no and  ip to add"));
                    return;
                }
                netManager.getInterface(args[2].toInt()).addAddress(IPv4Address.fromString(args[3]));


            } else if (args[1].equals("send")) {
                if(args.length < 4) {
                    shellMessageBuffer.push(new LogEvent("Please provide target and a message"));
                    return;
                }
                doSendMsg(shellMessageBuffer, stack, args[3], args[2]);


            }
        } else {
            shellMessageBuffer.push(new LogEvent("Please select subcommand_ receive or send."));
        }
    }


    public void doSendMsg(RingBuffer shellMessageBuffer, NetworkStack stack, String message, String target) {
        IPv4Address sendToIp = null;

        if (target.equals("arp")) {
            ArrayList arpCacheEntrys = stack.arpLayer.cache.getList();
            for (int i = 0; i < arpCacheEntrys.size(); i++) {
                ArpCache.Entry entry = (ArpCache.Entry) arpCacheEntrys._get(i);
                if (entry.mac != null) {
                    sendToIp = entry.ip;
                }
            }
        } else {
            if (target.countOccurences('.') == 0){
                target = String.concat("192.168.200.", target);
            } else if (target.countOccurences('.') == 1){
                target = String.concat("192.168.", target);
            }


            sendToIp = IPv4Address.fromString(target);
        }

        if (sendToIp == null) {
            shellMessageBuffer.push(new LogEvent("No recipient available."));
            return;
        }


        char[] charData = message.toChars();
        PackageBuffer buffer = stack.ipLayer.getBuffer(charData.length * 2);
        for (int i = 0; i < charData.length; i++){
            buffer.data[buffer.start+2*i] = (byte) (charData[i] >> 8) ;
            buffer.data[buffer.start+2*i+1] = (byte) charData[i];
            //LowlevelOutput.printChar(charData[i], i, 1, Color.RED);
        }

        stack.ipLayer.send(BindingsManager.UNSET_INTERFACE, sendToIp, Ip.PROTO_RAW_TEXT, buffer);


        //LowlevelLogging.printHexdump(MAGIC.addr(buffer.data[0]));
        //Kernel.stop();
    }


    public static void doReceive(NetworkStack stack) {
        boolean received = false;
        do {
            received = Kernel.networkManager.receive();
            //Kernel.wait(50);
        } while(received);
    }
}

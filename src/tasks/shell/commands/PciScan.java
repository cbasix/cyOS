package tasks.shell.commands;

import datastructs.ArrayList;
import datastructs.RingBuffer;
import drivers.pci.PCI;
import drivers.pci.PciBaseAddr;
import drivers.pci.PciDevice;
import tasks.LogEvent;

public class PciScan extends Command{
    @Override
    public String getCmd() {
        return "pciscan";
    }

    @Override
    public void execute(RingBuffer shellMessageBuffer, String[] args) {
        int busNo = 0;
        if (args.length > 1){
            // todo implement correct string to int parse
            busNo = args[1].charAt(0)-'1'+1;
        }
        shellMessageBuffer.push(new LogEvent(String.concat("Scanning bus: ", String.from(busNo))));
        ArrayList pciDevices = PCI.scan(busNo);
        for (int i = 0; i < pciDevices.size(); i++){
            PciDevice p = (PciDevice) pciDevices._get(i);

            String[] out = new String[14];
            shellMessageBuffer.push(new LogEvent(String.concat("Device Id:     0x",  String.hexFrom(p.deviceId))));
            shellMessageBuffer.push(new LogEvent(String.concat("Manufacturer:  0x",  String.hexFrom(p.vendorId))));
            shellMessageBuffer.push(new LogEvent(String.concat("BaseClassCode: 0x",  String.hexFrom(p.baseClassCode))));
            shellMessageBuffer.push(new LogEvent(String.concat("SubClassCode:  0x",  String.hexFrom(p.subClassCode))));
            shellMessageBuffer.push(new LogEvent(String.concat("Interf:        0x",  String.hexFrom(p.interf))));
            shellMessageBuffer.push(new LogEvent(String.concat("Revision:      0x",  String.hexFrom(p.revision))));
            shellMessageBuffer.push(new LogEvent(String.concat("Header:        0x",  String.hexFrom(p.header))));
            shellMessageBuffer.push(new LogEvent(String.concat("Cap Poiner:    0x",  String.hexFrom(p.capabilitiesPointer))));

            shellMessageBuffer.push(new LogEvent("Base Adresses:"));
            for (int j = 0; j < p.baseAddresses.size(); j++) {
                shellMessageBuffer.push(new LogEvent(
                        String.concat(
                            String.hexFrom(((PciBaseAddr) p.baseAddresses._get(j)).address),
                            String.concat(" ", String.hexFrom(((PciBaseAddr) p.baseAddresses._get(j)).size))
                        )
                ));
            }

            shellMessageBuffer.push(new LogEvent(""));

        }
        if(pciDevices.size() == 0){
            shellMessageBuffer.push(new LogEvent("No devices found"));
        }
        //Kernel.wait(50);
    }
}

package tasks.shell.commands;

import datastructs.ArrayList;
import datastructs.RingBuffer;
import drivers.pci.PCI;
import drivers.pci.PciDevice;
import io.LowlevelLogging;
import kernel.Kernel;
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
            busNo = args[1].charAt(0)-'1'+1;
        }
        shellMessageBuffer.push(new LogEvent(String.concat("Scanning bus: ", String.from(busNo))));
        ArrayList pciDevices = PCI.scan(busNo);
        for (int i = 0; i < pciDevices.size(); i++){
            PciDevice p = (PciDevice) pciDevices._get(i);
            shellMessageBuffer.push(new LogEvent(
                    String.concat(
                        String.concat("Device Id: 0x", String.hexFrom(p.deviceId)),
                        String.concat(" Manufacturer Id: 0x", String.hexFrom(p.manufacturerId))
                    )
            ));
        }
        //Kernel.wait(50);
    }
}

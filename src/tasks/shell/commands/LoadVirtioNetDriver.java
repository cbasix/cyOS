package tasks.shell.commands;

import datastructs.ArrayList;
import datastructs.RingBuffer;
import drivers.pci.PCI;
import drivers.pci.PciDevice;
import drivers.virtio.VirtIo;
import drivers.virtio.legacy.VirtioNetLegacy;
import drivers.virtio.net.VirtioNet;
import tasks.LogEvent;


public class LoadVirtioNetDriver extends Command{

    @Override
    public String getCmd() {
        return "virtio";
    }

    @Override
    public void execute(RingBuffer shellMessageBuffer, String[] args) {
        boolean found = false;
        ArrayList pciDevices = PCI.scan(0);
        for (int i = 0; i < pciDevices.size(); i++){
            PciDevice p = (PciDevice) pciDevices._get(i);

            if (p.vendorId == VirtIo.VIRTIO_VENDOR_ID && (
                    p.deviceId == VirtIo.VIRTIO_NETWORK_CARD_ID || p.deviceId == VirtIo.VIRTIO_NETWORK_CARD_ID_2)){
                //LowlevelLogging.debug("Device found");

                byte[] firstMessage = binimp.ByteData.discover;

                //VirtioNetLegacy deviceLegacy = new VirtioNetLegacy(p);
                VirtioNet device = VirtioNet.from(p);
                for (int j = 0; j < 100; j++) {
                    device.send(firstMessage);
                }

                found = true;
            }

        }
        if(!found){
            shellMessageBuffer.push(new LogEvent("No virtio network device found"));
        }
        //Kernel.wait(50);
    }
}

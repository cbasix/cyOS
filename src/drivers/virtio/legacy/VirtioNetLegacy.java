package drivers.virtio.legacy;

import drivers.pci.PciBaseAddr;
import drivers.pci.PciDevice;
import io.Color;
import io.GreenScreenOutput;
import io.LowlevelLogging;
import io.LowlevelOutput;
import kernel.Kernel;

public class VirtioNetLegacy {
    public static final int DEVICE_FEATURES = 0;
    public static final int GUEST_FEATURES = DEVICE_FEATURES + 32;
    public static final int QUEUE_ADDRESS = GUEST_FEATURES + 32;
    public static final int QUEUE_SIZE = QUEUE_ADDRESS + 32;
    public static final int QUEUE_SELECT = QUEUE_SIZE + 16;
    public static final int QUEUE_NOTIFY = QUEUE_SELECT + 16;
    public static final int DEVICE_STATUS = QUEUE_NOTIFY + 16;
    public static final int ISR_STATUS = DEVICE_STATUS + 8;

    public static final int VIRTIO_F_VERSION_1 = 32;

    public VirtioNetLegacy(PciDevice pciDevice){
        GreenScreenOutput out = new GreenScreenOutput();
        out.setCursor(0,0);

        PciBaseAddr bar0 = (PciBaseAddr) pciDevice.baseAddresses._get(0);
        int confAddr = bar0.address & ~3;

        out.print("device features: "); out.printHex(MAGIC.rIOs32(confAddr + DEVICE_FEATURES)); out.println();
        out.print("guest features: "); out.printHex(MAGIC.rIOs32(confAddr + GUEST_FEATURES)); out.println();
        out.print("queue select: "); out.printHex(MAGIC.rIOs16(confAddr + QUEUE_SELECT)); out.println();

        // select queue 0
        MAGIC.wIOs16(confAddr + QUEUE_SELECT, (short)1);
        int nowSelected = MAGIC.rIOs16(confAddr + QUEUE_SELECT);
        out.print("now selected: "); out.printHex(nowSelected); out.println();
        out.print("queue size: "); out.print(String.from(MAGIC.rIOs16(confAddr + QUEUE_SIZE))); out.println();

        if (nowSelected != 1){
            LowlevelLogging.debug("Legacy Interface NOT! supported");
        } else {
            LowlevelLogging.debug("Legacy Interface supported");
        }
    }


}

package drivers.pci;

import datastructs.ArrayList;

public class PCI {
    public static final int ADDR_REG = 0x0CF8;
    public static final int DATA_REG = 0x0CFC;

    public static final int FIXED = 0x80000000;

    @SJC.Inline
    public static int addr(int busNo, int deviceNo, int functionNo, int reg){
        return FIXED | (busNo << 16) | (deviceNo << 11) | (functionNo << 8) | (reg << 2);
    }

    public static ArrayList scan(int busNo){
        ArrayList pciDevices = new ArrayList();
        for (int deviceNo = 0; deviceNo < 32; deviceNo++) {
            int device_and_manufacturer = readConfigSpace(busNo, deviceNo, 0, 0);


            if (device_and_manufacturer != 0xFFFFFFFF && device_and_manufacturer != 0x0){
                pciDevices._add(new PciDevice(busNo, deviceNo));
            }
        }
        return pciDevices;
    }

    @SJC.Inline
    public static int readConfigSpace(int busNo, int deviceNo, int functionNo, int reg){
        MAGIC.wIOs32(ADDR_REG, addr(busNo, deviceNo, functionNo, reg));
        return MAGIC.rIOs32(DATA_REG);
    }

    @SJC.Inline
    public static void writeConfigSpace(int busNo, int deviceNo, int functionNo, int reg, int value){
        MAGIC.wIOs32(ADDR_REG, addr(busNo, deviceNo, functionNo, reg));
        MAGIC.wIOs32(DATA_REG, value);
    }
}

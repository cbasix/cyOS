package drivers.pci;

public class PciBaseAddr {
    public int address;
    public int size;

    public PciBaseAddr(int address, int size) {
        this.address = address;
        this.size = size;
    }
}

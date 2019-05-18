package network;

public class PackageBuffer {
    public PackageBuffer(byte[] data, int start, int usableSize) {
        this.data = data;
        this.start = start;
        this.usableSize = usableSize;
    }

    public byte[] data;
    public int start;
    public int usableSize;
}

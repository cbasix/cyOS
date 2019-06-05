package network.checksum;

public class OnesComplement {
    public static int calc(int checksum, int addr, int len, boolean last) {
        /*
         * Calculate the 16 bit one's complement of the one's complement sum of all
         * 16 bit words in the header. For computing the checksum, the checksum
         * field should be zero. This checksum may be replaced in the future.
         */
        while (len > 1) {
            // This is the inner loop
            checksum += ((int) MAGIC.rMem8(addr + 1) & 0xFF)
                    | ((int) (MAGIC.rMem8(addr) & 0xFF) << 8);
            len -= 2;
            addr += 2;
        }
        // Add left-over byte, if any
        if (len > 0) checksum += (int) MAGIC.rMem8(addr) & 0xFF;
        // Fold 32-bit checksum to 16 bits
        if (last) {
            while ((checksum >>> 16) > 0)
                checksum = (checksum & 0xffff) + (checksum >> 16);
            checksum=~checksum;
        }
        return checksum;
    }
}

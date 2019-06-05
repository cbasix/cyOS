package network.dns.msg;

public class NameParser {
    public static final int PTR_MASK = 0xc0;

    private byte[] data;
    private int pos;
    private String name;

    public NameParser(byte[] data, int pos) {
        this.data = data;
        this.pos = pos;
    }

    public int getPos() {
        return pos;
    }

    public String getName() {
        return name;
    }

    public NameParser invoke() {
        name = "";

        byte labelLenOrPtr;
        int ptrDepth = 0;
        int currentLabelAddr = pos;

        do {
            labelLenOrPtr = data[currentLabelAddr];
            // test if len is a real len or a pointer to another label
            if((labelLenOrPtr & PTR_MASK) == 0){
                int len = labelLenOrPtr;

                // is a label
                if (ptrDepth == 0){
                    pos += labelLenOrPtr;
                }

                char[] nameChars = new char[len];
                for (int i = 1; i <= len; i++) {
                    nameChars[i] = (char) data[currentLabelAddr + i]; // only working for ascii
                }
                name = String.concat(name, ".", new String(nameChars));

            } else {
                //it is a pointer to another label
                int ptr = labelLenOrPtr;

                if (ptrDepth == 0) {
                    pos += 2; // pointers are 2 bytes
                }


                // len is a pointer
                currentLabelAddr = labelLenOrPtr << 8 & data[pos+1];
                ptrDepth++;
            }
        } while (labelLenOrPtr > 0);
        return this;
    }
}
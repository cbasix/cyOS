package drivers.virtio;

public class VirtioPciCapability{

    char cap_vndr;    /* Generic PCI field: PCI_CAP_ID_VNDR */
    char cap_next;    /* Generic PCI field: next ptr. */
    char cap_len;     /* Generic PCI field: capability length */
    char cfg_type;    /* Identifies the structure. */
    char bar;         /* Where to find it. */
    char padding;     /* Pad to full dword. */
    short padding2;
    int offset;      /* Offset within bar. */
    int length;      /* Length of the structure, in bytes. */
}

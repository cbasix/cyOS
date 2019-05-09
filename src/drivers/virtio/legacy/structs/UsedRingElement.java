package drivers.virtio.legacy.structs;

public class UsedRingElement extends STRUCT {
    /* id indicates the head entry of the descriptor chain describing the
       buffer (thismatches an entry placed in the available ring by the guest earlier) */
    /* le32(int) is used here for ids for padding reasons. */
    /* Index of start of used descriptor chain. */
    public int id;

    /* Total length of the descriptor chain which was used (written to) */
    /*  =  The total of bytes written into the buffer */
    public int len;
}
package network.dns.msg.structs;

// based on https://tools.ietf.org/html/rfc1035

public class DnsHeader extends STRUCT{

    public static final int SIZE = 2*6;
    //HEADER
    public short id;
    public short flags;
    public short questionCount;
    public short answerCount;
    public short nameserverCount;
    public short additionalCount;

    //@SJC(count = 0)
    //Question[] question;
}


/* fragen:

    - "schöner umgang mit structs" bsp mac addr lesen ( oder wäre fromStruct(int addr) besser?  Mac als Struct . da structs übergeben geht.

    - reichen ARecords bei DNS / ohne timeouts / bad client checking / ohne netmasks ( wen kann ich direk erreichen, wenn nicht erreichbar fix zu gateway)
    - IPv4 only reicht

    - werden mac/ip addressen immer in network byte order angezeigt? JA
    - checksum (crc32)

    - dns server wird in linux nicht angezeigt
 */
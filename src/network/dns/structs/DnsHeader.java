package network.dns.structs;

// based on https://tools.ietf.org/html/rfc1035

public class DnsHeader extends STRUCT{

    //HEADER
    short id;
    short bits;
    short questionCount;
    short answerCount;
    short nameserverCount;
    short additionalCount;

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
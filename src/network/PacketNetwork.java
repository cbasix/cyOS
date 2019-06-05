package network;

import io.GreenScreenOutput;
import kernel.Kernel;
import kernel.interrupts.receivers.TimerCounter;

// copyright ? frenz & co

// UNUSED JUST FOR REFFERENCE

public class PacketNetwork {
    public final static short ETH_TYPE_ARP = 0x0806;
    public final static short ETH_TYPE_IP = 0x0800;

    public final static byte IP_PROT_ICMP = 0x01;
    public final static byte IP_PROT_UDP  = 0x11;

    public final static int ETH_HEAD = 14;
    public final static int IP_HEAD = 20;
    public final static int UDP_HEAD = 8;

    public final static byte ICMP_TYPE_REQUEST = 8;
    public final static byte ICMP_TYPE_REPLY   = 0;

    public final static int ARP_LEN = 42;
    public final static short ARP_OPER_REQUEST  = 1;
    public final static short ARP_OPER_RESPONSE = 2;

    public final static int ICMP_LEN_woDATA = 42;

    public final static int DHCP_LEN = 590;
    public final static short DHCP_SERVER_PORT = 67;
    public final static short DHCP_CLIENT_PORT = 68;
    public final static byte DHCP_REQUEST = 1;
    public final static byte DHCP_RESPONSE = 2;

    private final static String NO_FREE_TX = "no free TX buffer found";
    private final static boolean DEBUG = false;

    public static int myIP = 0x0A00020F; //0xC0A8022F;

    private static boolean skipPrintNoNetworkCard;

    public static GreenScreenOutput out;

    private static long myMAC=0l;
    public static int lastIP=0;
    public static long macOfLastARP=0l;

    public static class ETH extends STRUCT { //14 bytes
        public short dstMAC0, dstMAC1, dstMAC2;
        public short srcMAC0, srcMAC1, srcMAC2;
        public short type;
    }

    public static class ARP extends STRUCT { //14+28=42
        public ETH eth;
        public short hType, pType;
        public byte hLen, pLen;
        public short oper;
        public short sha0, sha1, sha2;
        public int spa;
        public short tha0, tha1, tha2;
        public int tpa;
    }

    public static class IP extends STRUCT { //14+20=34
        public ETH eth;
        public byte versionIhl, tos;
        public short len;
        public short id, flagsFrag;
        public byte ttl, prot;
        public short chk;
        public int srcIP, dstIP;
    }

    public static class ICMP extends STRUCT { //34+8+x=42+x
        public IP ip;
        public byte type, code;
        public short chk;
        public short id, seq;
        @SJC(count=0) public byte[] data;
    }

    public static class UDP extends STRUCT { //34+8=42
        public IP ip;
        public short srcPort, dstPort;
        public short len, chk;
    }

    public static class DHCP extends STRUCT { //42+44+192+312=590
        public UDP udp;
        public byte op, hType, hLen, hops;
        public int xid;
        public short secs, flags;
        public int ciAddrIP, yiAddrIP, siAddrIP, giAddrIP;
        public short chAddr0, chAddr1, chAddr2, chAddr3, chAddr4, chAddr5, chAddr6, chAddr7;
        @SJC(count=64) public byte[] sname;
        @SJC(count=128) public byte[] file;
        //dhcp extension
        public int cookie;
        @SJC(count=308) public byte[] options;
    }

    public PacketNetwork(){
        out = new GreenScreenOutput();
    }

    public static void dissect(int addr, int len) {
        if (DEBUG) {
            out.print("got packet with length ");
            out.println(len);
        }
        ETH eth=(ETH)MAGIC.cast2Struct(addr);
        switch (shortNH(eth.type)) {
            case ETH_TYPE_ARP:
                ARP arp=(ARP)MAGIC.cast2Struct(addr);
                if (intNH(arp.tpa)==myIP) {
                    switch (shortNH(arp.oper)) {
                        case ARP_OPER_REQUEST:
                            if (DEBUG) out.println("arp request for us");
                            sendARP(false, getMAC(shortNH(eth.srcMAC0), shortNH(eth.srcMAC1), shortNH(eth.srcMAC2)), intNH(arp.spa));
                            break;
                        case ARP_OPER_RESPONSE:
                            long responseMAC=getMAC(shortNH(arp.sha0), shortNH(arp.sha1), shortNH(arp.sha2));
                            out.print("got arp answer for ");
                            out.printIp(intNH(arp.spa));
                            out.print(": ");
                            out.printMac(responseMAC);
                            out.println();
                            if (intNH(arp.spa)==lastIP) macOfLastARP=responseMAC;
                            break;
                        default:
                            if (DEBUG) out.println("invalid arp operation");
                    }
                }
                else if (DEBUG) out.println("arp request with other IP");
                break;
            case ETH_TYPE_IP:
                IP ip=(IP)MAGIC.cast2Struct(addr);
                if (myIP==0 || ip.dstIP==-1 || intNH(ip.dstIP)==myIP) {
                    if (DEBUG) {
                        out.print("got ip packet proto ");
                        out.println((int)ip.prot&0xFF);
                    }
                    switch (ip.prot) {
                        case IP_PROT_ICMP:
                            ICMP icmp=(ICMP)MAGIC.cast2Struct(addr);
                            switch (icmp.type) {
                                case ICMP_TYPE_REQUEST:
                                    if (DEBUG) out.println("got icmp request");
                                    answerICMPRequest(addr, len);
                                    break;
                                case ICMP_TYPE_REPLY:
                                    out.print("got icmp reply from ");
                                    out.printIp(intNH(icmp.ip.srcIP));
                                    if (len==ICMP_LEN_woDATA+4) {
                                        int time=MAGIC.rMem32(MAGIC.addr(icmp.data[0]));
                                        out.print(", round trip time in ticks: ");
                                        out.print(TimerCounter.getCurrent()-time);
                                    }
                                    out.println();
                                    break;
                                default:
                                    if (DEBUG) out.println("unknown ICMP type");
                            }
                            break;
                        case IP_PROT_UDP:
                            UDP udp=(UDP)MAGIC.cast2Struct(addr);
                            if (DEBUG) {
                                out.print("udp destination port: ");
                                out.println((int)shortNH(udp.dstPort)&0xFFFF);
                            }
                            if (shortNH(udp.dstPort)==DHCP_CLIENT_PORT) {
                                if (DEBUG) out.println("got dhcp answer");
                                DHCP dhcp=(DHCP)MAGIC.cast2Struct(addr);
                                if (dhcp.xid!=0xDEADBEEF) {
                                    if (DEBUG) out.println("not our session id");
                                }
                                else if (dhcp.op!=DHCP_RESPONSE) {
                                    if (DEBUG) out.println("not a response");
                                }
                                else {
                                    out.print("offered your/client address ");
                                    out.printIp(myIP=intNH(dhcp.yiAddrIP)); //as we do dhcp, this should be done after following dhcp request and dhcp ack
                                    out.println();
                                }
                            }
                            break;
                        default:
                            if (DEBUG) out.println("unknown ip proto");
                    }
                }
                else if (DEBUG) {
                    out.print("ip packet with other IP: ");
                    out.printIp(intNH(ip.dstIP));
                    out.println();
                }
                break;
            default:
                if (DEBUG) out.println("unknown eth type");
        }
    }

    public static void sendARP(boolean request, long targetMAC, int otherIP) {
        if (!checkNIC()) return;
        int packetAddr;
        byte[] buf= null; //Kernel.networkManager.getTempTXBuffer();
        if (buf==null) {
            out.println(NO_FREE_TX);
            return;
        }
        ARP arp=(ARP)MAGIC.cast2Struct(packetAddr=(int)MAGIC.addr(buf[0]));
        setupETH(arp.eth, targetMAC, myMAC, ETH_TYPE_ARP);
        arp.sha0=arp.eth.srcMAC0;
        arp.sha1=arp.eth.srcMAC1;
        arp.sha2=arp.eth.srcMAC2;
        arp.hType=shortNH(1); //ethernet
        arp.pType=shortNH(ETH_TYPE_IP);
        arp.hLen=6; //size of ethernet address
        arp.pLen=4; //size of ip address
        arp.oper=shortNH(request ? ARP_OPER_REQUEST : ARP_OPER_RESPONSE);
        arp.spa=intNH(myIP);
        if (request) {
            arp.tha0=shortNH((int)(targetMAC>>>32));
            arp.tha1=shortNH((int)(targetMAC>>>16));
            arp.tha2=shortNH((int)targetMAC);
        }
        else arp.tha0=arp.tha1=arp.tha2=0;
        arp.tpa=intNH(otherIP);
        macOfLastARP=0l;
        lastIP=otherIP;
        //Kernel.networkManager.nic.send(0, 0, packetAddr, ARP_LEN);
        out.println("arp packet sent");
    }

    public static void sendICMP(long mac, int ip) {
        if (!checkNIC()) return;
        if (lastIP==0 || lastIP==-1 || macOfLastARP==0l) {
            out.println("need ip and mac address first");
            return;
        }
        byte[] buf=null; //Kernel.networkManager.getTempTXBuffer();
        if (buf==null) {
            out.println(NO_FREE_TX);
            return;
        }
        int addr=(int)MAGIC.addr(buf[0]);
        ICMP icmp=(ICMP)MAGIC.cast2Struct(addr);
        setupIP(icmp.ip, mac, ip, IP_PROT_ICMP, ICMP_LEN_woDATA+4-ETH_HEAD-IP_HEAD);
        icmp.type=ICMP_TYPE_REQUEST;
        icmp.chk=0;
        int time=TimerCounter.getCurrent();
        MAGIC.wMem32(MAGIC.addr(icmp.data[0]), time);
        icmp.chk=shortNH(calcChecksum(0, (int)MAGIC.addr(icmp.type), ICMP_LEN_woDATA+4-ETH_HEAD-IP_HEAD, true));
        //Kernel.networkManager.nic.send(0, 0, addr, ICMP_LEN_woDATA+4);
    }

    public static void sendDHCP() {
        //for BOOTP and DHCP see http://www.networksorcery.com/enp/protocol/bootp/options.htm
        if (!checkNIC()) return;
        myIP=0;
        int packetAddr;
        byte[] buf=null; //Kernel.networkManager.getTempTXBuffer();
        if (buf==null) {
            out.println(NO_FREE_TX);
            return;
        }
        DHCP dhcp=(DHCP)MAGIC.cast2Struct(packetAddr=(int)MAGIC.addr(buf[0]));
        dhcp.chAddr0=shortNH((int)(myMAC>>>32));
        dhcp.chAddr1=shortNH((int)(myMAC>>>16));
        dhcp.chAddr2=shortNH((int)myMAC);
        dhcp.op=DHCP_REQUEST;
        dhcp.hType=1;
        dhcp.hLen=6;
        dhcp.hops=0;
        dhcp.xid=0xDEADBEEF;
        dhcp.secs=0;
        dhcp.flags=shortNH(0x8000);
        dhcp.ciAddrIP=0;
        dhcp.yiAddrIP=0;
        dhcp.siAddrIP=0;
        dhcp.giAddrIP=0;
        dhcp.cookie=intNH(0x63825363);
        //fill in dhcp options
        int pos=0;
        dhcp.options[pos++]=0x35;       //dhcp message type
        dhcp.options[pos++]=1;          //  len==1
        dhcp.options[pos++]=1;          //  type==1 (dhcp discover)
        dhcp.options[pos++]=0x37;       //parameter request list
        dhcp.options[pos++]=4;          //  len==4
        dhcp.options[pos++]=0x01;       //  subnet mask
        dhcp.options[pos++]=0x1C;       //  broadcast address
        dhcp.options[pos++]=0x03;       //  router
        dhcp.options[pos++]=0x06;       //  domain name server
        dhcp.options[pos++]=(byte)0xFF; //end option
        //fill in header
        setupUDP(dhcp.udp, -1l, -1, DHCP_CLIENT_PORT, DHCP_SERVER_PORT, DHCP_LEN-ETH_HEAD-IP_HEAD-UDP_HEAD);
        //send it
        //Kernel.networkManager.nic.send(0, 0, packetAddr, DHCP_LEN);
        out.println("dhcp packet sent");
    }

    private static void setupETH(ETH eth, long dstMAC, long srcMAC, short type) {
        eth.dstMAC0=shortNH((int)(dstMAC>>>32));
        eth.dstMAC1=shortNH((int)(dstMAC>>>16));
        eth.dstMAC2=shortNH((int)dstMAC);
        eth.srcMAC0=shortNH((int)(myMAC>>>32));
        eth.srcMAC1=shortNH((int)(myMAC>>>16));
        eth.srcMAC2=shortNH((int)myMAC);
        eth.type=shortNH(type);
    }

    private static void setupIP(IP ip, long dstMAC, int dstIP, byte prot, int ipDataLen) {
        setupETH(ip.eth, dstMAC, myMAC, ETH_TYPE_IP);
        ip.versionIhl=0x45;
        ip.tos=0;
        ip.flagsFrag=shortNH(0x4000);
        ip.dstIP=intNH(dstIP);
        ip.srcIP=intNH(myIP);
        ip.ttl=64; //standard ttl is 64 (see RFC 1700)
        ip.prot=prot;
        ip.chk=0;
        ip.len=shortNH(ipDataLen+IP_HEAD);
        ip.chk=shortNH(calcChecksum(0, (int)MAGIC.addr(ip.versionIhl), IP_HEAD, true));
    }

    private static void setupUDP(UDP udp, long dstMAC, int dstIP, short srcPort, short dstPort, int udpDataLen) {
        setupIP(udp.ip, dstMAC, dstIP, IP_PROT_UDP, udpDataLen+UDP_HEAD);
        udp.srcPort=shortNH(srcPort);
        udp.dstPort=shortNH(dstPort);
        udp.len=shortNH(udpDataLen+UDP_HEAD);
        //calculate checksum
        int chk;
        chk=calcChecksum(0, (int)MAGIC.addr(udp.ip.srcIP), 8, false);
        chk+=(int)IP_PROT_UDP&0xFF; //0 and proto, already in network byte order
        chk=calcChecksum(chk, (int)MAGIC.addr(udp.len), 2, false);
        udp.chk=shortNH(calcChecksum(chk, (int)MAGIC.addr(udp.srcPort), udpDataLen+UDP_HEAD, true));
    }

    private static void answerICMPRequest(int requestAddr, int len) {
        if (!checkNIC()) return;
        byte[] buf=null; //Kernel.networkManager.getTempTXBuffer();
        if (buf==null) {
            out.println(NO_FREE_TX);
            return;
        }
        int answerAddr=(int)MAGIC.addr(buf[0]);
        for (int i=0; i<len; i++) MAGIC.wMem8(answerAddr+i, MAGIC.rMem8(requestAddr+i));
        ICMP reply=(ICMP)MAGIC.cast2Struct(answerAddr), request=(ICMP)MAGIC.cast2Struct(requestAddr);
        setupIP(reply.ip, getMAC(shortNH(request.ip.eth.srcMAC0), shortNH(request.ip.eth.srcMAC1), shortNH(request.ip.eth.srcMAC2)),
                request.ip.srcIP, IP_PROT_ICMP, len-ETH_HEAD-IP_HEAD);
        reply.type=ICMP_TYPE_REPLY;
        reply.chk=0;
        reply.chk=shortNH(calcChecksum(0, (int)MAGIC.addr(reply.type), len-ETH_HEAD-IP_HEAD, true));
        //Kernel.networkManager.nic.send(0, 0, answerAddr, len);
    }

    private static int calcChecksum(int checksum, int addr, int len, boolean last) {
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

    private static boolean checkNIC() {
        if (Kernel.networkManager.nic==null) {
            if (!skipPrintNoNetworkCard) {
                skipPrintNoNetworkCard=true;
                out.println("no network card found");
            }
            return false;
        }
        myMAC=Kernel.networkManager.nic.getMacAddress().toLong();
        return true;
    }

    private static short shortNH(int s) {
        int b0=s&0xFF;
        int b1=(s>>>8)&0xFF;
        return (short)((b0<<8)|b1);
    }

    private static int intNH(int i) {
        int b0=i&0xFF;
        int b1=(i>>>8)&0xFF;
        int b2=(i>>>16)&0xFF;
        int b3=(i>>>24)&0xFF;
        return ((b0<<24)|(b1<<16)|(b2<<8)|b3);
    }

    private static long getMAC(short s0, short s1, short s2) {
        long l0=s0&0xFFFF, l1=s1&0xFFFF, l2=s2&0xFFFF;
        return (l0<<32)|(l1<<16)|l2;
    }
}

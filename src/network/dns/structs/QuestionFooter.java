package network.dns.structs;

public class QuestionFooter extends STRUCT {
    public static final int SIZE = 4;

    public short qtype;
    public short qclass;
}

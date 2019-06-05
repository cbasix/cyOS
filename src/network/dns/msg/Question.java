package network.dns.msg;

public class Question {
    String name;
    int qType;
    int qClass;

    public Question(String name, int qType, int qClass) {
        this.name = name;
        this.qType = qType;
        this.qClass = qClass;
    }
}

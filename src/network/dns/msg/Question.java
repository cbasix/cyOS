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

    public String getName() {
        return name;
    }

    public Question setName(String name) {
        this.name = name;
        return this;
    }

    public int getqType() {
        return qType;
    }

    public Question setqType(int qType) {
        this.qType = qType;
        return this;
    }

    public int getqClass() {
        return qClass;
    }

    public Question setqClass(int qClass) {
        this.qClass = qClass;
        return this;
    }
}

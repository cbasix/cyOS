package network.dns.msg;

import conversions.Endianess;
import datastructs.ArrayList;
import network.dns.ARecord;
import network.dns.structs.DnsHeader;
import network.dns.structs.QuestionFooter;
import network.dns.structs.ResourceRecordFooter;

public class DnsMessage {
    public static final int BIT_QR = 15;
    public static final int BIT_OPCODE = 11;
    public static final int BIT_AA = 10;
    public static final int BIT_TC = 9;
    public static final int BIT_RD = 8;
    public static final int BIT_RA = 7;
    public static final int BIT_RCODE = 0;

    public static final int TYPE_QUERY = 0;
    public static final int TYPE_RESPONSE = 1;

    public static final int OPCODE_QUERY = 0;
    public static final int OPCODE_IQUERY = 1;
    public static final int OPCODE_STATUS = 2;

    public static final int RCODE_NO_ERROR = 0;
    public static final int RCODE_FORMAT_ERROR = 1;
    public static final int RCODE_NOT_IMPLEMENTED = 4;
    private static final int MAX_DNS_SIZE = 512;

    private int id;
    private int type = TYPE_QUERY;
    private int opcode = OPCODE_QUERY;
    private boolean authoritative = false;
    private boolean truncated = false;
    private boolean recursionDesired = false;
    private boolean recursionAvailable = false;
    private int responseCode = 0;
    private int questionCount = 0;
    private int answerCount = 0;
    private int authorityNameserversCount = 0;
    private int additionalCount = 0;

    private ArrayList questions;

    public DnsMessage setId(int id) {
        this.id = id;
        return this;
    }

    public DnsMessage setType(int type) {
        this.type = type;
        return this;
    }

    public DnsMessage setOpcode(int opcode) {
        this.opcode = opcode;
        return this;
    }

    public DnsMessage setAuthoritative(boolean authoritative) {
        this.authoritative = authoritative;
        return this;
    }

    public DnsMessage setTruncated(boolean truncated) {
        this.truncated = truncated;
        return this;
    }

    public DnsMessage setRecursionDesired(boolean recursionDesired) {
        this.recursionDesired = recursionDesired;
        return this;
    }

    public DnsMessage setRecursionAvailable(boolean recursionAvailable) {
        this.recursionAvailable = recursionAvailable;
        return this;
    }

    public DnsMessage setResponseCode(int responseCode) {
        this.responseCode = responseCode;
        return this;
    }

    public int getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    public int getOpcode() {
        return opcode;
    }

    public boolean isAuthoritative() {
        return authoritative;
    }

    public boolean isTruncated() {
        return truncated;
    }

    public boolean isRecursionDesired() {
        return recursionDesired;
    }

    public boolean isRecursionAvailable() {
        return recursionAvailable;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public int getQuestionCount() {
        return questionCount;
    }

    public int getAnswerCount() {
        return answerCount;
    }

    public int getAuthorityNameserversCount() {
        return authorityNameserversCount;
    }

    public int getAdditionalCount() {
        return additionalCount;
    }

    public ArrayList getQuestions() {
        return questions;
    }

    public ArrayList getAnswers() {
        return answers;
    }

    private ArrayList answers;
    //ArrayList authoritys;
    //ArrayList additional;

    /* ONLY QUESTION AND ANSWER SECTION ARE PARSED */

    public DnsMessage(){}

    public static DnsMessage fromBytes(byte[] data){
        DnsMessage msg = new DnsMessage();

        DnsHeader header = (DnsHeader) MAGIC.cast2Struct(MAGIC.addr(data[0]));
        msg.setHeader(header);

        int pos = header.SIZE;
        for (int i = 0; i < msg.questionCount; i++){
            pos = msg.readQuestion(data, pos);
        }
        for (int i = 0; i < msg.answerCount; i++){
            pos = msg.readAnswer(data, pos);
        }

        return null;
    }

    /* returns the new position to continue reading.*/
    private int readQuestion(byte[] data, int pos) {
        NameParser nameParser = new NameParser(data, pos).invoke();
        pos = nameParser.getPos();
        String name = nameParser.getName();

        QuestionFooter qf = (QuestionFooter) MAGIC.cast2Struct(MAGIC.addr(data[pos]));
        int qtype = Endianess.convert(qf.qtype);
        int qclass = Endianess.convert(qf.qclass);
        pos += QuestionFooter.SIZE;

        addQuestion(name, qtype, qclass);

        return pos;
    }

    private int readAnswer(byte[] data, int pos) {
        NameParser nameParser = new NameParser(data, pos).invoke();
        pos = nameParser.getPos();
        String name = nameParser.getName();

        ResourceRecordFooter rr = (ResourceRecordFooter) MAGIC.cast2Struct(MAGIC.addr(data[pos]));
        int qtype = Endianess.convert(rr.type);
        int qclass = Endianess.convert(rr.class_);
        int ttl = Endianess.convert(rr.ttl);
        int rdlength = Endianess.convert(rr.rdlength);
        pos += ResourceRecordFooter.SIZE;

        byte[] rdata = new byte[rdlength];
        for(int i = 0; i < rdlength; i++){
            rdata[i] = rr.rdata[i];
        }

        addAnswer(name, qtype, qclass, ttl, rdata);

        return pos;
    }

    private void setHeader(DnsHeader header){
        this.id = Endianess.convert(header.id);
        this.type = (header.bits & BIT_QR) == 0 ? 0: 1;
        this.opcode = (header.bits >> BIT_OPCODE) & 0xF;
        this.authoritative = (header.bits & BIT_AA) == 1;
        this.truncated = (header.bits & BIT_TC) == 1;
        this.recursionDesired = (header.bits & BIT_RD) == 1;
        this.recursionAvailable = (header.bits & BIT_RA) == 1;
        this.responseCode = header.bits & 0xF;

        this.questionCount = Endianess.convert(header.questionCount);
        this.answerCount = Endianess.convert(header.answerCount);
        this.authorityNameserversCount = Endianess.convert(header.nameserverCount);
        this.additionalCount = Endianess.convert(header.additionalCount);
    }

    private void writeHeaderTo(DnsHeader header) {
        header.id = Endianess.convert((short) this.id);
        header.bits |= (this.type & 1) << BIT_QR;
        header.bits |= (this.opcode & 0xF) << BIT_OPCODE;
        header.bits |= (this.authoritative ? 1 : 0) << BIT_AA;
        header.bits |= (this.truncated ? 1 : 0) << BIT_TC;
        header.bits |= (this.recursionDesired ? 1 : 0) << BIT_RD;
        header.bits |= (this.recursionAvailable ? 1 : 0) << BIT_RA;
        header.bits |= this.responseCode & 0xF;

        header.questionCount = Endianess.convert((short) this.questionCount);
        header.answerCount = Endianess.convert((short) this.answerCount);
        header.nameserverCount = Endianess.convert((short) this.authorityNameserversCount);
        header.additionalCount = Endianess.convert((short) this.additionalCount);
    }

    public DnsMessage addQuestion(String name, int qType, int qClass){
        questions._add(new Question(name, qType, qClass));
        questionCount = questions.size();
        return this;
    }

    public DnsMessage addAnswer(String name, int qtype, int qclass, int ttl, byte[] rdata) {
        answers._add(new ResourceRecord(name, qtype, qclass, ttl, rdata));
        answerCount = answers.size();
        return this;
    }

    public byte[] toBytes(){
        byte[] temp = new byte[MAX_DNS_SIZE];
        int pos = 0;

        DnsHeader header = (DnsHeader) MAGIC.cast2Struct(MAGIC.addr(temp[0]));
        this.writeHeaderTo(header);


        for (int i = 0; i < questions.size(); i++){
            Question q = (Question) questions._get(i);
            pos = writeQuestion(temp, pos, q);

        }

        for (int i = 0; i < answers.size(); i++){
            ResourceRecord q = (ResourceRecord) answers._get(i);
            pos = writeResourceRecord(temp, pos, q);

        }

        // now real length is known
        byte[] result = new byte[pos];
        for (int i = 0; i < pos; i++){
            result[i] = temp[i];
        }
        return result;

    }

    public int writeQuestion(byte[] temp, int pos, Question q) {
        pos = writeName(temp, pos, q.name);

        QuestionFooter qf = (QuestionFooter) MAGIC.cast2Struct(MAGIC.addr(temp[pos]));
        qf.qclass = Endianess.convert((short) q.qClass);
        qf.qtype = Endianess.convert((short) q.qType);
        pos += QuestionFooter.SIZE;

        return pos;
    }

    private int writeResourceRecord(byte[] temp, int pos, ResourceRecord q) {
        pos = writeName(temp, pos, q.name);

        ResourceRecordFooter rr = (ResourceRecordFooter) MAGIC.cast2Struct(MAGIC.addr(temp[pos]));
        rr.type = Endianess.convert((short) q.type);
        rr.class_ = Endianess.convert((short) q.class_);
        rr.ttl = Endianess.convert(q.ttl);
        rr.rdlength = Endianess.convert((short) q.rdata.length);
        pos += ResourceRecordFooter.SIZE;

        return pos;
    }

    public int writeName(byte[] temp, int pos, String name) {
        String[] labels = name.split('.');
        for (String label: labels){
            temp[pos++] = (byte)label.length(); // todo error if label > 63
            for (int i = 0; i < label.length(); i++){
                char c = label.charAt(i);
                temp[pos++] = (byte) c;
            }
        }
        return pos;
    }


}

package tests.highlevel;

import io.Color;
import io.LowlevelLogging;
import io.LowlevelOutput;
import network.dns.msg.DnsMessage;
import network.dns.msg.NameParser;
import network.dns.msg.Question;
import network.dns.msg.structs.DnsHeader;
import network.dns.msg.structs.QuestionFooter;

public class DnsTest {
    public static int test(){
        DnsMessage m = new DnsMessage().setId((short)4).setResponseCode(DnsMessage.RCODE_NOT_IMPLEMENTED);
        byte[] b = m.toBytes();

        if (b.length != DnsHeader.SIZE){
            LowlevelLogging.debug(String.from(b.length));
            return 3001;
        }
        String s = "test.example.com";
        m.addQuestion(s);
        b = m.toBytes();

        if (b.length != DnsHeader.SIZE + QuestionFooter.SIZE + s.length()+2){
            LowlevelOutput.printStr(String.from(b.length), 5, 10, Color.RED);
            LowlevelOutput.printStr(String.from(DnsHeader.SIZE + QuestionFooter.SIZE + s.length()+2), 5, 11, Color.GREEN);
            LowlevelLogging.printHexdump(MAGIC.addr(b[0]));
            return 3003;
        }

        DnsMessage m2 = DnsMessage.fromBytes(b);
        if (m2.getId() != m.getId()){
            LowlevelOutput.printStr(String.hexFrom(m2.getId()), 5, 10, Color.RED);
            LowlevelOutput.printStr(String.hexFrom(m.getId()), 5, 11, Color.GREEN);
            LowlevelLogging.printHexdump(MAGIC.addr(b[0]));

            return 3005;
        }
        if (m2.getResponseCode() != m.getResponseCode()){return 3006;}

        if (m2.getAnswers().size() != m.getAnswers().size()){return 3008;}
        if (m2.getQuestions().size() != m.getQuestions().size()){return 3010;}
        for(int i = 0; i < m2.getAnswers().size(); i++){
            Question mQuestion = (Question) m.getAnswers()._get(i);
            Question m2Question = (Question) m2.getAnswers()._get(i);

            if(!mQuestion.getName().equals(m2Question.getName())){return 3013;}
            if(mQuestion.getqClass() != m2Question.getqClass()){return 3015;}
        }

        byte[] testLabel = new byte[17];
        testLabel[0] = 0x0a;
        testLabel[1] = 0x64;
        testLabel[2] = 0x68;
        testLabel[3] = 0x63;
        testLabel[4] = 0x70;
        testLabel[5] = 0x73;
        testLabel[6] = 0x65;
        testLabel[7] = 0x72;
        testLabel[8] = 0x76;
        testLabel[9] = 0x65;
        testLabel[10] = 0x72;
        testLabel[11] = 0x04;
        testLabel[12] = 0x63;
        testLabel[13] = 0x79;
        testLabel[14] = 0x6f;
        testLabel[15] = 0x73;
        testLabel[16] = 0x00;
        //test name parser
        NameParser p = new NameParser(testLabel, 0).invoke();
        String name = p.getName();
        if (!p.getName().equals("dhcpserver.cyos")){return 3018;}


        return 0;
    }
}

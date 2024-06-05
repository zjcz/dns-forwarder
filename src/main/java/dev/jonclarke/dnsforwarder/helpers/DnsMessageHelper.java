package dev.jonclarke.dnsforwarder.helpers;

import dev.jonclarke.dnsforwarder.message.DnsHeader;
import dev.jonclarke.dnsforwarder.message.DnsMessage;
import dev.jonclarke.dnsforwarder.message.DnsQuestion;
import dev.jonclarke.dnsforwarder.message.DnsResourceRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DnsMessageHelper {
    public static DnsMessage createMessage(String[] domains, Short id) {
        Short msgId = id;
        if (msgId == null) {
            Random random = new Random();
            msgId = (short)random.nextInt(32767);
        }
        DnsHeader header = new DnsHeader(msgId, (short)0, (short)domains.length, (short)0, (short)0, (short)0);
        List<DnsQuestion> questions = new ArrayList<>();
        for (String d : domains) {
            questions.add(new DnsQuestion(d, (short)1, (short)1));
        }

        return new DnsMessage(header, questions.toArray(new DnsQuestion[]{}), null, null, null);
    }

    public static DnsMessage createMessage(DnsAnswer[] answers, Short id) {
        Short msgId = id;
        if (msgId == null) {
            Random random = new Random();
            msgId = (short)random.nextInt(32767);
        }
        List<DnsQuestion> dnsQuestions = new ArrayList<>();
        List<DnsResourceRecord> dnsAnswers = new ArrayList<>();
        for (DnsAnswer a : answers) {
            dnsQuestions.add(new DnsQuestion(a.domain(), (short)1, (short)1));
            if (a.ip().isPresent()) {
                dnsAnswers.add(new DnsResourceRecord(a.domain(), (short) 1, (short) 1, a.ttl(), a.ip().get()));
            }
        }
        DnsHeader header = new DnsHeader(msgId, (short)0, (short)dnsQuestions.size(), (short)dnsAnswers.size(), (short)0, (short)0);

        return new DnsMessage(header, dnsQuestions.toArray(new DnsQuestion[]{}), dnsAnswers.toArray(new DnsResourceRecord[]{}), null, null);
    }

    public static DnsMessage createErrorMessage(String[] domains, Short id, short rcode) {
            Short msgId = id;
            if (msgId == null) {
                Random random = new Random();
                msgId = (short)random.nextInt(32767);
            }
            DnsHeader header = new DnsHeader(msgId, rcode, (short)domains.length, (short)0, (short)0, (short)0);
            List<DnsQuestion> questions = new ArrayList<>();
            for (String d : domains) {
                questions.add(new DnsQuestion(d, (short)1, (short)1));
            }

        return new DnsMessage(header, questions.toArray(new DnsQuestion[]{}), null, null, null);
    }
}

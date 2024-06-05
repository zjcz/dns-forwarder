package dev.jonclarke.dnsforwarder.parsers;

import dev.jonclarke.dnsforwarder.message.DnsHeader;
import dev.jonclarke.dnsforwarder.message.DnsMessage;
import dev.jonclarke.dnsforwarder.message.DnsQuestion;
import dev.jonclarke.dnsforwarder.message.DnsResourceRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class DnsMessagePacker {
    protected static final Logger logger = LogManager.getLogger();

    public byte[] packRequest(DnsMessage message) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        try {
            DnsHeader header = message.header();
            dataOutputStream.writeShort(header.ID());
            dataOutputStream.writeShort(header.flags());
            dataOutputStream.writeShort(header.QDCOUNT());
            dataOutputStream.writeShort(header.ANCOUNT());
            dataOutputStream.writeShort(header.NSCOUNT());
            dataOutputStream.writeShort(header.ARCOUNT());

            if (message.questions() != null) {
                for (DnsQuestion question : message.questions()) {
                    String[] parts = question.QNAME().split("\\.");
                    for (String part : parts) {
                        byte[] partBytes = part.getBytes();
                        dataOutputStream.writeByte(partBytes.length);
                        dataOutputStream.write(partBytes);
                    }
                    dataOutputStream.writeByte(0);
                    dataOutputStream.writeShort(question.QTYPE());
                    dataOutputStream.writeShort(question.QCLASS());
                }
            }

            if (message.answers() != null) {
                for (DnsResourceRecord answer : message.answers()) {
                    String[] domainParts = answer.NAME().split("\\.");
                    for (String part : domainParts) {
                        byte[] partBytes = part.getBytes();
                        dataOutputStream.writeByte(partBytes.length);
                        dataOutputStream.write(partBytes);
                    }
                    dataOutputStream.writeByte(0);
                    dataOutputStream.writeShort(answer.TYPE());
                    dataOutputStream.writeShort(answer.CLASS());
                    dataOutputStream.writeInt(answer.TTL());

                    String[] ipParts = answer.RDATA().split("\\.");
                    dataOutputStream.writeShort(ipParts.length);
                    for (String part : ipParts) {
                        dataOutputStream.writeByte(Integer.parseInt(part));
                    }
                }
            }

            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            logger.error("Error packaging DNS message", e);
        }
        return null;
    }
}

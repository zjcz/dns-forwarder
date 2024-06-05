package dev.jonclarke.dnsforwarder.parsers;

import dev.jonclarke.dnsforwarder.message.DnsHeader;
import dev.jonclarke.dnsforwarder.message.DnsMessage;
import dev.jonclarke.dnsforwarder.message.DnsQuestion;
import dev.jonclarke.dnsforwarder.message.DnsResourceRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Unpacks a DNS message from a byte array into a DnsMessage object
 */
public class DnsMessageParser {
    protected static final Logger logger = LogManager.getLogger();

    public DnsMessage unpackRequest(byte[] message) {
        if (message == null || message.length == 0) {
            return null;
        }

        DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(message));
        try {
            List<DnsQuestion> questions = new ArrayList<>();
            List<DnsResourceRecord> answers = new ArrayList<>();
            List<DnsResourceRecord> authorities = new ArrayList<>();
            List<DnsResourceRecord> additionals = new ArrayList<>();

            DnsHeader header = parseHeader(dataInputStream);

            for (int n = 0; n < header.QDCOUNT(); n++) {
                questions.add(parseQuestion(dataInputStream));
            }
            for (int n = 0; n < header.ANCOUNT(); n++) {
                answers.add(parseResource(dataInputStream, message));
            }
            for (int n = 0; n < header.NSCOUNT(); n++) {
                authorities.add(parseResource(dataInputStream, message));
            }
            for (int n = 0; n < header.ARCOUNT(); n++) {
                additionals.add(parseResource(dataInputStream, message));
            }

            return new DnsMessage(header, questions.toArray(new DnsQuestion[]{}),
                                            answers.toArray(new DnsResourceRecord[]{}),
                                            authorities.toArray(new DnsResourceRecord[]{}),
                                            additionals.toArray(new DnsResourceRecord[]{}));
        } catch (IOException e) {
            logger.error("Error parsing DNS message", e);
            return null;
        }
    }

    private DnsHeader parseHeader(DataInputStream data) throws IOException {
        // https://datatracker.ietf.org/doc/html/rfc1035#section-4.1.1
        short id = data.readShort();// & 0xFFFF;
        short flags = data.readShort();
        short qdCount = data.readShort();
        short anCount = data.readShort();
        short nsCount = data.readShort();
        short arCount = data.readShort();

        return new DnsHeader(id, flags, qdCount, anCount, nsCount, arCount);
    }

    private DnsQuestion parseQuestion(DataInputStream data) throws IOException {
        // https://datatracker.ietf.org/doc/html/rfc1035#section-4.1.2
        StringBuilder question = new StringBuilder();
        int recLength = data.readByte();
        while (recLength > 0) {
            byte[] record = new byte[recLength];
            for (int i = 0; i < recLength; i++) {
                record[i] = data.readByte();
            }
            if (!question.isEmpty()) {
                question.append(".");
            }
            question.append(new String(record, StandardCharsets.UTF_8));
            recLength = data.readByte();
        }
        short qType = data.readShort();
        short qClass = data.readShort();

        return new DnsQuestion(question.toString(), qType, qClass);
    }

    private DnsResourceRecord parseResource(DataInputStream data, byte[] response) throws IOException {
        // https://datatracker.ietf.org/doc/html/rfc1035#section-4.1.3 - resource record format
        // https://datatracker.ietf.org/doc/html/rfc1035#section-4.1.4 - compression
        DnsResourceRecord resourceRecord = null;

        int recLength = data.readByte();
        int firstTwoBits = (recLength & 0b11000000) >>> 6;

        // if the first two bits are 11, then it is a pointer to a previous part of the message
        // see https://datatracker.ietf.org/doc/html/rfc1035#section-4.1.4 - compression
        if(firstTwoBits == 3) {
            byte currentByte = data.readByte();
            boolean stop = false;
            byte[] newArray = Arrays.copyOfRange(response, currentByte, response.length);
            DataInputStream sectionDataInputStream = new DataInputStream(new ByteArrayInputStream(newArray));
            ArrayList<Integer> RDATA = new ArrayList<>();
            ArrayList<String> DOMAINS = new ArrayList<>();
            short qType = 0;
            short qClass = 0;
            int ttl = 0;
            while (!stop) {
                byte nextByte = sectionDataInputStream.readByte();
                if(nextByte != 0) {
                    byte[] currentLabel = new byte[nextByte];
                    for(int j = 0; j < nextByte; j++) {
                        currentLabel[j] = sectionDataInputStream.readByte();
                    }
                    DOMAINS.add(new String(currentLabel, StandardCharsets.UTF_8));
                } else {
                    stop = true;
                    qType = data.readShort();
                    qClass = data.readShort();
                    ttl = data.readInt();
                    int rdLength = data.readShort();
                    for(int s = 0; s < rdLength; s++) {
                        int nx = data.readByte() & 255;
                        RDATA.add(nx);
                    }
                }
            }

            StringBuilder ip = new StringBuilder();
            StringBuilder domain = new StringBuilder();
            for (Integer ipPart : RDATA) {
                if (!ip.isEmpty()) {
                    ip.append(".");
                }

                ip.append(ipPart);
            }

            for (String domainPart : DOMAINS) {
                if(!domainPart.isEmpty()) {
                    if (!domain.isEmpty()) {
                        domain.append(".");
                    }
                    domain.append(domainPart);
                }
            }

            resourceRecord = new DnsResourceRecord(domain.toString(), qType, qClass, ttl, ip.toString());
        } else if(firstTwoBits == 0) {
            // no compression, this is a normal resource record
            // https://datatracker.ietf.org/doc/html/rfc1035#section-4.1.3 - resource record format
            ArrayList<Integer> RDATA = new ArrayList<>();
            StringBuilder ip = new StringBuilder();
            StringBuilder name = new StringBuilder();
            while (recLength > 0) {
                byte[] record = new byte[recLength];
                for (int i = 0; i < recLength; i++) {
                    record[i] = data.readByte();
                }

                if (!name.isEmpty()) {
                    name.append(".");
                }
                name.append(new String(record, StandardCharsets.UTF_8));
                recLength = data.readByte();
            }
            short qType = data.readShort();
            short qClass = data.readShort();
            int ttl = data.readInt();
            int rdLength = data.readShort();
            for(int s = 0; s < rdLength; s++) {
                int nx = data.readByte() & 255;
                RDATA.add(nx);
            }

            for (Integer ipPart : RDATA) {
                if (!ip.isEmpty()) {
                    ip.append(".");
                }

                ip.append(ipPart);
            }

            resourceRecord = new DnsResourceRecord(name.toString(), qType, qClass, ttl, ip.toString());
        }

        return resourceRecord;
    }
}

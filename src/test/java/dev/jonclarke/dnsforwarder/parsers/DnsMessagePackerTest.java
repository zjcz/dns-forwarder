package dev.jonclarke.dnsforwarder.parsers;

import dev.jonclarke.dnsforwarder.helpers.DnsAnswer;
import dev.jonclarke.dnsforwarder.helpers.DnsMessageHelper;
import dev.jonclarke.dnsforwarder.message.DnsMessage;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class DnsMessagePackerTest {
    DnsMessagePacker packer = new DnsMessagePacker();

    @Test
    void givenValidDnsRequest_whenPacked_thenExpectValidDnsMessage() {
        // Arrange
        DnsMessage message = DnsMessageHelper.createMessage(new String[] {"www.google.com"}, (short)1234);

        // Act
        byte[] request = packer.packRequest(message);

        // Assert
        assertNotNull(request);
    }

    @Test
    void givenValidDnsRequest_whenPacked_thenExpectValidDnsMessageHeader() {
        // Arrange
        DnsMessage message = DnsMessageHelper.createMessage(new String[] {"www.google.com"}, (short)1234);

        // Act
        byte[] request = packer.packRequest(message);

        // Assert
        // convert back and test
        DnsMessageParser parser = new DnsMessageParser();
        DnsMessage unpacked = parser.unpackRequest(request);
        assertNotNull(unpacked);
        assertEquals(message.header().ID(), unpacked.header().ID());
        assertEquals(message.header().flags(), unpacked.header().flags());
        assertEquals(message.header().QDCOUNT(), unpacked.header().QDCOUNT());
        assertEquals(message.header().ANCOUNT(), unpacked.header().ANCOUNT());
        assertEquals(message.header().NSCOUNT(), unpacked.header().NSCOUNT());
        assertEquals(message.header().ARCOUNT(), unpacked.header().ARCOUNT());
    }

    @Test
    void givenValidDnsRequest_whenPacked_thenExpectValidDnsMessageQuestion() {
        // Arrange
        DnsMessage message = DnsMessageHelper.createMessage(new String[] {"www.google.com"}, (short)1234);

        // Act
        byte[] request = packer.packRequest(message);

        // Assert
        // convert back and test
        DnsMessageParser parser = new DnsMessageParser();
        DnsMessage unpacked = parser.unpackRequest(request);
        assertNotNull(unpacked);
        assertEquals(1, unpacked.header().QDCOUNT());
        assertEquals(message.questions()[0].QNAME(), unpacked.questions()[0].QNAME());
        assertEquals(message.questions()[0].QTYPE(), unpacked.questions()[0].QTYPE());
        assertEquals(message.questions()[0].QCLASS(), unpacked.questions()[0].QCLASS());
    }

    @Test
    void givenValidDnsRequest_whenPacked_thenExpectValidDnsMessageAnswer() {
        // Arrange
        DnsMessage message = DnsMessageHelper.createMessage(new DnsAnswer[] {new DnsAnswer("www.google.com", Optional.of("1.2.3.4"), 567)}, (short)1234);

        // Act
        byte[] request = packer.packRequest(message);

        // Assert
        // convert back and test
        DnsMessageParser parser = new DnsMessageParser();
        DnsMessage unpacked = parser.unpackRequest(request);
        assertNotNull(unpacked);
        assertEquals(1, unpacked.header().QDCOUNT());
        assertEquals(1, unpacked.header().ANCOUNT());
        assertEquals(1, unpacked.questions().length);
        assertEquals(message.questions()[0].QNAME(), unpacked.questions()[0].QNAME());
        assertEquals(message.questions()[0].QTYPE(), unpacked.questions()[0].QTYPE());
        assertEquals(message.questions()[0].QCLASS(), unpacked.questions()[0].QCLASS());

        assertEquals(1, unpacked.answers().length);
        assertEquals(message.answers()[0].NAME(), unpacked.answers()[0].NAME());
        assertEquals(message.answers()[0].TYPE(), unpacked.answers()[0].TYPE());
        assertEquals(message.answers()[0].CLASS(), unpacked.answers()[0].CLASS());
        assertEquals(message.answers()[0].TTL(), unpacked.answers()[0].TTL());
        assertEquals(message.answers()[0].RDATA(), unpacked.answers()[0].RDATA());
    }
}

package dev.jonclarke.dnsforwarder.helpers;

import dev.jonclarke.dnsforwarder.message.DnsMessage;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DnsMessageHelperTest {
    @Test
    void givenValidDomainRequest_whenMessageCreated_thenExpectValidDnsMessage() {
        // Arrange
        String domain = "www.google.com";
        Short id = 1234;

        // Act
        DnsMessage request = DnsMessageHelper.createMessage(new String[] {domain}, id);

        // Assert
        assertNotNull(request);
        assertEquals(id, request.header().ID());
        assertEquals(0, request.header().flags());
        assertEquals(1, request.header().QDCOUNT());
        assertEquals(0, request.header().ANCOUNT());
        assertEquals(0, request.header().NSCOUNT());
        assertEquals(0, request.header().ARCOUNT());
        assertEquals(domain, request.questions()[0].QNAME());
    }

    @Test
    void givenValidDomainRequestWithNullId_whenMessageCreated_thenExpectValidDnsMessageWithRandomId() {
        // Arrange
        String domain = "www.google.com";
        Short id = null;

        // Act
        DnsMessage request = DnsMessageHelper.createMessage(new String[] {domain}, id);

        // Assert
        assertNotNull(request);
        assertTrue(request.header().ID() > 0);
    }

    @Test
    void givenValidDomainRequest_whenMessageCreatedWithMultipleDomains_thenExpectValidDnsMessage() {
        // Arrange
        String domain1 = "www.google.com";
        String domain2 = "www.apple.com";
        Short id = 1234;

        // Act
        DnsMessage request = DnsMessageHelper.createMessage(new String[] {domain1, domain2}, id);

        // Assert
        assertNotNull(request);
        assertEquals(id, request.header().ID());
        assertEquals(0, request.header().flags());
        assertEquals(2, request.header().QDCOUNT());
        assertEquals(0, request.header().ANCOUNT());
        assertEquals(0, request.header().NSCOUNT());
        assertEquals(0, request.header().ARCOUNT());
        assertEquals(2, request.questions().length);
        assertEquals(domain1, request.questions()[0].QNAME());
        assertEquals(domain2, request.questions()[1].QNAME());
    }

    @Test
    void givenValidAnswerRequest_whenMessageCreatedWithMultipleAnswers_thenExpectValidDnsMessage() {
        // Arrange
        DnsAnswer answer1 = new DnsAnswer("www.google.com", Optional.of("1.2.3.4"), 567);
        DnsAnswer answer2 = new DnsAnswer("www.apple.com", Optional.of("5.6.7.8"), 890);
        Short id = 1234;

        // Act
        DnsMessage request = DnsMessageHelper.createMessage(new DnsAnswer[] {answer1, answer2}, id);

        // Assert
        assertNotNull(request);
        assertEquals(id, request.header().ID());
        assertEquals(0, request.header().flags());
        assertEquals(2, request.header().QDCOUNT());
        assertEquals(2, request.header().ANCOUNT());
        assertEquals(0, request.header().NSCOUNT());
        assertEquals(0, request.header().ARCOUNT());
    }

    @Test
    void givenValidAnswerRequest_whenMessageCreatedWithMultipleAnswers_thenExpectValidDnsQuestion() {
        // Arrange
        DnsAnswer answer1 = new DnsAnswer("www.google.com", Optional.of("1.2.3.4"), 567);
        DnsAnswer answer2 = new DnsAnswer("www.apple.com", Optional.of("5.6.7.8"), 890);
        Short id = 1234;

        // Act
        DnsMessage request = DnsMessageHelper.createMessage(new DnsAnswer[] {answer1, answer2}, id);

        // Assert
        assertNotNull(request);
        assertEquals(2, request.questions().length);
        assertEquals(answer1.domain(), request.questions()[0].QNAME());
        assertEquals(answer2.domain(), request.questions()[1].QNAME());
    }


    @Test
    void givenValidAnswerRequest_whenMessageCreatedWithMultipleAnswers_thenExpectValidDnsAnswer() {
        // Arrange
        DnsAnswer answer1 = new DnsAnswer("www.google.com", Optional.of("1.2.3.4"), 567);
        DnsAnswer answer2 = new DnsAnswer("www.apple.com", Optional.of("5.6.7.8"), 890);
        Short id = 1234;

        // Act
        DnsMessage request = DnsMessageHelper.createMessage(new DnsAnswer[] {answer1, answer2}, id);

        // Assert
        assertNotNull(request);
        assertEquals(2, request.answers().length);
        assertEquals(answer1.domain(), request.answers()[0].NAME());
        assertEquals(answer1.ttl(), request.answers()[0].TTL());
        assertEquals(answer1.ip().orElse(null), request.answers()[0].RDATA());
        assertEquals(answer2.domain(), request.answers()[1].NAME());
        assertEquals(answer2.ttl(), request.answers()[1].TTL());
        assertEquals(answer2.ip().orElse(null), request.answers()[1].RDATA());

    }
}

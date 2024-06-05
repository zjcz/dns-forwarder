package dev.jonclarke.dnsforwarder.message;

public record DnsQuestion(String QNAME, short QTYPE, short QCLASS) {
}

package dev.jonclarke.dnsforwarder.message;

public record DnsResourceRecord(String NAME, short TYPE, short CLASS, int TTL, String RDATA) {
}

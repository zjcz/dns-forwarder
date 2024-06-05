package dev.jonclarke.dnsforwarder.message;

public record DnsHeader(Short ID, int flags, short QDCOUNT, short ANCOUNT, short NSCOUNT, short ARCOUNT){
}

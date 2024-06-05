package dev.jonclarke.dnsforwarder.message;

public record DnsMessage(DnsHeader header, DnsQuestion[] questions, DnsResourceRecord[] answers, DnsResourceRecord[] authority, DnsResourceRecord[] additional) {}

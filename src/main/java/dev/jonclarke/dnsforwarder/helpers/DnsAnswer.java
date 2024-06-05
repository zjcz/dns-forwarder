package dev.jonclarke.dnsforwarder.helpers;

import java.util.Optional;

public record DnsAnswer(String domain, Optional<String> ip, int ttl) {
}

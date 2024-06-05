package dev.jonclarke.dnsforwarder.caching;

public record ExpiryCacheEntry(String ipAddress, long expiry) {
}

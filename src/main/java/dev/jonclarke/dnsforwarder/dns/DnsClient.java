package dev.jonclarke.dnsforwarder.dns;

public interface DnsClient {
    public static final String GOOGLE_DNS_ADDRESS = "8.8.8.8";
    public static final int GOOGLE_DNS_PORT = 53;

    byte[] sendRequest(short id, String[] domainName) throws Exception;
}

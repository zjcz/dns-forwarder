package dev.jonclarke.dnsforwarder.exceptions;

import java.util.Map;

public class DnsRequestException extends Exception {
    private short rcode;
    private final Map<Integer, String> rcodeMessages = Map.of(
        1, "Format error",
        2, "Server failure",
        3, "Name error",
        4, "Not implemented",
        5, "Refused"
    );

    public DnsRequestException(String message, short rcode) {
        super(message);
        this.rcode = rcode;
    }

    public short getRcode() {
        return rcode;
    }

    public String getRcodeMessage() {
        return rcodeMessages.get((int)rcode);
    }
}

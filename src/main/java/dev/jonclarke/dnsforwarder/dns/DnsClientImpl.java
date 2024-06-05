package dev.jonclarke.dnsforwarder.dns;

import dev.jonclarke.dnsforwarder.helpers.DnsMessageHelper;
import dev.jonclarke.dnsforwarder.message.DnsMessage;
import dev.jonclarke.dnsforwarder.parsers.DnsMessagePacker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * A simple DNS client that sends a DNS request to a DNS server and returns the response.
 */
public class DnsClientImpl implements DnsClient {
    protected static final Logger logger = LogManager.getLogger();

    private final String dnsServerAddress;
    private final int dnsServerPort;

    public DnsClientImpl(String dnsServerAddress, int dnsServerPort) {
        this.dnsServerAddress = dnsServerAddress;
        this.dnsServerPort = dnsServerPort;
    }

    /**
     * Sends a DNS request to the specified DNS server and returns the response.
     * @param id The ID of the request
     * @param domainName The domain name to query
     * @return The response from the DNS server
     */
    public byte[] sendRequest(short id, String[] domainName) throws Exception {
        byte[] response;

        // Send the request to the DNS server
        DnsMessage message = DnsMessageHelper.createMessage(domainName, id);
        byte[] request = new DnsMessagePacker().packRequest(message);

        try (DatagramSocket socket = new DatagramSocket()) {
            // send request
            DatagramPacket requestPacket = new DatagramPacket(request, request.length, java.net.InetAddress.getByName(dnsServerAddress), dnsServerPort);
            socket.send(requestPacket);

            // receive response
            response = new byte[1024];
            DatagramPacket responsePacket = new DatagramPacket(response, response.length);
            socket.receive(responsePacket);
        } catch (Exception e) {
            logger.error(e);
            throw new Exception("error sending message to DNS Server" ,e);
        }

        return response;
    }
}

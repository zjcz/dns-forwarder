package dev.jonclarke.dnsforwarder.servers;

import dev.jonclarke.dnsforwarder.caching.ExpiryCache;
import dev.jonclarke.dnsforwarder.dns.DnsClient;
import dev.jonclarke.dnsforwarder.dns.DnsClientImpl;
import dev.jonclarke.dnsforwarder.dns.DnsHandler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UdpServer extends Thread {
    protected static final Logger logger = LogManager.getLogger();

    private DatagramSocket socket;
    private boolean running;
    private byte[] buf = new byte[256];

    public UdpServer(int portNo) {
        try {
            socket = new DatagramSocket(portNo);
        } catch(SocketException ex) {
            logger.error("Error initialising socket for port {}: {}", portNo, ex.getMessage());
            socket = null;
        }
    }

    public void run() {

        running = true;

        while (running) {
            DatagramPacket requestPacket
                    = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(requestPacket);

                // process the request and get the response
                DnsHandler dnsHandler = new DnsHandler(new DnsClientImpl(DnsClient.GOOGLE_DNS_ADDRESS, DnsClient.GOOGLE_DNS_PORT), ExpiryCache.getInstance());
                byte[] dnsResponse = dnsHandler.handleRequest(requestPacket.getData());
                if (dnsResponse != null) {
                    // send the response to the client
                    InetAddress address = requestPacket.getAddress();
                    int port = requestPacket.getPort();
                    DatagramPacket responsePacket = new DatagramPacket(dnsResponse, dnsResponse.length, address, port);

                    try {
                        socket.send(responsePacket);
                    } catch (IOException ex) {
                        logger.error("Error sending response: {}", ex.getMessage());
                    }
                } else {
                    logger.error("Error parsing request");
                }
            } catch (IOException ex) {
                logger.error("Error receiving request: {}", ex.getMessage());
            }
        }
        socket.close();
    }


}

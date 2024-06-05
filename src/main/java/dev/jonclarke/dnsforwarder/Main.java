package dev.jonclarke.dnsforwarder;

import dev.jonclarke.dnsforwarder.servers.UdpServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Main entry point for the DNS forwarder application.
 */
public class Main {
    protected static final Logger logger = LogManager.getLogger();

    public static void main(String[] args) {
        // parse the command line arguments to extract the port number to use
        int portNumber = 1053;
        if (args.length > 0) {
            try {
                portNumber = Integer.parseInt(args[0]);
            } catch (NumberFormatException ex) {
                logger.warn("Invalid port number specified: {}, using default port 1053", args[0]);
            }
        }

        UdpServer server = new UdpServer(portNumber);
        server.start();
        logger.info("DNS Forwarder started on port {}", portNumber);
    }
}
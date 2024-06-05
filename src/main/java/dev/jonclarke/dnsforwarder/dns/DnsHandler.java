package dev.jonclarke.dnsforwarder.dns;

import dev.jonclarke.dnsforwarder.caching.ExpiryCache;
import dev.jonclarke.dnsforwarder.caching.ExpiryCacheEntry;
import dev.jonclarke.dnsforwarder.exceptions.DnsRequestException;
import dev.jonclarke.dnsforwarder.helpers.ByteHelper;
import dev.jonclarke.dnsforwarder.helpers.DnsAnswer;
import dev.jonclarke.dnsforwarder.helpers.DnsMessageHelper;
import dev.jonclarke.dnsforwarder.message.DnsHeader;
import dev.jonclarke.dnsforwarder.message.DnsMessage;
import dev.jonclarke.dnsforwarder.message.DnsQuestion;
import dev.jonclarke.dnsforwarder.message.DnsResourceRecord;
import dev.jonclarke.dnsforwarder.parsers.DnsMessagePacker;
import dev.jonclarke.dnsforwarder.parsers.DnsMessageParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DnsHandler {
    protected static final Logger logger = LogManager.getLogger();

    private final DnsClient dnsClient;
    private final ExpiryCache cache;

    public DnsHandler(DnsClient dnsClient, ExpiryCache cache) {
        this.dnsClient = dnsClient;
        this.cache = cache;
    }

    public byte[] handleRequest(byte[] request) {
        byte[] response = null;

        // parse the received message
        DnsMessageParser parser = new DnsMessageParser();
        DnsMessage message = parser.unpackRequest(request);
        if (message != null) {
            logger.info("MSG_Received");
            logger.debug(ByteHelper.convertByteArrayToPrintStatement(request));

            DnsHeader header = message.header();
            logger.info("Request Header - id:{} flags:{} questions:{} answers:{} authorities:{} additionals:{}", header.ID(), header.flags(), header.QDCOUNT(), header.ANCOUNT(), header.NSCOUNT(), header.ARCOUNT());
            for (DnsQuestion question : message.questions()) {
                logger.info("Request Questions - name:{} type:{} class:{}", question.QNAME(), question.QTYPE(), question.QCLASS());
            }

            // send the request to the DNS server
            String[] domainNames = Arrays.stream(message.questions())
                    .map(DnsQuestion::QNAME)
                    .toArray(String[]::new);

            DnsMessage responseMessage;
            try {
                DnsAnswer[] answers = lookupAnswer(header.ID(), domainNames, parser);
                responseMessage = DnsMessageHelper.createMessage(answers, header.ID());
            } catch (DnsRequestException e) {
                logger.error("Error forwarding request: {} - RCODE: {} - {}", e.getMessage(), e.getRcode(), e.getRcodeMessage());
                responseMessage = DnsMessageHelper.createErrorMessage(domainNames, header.ID(), e.getRcode());
            }

            DnsMessagePacker packer = new DnsMessagePacker();
            response = packer.packRequest(responseMessage);
        }
        return response;
    }

    /**
     * lookup the ip of the domain.  If not found in the local cache, forward the request to the DNS server
     * @param id the id of the request
     * @param domains the domains to lookup
     * @param parser the parser to use to unpack the response
     * @return the answer to the request
     */
    private DnsAnswer[] lookupAnswer(int id, String[] domains, DnsMessageParser parser) throws DnsRequestException {
        DnsAnswer[] answers = new DnsAnswer[domains.length];
        Map<String, Integer> domainToIndex = new HashMap<>();

        // check to see if the answer is in the cache
        for (int n = 0; n < domains.length; n++) {
            String domain = domains[n];
            ExpiryCacheEntry entry = this.cache.get(domain);
            if (entry == null) {
                // not found in cache
                domainToIndex.put(domain, n);
            } else {
                logger.info("Found domain '{}' in cache", domain);
                answers[n] = new DnsAnswer(domain, Optional.ofNullable(entry.ipAddress()), (int) (entry.expiry() - System.currentTimeMillis()) / 1000);
            }
        }

        // with the domains that are not in the cache, forward the request to the DNS server
        if (!domainToIndex.isEmpty()) {
            // couldn't find in the cache
            try {
                byte[] dnsResponse = this.dnsClient.sendRequest((short) id, domainToIndex.keySet().toArray(new String[0]));
                logger.info("Forwarded Question");
                logger.debug(ByteHelper.convertByteArrayToPrintStatement(dnsResponse));
                DnsMessage forwardedMessage = parser.unpackRequest(dnsResponse);
                if (forwardedMessage != null) {
                    logger.info("MSG_Forwarded");
                    DnsHeader forwardedHeader = forwardedMessage.header();
                    logger.info("Response Header - id:{} flags:{} questions:{} answers:{} authorities:{} additionals:{}", forwardedHeader.ID(), forwardedHeader.flags(), forwardedHeader.QDCOUNT(), forwardedHeader.ANCOUNT(), forwardedHeader.NSCOUNT(), forwardedHeader.ARCOUNT());
                    for (DnsQuestion forwardedQuestion : forwardedMessage.questions()) {
                        logger.info("Response Questions - name:{} type:{} class:{}", forwardedQuestion.QNAME(), forwardedQuestion.QTYPE(), forwardedQuestion.QCLASS());
                    }

                    if (forwardedMessage.header().ANCOUNT() == 0) {
                        logger.error("No answers in forwarded message");
                        int rcode = forwardedHeader.flags() & 0b00001111;
                        if (rcode > 0) {
                            logger.error("RCODE response = {}", rcode);
                            throw new DnsRequestException("No answers in forwarded message", (short) rcode);
                        }
                        // if there are no answers, return an empty response
                        for (String domain : domainToIndex.keySet()) {
                            answers[domainToIndex.get(domain)] = new DnsAnswer(domain, Optional.empty(), 0);
                        }
                    } else {
                        for (DnsResourceRecord answer : forwardedMessage.answers()) {
                            logger.info("Response Answers - name:{} type:{} class:{} ttl:{} data:{}", answer.NAME(), answer.TYPE(), answer.CLASS(), answer.TTL(), answer.RDATA());

                            DnsAnswer dnsAnswer = new DnsAnswer(answer.NAME(), Optional.ofNullable(answer.RDATA()), answer.TTL());
                            if (dnsAnswer.ip().isPresent()) {
                                this.cache.put(dnsAnswer.domain(), new ExpiryCacheEntry(dnsAnswer.ip().get(), System.currentTimeMillis() + dnsAnswer.ttl() * 1000L));
                            }
                            answers[domainToIndex.get(dnsAnswer.domain())] = dnsAnswer;
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error forwarding request", e);
            }
        }

        return answers;
    }

}

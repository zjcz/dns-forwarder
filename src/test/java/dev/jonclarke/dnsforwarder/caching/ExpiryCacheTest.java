package dev.jonclarke.dnsforwarder.caching;

import org.junit.jupiter.api.Test;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class ExpiryCacheTest {
    @Test
    void givenCache_whenPutNewItem_thenRetrieveItSuccessfully() {
        // Arrange
        String domain = "www.google.com";
        String ip = "1.2.3.4";
        long expiry = System.currentTimeMillis() + 1000;
        ExpiryCache.getInstance().put(domain, new ExpiryCacheEntry(ip, expiry));

        // Act
        ExpiryCacheEntry cachedItem = ExpiryCache.getInstance().get(domain);

        // Assert
        assertNotNull(cachedItem);
        assertEquals(ip, cachedItem.ipAddress());
        assertEquals(expiry, cachedItem.expiry());
    }

    @Test
    void givenCacheWithItem_whenGetWithInvalidName_thenExpectNull() {
        // Arrange
        String domain = "www.google.com";
        String ip = "1.2.3.4";
        long expiry = System.currentTimeMillis() + 1000;
        ExpiryCache.getInstance().put(domain, new ExpiryCacheEntry(ip, expiry));

        // Act
        ExpiryCacheEntry cachedItem = ExpiryCache.getInstance().get("invalid");

        // Assert
        assertNull(cachedItem);
    }

    @Test
    void givenCacheWithExpiredItem_whenRetrieveWithValidDomainName_thenExpectNull() throws InterruptedException {
        // Arrange
        String domain = "www.google.com";
        String ip = "1.2.3.4";
        long expiry = System.currentTimeMillis() + 1;
        ExpiryCache.getInstance().put(domain, new ExpiryCacheEntry(ip, expiry));

        // Act
        TimeUnit.MILLISECONDS.sleep(2);
        ExpiryCacheEntry cachedItem = ExpiryCache.getInstance().get(domain);

        // Assert
        assertNull(cachedItem);
    }
}

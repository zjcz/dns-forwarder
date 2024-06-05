package dev.jonclarke.dnsforwarder.caching;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ExpiryCache {
    private static ExpiryCache instance;

    ConcurrentMap<String, ExpiryCacheEntry> list = new ConcurrentHashMap<>();
    // TODO - do we want a scheduled task to remove expired entries?

    private ExpiryCache() {
    }

    public static ExpiryCache getInstance() {
        if (instance == null) {
            instance = new ExpiryCache();
        }
        return instance;
    }

    public void put(String key, ExpiryCacheEntry value) {
        list.put(key, value);
    }

    public ExpiryCacheEntry get(String key) {
        ExpiryCacheEntry entry = list.get(key);
        if (entry == null) {
            return null;
        }
        if (entry.expiry() < System.currentTimeMillis()) {
            list.remove(key);
            return null;
        }
        return entry;
    }

}

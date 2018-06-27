package com.adobe.acs.commons.httpcache.keys;

/**
 * CacheKey with a custom expiry method added.
 */
public interface CacheKeyWithCustomExpiry extends CacheKey {

    /**
     * Get's the custom
     * @return
     */
    Integer getCustomExpiry();
}

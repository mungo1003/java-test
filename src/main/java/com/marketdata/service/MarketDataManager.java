package com.marketdata.service;

import com.marketdata.model.MarketDepth;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages market data for multiple instruments across multiple venues.
 */
public class MarketDataManager {
    private final Map<String, Map<String, MarketDepth>> marketData;
    private final Map<String, Runnable> instrumentUpdateCallbacks;

    public MarketDataManager() {
        this.marketData = new ConcurrentHashMap<>();
        this.instrumentUpdateCallbacks = new HashMap<>();
    }

    /**
     * Registers a callback to be executed when an instrument's market data is updated.
     *
     * @param instrumentId The instrument ID
     * @param callback The callback to execute
     */
    public void registerInstrumentCallback(String instrumentId, Runnable callback) {
        instrumentUpdateCallbacks.put(instrumentId, callback);
    }

    /**
     * Updates market depth for a specific instrument at a specific venue.
     *
     * @param instrumentId The instrument ID
     * @param venueId The venue ID
     * @param side The side (bid or ask)
     * @param price The price
     * @param quantity The quantity
     */
    public void updateMarketDepth(String instrumentId, String venueId, 
                                 String side, double price, double quantity) {
        Map<String, MarketDepth> venueMap = marketData.computeIfAbsent(
            instrumentId, k -> new ConcurrentHashMap<>());
        
        MarketDepth depth = venueMap.computeIfAbsent(
            venueId, k -> new MarketDepth(instrumentId, venueId));
        
        if ("bid".equalsIgnoreCase(side)) {
            depth.updateBid(price, quantity);
        } else if ("ask".equalsIgnoreCase(side)) {
            depth.updateAsk(price, quantity);
        }
        
        Runnable callback = instrumentUpdateCallbacks.get(instrumentId);
        if (callback != null) {
            callback.run();
        }
    }

    /**
     * Gets all market depths for a specific instrument across all venues.
     *
     * @param instrumentId The instrument ID
     * @return Map of venue IDs to market depths
     */
    public Map<String, MarketDepth> getMarketDepthsForInstrument(String instrumentId) {
        return marketData.getOrDefault(instrumentId, new HashMap<>());
    }
}

package com.marketdata.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Represents market depth data for a specific instrument at a specific venue.
 * Optimized to reduce garbage generation by using NavigableMap for price levels.
 */
public class MarketDepth {
    private final String instrumentId;
    private final String venueId;
    
    private final NavigableMap<Double, PriceLevel> bids;
    
    private final NavigableMap<Double, PriceLevel> asks;
    
    private static final Comparator<Double> ASC_COMPARATOR = Double::compare;
    private static final Comparator<Double> DESC_COMPARATOR = Collections.reverseOrder();

    public MarketDepth(String instrumentId, String venueId) {
        this.instrumentId = instrumentId;
        this.venueId = venueId;
        this.bids = new TreeMap<>(DESC_COMPARATOR);
        this.asks = new TreeMap<>(ASC_COMPARATOR);
    }

    public String getInstrumentId() {
        return instrumentId;
    }

    public String getVenueId() {
        return venueId;
    }

    /**
     * Returns an unmodifiable view of the bids map.
     * This prevents callers from modifying the internal data structure.
     */
    public Map<Double, PriceLevel> getBids() {
        return Collections.unmodifiableMap(bids);
    }

    /**
     * Returns an unmodifiable view of the asks map.
     * This prevents callers from modifying the internal data structure.
     */
    public Map<Double, PriceLevel> getAsks() {
        return Collections.unmodifiableMap(asks);
    }

    /**
     * Returns a list of bid price levels.
     * This method creates a new list to avoid exposing the internal data structure.
     */
    public List<PriceLevel> getBidLevels() {
        return new ArrayList<>(bids.values());
    }

    /**
     * Returns a list of ask price levels.
     * This method creates a new list to avoid exposing the internal data structure.
     */
    public List<PriceLevel> getAskLevels() {
        return new ArrayList<>(asks.values());
    }

    /**
     * Updates a bid price level.
     * @param price The price level
     * @param quantity The quantity at this price level
     */
    public void updateBid(double price, double quantity) {
        updatePriceLevel(bids, price, quantity);
    }

    /**
     * Updates an ask price level.
     * @param price The price level
     * @param quantity The quantity at this price level
     */
    public void updateAsk(double price, double quantity) {
        updatePriceLevel(asks, price, quantity);
    }

    /**
     * Updates a price level in the specified map.
     * @param levels The map of price levels
     * @param price The price level
     * @param quantity The quantity at this price level
     */
    private void updatePriceLevel(NavigableMap<Double, PriceLevel> levels, double price, double quantity) {
        if (quantity <= 0) {
            levels.remove(price);
        } else {
            PriceLevel level = levels.get(price);
            if (level == null) {
                level = new PriceLevel(price, quantity);
                levels.put(price, level);
            } else {
                level.setQuantity(quantity);
            }
        }
    }

    @Override
    public String toString() {
        return "MarketDepth{" +
                "instrumentId='" + instrumentId + '\'' +
                ", venueId='" + venueId + '\'' +
                ", bids=" + bids.values() +
                ", asks=" + asks.values() +
                '}';
    }
}

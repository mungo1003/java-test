package com.marketdata.model;

import gnu.trove.map.TDoubleObjectMap;
import gnu.trove.map.hash.TDoubleObjectHashMap;
import gnu.trove.procedure.TDoubleProcedure;
import gnu.trove.procedure.TDoubleObjectProcedure;
import gnu.trove.set.TDoubleSet;
import gnu.trove.set.hash.TDoubleHashSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Represents market depth data for a specific instrument at a specific venue.
 * Optimized to reduce garbage generation by using Trove collections for primitive types.
 */
public class MarketDepth {
    private final String instrumentId;
    private final String venueId;
    
    private final TDoubleObjectMap<PriceLevel> bids;
    private final TDoubleObjectMap<PriceLevel> asks;
    
    private final TDoubleSet bidPrices;
    private final TDoubleSet askPrices;
    
    public MarketDepth(String instrumentId, String venueId) {
        this.instrumentId = instrumentId;
        this.venueId = venueId;
        this.bids = new TDoubleObjectHashMap<>();
        this.asks = new TDoubleObjectHashMap<>();
        this.bidPrices = new TDoubleHashSet();
        this.askPrices = new TDoubleHashSet();
    }

    public String getInstrumentId() {
        return instrumentId;
    }

    public String getVenueId() {
        return venueId;
    }

    /**
     * Returns a read-only view of the bids.
     * This prevents callers from modifying the internal data structure.
     */
    public Map<Double, PriceLevel> getBids() {
        Map<Double, PriceLevel> result = new TreeMap<>(Collections.reverseOrder());
        bids.forEachEntry((price, level) -> {
            result.put(price, level);
            return true;
        });
        return Collections.unmodifiableMap(result);
    }

    /**
     * Returns a read-only view of the asks.
     * This prevents callers from modifying the internal data structure.
     */
    public Map<Double, PriceLevel> getAsks() {
        Map<Double, PriceLevel> result = new TreeMap<>();
        asks.forEachEntry((price, level) -> {
            result.put(price, level);
            return true;
        });
        return Collections.unmodifiableMap(result);
    }

    /**
     * Returns a list of bid price levels sorted by price (descending).
     */
    public List<PriceLevel> getBidLevels() {
        List<PriceLevel> result = new ArrayList<>(bids.size());
        
        double[] prices = bidPrices.toArray();
        java.util.Arrays.sort(prices);
        
        for (int i = prices.length - 1; i >= 0; i--) {
            result.add(bids.get(prices[i]));
        }
        
        return result;
    }

    /**
     * Returns a list of ask price levels sorted by price (ascending).
     */
    public List<PriceLevel> getAskLevels() {
        List<PriceLevel> result = new ArrayList<>(asks.size());
        
        double[] prices = askPrices.toArray();
        java.util.Arrays.sort(prices);
        
        for (int i = 0; i < prices.length; i++) {
            result.add(asks.get(prices[i]));
        }
        
        return result;
    }

    /**
     * Updates a bid price level.
     * @param price The price level
     * @param quantity The quantity at this price level
     */
    public void updateBid(double price, double quantity) {
        updatePriceLevel(bids, bidPrices, price, quantity);
    }

    /**
     * Updates an ask price level.
     * @param price The price level
     * @param quantity The quantity at this price level
     */
    public void updateAsk(double price, double quantity) {
        updatePriceLevel(asks, askPrices, price, quantity);
    }

    /**
     * Updates a price level in the specified map.
     * @param levels The map of price levels
     * @param prices The set of prices for maintaining order
     * @param price The price level
     * @param quantity The quantity at this price level
     */
    private void updatePriceLevel(TDoubleObjectMap<PriceLevel> levels, TDoubleSet prices, 
                                 double price, double quantity) {
        if (quantity <= 0) {
            levels.remove(price);
            prices.remove(price);
        } else {
            PriceLevel level = levels.get(price);
            if (level == null) {
                level = new PriceLevel(price, quantity);
                levels.put(price, level);
                prices.add(price);
            } else {
                level.setQuantity(quantity);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("MarketDepth{instrumentId='").append(instrumentId).append('\'');
        sb.append(", venueId='").append(venueId).append('\'');
        
        sb.append(", bids=[");
        List<PriceLevel> bidLevels = getBidLevels();
        for (int i = 0; i < bidLevels.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(bidLevels.get(i));
        }
        sb.append("]");
        
        sb.append(", asks=[");
        List<PriceLevel> askLevels = getAskLevels();
        for (int i = 0; i < askLevels.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(askLevels.get(i));
        }
        sb.append("]");
        
        sb.append('}');
        return sb.toString();
    }
}

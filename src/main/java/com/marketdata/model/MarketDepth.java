package com.marketdata.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents market depth data for a specific instrument at a specific venue.
 */
public class MarketDepth {
    private final String instrumentId;
    private final String venueId;
    private final List<PriceLevel> bids;
    private final List<PriceLevel> asks;

    public MarketDepth(String instrumentId, String venueId) {
        this.instrumentId = instrumentId;
        this.venueId = venueId;
        this.bids = new ArrayList<>();
        this.asks = new ArrayList<>();
    }

    public String getInstrumentId() {
        return instrumentId;
    }

    public String getVenueId() {
        return venueId;
    }

    public List<PriceLevel> getBids() {
        return bids;
    }

    public List<PriceLevel> getAsks() {
        return asks;
    }

    public void updateBid(double price, double quantity) {
        updatePriceLevel(bids, price, quantity);
    }

    public void updateAsk(double price, double quantity) {
        updatePriceLevel(asks, price, quantity);
    }

    private void updatePriceLevel(List<PriceLevel> levels, double price, double quantity) {
        for (int i = 0; i < levels.size(); i++) {
            PriceLevel level = levels.get(i);
            if (level.getPrice() == price) {
                if (quantity <= 0) {
                    levels.remove(i);
                } else {
                    level.setQuantity(quantity);
                }
                return;
            }
        }

        if (quantity > 0) {
            levels.add(new PriceLevel(price, quantity));
            
            if (levels == bids) {
                bids.sort((a, b) -> Double.compare(b.getPrice(), a.getPrice()));
            } 
            else if (levels == asks) {
                asks.sort((a, b) -> Double.compare(a.getPrice(), b.getPrice()));
            }
        }
    }

    @Override
    public String toString() {
        return "MarketDepth{" +
                "instrumentId='" + instrumentId + '\'' +
                ", venueId='" + venueId + '\'' +
                ", bids=" + bids +
                ", asks=" + asks +
                '}';
    }
}

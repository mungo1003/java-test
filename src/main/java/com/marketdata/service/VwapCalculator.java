package com.marketdata.service;

import com.marketdata.model.MarketDepth;
import com.marketdata.model.PriceLevel;

/**
 * Calculator for Volume-Weighted Average Price (VWAP) based on market depth data.
 */
public class VwapCalculator {

    /**
     * Calculates the bid VWAP price for a given market depth.
     *
     * @param marketDepth The market depth data
     * @return The volume-weighted average bid price, or 0 if no bids exist
     */
    public static double calculateBidVwap(MarketDepth marketDepth) {
        return calculateVwap(marketDepth.getBids());
    }

    /**
     * Calculates the ask VWAP price for a given market depth.
     *
     * @param marketDepth The market depth data
     * @return The volume-weighted average ask price, or 0 if no asks exist
     */
    public static double calculateAskVwap(MarketDepth marketDepth) {
        return calculateVwap(marketDepth.getAsks());
    }

    private static double calculateVwap(java.util.List<PriceLevel> levels) {
        double totalValue = 0.0;
        double totalVolume = 0.0;

        for (PriceLevel level : levels) {
            double price = level.getPrice();
            double quantity = level.getQuantity();
            
            totalValue += price * quantity;
            totalVolume += quantity;
        }

        return totalVolume > 0 ? totalValue / totalVolume : 0;
    }
}

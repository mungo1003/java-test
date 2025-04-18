package com.marketdata.service;

import com.marketdata.model.MarketDepth;
import com.marketdata.model.PriceLevel;

import java.util.List;
import java.util.Map;

/**
 * Calculator for Volume-Weighted Average Price (VWAP) based on market depth data.
 * Optimized to reduce garbage generation.
 */
public class VwapCalculator {

    /**
     * Calculates the bid VWAP price for a given market depth.
     *
     * @param marketDepth The market depth data
     * @return The volume-weighted average bid price, or 0 if no bids exist
     */
    public static double calculateBidVwap(MarketDepth marketDepth) {
        return calculateVwapFromLevels(marketDepth.getBidLevels());
    }

    /**
     * Calculates the ask VWAP price for a given market depth.
     *
     * @param marketDepth The market depth data
     * @return The volume-weighted average ask price, or 0 if no asks exist
     */
    public static double calculateAskVwap(MarketDepth marketDepth) {
        return calculateVwapFromLevels(marketDepth.getAskLevels());
    }

    /**
     * Calculates VWAP from a list of price levels.
     * 
     * @param levels List of price levels
     * @return The volume-weighted average price
     */
    private static double calculateVwapFromLevels(List<PriceLevel> levels) {
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
    
    /**
     * Calculates VWAP from a map of price levels.
     * This method is provided for backward compatibility.
     * 
     * @param levels Map of price to price level
     * @return The volume-weighted average price
     */
    private static double calculateVwap(Map<Double, PriceLevel> levels) {
        double totalValue = 0.0;
        double totalVolume = 0.0;

        for (PriceLevel level : levels.values()) {
            double price = level.getPrice();
            double quantity = level.getQuantity();
            
            totalValue += price * quantity;
            totalVolume += quantity;
        }

        return totalVolume > 0 ? totalValue / totalVolume : 0;
    }
}

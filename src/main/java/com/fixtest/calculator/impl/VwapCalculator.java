package com.fixtest.calculator.impl;

import com.fixtest.calculator.Calculator;
import com.fixtest.calculator.MarketUpdate;
import com.fixtest.calculator.TwoWayPrice;
import com.fixtest.model.Instrument;
import com.fixtest.model.Market;
import com.fixtest.model.State;

import java.util.EnumMap;
import java.util.Map;

/**
 * Implementation of the Calculator interface that calculates VWAP (Volume Weighted Average Price)
 * for financial instruments across multiple markets.
 * 
 * This implementation:
 * - Stores the most recent price update for each market-instrument combination
 * - Maintains running totals for efficient VWAP calculation
 * - Calculates VWAP by considering the most recent price update for each market
 * - Marks the result as INDICATIVE if any contributing price is INDICATIVE
 * - Is designed for single-threaded access
 */
public class VwapCalculator implements Calculator {
    
    private final Map<Instrument, VwapState> vwapData;
    
    /**
     * Constructs a new VwapCalculator with empty price data.
     */
    public VwapCalculator() {
        this.vwapData = new EnumMap<>(Instrument.class);
    }
    
    @Override
    public TwoWayPrice applyMarketUpdate(final MarketUpdate marketUpdate) {
        if (marketUpdate == null) {
            throw new IllegalArgumentException("Market update cannot be null");
        }
        
        TwoWayPrice price = marketUpdate.getTwoWayPrice();
        if (price == null) {
            throw new IllegalArgumentException("Two-way price in market update cannot be null");
        }
        
        Market market = marketUpdate.getMarket();
        Instrument instrument = price.getInstrument();
        
        VwapState state = vwapData.computeIfAbsent(instrument, k -> new VwapState());
        
        state.updatePrice(market, price);
        
        return state.calculateVwap(instrument);
    }
    
    /**
     * Inner class to maintain the state for VWAP calculation for a specific instrument.
     * This improves efficiency by maintaining running totals instead of recalculating from scratch.
     */
    private static class VwapState {
        // Map to store the latest price for each market
        private final Map<Market, TwoWayPrice> latestPrices = new EnumMap<>(Market.class);
        
        private double totalBidAmount = 0.0;
        private double totalBidPriceAmount = 0.0;
        
        private double totalOfferAmount = 0.0;
        private double totalOfferPriceAmount = 0.0;
        
        private boolean hasIndicative = false;
        
        /**
         * Updates the state with a new price for a specific market.
         * Efficiently updates running totals by removing the old price's contribution
         * and adding the new price's contribution.
         * 
         * @param market the market
         * @param newPrice the new price
         */
        public void updatePrice(Market market, TwoWayPrice newPrice) {
            TwoWayPrice oldPrice = latestPrices.get(market);
            
            if (newPrice.getState() == State.INDICATIVE) {
                hasIndicative = true;
            } else if (oldPrice != null && oldPrice.getState() == State.INDICATIVE) {
                recalculateIndicativeFlag();
            }
            
            if (oldPrice != null) {
                subtractFromTotals(oldPrice);
            }
            
            addToTotals(newPrice);
            
            latestPrices.put(market, newPrice);
        }
        
        /**
         * Recalculates the indicative flag by checking all prices.
         * Only called when necessary to avoid unnecessary iteration.
         */
        private void recalculateIndicativeFlag() {
            hasIndicative = false;
            for (TwoWayPrice price : latestPrices.values()) {
                if (price.getState() == State.INDICATIVE) {
                    hasIndicative = true;
                    break;
                }
            }
        }
        
        /**
         * Subtracts a price's contribution from the running totals.
         * 
         * @param price the price to subtract
         */
        private void subtractFromTotals(TwoWayPrice price) {
            double bidAmount = price.getBidAmount();
            double bidPrice = price.getBidPrice();
            if (bidAmount > 0 && !Double.isNaN(bidPrice)) {
                totalBidAmount -= bidAmount;
                totalBidPriceAmount -= bidPrice * bidAmount;
            }
            
            double offerAmount = price.getOfferAmount();
            double offerPrice = price.getOfferPrice();
            if (offerAmount > 0 && !Double.isNaN(offerPrice)) {
                totalOfferAmount -= offerAmount;
                totalOfferPriceAmount -= offerPrice * offerAmount;
            }
        }
        
        /**
         * Adds a price's contribution to the running totals.
         * 
         * @param price the price to add
         */
        private void addToTotals(TwoWayPrice price) {
            double bidAmount = price.getBidAmount();
            double bidPrice = price.getBidPrice();
            if (bidAmount > 0 && !Double.isNaN(bidPrice)) {
                totalBidAmount += bidAmount;
                totalBidPriceAmount += bidPrice * bidAmount;
            }
            
            double offerAmount = price.getOfferAmount();
            double offerPrice = price.getOfferPrice();
            if (offerAmount > 0 && !Double.isNaN(offerPrice)) {
                totalOfferAmount += offerAmount;
                totalOfferPriceAmount += offerPrice * offerAmount;
            }
        }
        
        /**
         * Calculates the VWAP using the running totals.
         * 
         * @param instrument the instrument
         * @return the calculated VWAP as a TwoWayPrice
         */
        public TwoWayPrice calculateVwap(Instrument instrument) {
            if (latestPrices.isEmpty()) {
                throw new IllegalStateException("No price data available for instrument: " + instrument);
            }
            
            double vwapBidPrice = (totalBidAmount > 0) ? totalBidPriceAmount / totalBidAmount : Double.NaN;
            double vwapOfferPrice = (totalOfferAmount > 0) ? totalOfferPriceAmount / totalOfferAmount : Double.NaN;
            
            State state = hasIndicative ? State.INDICATIVE : State.FIRM;
            
            return new VwapTwoWayPrice(
                instrument,
                state,
                vwapBidPrice,
                vwapOfferPrice,
                totalBidAmount,
                totalOfferAmount
            );
        }
    }
}

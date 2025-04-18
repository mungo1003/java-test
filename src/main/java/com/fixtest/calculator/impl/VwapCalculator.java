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
    
    private final VwapState[] vwapStates;
    private final VwapTwoWayPricePool pricePool;
    
    /**
     * Constructs a new VwapCalculator with pre-allocated state objects.
     */
    public VwapCalculator() {
        int instrumentCount = Instrument.values().length;
        this.vwapStates = new VwapState[instrumentCount];
        
        for (Instrument instrument : Instrument.values()) {
            this.vwapStates[instrument.ordinal()] = new VwapState();
        }
        
        this.pricePool = new VwapTwoWayPricePool();
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
        
        VwapState state = vwapStates[instrument.ordinal()];
        
        state.updatePrice(market, price);
        
        return state.calculateVwap(instrument);
    }
    
    /**
     * Inner class to maintain the state for VWAP calculation for a specific instrument.
     * This improves efficiency by maintaining running totals instead of recalculating from scratch.
     */
    private static class VwapState {
        // Array to store the latest price for each market
        private final TwoWayPrice[] latestPrices;
        private final boolean[] marketHasPrice;
        
        private double totalBidAmount = 0.0;
        private double totalBidPriceAmount = 0.0;
        
        private double totalOfferAmount = 0.0;
        private double totalOfferPriceAmount = 0.0;
        
        private boolean hasIndicative = false;
        
        /**
         * Constructs a new VwapState with arrays for price storage.
         */
        public VwapState() {
            int marketCount = Market.values().length;
            this.latestPrices = new TwoWayPrice[marketCount];
            this.marketHasPrice = new boolean[marketCount];
        }
        
        /**
         * Updates the state with a new price for a specific market.
         * Efficiently updates running totals by removing the old price's contribution
         * and adding the new price's contribution.
         * 
         * @param market the market
         * @param newPrice the new price
         */
        public void updatePrice(Market market, TwoWayPrice newPrice) {
            int marketIndex = market.ordinal();
            TwoWayPrice oldPrice = marketHasPrice[marketIndex] ? latestPrices[marketIndex] : null;
            
            if (newPrice.getState() == State.INDICATIVE) {
                hasIndicative = true;
            } else if (oldPrice != null && oldPrice.getState() == State.INDICATIVE) {
                recalculateIndicativeFlag();
            }
            
            if (oldPrice != null) {
                subtractFromTotals(oldPrice);
            }
            
            addToTotals(newPrice);
            
            latestPrices[marketIndex] = newPrice;
            marketHasPrice[marketIndex] = true;
        }
        
        /**
         * Recalculates the indicative flag by checking all prices.
         * Only called when necessary to avoid unnecessary iteration.
         */
        private void recalculateIndicativeFlag() {
            hasIndicative = false;
            for (int i = 0; i < latestPrices.length; i++) {
                if (marketHasPrice[i] && latestPrices[i].getState() == State.INDICATIVE) {
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
            boolean hasAnyPrice = false;
            for (boolean hasPrice : marketHasPrice) {
                if (hasPrice) {
                    hasAnyPrice = true;
                    break;
                }
            }
            
            if (!hasAnyPrice) {
                throw new IllegalStateException("No price data available for instrument: " + instrument);
            }
            
            double vwapBidPrice = (totalBidAmount > 0) ? totalBidPriceAmount / totalBidAmount : Double.NaN;
            double vwapOfferPrice = (totalOfferAmount > 0) ? totalOfferPriceAmount / totalOfferAmount : Double.NaN;
            
            State state = hasIndicative ? State.INDICATIVE : State.FIRM;
            
            return pricePool.getPrice(instrument)
                    .update(state, vwapBidPrice, vwapOfferPrice, totalBidAmount, totalOfferAmount);
        }
    }
}

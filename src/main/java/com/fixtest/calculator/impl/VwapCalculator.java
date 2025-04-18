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
 * - Calculates VWAP by considering the most recent price update for each market
 * - Marks the result as INDICATIVE if any contributing price is INDICATIVE
 * - Is designed for single-threaded access
 */
public class VwapCalculator implements Calculator {
    
    private final Map<Instrument, Map<Market, TwoWayPrice>> latestPrices;
    
    /**
     * Constructs a new VwapCalculator with empty price data.
     */
    public VwapCalculator() {
        this.latestPrices = new EnumMap<>(Instrument.class);
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
        
        storeLatestPrice(market, price);
        
        return calculateVwap(instrument);
    }
    
    /**
     * Stores the latest price for a specific market-instrument combination.
     * 
     * @param market the market
     * @param price the two-way price
     */
    private void storeLatestPrice(Market market, TwoWayPrice price) {
        Instrument instrument = price.getInstrument();
        
        Map<Market, TwoWayPrice> marketMap = latestPrices.computeIfAbsent(
            instrument, k -> new EnumMap<>(Market.class)
        );
        
        marketMap.put(market, price);
    }
    
    /**
     * Calculates the VWAP for a specific instrument across all markets.
     * 
     * @param instrument the instrument to calculate VWAP for
     * @return the calculated VWAP as a TwoWayPrice
     */
    private TwoWayPrice calculateVwap(Instrument instrument) {
        Map<Market, TwoWayPrice> marketMap = latestPrices.get(instrument);
        if (marketMap == null || marketMap.isEmpty()) {
            throw new IllegalStateException("No price data available for instrument: " + instrument);
        }
        
        double totalBidAmount = 0.0;
        double totalBidPriceAmount = 0.0;
        double totalOfferAmount = 0.0;
        double totalOfferPriceAmount = 0.0;
        boolean hasIndicative = false;
        
        for (TwoWayPrice price : marketMap.values()) {
            if (price.getState() == State.INDICATIVE) {
                hasIndicative = true;
            }
            
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

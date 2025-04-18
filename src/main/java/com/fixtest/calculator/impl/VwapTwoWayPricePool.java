package com.fixtest.calculator.impl;

import com.fixtest.calculator.TwoWayPrice;
import com.fixtest.model.Instrument;
import com.fixtest.model.State;

/**
 * Pool of reusable VwapTwoWayPrice objects to minimize garbage collection.
 * This class is not thread-safe and should only be used in a single-threaded context.
 */
public class VwapTwoWayPricePool {
    
    private final MutableVwapTwoWayPrice[][] pricePool;
    private final boolean[][] inUse;
    
    private static final int INSTRUMENTS_COUNT = Instrument.values().length;
    private static final int POOL_SIZE_PER_INSTRUMENT = 2; // Adjust based on usage patterns
    
    /**
     * Constructs a new price pool with pre-allocated objects.
     */
    public VwapTwoWayPricePool() {
        pricePool = new MutableVwapTwoWayPrice[INSTRUMENTS_COUNT][POOL_SIZE_PER_INSTRUMENT];
        inUse = new boolean[INSTRUMENTS_COUNT][POOL_SIZE_PER_INSTRUMENT];
        
        for (int i = 0; i < INSTRUMENTS_COUNT; i++) {
            Instrument instrument = Instrument.values()[i];
            for (int j = 0; j < POOL_SIZE_PER_INSTRUMENT; j++) {
                pricePool[i][j] = new MutableVwapTwoWayPrice(instrument);
            }
        }
    }
    
    /**
     * Gets a price object from the pool or creates a new one if necessary.
     * 
     * @param instrument the instrument
     * @return a price object
     */
    public MutableVwapTwoWayPrice getPrice(Instrument instrument) {
        int instrumentIndex = instrument.ordinal();
        
        for (int i = 0; i < POOL_SIZE_PER_INSTRUMENT; i++) {
            if (!inUse[instrumentIndex][i]) {
                inUse[instrumentIndex][i] = true;
                return pricePool[instrumentIndex][i];
            }
        }
        
        return new MutableVwapTwoWayPrice(instrument);
    }
    
    /**
     * Returns a price object to the pool.
     * 
     * @param price the price object to return
     */
    public void returnPrice(TwoWayPrice price) {
        if (!(price instanceof MutableVwapTwoWayPrice)) {
            return;
        }
        
        MutableVwapTwoWayPrice mutablePrice = (MutableVwapTwoWayPrice) price;
        int instrumentIndex = mutablePrice.getInstrument().ordinal();
        
        for (int i = 0; i < POOL_SIZE_PER_INSTRUMENT; i++) {
            if (pricePool[instrumentIndex][i] == mutablePrice) {
                inUse[instrumentIndex][i] = false;
                return;
            }
        }
    }
}

package com.asx.fix;

import quickfix.fix44.MarketDataRequestReject;

/**
 * Interface for listening to market data updates.
 */
public interface MarketDataListener {
    /**
     * Called when market data is updated.
     * 
     * @param symbol The symbol
     * @param entry The market data entry
     */
    void onMarketDataUpdate(String symbol, MarketDataEntry entry);
    
    /**
     * Called when market data is deleted.
     * 
     * @param symbol The symbol
     * @param entryType The entry type that was deleted
     */
    void onMarketDataDelete(String symbol, char entryType);
    
    /**
     * Called when a market data request is rejected.
     * 
     * @param mdReqId The market data request ID
     * @param message The reject message
     */
    void onMarketDataRequestReject(String mdReqId, MarketDataRequestReject message);
}

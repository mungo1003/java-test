package com.asx.fix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.ConfigError;

/**
 * Main application class for ASX FIX Market Data.
 */
public class ASXFIXMarketDataApp {
    private static final Logger LOGGER = LoggerFactory.getLogger(ASXFIXMarketDataApp.class);
    
    private final ASXFIXClient client;
    private final DefaultMessageHandler messageHandler;
    
    public ASXFIXMarketDataApp(String configFile) throws ConfigError {
        messageHandler = new DefaultMessageHandler();
        client = new ASXFIXClient(configFile, messageHandler);
    }
    
    /**
     * Start the application.
     */
    public void start() throws ConfigError {
        client.start();
    }
    
    /**
     * Stop the application.
     */
    public void stop() {
        client.stop();
    }
    
    /**
     * Subscribe to market data for the specified symbols.
     * 
     * @param symbols Array of symbols to subscribe to
     * @param entryTypes Array of entry types (0=Bid, 1=Offer, 2=Trade, etc.)
     * @return The MDReqID of the request
     */
    public String subscribeMarketData(String[] symbols, char[] entryTypes) {
        return client.sendMarketDataRequest(symbols, entryTypes, 1); // 1 = Snapshot + Updates
    }
    
    /**
     * Get a snapshot of market data for the specified symbols.
     * 
     * @param symbols Array of symbols to get snapshot for
     * @param entryTypes Array of entry types (0=Bid, 1=Offer, 2=Trade, etc.)
     * @return The MDReqID of the request
     */
    public String getMarketDataSnapshot(String[] symbols, char[] entryTypes) {
        return client.sendMarketDataRequest(symbols, entryTypes, 0); // 0 = Snapshot
    }
    
    /**
     * Cancel a market data subscription.
     * 
     * @param mdReqId The MDReqID of the subscription to cancel
     */
    public void cancelMarketDataSubscription(String mdReqId) {
        client.cancelMarketDataRequest(mdReqId);
    }
    
    /**
     * Request trade replay for a symbol.
     * 
     * @param symbol Symbol to request trade replay for
     * @param startTime Start time for trade replay
     * @param endTime End time for trade replay
     * @return The MDReqID of the request
     */
    public String requestTradeReplay(String symbol, String startTime, String endTime) {
        return client.sendTradeReplayRequest(symbol, startTime, endTime);
    }
    
    /**
     * Add a market data listener.
     * 
     * @param listener The listener to add
     */
    public void addMarketDataListener(MarketDataListener listener) {
        messageHandler.addMarketDataListener(listener);
    }
    
    /**
     * Remove a market data listener.
     * 
     * @param listener The listener to remove
     */
    public void removeMarketDataListener(MarketDataListener listener) {
        messageHandler.removeMarketDataListener(listener);
    }
    
    /**
     * Get the current market data for a symbol.
     * 
     * @param symbol The symbol to get market data for
     * @return Map of entry types to market data entries
     */
    public java.util.Map<Character, MarketDataEntry> getMarketData(String symbol) {
        return messageHandler.getMarketData(symbol);
    }
    
    /**
     * Main method for running the application.
     */
    public static void main(String[] args) {
        try {
            String configFile = "config/quickfix.cfg";
            if (args.length > 0) {
                configFile = args[0];
            }
            
            ASXFIXMarketDataApp app = new ASXFIXMarketDataApp(configFile);
            app.start();
            
            Runtime.getRuntime().addShutdownHook(new Thread(app::stop));
            
            LOGGER.info("ASX FIX Market Data application started");
        } catch (Exception e) {
            LOGGER.error("Error starting application: {}", e.getMessage(), e);
        }
    }
}

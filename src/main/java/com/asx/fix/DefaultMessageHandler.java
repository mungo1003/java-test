package com.asx.fix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.FieldNotFound;
import quickfix.SessionID;
import quickfix.field.*;
import quickfix.fix44.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of the MessageHandler interface.
 */
public class DefaultMessageHandler implements MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultMessageHandler.class);
    
    private final Map<String, Map<Character, MarketDataEntry>> marketDataBySymbol = new HashMap<>();
    
    private final List<MarketDataListener> listeners = new ArrayList<>();
    
    /**
     * Add a market data listener.
     */
    public void addMarketDataListener(MarketDataListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Remove a market data listener.
     */
    public void removeMarketDataListener(MarketDataListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Get the current market data for a symbol.
     */
    public Map<Character, MarketDataEntry> getMarketData(String symbol) {
        return marketDataBySymbol.getOrDefault(symbol, new HashMap<>());
    }
    
    @Override
    public void onLogon(SessionID sessionId) {
        LOGGER.info("Logon successful for session: {}", sessionId);
    }
    
    @Override
    public void onLogout(SessionID sessionId) {
        LOGGER.info("Logout for session: {}", sessionId);
    }
    
    @Override
    public void onHeartbeat(Heartbeat message, SessionID sessionId) {
        LOGGER.debug("Received Heartbeat: {}", message);
    }
    
    @Override
    public void onTestRequest(TestRequest message, SessionID sessionId) {
        LOGGER.debug("Received TestRequest: {}", message);
        try {
            Heartbeat heartbeat = new Heartbeat();
            heartbeat.set(new TestReqID(message.getTestReqID().getValue()));
            quickfix.Session.sendToTarget(heartbeat, sessionId);
        } catch (Exception e) {
            LOGGER.error("Error responding to TestRequest: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public void onResendRequest(ResendRequest message, SessionID sessionId) {
        LOGGER.debug("Received ResendRequest: {}", message);
    }
    
    @Override
    public void onReject(Reject message, SessionID sessionId) {
        LOGGER.warn("Received Reject: {}", message);
    }
    
    @Override
    public void onSequenceReset(SequenceReset message, SessionID sessionId) {
        LOGGER.debug("Received SequenceReset: {}", message);
    }
    
    @Override
    public void onLogout(Logout message, SessionID sessionId) {
        LOGGER.info("Received Logout: {}", message);
    }
    
    @Override
    public void onMarketDataSnapshotFullRefresh(MarketDataSnapshotFullRefresh message, SessionID sessionId) {
        try {
            String symbol = message.getSymbol().getValue();
            LOGGER.info("Received MarketDataSnapshotFullRefresh for symbol: {}", symbol);
            
            Map<Character, MarketDataEntry> symbolData = marketDataBySymbol.computeIfAbsent(symbol, k -> new HashMap<>());
            
            symbolData.clear();
            
            int numEntries = message.getNoMDEntries().getValue();
            for (int i = 0; i < numEntries; i++) {
                MarketDataSnapshotFullRefresh.NoMDEntries group = new MarketDataSnapshotFullRefresh.NoMDEntries();
                message.getGroup(i + 1, group);
                
                char entryType = group.getMDEntryType().getValue();
                double price = group.getMDEntryPx().getValue();
                double size = 0;
                
                if (group.isSetMDEntrySize()) {
                    size = group.getMDEntrySize().getValue();
                }
                
                String entryId = "";
                try {
                    entryId = group.getString(MDEntryID.FIELD);
                } catch (FieldNotFound e) {
                }
                
                MarketDataEntry entry = new MarketDataEntry(entryType, price, size, entryId);
                symbolData.put(entryType, entry);
                
                for (MarketDataListener listener : listeners) {
                    listener.onMarketDataUpdate(symbol, entry);
                }
            }
        } catch (FieldNotFound e) {
            LOGGER.error("Error processing MarketDataSnapshotFullRefresh: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public void onMarketDataIncrementalRefresh(MarketDataIncrementalRefresh message, SessionID sessionId) {
        try {
            LOGGER.info("Received MarketDataIncrementalRefresh");
            
            int numEntries = message.getNoMDEntries().getValue();
            for (int i = 0; i < numEntries; i++) {
                MarketDataIncrementalRefresh.NoMDEntries group = new MarketDataIncrementalRefresh.NoMDEntries();
                message.getGroup(i + 1, group);
                
                String symbol = group.getSymbol().getValue();
                char entryType = group.getMDEntryType().getValue();
                char updateAction = group.getMDUpdateAction().getValue();
                
                Map<Character, MarketDataEntry> symbolData = marketDataBySymbol.computeIfAbsent(symbol, k -> new HashMap<>());
                
                if (updateAction == '0') { // New
                    double price = group.getMDEntryPx().getValue();
                    double size = 0;
                    
                    if (group.isSetMDEntrySize()) {
                        size = group.getMDEntrySize().getValue();
                    }
                    
                    String entryId = "";
                    try {
                        entryId = group.getString(MDEntryID.FIELD);
                    } catch (FieldNotFound e) {
                    }
                    
                    MarketDataEntry entry = new MarketDataEntry(entryType, price, size, entryId);
                    symbolData.put(entryType, entry);
                    
                    for (MarketDataListener listener : listeners) {
                        listener.onMarketDataUpdate(symbol, entry);
                    }
                } else if (updateAction == '1') { // Change
                    MarketDataEntry existingEntry = symbolData.get(entryType);
                    if (existingEntry != null) {
                        double price;
                        try {
                            price = group.getMDEntryPx().getValue();
                        } catch (FieldNotFound e) {
                            price = existingEntry.getPrice();
                        }
                        
                        double size;
                        try {
                            size = group.getMDEntrySize().getValue();
                        } catch (FieldNotFound e) {
                            size = existingEntry.getSize();
                        }
                        
                        String entryId;
                        try {
                            entryId = group.getString(MDEntryID.FIELD);
                        } catch (FieldNotFound e) {
                            entryId = existingEntry.getEntryId();
                        }
                        
                        MarketDataEntry updatedEntry = new MarketDataEntry(entryType, price, size, entryId);
                        symbolData.put(entryType, updatedEntry);
                        
                        for (MarketDataListener listener : listeners) {
                            listener.onMarketDataUpdate(symbol, updatedEntry);
                        }
                    }
                } else if (updateAction == '2') { // Delete
                    MarketDataEntry removedEntry = symbolData.remove(entryType);
                    
                    if (removedEntry != null) {
                        for (MarketDataListener listener : listeners) {
                            listener.onMarketDataDelete(symbol, entryType);
                        }
                    }
                }
            }
        } catch (FieldNotFound e) {
            LOGGER.error("Error processing MarketDataIncrementalRefresh: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public void onMarketDataRequestReject(MarketDataRequestReject message, SessionID sessionId) {
        try {
            LOGGER.warn("Received MarketDataRequestReject: {}", message);
            String mdReqId = message.getMDReqID().getValue();
            
            for (MarketDataListener listener : listeners) {
                listener.onMarketDataRequestReject(mdReqId, message);
            }
        } catch (FieldNotFound e) {
            LOGGER.error("Error processing MarketDataRequestReject: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public void onSecurityStatus(quickfix.fix44.SecurityStatus message, SessionID sessionId) {
        LOGGER.info("Received SecurityStatus: {}", message);
    }
    
    @Override
    public void onTradingSessionStatus(TradingSessionStatus message, SessionID sessionId) {
        LOGGER.info("Received TradingSessionStatus: {}", message);
    }
    
    @Override
    public void onNews(News message, SessionID sessionId) {
        LOGGER.info("Received News: {}", message);
    }
}

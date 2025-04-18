package com.asx.fix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;
import quickfix.field.*;
import quickfix.fix44.*;

import java.util.UUID;

/**
 * Client implementation for ASX FIX Market Data.
 */
public class ASXFIXClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(ASXFIXClient.class);
    
    private final Initiator initiator;
    private final SessionID sessionID;
    private final quickfix.MessageFactory messageFactory = new quickfix.DefaultMessageFactory();
    
    public ASXFIXClient(String configFile, MessageHandler messageHandler) throws ConfigError {
        SessionSettings settings = new SessionSettings(configFile);
        MessageStoreFactory storeFactory = new FileStoreFactory(settings);
        LogFactory logFactory = new FileLogFactory(settings);
        Application application = new ASXFIXApplication(messageHandler);
        
        initiator = new SocketInitiator(application, storeFactory, settings, logFactory, messageFactory);
        sessionID = initiator.getSessions().get(0);
    }
    
    /**
     * Start the FIX client.
     */
    public void start() throws ConfigError {
        if (!initiator.isLoggedOn()) {
            initiator.start();
            LOGGER.info("FIX client started");
        }
    }
    
    /**
     * Stop the FIX client.
     */
    public void stop() {
        if (initiator.isLoggedOn()) {
            initiator.stop();
            LOGGER.info("FIX client stopped");
        }
    }
    
    /**
     * Send a market data request for the specified symbols.
     * 
     * @param symbols Array of symbols to subscribe to
     * @param entryTypes Array of entry types (0=Bid, 1=Offer, 2=Trade, etc.)
     * @param subscriptionType Type of subscription (0=Snapshot, 1=Snapshot+Updates, 2=Disable previous snapshot)
     * @return The MDReqID of the request
     */
    public String sendMarketDataRequest(String[] symbols, char[] entryTypes, int subscriptionType) {
        try {
            MarketDataRequest request = new MarketDataRequest();
            String mdReqId = UUID.randomUUID().toString();
            
            request.set(new MDReqID(mdReqId));
            request.set(new SubscriptionRequestType(Character.forDigit(subscriptionType, 10)));
            request.set(new MarketDepth(0)); // Full book
            
            request.set(new MDUpdateType(0));
            
            MarketDataRequest.NoMDEntryTypes entryTypesGroup = new MarketDataRequest.NoMDEntryTypes();
            for (char entryType : entryTypes) {
                entryTypesGroup.set(new MDEntryType(entryType));
                request.addGroup(entryTypesGroup);
            }
            
            MarketDataRequest.NoRelatedSym relatedSymGroup = new MarketDataRequest.NoRelatedSym();
            for (String symbol : symbols) {
                relatedSymGroup.set(new Symbol(symbol));
                request.addGroup(relatedSymGroup);
            }
            
            Session.sendToTarget(request, sessionID);
            LOGGER.info("Sent market data request: {}", request);
            return mdReqId;
        } catch (Exception e) {
            LOGGER.error("Error sending market data request: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Cancel a market data subscription.
     * 
     * @param mdReqId The MDReqID of the subscription to cancel
     */
    public void cancelMarketDataRequest(String mdReqId) {
        try {
            MarketDataRequest request = new MarketDataRequest();
            request.set(new MDReqID(mdReqId));
            request.set(new SubscriptionRequestType('2')); // Disable previous snapshot
            
            Session.sendToTarget(request, sessionID);
            LOGGER.info("Sent market data cancel request for MDReqID: {}", mdReqId);
        } catch (Exception e) {
            LOGGER.error("Error canceling market data request: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Send a trade replay request.
     * 
     * @param symbol Symbol to request trade replay for
     * @param startTime Start time for trade replay
     * @param endTime End time for trade replay
     * @return The MDReqID of the request
     */
    public String sendTradeReplayRequest(String symbol, String startTime, String endTime) {
        try {
            MarketDataRequest request = new MarketDataRequest();
            String mdReqId = UUID.randomUUID().toString();
            
            request.set(new MDReqID(mdReqId));
            request.set(new SubscriptionRequestType('0')); // Snapshot
            request.set(new MarketDepth(0));
            
            MarketDataRequest.NoMDEntryTypes entryTypesGroup = new MarketDataRequest.NoMDEntryTypes();
            entryTypesGroup.set(new MDEntryType('2')); // Trade
            request.addGroup(entryTypesGroup);
            
            MarketDataRequest.NoRelatedSym relatedSymGroup = new MarketDataRequest.NoRelatedSym();
            relatedSymGroup.set(new Symbol(symbol));
            
            if (startTime != null && !startTime.isEmpty()) {
                relatedSymGroup.setString(1300, startTime); // MDReqStartTime (custom tag)
            }
            
            if (endTime != null && !endTime.isEmpty()) {
                relatedSymGroup.setString(1301, endTime); // MDReqEndTime (custom tag)
            }
            
            request.addGroup(relatedSymGroup);
            
            Session.sendToTarget(request, sessionID);
            LOGGER.info("Sent trade replay request: {}", request);
            return mdReqId;
        } catch (Exception e) {
            LOGGER.error("Error sending trade replay request: {}", e.getMessage(), e);
            return null;
        }
    }
}

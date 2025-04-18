package com.asx.fix;

import quickfix.SessionID;
import quickfix.fix44.*;

/**
 * Interface for handling FIX messages.
 */
public interface MessageHandler {
    void onLogon(SessionID sessionId);
    void onLogout(SessionID sessionId);
    void onHeartbeat(Heartbeat message, SessionID sessionId);
    void onTestRequest(TestRequest message, SessionID sessionId);
    void onResendRequest(ResendRequest message, SessionID sessionId);
    void onReject(Reject message, SessionID sessionId);
    void onSequenceReset(SequenceReset message, SessionID sessionId);
    void onLogout(Logout message, SessionID sessionId);
    
    void onMarketDataSnapshotFullRefresh(MarketDataSnapshotFullRefresh message, SessionID sessionId);
    void onMarketDataIncrementalRefresh(MarketDataIncrementalRefresh message, SessionID sessionId);
    void onMarketDataRequestReject(MarketDataRequestReject message, SessionID sessionId);
    
    void onSecurityStatus(quickfix.fix44.SecurityStatus message, SessionID sessionId);
    void onTradingSessionStatus(TradingSessionStatus message, SessionID sessionId);
    void onNews(News message, SessionID sessionId);
}

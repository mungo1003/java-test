package com.asx.fix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;
import quickfix.field.*;
import quickfix.fix44.*;

/**
 * Main FIX application class that handles session-level messages and market data.
 */
public class ASXFIXApplication implements Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(ASXFIXApplication.class);
    private final MessageHandler messageHandler;

    public ASXFIXApplication(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    @Override
    public void onCreate(SessionID sessionId) {
        LOGGER.info("Session created: {}", sessionId);
    }

    @Override
    public void onLogon(SessionID sessionId) {
        LOGGER.info("Logon successful for session: {}", sessionId);
        messageHandler.onLogon(sessionId);
    }

    @Override
    public void onLogout(SessionID sessionId) {
        LOGGER.info("Logout for session: {}", sessionId);
        messageHandler.onLogout(sessionId);
    }

    @Override
    public void toAdmin(quickfix.Message message, SessionID sessionId) {
        try {
            if (message instanceof Logon) {
                Logon logon = (Logon) message;
                logon.set(new EncryptMethod(0)); // No encryption
                logon.set(new HeartBtInt(30)); // 30 second heartbeat
                
                logon.setString(96, "XXXXXX"); // Password
                logon.setString(554, "XXXXX"); // Username
                logon.setInt(108, 30); // HeartBtInt
                logon.setBoolean(141, true); // ResetSeqNumFlag
                
                LOGGER.info("Sending Logon message: {}", logon);
            }
        } catch (Exception e) {
            LOGGER.error("Error in toAdmin: {}", e.getMessage(), e);
        }
    }

    @Override
    public void fromAdmin(quickfix.Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
        try {
            LOGGER.info("Received admin message: {}", message);
            
            if (message instanceof Heartbeat) {
                messageHandler.onHeartbeat((Heartbeat) message, sessionId);
            } else if (message instanceof TestRequest) {
                messageHandler.onTestRequest((TestRequest) message, sessionId);
            } else if (message instanceof ResendRequest) {
                messageHandler.onResendRequest((ResendRequest) message, sessionId);
            } else if (message instanceof Reject) {
                messageHandler.onReject((Reject) message, sessionId);
            } else if (message instanceof SequenceReset) {
                messageHandler.onSequenceReset((SequenceReset) message, sessionId);
            } else if (message instanceof Logout) {
                messageHandler.onLogout((Logout) message, sessionId);
            }
        } catch (Exception e) {
            LOGGER.error("Error in fromAdmin: {}", e.getMessage(), e);
        }
    }

    @Override
    public void toApp(quickfix.Message message, SessionID sessionId) throws DoNotSend {
        LOGGER.info("Sending application message: {}", message);
    }

    @Override
    public void fromApp(quickfix.Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        try {
            LOGGER.info("Received application message: {}", message);
            
            if (message instanceof MarketDataSnapshotFullRefresh) {
                messageHandler.onMarketDataSnapshotFullRefresh((MarketDataSnapshotFullRefresh) message, sessionId);
            } else if (message instanceof MarketDataIncrementalRefresh) {
                messageHandler.onMarketDataIncrementalRefresh((MarketDataIncrementalRefresh) message, sessionId);
            } else if (message instanceof MarketDataRequestReject) {
                messageHandler.onMarketDataRequestReject((MarketDataRequestReject) message, sessionId);
            } else if (message instanceof quickfix.fix44.SecurityStatus) {
                messageHandler.onSecurityStatus((quickfix.fix44.SecurityStatus) message, sessionId);
            } else if (message instanceof TradingSessionStatus) {
                messageHandler.onTradingSessionStatus((TradingSessionStatus) message, sessionId);
            } else if (message instanceof News) {
                messageHandler.onNews((News) message, sessionId);
            }
        } catch (Exception e) {
            LOGGER.error("Error in fromApp: {}", e.getMessage(), e);
        }
    }
}

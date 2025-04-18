package com.asx.fix;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import quickfix.*;
import quickfix.field.*;
import quickfix.fix44.*;
import quickfix.StringField;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for the ASX FIX Market Data implementation.
 */
public class ASXFIXMarketDataTest {
    private Application application;
    private MessageHandler messageHandler;
    private SessionID sessionID;
    private Session session;
    
    @BeforeEach
    public void setUp() throws Exception {
        messageHandler = mock(MessageHandler.class);
        application = new ASXFIXApplication(messageHandler);
        sessionID = new SessionID("FIX.4.4", "CLIENT", "ASX");
        
        session = mock(Session.class);
        
        try (var sessionMock = mockStatic(Session.class)) {
            sessionMock.when(() -> Session.lookupSession(sessionID)).thenReturn(session);
            sessionMock.when(() -> Session.sendToTarget(any(quickfix.Message.class), eq(sessionID))).thenReturn(true);
        }
    }
    
    @AfterEach
    public void tearDown() {
        Mockito.reset(messageHandler, session);
    }
    
    @Test
    public void testLogonMessage() throws Exception {
        Logon logon = new Logon();
        
        application.toAdmin(logon, sessionID);
        
        assertEquals(0, logon.getEncryptMethod().getValue());
        assertEquals(30, logon.getHeartBtInt().getValue());
        assertTrue(logon.getBoolean(141)); // ResetSeqNumFlag
        assertEquals("XXXXXX", logon.getString(96)); // Password
        assertEquals("XXXXX", logon.getString(554)); // Username
    }
    
    @Test
    public void testHeartbeatHandling() throws Exception {
        Heartbeat heartbeat = new Heartbeat();
        heartbeat.set(new TestReqID("TEST123"));
        
        application.fromAdmin(heartbeat, sessionID);
        
        verify(messageHandler).onHeartbeat(eq(heartbeat), eq(sessionID));
    }
    
    @Test
    public void testTestRequestHandling() throws Exception {
        TestRequest testRequest = new TestRequest();
        testRequest.set(new TestReqID("TEST123"));
        
        application.fromAdmin(testRequest, sessionID);
        
        verify(messageHandler).onTestRequest(eq(testRequest), eq(sessionID));
    }
    
    @Test
    public void testMarketDataSnapshotHandling() throws Exception {
        MarketDataSnapshotFullRefresh snapshot = new MarketDataSnapshotFullRefresh();
        snapshot.set(new Symbol("APT"));
        snapshot.set(new NoMDEntries(2));
        
        MarketDataSnapshotFullRefresh.NoMDEntries bidGroup = new MarketDataSnapshotFullRefresh.NoMDEntries();
        bidGroup.set(new MDEntryType('0')); // Bid
        bidGroup.set(new MDEntryPx(100.5));
        bidGroup.set(new MDEntrySize(10));
        bidGroup.setString(MDEntryID.FIELD, "BID1");
        snapshot.addGroup(bidGroup);
        
        MarketDataSnapshotFullRefresh.NoMDEntries offerGroup = new MarketDataSnapshotFullRefresh.NoMDEntries();
        offerGroup.set(new MDEntryType('1')); // Offer
        offerGroup.set(new MDEntryPx(101.0));
        offerGroup.set(new MDEntrySize(5));
        offerGroup.setString(MDEntryID.FIELD, "OFFER1");
        snapshot.addGroup(offerGroup);
        
        application.fromApp(snapshot, sessionID);
        
        verify(messageHandler).onMarketDataSnapshotFullRefresh(eq(snapshot), eq(sessionID));
    }
    
    @Test
    public void testMarketDataIncrementalHandling() throws Exception {
        MarketDataIncrementalRefresh refresh = new MarketDataIncrementalRefresh();
        refresh.set(new NoMDEntries(1));
        
        MarketDataIncrementalRefresh.NoMDEntries tradeGroup = new MarketDataIncrementalRefresh.NoMDEntries();
        tradeGroup.set(new MDUpdateAction('0')); // New
        tradeGroup.set(new Symbol("APT"));
        tradeGroup.set(new MDEntryType('2')); // Trade
        tradeGroup.set(new MDEntryPx(100.75));
        tradeGroup.set(new MDEntrySize(7));
        tradeGroup.setString(MDEntryID.FIELD, "TRADE1");
        refresh.addGroup(tradeGroup);
        
        application.fromApp(refresh, sessionID);
        
        verify(messageHandler).onMarketDataIncrementalRefresh(eq(refresh), eq(sessionID));
    }
    
    @Test
    public void testDefaultMessageHandlerMarketDataProcessing() throws FieldNotFound {
        DefaultMessageHandler handler = new DefaultMessageHandler();
        
        TestMarketDataListener listener = new TestMarketDataListener();
        handler.addMarketDataListener(listener);
        
        MarketDataSnapshotFullRefresh snapshot = new MarketDataSnapshotFullRefresh();
        snapshot.set(new Symbol("APT"));
        snapshot.set(new NoMDEntries(1));
        
        MarketDataSnapshotFullRefresh.NoMDEntries bidGroup = new MarketDataSnapshotFullRefresh.NoMDEntries();
        bidGroup.set(new MDEntryType('0')); // Bid
        bidGroup.set(new MDEntryPx(100.5));
        bidGroup.set(new MDEntrySize(10));
        bidGroup.setString(MDEntryID.FIELD, "BID1");
        snapshot.addGroup(bidGroup);
        
        handler.onMarketDataSnapshotFullRefresh(snapshot, sessionID);
        
        assertTrue(listener.updateCalled);
        assertEquals("APT", listener.symbol);
        assertEquals('0', listener.entry.getEntryType());
        assertEquals(100.5, listener.entry.getPrice());
        assertEquals(10, listener.entry.getSize());
        assertEquals("BID1", listener.entry.getEntryId());
        
        listener.reset();
        
        MarketDataIncrementalRefresh refresh = new MarketDataIncrementalRefresh();
        refresh.set(new NoMDEntries(1));
        
        MarketDataIncrementalRefresh.NoMDEntries updateGroup = new MarketDataIncrementalRefresh.NoMDEntries();
        updateGroup.set(new MDUpdateAction('1')); // Change
        updateGroup.set(new Symbol("APT"));
        updateGroup.set(new MDEntryType('0')); // Bid
        updateGroup.set(new MDEntryPx(101.0));
        updateGroup.set(new MDEntrySize(15));
        updateGroup.setString(MDEntryID.FIELD, "BID1");
        refresh.addGroup(updateGroup);
        
        handler.onMarketDataIncrementalRefresh(refresh, sessionID);
        
        assertTrue(listener.updateCalled);
        assertEquals("APT", listener.symbol);
        assertEquals('0', listener.entry.getEntryType());
        assertEquals(101.0, listener.entry.getPrice());
        assertEquals(15, listener.entry.getSize());
        assertEquals("BID1", listener.entry.getEntryId());
        
        listener.reset();
        
        refresh = new MarketDataIncrementalRefresh();
        refresh.set(new NoMDEntries(1));
        
        updateGroup = new MarketDataIncrementalRefresh.NoMDEntries();
        updateGroup.set(new MDUpdateAction('2')); // Delete
        updateGroup.set(new Symbol("APT"));
        updateGroup.set(new MDEntryType('0')); // Bid
        refresh.addGroup(updateGroup);
        
        handler.onMarketDataIncrementalRefresh(refresh, sessionID);
        
        assertTrue(listener.deleteCalled);
        assertEquals("APT", listener.symbol);
        assertEquals('0', listener.deletedEntryType);
    }
    
    @Test
    public void testASXFIXClientMarketDataRequest() throws Exception {
        MessageHandler handler = mock(MessageHandler.class);
        
        SessionSettings settings = mock(SessionSettings.class);
        when(settings.getDefaultProperties()).thenReturn(new java.util.Properties());
        java.util.ArrayList<SessionID> sessionIDs = new java.util.ArrayList<>();
        sessionIDs.add(sessionID);
        when(settings.getSessionIDs()).thenReturn(sessionIDs);
        when(settings.getSessionProperties(sessionID)).thenReturn(new java.util.Properties());
        
        Initiator initiator = mock(Initiator.class);
        when(initiator.getSessions()).thenReturn(java.util.Collections.singletonList(sessionID));
        
        try (var initiatorMock = mockStatic(SocketInitiator.class)) {
            initiatorMock.when(() -> new SocketInitiator(any(), any(), any(), any(), any())).thenReturn(initiator);
            
            ASXFIXClient client = new ASXFIXClient("config/quickfix.cfg", handler);
            
            try (var sessionMock = mockStatic(Session.class)) {
                ArgumentCaptor<quickfix.Message> messageCaptor = ArgumentCaptor.forClass(quickfix.Message.class);
                sessionMock.when(() -> Session.sendToTarget(messageCaptor.capture(), eq(sessionID))).thenReturn(true);
                
                String[] symbols = {"APT", "BHP"};
                char[] entryTypes = {'0', '1', '2'}; // Bid, Offer, Trade
                String mdReqId = client.sendMarketDataRequest(symbols, entryTypes, 1);
                
                quickfix.Message sentMessage = messageCaptor.getValue();
                assertTrue(sentMessage instanceof MarketDataRequest);
                
                MarketDataRequest request = (MarketDataRequest) sentMessage;
                assertEquals('1', request.getSubscriptionRequestType().getValue()); // Snapshot + Updates
                assertEquals(0, request.getMarketDepth().getValue()); // Full book
                
                int numEntryTypes = request.getNoMDEntryTypes().getValue();
                assertEquals(3, numEntryTypes);
                
                int numSymbols = request.getNoRelatedSym().getValue();
                assertEquals(2, numSymbols);
                
                assertNotNull(mdReqId);
            }
        }
    }
    
    /**
     * Test implementation of MarketDataListener.
     */
    private static class TestMarketDataListener implements MarketDataListener {
        boolean updateCalled = false;
        boolean deleteCalled = false;
        boolean rejectCalled = false;
        String symbol;
        MarketDataEntry entry;
        char deletedEntryType;
        String rejectedMdReqId;
        
        @Override
        public void onMarketDataUpdate(String symbol, MarketDataEntry entry) {
            this.updateCalled = true;
            this.symbol = symbol;
            this.entry = entry;
        }
        
        @Override
        public void onMarketDataDelete(String symbol, char entryType) {
            this.deleteCalled = true;
            this.symbol = symbol;
            this.deletedEntryType = entryType;
        }
        
        @Override
        public void onMarketDataRequestReject(String mdReqId, MarketDataRequestReject message) {
            this.rejectCalled = true;
            this.rejectedMdReqId = mdReqId;
        }
        
        public void reset() {
            updateCalled = false;
            deleteCalled = false;
            rejectCalled = false;
            symbol = null;
            entry = null;
            deletedEntryType = 0;
            rejectedMdReqId = null;
        }
    }
}

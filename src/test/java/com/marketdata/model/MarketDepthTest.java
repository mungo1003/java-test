package com.marketdata.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.Test;

public class MarketDepthTest {

    @Test
    public void testAddBidPriceLevel() {
        MarketDepth depth = new MarketDepth("AAPL", "VENUE-1");
        depth.updateBid(100.0, 10.0);
        
        Map<Double, PriceLevel> bids = depth.getBids();
        assertEquals(1, bids.size());
        assertTrue(bids.containsKey(100.0));
        assertEquals(10.0, bids.get(100.0).getQuantity(), 0.001);
    }
    
    @Test
    public void testAddAskPriceLevel() {
        MarketDepth depth = new MarketDepth("AAPL", "VENUE-1");
        depth.updateAsk(101.0, 5.0);
        
        Map<Double, PriceLevel> asks = depth.getAsks();
        assertEquals(1, asks.size());
        assertTrue(asks.containsKey(101.0));
        assertEquals(5.0, asks.get(101.0).getQuantity(), 0.001);
    }
    
    @Test
    public void testUpdateExistingPriceLevel() {
        MarketDepth depth = new MarketDepth("AAPL", "VENUE-1");
        depth.updateBid(100.0, 10.0);
        depth.updateBid(100.0, 15.0);
        
        Map<Double, PriceLevel> bids = depth.getBids();
        assertEquals(1, bids.size());
        assertEquals(15.0, bids.get(100.0).getQuantity(), 0.001);
    }
    
    @Test
    public void testRemovePriceLevel() {
        MarketDepth depth = new MarketDepth("AAPL", "VENUE-1");
        depth.updateBid(100.0, 10.0);
        depth.updateBid(100.0, 0.0);
        
        Map<Double, PriceLevel> bids = depth.getBids();
        assertEquals(0, bids.size());
        assertFalse(bids.containsKey(100.0));
    }
    
    @Test
    public void testBidsSortedDescending() {
        MarketDepth depth = new MarketDepth("AAPL", "VENUE-1");
        depth.updateBid(100.0, 10.0);
        depth.updateBid(101.0, 5.0);
        depth.updateBid(99.0, 15.0);
        
        Double[] prices = depth.getBids().keySet().toArray(new Double[0]);
        assertEquals(3, prices.length);
        assertEquals(101.0, prices[0], 0.001);
        assertEquals(100.0, prices[1], 0.001);
        assertEquals(99.0, prices[2], 0.001);
    }
    
    @Test
    public void testAsksSortedAscending() {
        MarketDepth depth = new MarketDepth("AAPL", "VENUE-1");
        depth.updateAsk(100.0, 10.0);
        depth.updateAsk(101.0, 5.0);
        depth.updateAsk(99.0, 15.0);
        
        Double[] prices = depth.getAsks().keySet().toArray(new Double[0]);
        assertEquals(3, prices.length);
        assertEquals(99.0, prices[0], 0.001);
        assertEquals(100.0, prices[1], 0.001);
        assertEquals(101.0, prices[2], 0.001);
    }
    
    @Test
    public void testGetBidLevels() {
        MarketDepth depth = new MarketDepth("AAPL", "VENUE-1");
        depth.updateBid(100.0, 10.0);
        depth.updateBid(101.0, 5.0);
        
        List<PriceLevel> levels = depth.getBidLevels();
        assertEquals(2, levels.size());
        
        assertEquals(101.0, levels.get(0).getPrice(), 0.001);
        assertEquals(100.0, levels.get(1).getPrice(), 0.001);
    }
    
    @Test
    public void testGetAskLevels() {
        MarketDepth depth = new MarketDepth("AAPL", "VENUE-1");
        depth.updateAsk(100.0, 10.0);
        depth.updateAsk(101.0, 5.0);
        
        List<PriceLevel> levels = depth.getAskLevels();
        assertEquals(2, levels.size());
        
        assertEquals(100.0, levels.get(0).getPrice(), 0.001);
        assertEquals(101.0, levels.get(1).getPrice(), 0.001);
    }
    
    @Test
    public void testPerformanceWithMultipleUpdates() {
        MarketDepth depth = new MarketDepth("AAPL", "VENUE-1");
        
        for (int i = 0; i < 1000; i++) {
            double price = 100.0 + (i * 0.01);
            depth.updateBid(price, i + 1);
        }
        
        for (int i = 0; i < 1000; i++) {
            double price = 100.0 + (i * 0.01);
            depth.updateBid(price, i + 2);
        }
        
        for (int i = 0; i < 500; i++) {
            double price = 100.0 + (i * 0.01);
            depth.updateBid(price, 0);
        }
        
        assertEquals(500, depth.getBids().size());
        assertEquals(500, depth.getBidLevels().size());
    }
}

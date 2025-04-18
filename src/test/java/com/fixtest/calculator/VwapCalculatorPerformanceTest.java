package com.fixtest.calculator;

import com.fixtest.calculator.impl.VwapCalculator;
import com.fixtest.calculator.impl.MutableVwapTwoWayPrice;
import com.fixtest.model.Instrument;
import com.fixtest.model.Market;
import com.fixtest.model.State;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Performance tests for the VwapCalculator implementation.
 * These tests measure memory usage and execution time to verify the GC-free optimizations.
 */
public class VwapCalculatorPerformanceTest {
    
    private static final int WARMUP_ITERATIONS = 5;
    private static final int TEST_ITERATIONS = 10;
    private static final int UPDATES_PER_ITERATION = 100_000;
    
    /**
     * Main method to run the performance test.
     */
    public static void main(String[] args) {
        VwapCalculatorPerformanceTest test = new VwapCalculatorPerformanceTest();
        test.testCalculatorPerformance();
    }
    
    /**
     * Test to measure the performance of the VwapCalculator implementation.
     * This test generates a large number of market updates and measures:
     * 1. Execution time
     * 2. Memory usage (indirectly through GC activity)
     */
    public void testCalculatorPerformance() {
        Calculator calculator = new VwapCalculator();
        
        List<MarketUpdate> updates = generateMarketUpdates(UPDATES_PER_ITERATION);
        
        System.out.println("Warming up JVM...");
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            processUpdates(calculator, updates);
            System.gc(); // Request garbage collection between iterations
        }
        
        long memoryBefore = getUsedMemory();
        
        System.out.println("Running performance test...");
        long startTime = System.nanoTime();
        
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            processUpdates(calculator, updates);
        }
        
        long endTime = System.nanoTime();
        long executionTime = (endTime - startTime) / 1_000_000; // Convert to milliseconds
        
        System.gc(); // Request garbage collection to get accurate memory usage
        long memoryAfter = getUsedMemory();
        long memoryUsed = memoryAfter - memoryBefore;
        
        System.out.println("Performance Test Results:");
        System.out.println("-------------------------");
        System.out.println("Total updates processed: " + (TEST_ITERATIONS * UPDATES_PER_ITERATION));
        System.out.println("Execution time: " + executionTime + " ms");
        System.out.println("Updates per second: " + 
                (TEST_ITERATIONS * UPDATES_PER_ITERATION * 1000L / executionTime));
        System.out.println("Memory usage: " + (memoryUsed / (1024 * 1024)) + " MB");
        System.out.println("Memory per update: " + 
                (memoryUsed / (TEST_ITERATIONS * UPDATES_PER_ITERATION)) + " bytes");
        
    }
    
    /**
     * Process all market updates through the calculator.
     */
    private void processUpdates(Calculator calculator, List<MarketUpdate> updates) {
        for (MarketUpdate update : updates) {
            calculator.applyMarketUpdate(update);
        }
    }
    
    /**
     * Generate a list of random market updates for testing.
     */
    private List<MarketUpdate> generateMarketUpdates(int count) {
        List<MarketUpdate> updates = new ArrayList<>(count);
        Random random = new Random(42); // Fixed seed for reproducibility
        
        Instrument[] instruments = Instrument.values();
        Market[] markets = Market.values();
        
        for (int i = 0; i < count; i++) {
            Instrument instrument = instruments[random.nextInt(instruments.length)];
            Market market = markets[random.nextInt(markets.length)];
            State state = random.nextDouble() < 0.1 ? State.INDICATIVE : State.FIRM; // 10% indicative
            
            double bidPrice = 100 + random.nextDouble() * 10;
            double offerPrice = bidPrice + 0.1 + random.nextDouble();
            double bidAmount = 10 + random.nextDouble() * 90;
            double offerAmount = 10 + random.nextDouble() * 90;
            
            TwoWayPrice price = new MutableVwapTwoWayPrice(instrument)
                    .update(state, bidPrice, offerPrice, bidAmount, offerAmount);
            
            updates.add(createMarketUpdate(market, price));
        }
        
        return updates;
    }
    
    /**
     * Create a market update with the specified market and price.
     */
    private MarketUpdate createMarketUpdate(Market market, TwoWayPrice price) {
        return new MarketUpdate() {
            @Override
            public Market getMarket() {
                return market;
            }
            
            @Override
            public TwoWayPrice getTwoWayPrice() {
                return price;
            }
        };
    }
    
    /**
     * Get the current used memory in bytes.
     */
    private long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }
}

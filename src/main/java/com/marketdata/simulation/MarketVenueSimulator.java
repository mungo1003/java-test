package com.marketdata.simulation;

import com.marketdata.service.MarketDataManager;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Simulates market venues publishing market depth updates.
 */
public class MarketVenueSimulator {
    private final MarketDataManager marketDataManager;
    private final String[] instrumentIds;
    private final String[] venueIds;
    private final Random random = new Random();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    /**
     * Creates a new market venue simulator.
     *
     * @param marketDataManager The market data manager to publish updates to
     * @param instrumentCount The number of instruments to simulate
     * @param venueCount The number of venues to simulate
     */
    public MarketVenueSimulator(MarketDataManager marketDataManager, int instrumentCount, int venueCount) {
        this.marketDataManager = marketDataManager;
        this.instrumentIds = new String[instrumentCount];
        this.venueIds = new String[venueCount];
        
        for (int i = 0; i < instrumentCount; i++) {
            instrumentIds[i] = "INSTRUMENT-" + (i + 1);
        }
        
        for (int i = 0; i < venueCount; i++) {
            venueIds[i] = "VENUE-" + (i + 1);
        }
    }

    /**
     * Starts the simulation.
     */
    public void start() {
        for (String venueId : venueIds) {
            for (String instrumentId : instrumentIds) {
                int updateInterval = 1000 + random.nextInt(4000);
                
                scheduler.scheduleAtFixedRate(
                    () -> generateMarketDepthUpdate(instrumentId, venueId),
                    random.nextInt(1000), // Initial delay
                    updateInterval,        // Period
                    TimeUnit.MILLISECONDS
                );
            }
        }
    }

    /**
     * Stops the simulation.
     */
    public void stop() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void generateMarketDepthUpdate(String instrumentId, String venueId) {
        try {
            int instrumentIndex = Integer.parseInt(instrumentId.split("-")[1]) - 1;
            double basePrice = 100.0 + (instrumentIndex * 10); // Different base price per instrument
            
            double bidPrice = basePrice - (random.nextDouble() * 2);
            double askPrice = basePrice + (random.nextDouble() * 2);
            
            double bidQuantity = 10 + (random.nextDouble() * 90);
            double askQuantity = 10 + (random.nextDouble() * 90);
            
            int updateType = random.nextInt(3);
            
            if (updateType == 0 || updateType == 2) {
                marketDataManager.updateMarketDepth(instrumentId, venueId, "bid", bidPrice, bidQuantity);
            }
            
            if (updateType == 1 || updateType == 2) {
                marketDataManager.updateMarketDepth(instrumentId, venueId, "ask", askPrice, askQuantity);
            }
        } catch (Exception e) {
            System.err.println("Error generating market depth update: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

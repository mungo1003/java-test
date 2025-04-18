package com.marketdata;

import com.marketdata.model.MarketDepth;
import com.marketdata.service.MarketDataManager;
import com.marketdata.service.VwapCalculator;
import com.marketdata.simulation.MarketVenueSimulator;

import java.util.Map;
import java.util.Scanner;

/**
 * Main application class for the market data VWAP prototype.
 */
public class MarketDataApp {
    
    public static void main(String[] args) {
        int instrumentCount = 3;
        int venueCount = 2;
        
        if (args.length >= 2) {
            try {
                instrumentCount = Integer.parseInt(args[0]);
                venueCount = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid arguments. Using default values.");
            }
        }
        
        System.out.println("Starting Market Data VWAP Application");
        System.out.println("Instruments: " + instrumentCount);
        System.out.println("Venues: " + venueCount);
        
        MarketDataManager marketDataManager = new MarketDataManager();
        
        for (int i = 1; i <= instrumentCount; i++) {
            final String instrumentId = "INSTRUMENT-" + i;
            
            marketDataManager.registerInstrumentCallback(instrumentId, () -> {
                Map<String, MarketDepth> depths = marketDataManager.getMarketDepthsForInstrument(instrumentId);
                
                double totalBidValue = 0.0;
                double totalBidVolume = 0.0;
                double totalAskValue = 0.0;
                double totalAskVolume = 0.0;
                
                for (MarketDepth depth : depths.values()) {
                    double bidVwap = VwapCalculator.calculateBidVwap(depth);
                    double askVwap = VwapCalculator.calculateAskVwap(depth);
                    
                    double bidVolume = depth.getBids().stream().mapToDouble(level -> level.getQuantity()).sum();
                    double askVolume = depth.getAsks().stream().mapToDouble(level -> level.getQuantity()).sum();
                    
                    if (bidVolume > 0) {
                        totalBidValue += bidVwap * bidVolume;
                        totalBidVolume += bidVolume;
                    }
                    
                    if (askVolume > 0) {
                        totalAskValue += askVwap * askVolume;
                        totalAskVolume += askVolume;
                    }
                }
                
                double aggregateBidVwap = totalBidVolume > 0 ? totalBidValue / totalBidVolume : 0;
                double aggregateAskVwap = totalAskVolume > 0 ? totalAskValue / totalAskVolume : 0;
                
                System.out.printf("[%s] VWAP: Bid = %.4f, Ask = %.4f%n", 
                                  instrumentId, aggregateBidVwap, aggregateAskVwap);
            });
        }
        
        MarketVenueSimulator simulator = new MarketVenueSimulator(marketDataManager, instrumentCount, venueCount);
        simulator.start();
        
        System.out.println("\nPress Enter to exit...");
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
        scanner.close();
        
        simulator.stop();
        System.out.println("Application stopped.");
    }
}

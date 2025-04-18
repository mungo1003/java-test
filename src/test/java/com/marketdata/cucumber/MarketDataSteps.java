package com.marketdata.cucumber;

import static org.junit.Assert.assertEquals;

import com.marketdata.model.MarketDepth;
import com.marketdata.service.VwapCalculator;
import io.cucumber.java.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarketDataSteps {
    private final Map<String, Map<String, MarketDepth>> marketData = new HashMap<>();
    private final Map<String, Double> bidVwaps = new HashMap<>();
    private final Map<String, Double> askVwaps = new HashMap<>();
    
    @Given("an instrument {string} with the following bid levels at venue {string}:")
    public void anInstrumentWithBidLevels(String instrumentId, String venueId, DataTable dataTable) {
        MarketDepth depth = getOrCreateMarketDepth(instrumentId, venueId);
        
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            double price = Double.parseDouble(row.get("Price"));
            double quantity = Double.parseDouble(row.get("Quantity"));
            depth.updateBid(price, quantity);
        }
    }
    
    @Given("the instrument {string} has the following ask levels at venue {string}:")
    public void theInstrumentHasAskLevels(String instrumentId, String venueId, DataTable dataTable) {
        MarketDepth depth = getOrCreateMarketDepth(instrumentId, venueId);
        
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            double price = Double.parseDouble(row.get("Price"));
            double quantity = Double.parseDouble(row.get("Quantity"));
            depth.updateAsk(price, quantity);
        }
    }
    
    @When("I calculate the VWAP prices for instrument {string}")
    public void iCalculateVwapPrices(String instrumentId) {
        Map<String, MarketDepth> venues = marketData.get(instrumentId);
        if (venues == null || venues.isEmpty()) {
            throw new IllegalStateException("No market data found for instrument: " + instrumentId);
        }
        
        double totalBidValue = 0.0;
        double totalBidVolume = 0.0;
        double totalAskValue = 0.0;
        double totalAskVolume = 0.0;
        
        for (MarketDepth depth : venues.values()) {
            double bidVwap = VwapCalculator.calculateBidVwap(depth);
            double askVwap = VwapCalculator.calculateAskVwap(depth);
            
            double bidVolume = depth.getBidLevels().stream()
                    .mapToDouble(level -> level.getQuantity())
                    .sum();
            
            double askVolume = depth.getAskLevels().stream()
                    .mapToDouble(level -> level.getQuantity())
                    .sum();
            
            if (bidVolume > 0) {
                totalBidValue += bidVwap * bidVolume;
                totalBidVolume += bidVolume;
            }
            
            if (askVolume > 0) {
                totalAskValue += askVwap * askVolume;
                totalAskVolume += askVolume;
            }
        }
        
        bidVwaps.put(instrumentId, totalBidVolume > 0 ? totalBidValue / totalBidVolume : 0);
        askVwaps.put(instrumentId, totalAskVolume > 0 ? totalAskValue / totalAskVolume : 0);
    }
    
    @When("I update the bid level for {string} at venue {string} with price {double} and quantity {double}")
    public void iUpdateBidLevel(String instrumentId, String venueId, double price, double quantity) {
        MarketDepth depth = getOrCreateMarketDepth(instrumentId, venueId);
        depth.updateBid(price, quantity);
    }
    
    @Then("the bid VWAP should be {double} with tolerance {double}")
    public void theBidVwapShouldBe(double expectedVwap, double tolerance) {
        for (Map.Entry<String, Double> entry : bidVwaps.entrySet()) {
            assertEquals("Bid VWAP for " + entry.getKey(), 
                    expectedVwap, entry.getValue(), tolerance);
        }
    }
    
    @Then("the ask VWAP should be {double} with tolerance {double}")
    public void theAskVwapShouldBe(double expectedVwap, double tolerance) {
        for (Map.Entry<String, Double> entry : askVwaps.entrySet()) {
            assertEquals("Ask VWAP for " + entry.getKey(), 
                    expectedVwap, entry.getValue(), tolerance);
        }
    }
    
    private MarketDepth getOrCreateMarketDepth(String instrumentId, String venueId) {
        Map<String, MarketDepth> venues = marketData.computeIfAbsent(
                instrumentId, k -> new HashMap<>());
        
        return venues.computeIfAbsent(
                venueId, k -> new MarketDepth(instrumentId, venueId));
    }
}

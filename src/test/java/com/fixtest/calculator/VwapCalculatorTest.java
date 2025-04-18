package com.fixtest.calculator;

import com.fixtest.calculator.impl.VwapCalculator;
import com.fixtest.calculator.impl.MutableVwapTwoWayPrice;
import com.fixtest.model.Instrument;
import com.fixtest.model.Market;
import com.fixtest.model.State;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the VwapCalculator implementation.
 */
public class VwapCalculatorTest {
    
    private Calculator calculator;
    
    @BeforeEach
    public void setUp() {
        calculator = new VwapCalculator();
    }
    
    @Test
    public void testSingleMarketUpdate() {
        final TwoWayPrice price = createTwoWayPrice(Instrument.INSTRUMENT0, State.FIRM, 100.0, 101.0, 10.0, 10.0);
        final MarketUpdate update = createMarketUpdate(Market.MARKET0, price);
        
        TwoWayPrice result = calculator.applyMarketUpdate(update);
        
        assertEquals(Instrument.INSTRUMENT0, result.getInstrument());
        assertEquals(State.FIRM, result.getState());
        assertEquals(100.0, result.getBidPrice(), 0.0001);
        assertEquals(101.0, result.getOfferPrice(), 0.0001);
        assertEquals(10.0, result.getBidAmount(), 0.0001);
        assertEquals(10.0, result.getOfferAmount(), 0.0001);
    }
    
    @Test
    public void testMultipleMarketUpdatesForSameInstrument() {
        final TwoWayPrice price1 = createTwoWayPrice(Instrument.INSTRUMENT0, State.FIRM, 100.0, 101.0, 10.0, 10.0);
        final MarketUpdate update1 = createMarketUpdate(Market.MARKET0, price1);
        
        final TwoWayPrice price2 = createTwoWayPrice(Instrument.INSTRUMENT0, State.FIRM, 102.0, 103.0, 20.0, 20.0);
        final MarketUpdate update2 = createMarketUpdate(Market.MARKET1, price2);
        
        calculator.applyMarketUpdate(update1);
        TwoWayPrice result = calculator.applyMarketUpdate(update2);
        
        assertEquals(Instrument.INSTRUMENT0, result.getInstrument());
        assertEquals(State.FIRM, result.getState());
        
        assertEquals(101.33, result.getBidPrice(), 0.01);
        assertEquals(102.33, result.getOfferPrice(), 0.01);
        assertEquals(30.0, result.getBidAmount(), 0.0001);
        assertEquals(30.0, result.getOfferAmount(), 0.0001);
    }
    
    @Test
    public void testMarketUpdateOverride() {
        final TwoWayPrice price1 = createTwoWayPrice(Instrument.INSTRUMENT0, State.FIRM, 100.0, 101.0, 10.0, 10.0);
        final MarketUpdate update1 = createMarketUpdate(Market.MARKET0, price1);
        
        final TwoWayPrice price2 = createTwoWayPrice(Instrument.INSTRUMENT0, State.FIRM, 102.0, 103.0, 20.0, 20.0);
        final MarketUpdate update2 = createMarketUpdate(Market.MARKET0, price2);
        
        calculator.applyMarketUpdate(update1);
        TwoWayPrice result = calculator.applyMarketUpdate(update2);
        
        assertEquals(Instrument.INSTRUMENT0, result.getInstrument());
        assertEquals(State.FIRM, result.getState());
        
        assertEquals(102.0, result.getBidPrice(), 0.0001);
        assertEquals(103.0, result.getOfferPrice(), 0.0001);
        assertEquals(20.0, result.getBidAmount(), 0.0001);
        assertEquals(20.0, result.getOfferAmount(), 0.0001);
    }
    
    @Test
    public void testIndicativeState() {
        final TwoWayPrice price1 = createTwoWayPrice(Instrument.INSTRUMENT0, State.FIRM, 100.0, 101.0, 10.0, 10.0);
        final MarketUpdate update1 = createMarketUpdate(Market.MARKET0, price1);
        
        final TwoWayPrice price2 = createTwoWayPrice(Instrument.INSTRUMENT0, State.INDICATIVE, 102.0, 103.0, 20.0, 20.0);
        final MarketUpdate update2 = createMarketUpdate(Market.MARKET1, price2);
        
        calculator.applyMarketUpdate(update1);
        TwoWayPrice result = calculator.applyMarketUpdate(update2);
        
        assertEquals(Instrument.INSTRUMENT0, result.getInstrument());
        assertEquals(State.INDICATIVE, result.getState());
        
        assertEquals(101.33, result.getBidPrice(), 0.01);
        assertEquals(102.33, result.getOfferPrice(), 0.01);
    }
    
    @Test
    public void testMultipleInstruments() {
        final TwoWayPrice price1 = createTwoWayPrice(Instrument.INSTRUMENT0, State.FIRM, 100.0, 101.0, 10.0, 10.0);
        final MarketUpdate update1 = createMarketUpdate(Market.MARKET0, price1);
        
        final TwoWayPrice price2 = createTwoWayPrice(Instrument.INSTRUMENT1, State.FIRM, 200.0, 201.0, 20.0, 20.0);
        final MarketUpdate update2 = createMarketUpdate(Market.MARKET0, price2);
        
        calculator.applyMarketUpdate(update1);
        TwoWayPrice result = calculator.applyMarketUpdate(update2);
        
        assertEquals(Instrument.INSTRUMENT1, result.getInstrument());
        assertEquals(State.FIRM, result.getState());
        assertEquals(200.0, result.getBidPrice(), 0.0001);
        assertEquals(201.0, result.getOfferPrice(), 0.0001);
    }
    
    @Test
    public void testNullMarketUpdate() {
        assertThrows(IllegalArgumentException.class, () -> calculator.applyMarketUpdate(null));
    }
    
    
    private TwoWayPrice createTwoWayPrice(Instrument instrument, State state, 
                                         double bidPrice, double offerPrice, 
                                         double bidAmount, double offerAmount) {
        return new MutableVwapTwoWayPrice(instrument).update(state, bidPrice, offerPrice, bidAmount, offerAmount);
    }
    
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
}

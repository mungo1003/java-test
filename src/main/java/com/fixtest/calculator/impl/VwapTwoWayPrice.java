package com.fixtest.calculator.impl;

import com.fixtest.calculator.TwoWayPrice;
import com.fixtest.model.Instrument;
import com.fixtest.model.State;

/**
 * Implementation of TwoWayPrice that represents a VWAP (Volume Weighted Average Price).
 * This class is immutable and thread-safe.
 */
public class VwapTwoWayPrice implements TwoWayPrice {
    private final Instrument instrument;
    private final State state;
    private final double bidPrice;
    private final double offerPrice;
    private final double bidAmount;
    private final double offerAmount;

    /**
     * Constructs a new VwapTwoWayPrice with the specified parameters.
     *
     * @param instrument the instrument this price is for
     * @param state      the state of the price (FIRM or INDICATIVE)
     * @param bidPrice   the bid price
     * @param offerPrice the offer price
     * @param bidAmount  the bid amount
     * @param offerAmount the offer amount
     */
    public VwapTwoWayPrice(Instrument instrument, State state, double bidPrice, double offerPrice, 
                          double bidAmount, double offerAmount) {
        this.instrument = instrument;
        this.state = state;
        this.bidPrice = bidPrice;
        this.offerPrice = offerPrice;
        this.bidAmount = bidAmount;
        this.offerAmount = offerAmount;
    }

    @Override
    public Instrument getInstrument() {
        return instrument;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public double getBidPrice() {
        return bidPrice;
    }

    @Override
    public double getOfferPrice() {
        return offerPrice;
    }

    @Override
    public double getBidAmount() {
        return bidAmount;
    }

    @Override
    public double getOfferAmount() {
        return offerAmount;
    }

    @Override
    public String toString() {
        return "VwapTwoWayPrice{" +
                "instrument=" + instrument +
                ", state=" + state +
                ", bidPrice=" + bidPrice +
                ", offerPrice=" + offerPrice +
                ", bidAmount=" + bidAmount +
                ", offerAmount=" + offerAmount +
                '}';
    }
}

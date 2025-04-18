package com.fixtest.calculator.impl;

import com.fixtest.calculator.TwoWayPrice;
import com.fixtest.model.Instrument;
import com.fixtest.model.State;

/**
 * Mutable implementation of TwoWayPrice that represents a VWAP (Volume Weighted Average Price).
 * This class allows updating its values to avoid creating new objects.
 */
public class MutableVwapTwoWayPrice implements TwoWayPrice {
    private final Instrument instrument;
    private State state;
    private double bidPrice;
    private double offerPrice;
    private double bidAmount;
    private double offerAmount;

    /**
     * Constructs a new MutableVwapTwoWayPrice with the specified instrument.
     *
     * @param instrument the instrument this price is for
     */
    public MutableVwapTwoWayPrice(Instrument instrument) {
        this.instrument = instrument;
        this.state = State.FIRM;
        this.bidPrice = Double.NaN;
        this.offerPrice = Double.NaN;
        this.bidAmount = 0.0;
        this.offerAmount = 0.0;
    }

    /**
     * Updates the values of this price object.
     *
     * @param state      the state of the price (FIRM or INDICATIVE)
     * @param bidPrice   the bid price
     * @param offerPrice the offer price
     * @param bidAmount  the bid amount
     * @param offerAmount the offer amount
     * @return this object for method chaining
     */
    public MutableVwapTwoWayPrice update(State state, double bidPrice, double offerPrice, 
                                      double bidAmount, double offerAmount) {
        this.state = state;
        this.bidPrice = bidPrice;
        this.offerPrice = offerPrice;
        this.bidAmount = bidAmount;
        this.offerAmount = offerAmount;
        return this;
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
        return "MutableVwapTwoWayPrice{" +
                "instrument=" + instrument +
                ", state=" + state +
                ", bidPrice=" + bidPrice +
                ", offerPrice=" + offerPrice +
                ", bidAmount=" + bidAmount +
                ", offerAmount=" + offerAmount +
                '}';
    }
}

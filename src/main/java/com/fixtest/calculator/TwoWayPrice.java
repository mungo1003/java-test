package com.fixtest.calculator;

import com.fixtest.model.Instrument;
import com.fixtest.model.State;

public interface TwoWayPrice {
    Instrument getInstrument();
    State getState();
    double getBidPrice();
    double getOfferAmount();
    double getOfferPrice();
    double getBidAmount();
}

package com.fixtest.calculator;

import com.fixtest.model.Market;

public interface MarketUpdate {
    Market getMarket();
    TwoWayPrice getTwoWayPrice();
}

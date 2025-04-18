package com.marketdata.model;

/**
 * Represents a price level in the order book with a price and quantity.
 */
public class PriceLevel {
    private final double price;
    private double quantity;

    public PriceLevel(double price, double quantity) {
        this.price = price;
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return String.format("%.2f @ %.2f", quantity, price);
    }
}

package com.asx.fix;

/**
 * Represents a market data entry with type, price, size, and ID.
 */
public class MarketDataEntry {
    private final char entryType;
    private final double price;
    private final double size;
    private final String entryId;
    
    public MarketDataEntry(char entryType, double price, double size, String entryId) {
        this.entryType = entryType;
        this.price = price;
        this.size = size;
        this.entryId = entryId;
    }
    
    /**
     * Get the entry type.
     * 0 = Bid
     * 1 = Offer
     * 2 = Trade
     * 4 = Opening Price
     * 5 = Closing Price
     * 6 = Settlement Price
     * 7 = Trading Session High Price
     * 8 = Trading Session Low Price
     * B = Trade Volume
     * C = Open Interest
     */
    public char getEntryType() {
        return entryType;
    }
    
    /**
     * Get the price.
     */
    public double getPrice() {
        return price;
    }
    
    /**
     * Get the size.
     */
    public double getSize() {
        return size;
    }
    
    /**
     * Get the entry ID.
     */
    public String getEntryId() {
        return entryId;
    }
    
    @Override
    public String toString() {
        return "MarketDataEntry{" +
                "entryType=" + entryType +
                ", price=" + price +
                ", size=" + size +
                ", entryId='" + entryId + '\'' +
                '}';
    }
}

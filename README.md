# ASX FIX Market Data Engine

This project implements a FIX engine for ASX Market Data using QuickFIX/J. It provides functionality for session-level messages, market data subscription, and market data snapshot and delta handling according to the ASX FIX Market Data Specification.

## Features

- Session-level message handling (Logon, Heartbeat, TestRequest, etc.)
- Market data subscription
- Market data snapshot and incremental refresh handling
- Trade replay functionality
- Comprehensive test suite

## Project Structure

- `ASXFIXApplication`: Main FIX application class that handles session-level messages and market data
- `ASXFIXClient`: Client implementation for ASX FIX Market Data
- `ASXFIXMarketDataApp`: Main application class with high-level API
- `DefaultMessageHandler`: Default implementation of the MessageHandler interface
- `MarketDataEntry`: Represents a market data entry with type, price, size, and ID
- `MarketDataListener`: Interface for listening to market data updates

## Configuration

The FIX engine is configured using the `quickfix.cfg` file in the `config` directory. You can customize this file to match your environment.

## Usage

```java
// Create the application
ASXFIXMarketDataApp app = new ASXFIXMarketDataApp("config/quickfix.cfg");

// Start the application
app.start();

// Add a market data listener
app.addMarketDataListener(new MarketDataListener() {
    @Override
    public void onMarketDataUpdate(String symbol, MarketDataEntry entry) {
        System.out.println("Market data update for " + symbol + ": " + entry);
    }
    
    @Override
    public void onMarketDataDelete(String symbol, char entryType) {
        System.out.println("Market data delete for " + symbol + ", entry type: " + entryType);
    }
    
    @Override
    public void onMarketDataRequestReject(String mdReqId, MarketDataRequestReject message) {
        System.out.println("Market data request rejected: " + mdReqId);
    }
});

// Subscribe to market data
String[] symbols = {"APT", "BHP"};
char[] entryTypes = {'0', '1', '2'}; // Bid, Offer, Trade
String mdReqId = app.subscribeMarketData(symbols, entryTypes);

// Get market data snapshot
app.getMarketDataSnapshot(symbols, entryTypes);

// Cancel subscription
app.cancelMarketDataSubscription(mdReqId);

// Stop the application
app.stop();
```

## Testing

The project includes comprehensive tests for the FIX engine implementation. Run the tests using Maven:

```
mvn test
```

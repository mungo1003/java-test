# Market Data VWAP Prototype

This Java application simulates market depth updates for multiple instruments across multiple market venues and calculates the Volume-Weighted Average Price (VWAP) in real-time.

## Features

- Subscribes to market depth updates for multiple instruments across multiple venues
- Calculates VWAP two-way prices (bid and ask) for each instrument in real-time
- Displays VWAP prices whenever a market depth tick is received
- Configurable number of instruments and venues

## Building the Application

The application uses Maven for building and dependency management:

```bash
mvn clean package
```

## Running the Application

Run the application with default settings (3 instruments, 2 venues):

```bash
java -cp target/market-data-vwap-1.0-SNAPSHOT.jar com.marketdata.MarketDataApp
```

Or specify the number of instruments and venues:

```bash
java -cp target/market-data-vwap-1.0-SNAPSHOT.jar com.marketdata.MarketDataApp 5 3
```

This would run the application with 5 instruments and 3 venues.

## Project Structure

- `model`: Contains data models for market depth and price levels
- `service`: Contains the market data manager and VWAP calculator
- `simulation`: Contains the market venue simulator for testing

## Implementation Details

- The application uses a publisher-subscriber pattern for market data updates
- VWAP is calculated by summing (price * quantity) for all price levels and dividing by the total quantity
- The simulator generates random market depth updates at configurable intervals

Feature: Market Data VWAP Calculation
  As a market data consumer
  I want to receive VWAP prices for instruments
  So that I can make informed trading decisions

  Scenario: Calculate VWAP for a single instrument with multiple price levels
    Given an instrument "AAPL" with the following bid levels at venue "VENUE-1":
      | Price | Quantity |
      | 100.0 | 10.0     |
      | 99.0  | 20.0     |
      | 98.0  | 30.0     |
    And the instrument "AAPL" has the following ask levels at venue "VENUE-1":
      | Price | Quantity |
      | 101.0 | 15.0     |
      | 102.0 | 25.0     |
      | 103.0 | 5.0      |
    When I calculate the VWAP prices for instrument "AAPL"
    Then the bid VWAP should be 98.83 with tolerance 0.01
    And the ask VWAP should be 101.89 with tolerance 0.01

  Scenario: Calculate VWAP for an instrument across multiple venues
    Given an instrument "MSFT" with the following bid levels at venue "VENUE-1":
      | Price | Quantity |
      | 200.0 | 5.0      |
      | 199.0 | 10.0     |
    And the instrument "MSFT" has the following ask levels at venue "VENUE-1":
      | Price | Quantity |
      | 201.0 | 8.0      |
      | 202.0 | 12.0     |
    And the instrument "MSFT" has the following bid levels at venue "VENUE-2":
      | Price | Quantity |
      | 200.5 | 7.0      |
      | 199.5 | 15.0     |
    And the instrument "MSFT" has the following ask levels at venue "VENUE-2":
      | Price | Quantity |
      | 201.5 | 10.0     |
      | 202.5 | 5.0      |
    When I calculate the VWAP prices for instrument "MSFT"
    Then the bid VWAP should be 199.62 with tolerance 0.01
    And the ask VWAP should be 201.74 with tolerance 0.01

  Scenario: Update market depth and recalculate VWAP
    Given an instrument "GOOG" with the following bid levels at venue "VENUE-1":
      | Price | Quantity |
      | 300.0 | 10.0     |
    When I update the bid level for "GOOG" at venue "VENUE-1" with price 300.0 and quantity 20.0
    And I calculate the VWAP prices for instrument "GOOG"
    Then the bid VWAP should be 300.0 with tolerance 0.01

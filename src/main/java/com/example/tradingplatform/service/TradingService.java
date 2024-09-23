// TradingService.java
package com.example.tradingplatform.service;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.tradingplatform.model.Order;
import com.example.tradingplatform.model.OrderBook;
import com.example.tradingplatform.model.Trade;

@Service
public class TradingService {
    private final OrderBook orderBook;
    private final MatchingEngine matchingEngine;
    private final List<VenueConnector> venueConnectors;

    @Autowired
    public TradingService(OrderBook orderBook,
                          MatchingEngine matchingEngine,
                          List<VenueConnector> venueConnectors) {
        this.orderBook = orderBook;
        this.matchingEngine = matchingEngine;
        this.venueConnectors = venueConnectors;
    }

    public void placeOrder(Order order) {
        orderBook.insertOrder(order);
        List<Trade> trades = matchingEngine.matchOrders(orderBook, order);
        for (VenueConnector connector : venueConnectors) {
            connector.placeOrder(order);
        }
    }

    public void amendOrder(String orderId, int newQuantity) {
        orderBook.amendOrder(orderId, newQuantity);
        for (VenueConnector connector : venueConnectors) {
            connector.amendOrder(orderId, newQuantity);
        }
    }

    public void cancelOrder(String orderId) {
        orderBook.cancelOrder(orderId);
        // Cancel order in external venues
        for (VenueConnector connector : venueConnectors) {
            connector.cancelOrder(orderId);
        }
    }

    public Order getBestBid() {
        return orderBook.getBestBid();
    }

    public Order getBestAsk() {
        return orderBook.getBestAsk();
    }

    public double getMarketPrice(String instrument) {
        List<Double> prices = new ArrayList<>();

        // Get price from internal order book
        double internalPrice = orderBook.getCurrentPrice(instrument);
        if (internalPrice > 0) {
            prices.add(internalPrice);
        }

        // Get prices from all connected venues
        for (VenueConnector connector : venueConnectors) {
            double venuePrice = connector.getCurrentPrice(instrument);
            if (venuePrice > 0) {
                prices.add(venuePrice);
            }
        }

        // Calculate average price
        OptionalDouble averagePrice = prices.stream().mapToDouble(Double::doubleValue).average();

        if (averagePrice.isPresent()) {
            return averagePrice.getAsDouble();
        } else {
            throw new IllegalStateException("No valid market price available for " + instrument);
        }
    }
}
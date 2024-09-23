package com.example.tradingplatform.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.example.tradingplatform.model.Order;
import com.example.tradingplatform.model.OrderBook;
import com.example.tradingplatform.model.Side;



public class ExchangeConnector implements VenueConnector {
    private final String exchangeName;
    private final Map<String, OrderBook> orderBooks;
    private boolean isConnected;
    private final Map<String, AtomicLong> orderIdCounters;

    public ExchangeConnector(String exchangeName) {
        this.exchangeName = exchangeName;
        this.orderBooks = new ConcurrentHashMap<>();
        new ConcurrentHashMap<>();
        this.isConnected = false;
        this.orderIdCounters = null;
    }

    @Override
    public void connect() {
        // Simulating connection to the exchange
        System.out.println("Connecting to exchange: " + exchangeName);
        isConnected = true;
    }

    @Override
    public void disconnect() {
        // Simulating disconnection from the exchange
        System.out.println("Disconnecting from exchange: " + exchangeName);
        isConnected = false;
    }

    @Override
    public String placeOrder(Order order) {
        if (!isConnected) {
            throw new IllegalStateException("Not connected to the exchange");
        }

        String instrument = order.getInstrument();
        OrderBook orderBook = orderBooks.computeIfAbsent(instrument, k -> new OrderBook());
        String orderId = generateOrderId(instrument);
        order.setId(orderId);
        orderBook.insertOrder(order);

        System.out.println("Placed order on exchange " + exchangeName + ": " + order);
        return orderId;
    }

    private String generateOrderId(String instrument) {
        AtomicLong counter = orderIdCounters.computeIfAbsent(instrument, k -> new AtomicLong());
        long orderId = counter.incrementAndGet();
        return String.format("%s-%s-%d", exchangeName, instrument, orderId);
    }

	@Override
    public void cancelOrder(String orderId) {
        if (!isConnected) {
            throw new IllegalStateException("Not connected to the exchange");
        }

        for (OrderBook orderBook : orderBooks.values()) {
            orderBook.cancelOrder(orderId);
        }

        System.out.println("Cancelled order on exchange " + exchangeName + ": " + orderId);
    }


    @Override
    public List<Order> getOrderBook(String instrument) {
        if (!isConnected) {
            throw new IllegalStateException("Not connected to the exchange");
        }

        OrderBook orderBook = orderBooks.get(instrument);
        if (orderBook == null) {
            return new ArrayList<>();
        }

        List<Order> allOrders = new ArrayList<>();
        Order bestBid = orderBook.getBestBid();
        Order bestAsk = orderBook.getBestAsk();

        if (bestBid != null) {
            allOrders.addAll(orderBook.getOrdersAtPrice(Side.BUY, bestBid.getPrice()));
        }
        if (bestAsk != null) {
            allOrders.addAll(orderBook.getOrdersAtPrice(Side.SELL, bestAsk.getPrice()));
        }
        return allOrders;
    }

    @Override
    public double getCurrentPrice(String instrument) {
        if (!isConnected) {
            throw new IllegalStateException("Not connected to the exchange");
        }

        OrderBook orderBook = orderBooks.get(instrument);
        if (orderBook == null || orderBook.getBestBid() == null || orderBook.getBestAsk() == null) {
            return 0.0;
        }

        return (orderBook.getBestBid().getPrice() + orderBook.getBestAsk().getPrice()) / 2.0;
    }

     public void amendOrder(String orderId, int newQuantity) {
        if (!isConnected) {
            throw new IllegalStateException("Not connected to the exchange");
        }

        for (OrderBook orderBook : orderBooks.values()) {
            // Attempt to find and update the order
            boolean orderUpdated = orderBook.amendOrder(orderId, newQuantity);
            if (orderUpdated) {
                System.out.println("Amended order on exchange " + exchangeName + ": " + orderId + " to quantity " + newQuantity);
                return;
            }
        }

        throw new IllegalArgumentException("Order not found: " + orderId);
    }

}
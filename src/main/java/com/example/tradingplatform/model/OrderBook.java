package com.example.tradingplatform.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;


public class OrderBook {
    private final TreeMap<Double, PriceLevel> bids = new TreeMap<>(Collections.reverseOrder());
    private final TreeMap<Double, PriceLevel> asks = new TreeMap<>();
    private final Map<String, Order> orderMap = new HashMap<>();

    public void insertOrder(Order order) {
        orderMap.put(order.getId(), order);
        TreeMap<Double, PriceLevel> book = order.getSide() == Side.BUY ? bids : asks;
        book.computeIfAbsent(order.getPrice(), k -> new PriceLevel()).addOrder(order);
    }

    public boolean amendOrder(String orderId, int newQuantity) {
        Order order = orderMap.get(orderId);
        if (order == null) {
            return false;
        }

        if (newQuantity <= 0) {
            cancelOrder(orderId);
        } else {
            TreeMap<Double, PriceLevel> book = order.getSide() == Side.BUY ? bids : asks;
            PriceLevel level = book.get(order.getPrice());
            level.updateOrderQuantity(order, newQuantity);
            if (level.isEmpty()) {
                book.remove(order.getPrice());
            }
        }

        return true;
    }

    public void cancelOrder(String orderId) {
        Order order = orderMap.remove(orderId);
        if (order != null) {
            TreeMap<Double, PriceLevel> book = order.getSide() == Side.BUY ? bids : asks;
            PriceLevel level = book.get(order.getPrice());
            level.removeOrder(order);
            if (level.isEmpty()) {
                book.remove(order.getPrice());
            }
        }
    }

    public List<Order> getOrdersBySide(Side side) {
        List<Order> orders = new ArrayList<>();
        TreeMap<Double, PriceLevel> book = (side == Side.BUY) ? bids : asks;

        for (PriceLevel level : book.values()) {
            orders.addAll(level.getOrders());
        }

        return orders;
    }

    public double getCurrentPrice(String instrument) {
        Order bestBid = getBestBid();
        Order bestAsk = getBestAsk();

        if (bestBid != null && bestAsk != null) {
            // If we have both bid and ask, return the mid-price
            return (bestBid.getPrice() + bestAsk.getPrice()) / 2.0;
        } else if (bestBid != null) {
            // If we only have a bid, return the bid price
            return bestBid.getPrice();
        } else if (bestAsk != null) {
            // If we only have an ask, return the ask price
            return bestAsk.getPrice();
        } else {
            // If the order book is empty, return 0 or throw an exception
            // Depending on your requirements, you might want to handle this differently
            return 0.0;
            // Alternatively: throw new IllegalStateException("Order book is empty for instrument: " + instrument);
        }
    }

    public Order getBestBid() {
        return !bids.isEmpty() ? bids.firstEntry().getValue().getFirstOrder() : null;
    }

    public Order getBestAsk() {
        return !asks.isEmpty() ? asks.firstEntry().getValue().getFirstOrder() : null;
    }

    public List<Order> getOrdersAtPrice(Side side, double price) {
        TreeMap<Double, PriceLevel> book = side == Side.BUY ? bids : asks;
        PriceLevel level = book.get(price);
        return level != null ? new ArrayList<>(level.getOrders()) : new ArrayList<>();
    }

    public Iterator<Order> getOrderIterator(Side side) {
        TreeMap<Double, PriceLevel> book = side == Side.BUY ? bids : asks;
        return new OrderIterator(book);
    }

    private static class PriceLevel {
        private final LinkedHashSet<Order> orders = new LinkedHashSet<>();

        public void addOrder(Order order) {
            orders.add(order);
        }

        public void removeOrder(Order order) {
            orders.remove(order);
        }

        public void updateOrderQuantity(Order order, int newQuantity) {
            order.setQuantity(newQuantity);
        }

        public boolean isEmpty() {
            return orders.isEmpty();
        }

        public Order getFirstOrder() {
            return orders.isEmpty() ? null : orders.iterator().next();
        }

        public Set<Order> getOrders() {
            return Collections.unmodifiableSet(orders);
        }
    }

    private static class OrderIterator implements Iterator<Order> {
        private final Iterator<PriceLevel> levelIterator;
        private Iterator<Order> orderIterator;

        public OrderIterator(TreeMap<Double, PriceLevel> book) {
            this.levelIterator = book.values().iterator();
            if (levelIterator.hasNext()) {
                this.orderIterator = levelIterator.next().getOrders().iterator();
            }
        }

        @Override
        public boolean hasNext() {
            while ((orderIterator == null || !orderIterator.hasNext()) && levelIterator.hasNext()) {
                orderIterator = levelIterator.next().getOrders().iterator();
            }
            return orderIterator != null && orderIterator.hasNext();
        }

        @Override
        public Order next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return orderIterator.next();
        }
    }
}
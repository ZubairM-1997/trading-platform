package com.example.tradingplatform.service;

import java.util.List;

import com.example.tradingplatform.model.Order;

public interface VenueConnector {
    void connect();
    void disconnect();
    String placeOrder(Order order);
    void cancelOrder(String orderId);
    List<Order> getOrderBook(String instrument);
    double getCurrentPrice(String instrument);
	void amendOrder(String orderId, int newQuantity);
}
package com.example.tradingplatform.service;

import java.util.List;

import com.example.tradingplatform.model.Order;
import com.example.tradingplatform.model.OrderBook;
import com.example.tradingplatform.model.Trade;

public interface MatchingEngine {
    List<Trade> matchOrders(OrderBook orderBook, Order incomingOrder);
}
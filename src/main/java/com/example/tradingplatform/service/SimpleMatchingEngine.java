package com.example.tradingplatform.service;

import com.example.tradingplatform.model.Order;
import com.example.tradingplatform.model.Side;
import com.example.tradingplatform.model.Trade;
import com.example.tradingplatform.model.OrderBook;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class SimpleMatchingEngine implements MatchingEngine {

    public List<Trade> matchOrders(OrderBook orderBook, Order incomingOrder) {
        List<Trade> trades = new ArrayList<>();

        Side counterSide = (incomingOrder.getSide() == Side.BUY) ? Side.SELL : Side.BUY;
        List<Order> counterOrders = orderBook.getOrdersBySide(counterSide);

        int remainingQuantity = incomingOrder.getQuantity();

        for (Order counterOrder : counterOrders) {
            if (isMatchingPrice(incomingOrder, counterOrder)) {
                int tradeQuantity = Math.min(remainingQuantity, counterOrder.getQuantity());
                double tradePrice = counterOrder.getPrice();

                Trade trade = new Trade(incomingOrder.getId(), counterOrder.getId(), tradePrice, tradeQuantity);
                trades.add(trade);

                remainingQuantity -= tradeQuantity;
                counterOrder.setQuantity(counterOrder.getQuantity() - tradeQuantity);

                if (counterOrder.getQuantity() == 0) {
                    orderBook.cancelOrder(counterOrder.getId());
                }

                if (remainingQuantity == 0) {
                    break;
                }
            }
        }

        if (remainingQuantity > 0) {
            incomingOrder.setQuantity(remainingQuantity);
            orderBook.insertOrder(incomingOrder);
        }

        return trades;
    }

    private boolean isMatchingPrice(Order incomingOrder, Order bookOrder) {
        if (incomingOrder.getSide() == Side.BUY) {
            return incomingOrder.getPrice() >= bookOrder.getPrice();
        } else {
            return incomingOrder.getPrice() <= bookOrder.getPrice();
        }
    }
}
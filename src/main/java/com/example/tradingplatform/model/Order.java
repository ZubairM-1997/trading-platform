package com.example.tradingplatform.model;


public class Order {
    private String id;
    private final Side side;
    private final double price;
    private int quantity;
    private final String instrument;

    public Order(String id, Side side, double price, int quantity, String instrument) {
        this.id = id;
        this.side = side;
        this.price = price;
        this.quantity = quantity;
        this.instrument = instrument;
    }

    // Getters and setters
    public String getId() { return id; }
    public Side getSide() { return side; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getInstrument() {
        return instrument;
    }

    public void setId(String orderId) {
        this.id = orderId;
    }
}
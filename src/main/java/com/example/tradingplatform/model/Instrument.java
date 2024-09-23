
package com.example.tradingplatform.model;


public class Instrument {
    private final String id;
    private final String symbol;

    public Instrument(String id, String symbol) {
        this.id = id;
        this.symbol = symbol;
    }

    public String getId() {
        return this.id;
    }

    public String getSymbol() {
        return this.symbol;
    }
}
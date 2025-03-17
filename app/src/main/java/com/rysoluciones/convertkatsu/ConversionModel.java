package com.rysoluciones.convertkatsu;

public class ConversionModel {
    private int id; // Agrega el ID
    private String fromCurrency;
    private String toCurrency;
    private double rate;
    private double amount;
    private double result;
    private String timestamp;

    // Constructor con ID
    public ConversionModel(int id, String fromCurrency, String toCurrency, double rate, double amount, double result, String timestamp) {
        this.id = id;
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.rate = rate;
        this.amount = amount;
        this.result = result;
        this.timestamp = timestamp;
    }

    // Constructor sin ID (por si lo necesitas en otro momento)
    public ConversionModel(String fromCurrency, String toCurrency, double rate, double amount, double result, String timestamp) {
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.rate = rate;
        this.amount = amount;
        this.result = result;
        this.timestamp = timestamp;
    }

    // Getters
    public int getId() { return id; } // Agrega el getter para ID
    public String getFromCurrency() { return fromCurrency; }
    public String getToCurrency() { return toCurrency; }
    public double getRate() { return rate; }
    public double getAmount() { return amount; }
    public double getResult() { return result; }
    public String getTimestamp() { return timestamp; }

    // Setter para ID (por si lo necesitas)
    public void setId(int id) { this.id = id; }
}

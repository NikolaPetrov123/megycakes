package com.megycakes.cart;

public class CartItem {
    private Long productId;
    private String name;
    private int priceCents;
    private int quantity;

    public CartItem() {}
    public CartItem(Long productId, String name, int priceCents, int quantity) {
        this.productId = productId; this.name = name; this.priceCents = priceCents; this.quantity = quantity;
    }
    public Long getProductId() { return productId; }
    public String getName() { return name; }
    public int getPriceCents() { return priceCents; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getLineTotalCents() { return priceCents * quantity; }
}
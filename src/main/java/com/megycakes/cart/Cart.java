package com.megycakes.cart;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.util.*;

@Component
@SessionScope
public class Cart {
    private final Map<Long, CartItem> items = new LinkedHashMap<>();

    public Collection<CartItem> getItems() { return items.values(); }

    public void add(Long id, String name, int priceCents, int qty) {
        items.merge(id, new CartItem(id, name, priceCents, qty),
                    (existing, incoming) -> { existing.setQuantity(existing.getQuantity() + incoming.getQuantity()); return existing; });
    }

    public void updateQty(Long id, int qty) {
        if (qty <= 0) items.remove(id); else {
            CartItem it = items.get(id);
            if (it != null) it.setQuantity(qty);
        }
    }

    public void remove(Long id) { items.remove(id); }

    public int totalCents() {
        return items.values().stream().mapToInt(CartItem::getLineTotalCents).sum();
    }

    public int itemCount() {
        return items.values().stream().mapToInt(CartItem::getQuantity).sum();
    }

    public boolean isEmpty() { return items.isEmpty(); }

    public void clear() {
        items.clear();
    }
}
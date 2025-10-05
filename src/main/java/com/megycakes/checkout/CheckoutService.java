package com.megycakes.checkout;

import com.megycakes.cart.Cart;
import com.megycakes.cart.CartItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Locale;

@Service
public class CheckoutService {
    private final OrderRepository orders;

    private static final int DELIVERY_FLAT_FEE_CENTS = 500; // €5.00 when delivery

    public CheckoutService(OrderRepository orders) {
        this.orders = orders;
    }

    @Transactional
    public Order createOrderFromCart(String name, String email, String phone, boolean pickup,
                                     String address1, String address2, String city, String postal, String country,
                                     Cart cart) {
        if (cart == null || cart.isEmpty()) throw new IllegalStateException("Cart is empty");

        Order o = new Order();
        o.setOrderNumber(generateOrderNumber());
        o.setCustomerName(name);
        o.setCustomerEmail(email);
        o.setStatus(OrderStatus.NEW);
        o.setCurrency("EUR");

        if (pickup) {
            o.setDeliveryMethod("LOCAL_PICKUP");
            o.setDeliveryFeeCents(0);
        } else {
            o.setDeliveryMethod("DELIVERY");
            o.setDeliveryFeeCents(DELIVERY_FLAT_FEE_CENTS);
        }

        int subtotal = 0;
        for (CartItem it : cart.getItems()) {
            OrderItem oi = new OrderItem();
            oi.setProductId(it.getProductId());
            oi.setNameSnapshot(it.getName());
            oi.setPriceCentsSnapshot(it.getPriceCents());
            oi.setQuantity(it.getQuantity());
            o.addItem(oi);
            subtotal += it.getPriceCents() * it.getQuantity();
        }

        int total = subtotal + o.getDeliveryFeeCents();
        o.setTotalCents(total);

        return orders.save(o);
    }

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // no 0/O/1/I
    private static final SecureRandom RNG = new SecureRandom();

    private static String generateOrderNumber() {
        char[] out = new char[10];
        for (int i = 0; i < out.length; i++) {
            out[i] = ALPHABET.charAt(RNG.nextInt(ALPHABET.length()));
        }
        return new String(out).toUpperCase(Locale.ROOT);
    }
}
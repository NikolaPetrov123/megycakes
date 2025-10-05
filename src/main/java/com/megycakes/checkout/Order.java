package com.megycakes.checkout;

import jakarta.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    @Column(name = "customer_email", nullable = false)
    private String customerEmail;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "total_cents", nullable = false)
    private int totalCents;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "EUR";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status = OrderStatus.NEW;

    @Column(name = "delivery_method", nullable = false, length = 32)
    private String deliveryMethod = "LOCAL_PICKUP"; // or "DELIVERY"

    @Column(name = "delivery_fee_cents", nullable = false)
    private int deliveryFeeCents;

    // --- New address/contact fields (nullable for pickup) ---
    @Column(name = "address_line1")
    private String addressLine1;

    @Column(name = "address_line2")
    private String addressLine2;

    @Column(name = "city")
    private String city;

    @Column(name = "postal_code", length = 16)
    private String postalCode;

    @Column(name = "country", length = 2) // ISO-2 recommended (e.g., "BG", "DE")
    private String country;

    @Column(name = "phone", length = 32)
    private String phone;

    @Column(name = "stripe_session_id")
    private String stripeSessionId;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Timestamp updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    // --- Domain helpers ---

    /** True if delivery method is pickup (case-insensitive). */
    @Transient
    public boolean isPickup() {
        return "LOCAL_PICKUP".equalsIgnoreCase(deliveryMethod) || "PICKUP".equalsIgnoreCase(deliveryMethod);
    }

    /** True if delivery (not pickup). */
    @Transient
    public boolean isDelivery() {
        return !isPickup();
    }

    /** Grand total = items total + delivery fee (only when delivery). */
    @Transient
    public int getGrandTotalCents() {
        return totalCents + (isDelivery() ? Math.max(0, deliveryFeeCents) : 0);
    }

    /** Do we have enough address to show anything meaningful? */
    @Transient
    public boolean hasAddress() {
        return notBlank(addressLine1) || notBlank(city) || notBlank(postalCode) || notBlank(country);
    }

    /** A single-line formatted address (skips blank parts). */
    @Transient
    public String getFormattedAddress() {
        StringBuilder sb = new StringBuilder();
        appendPart(sb, addressLine1);
        appendPart(sb, addressLine2);
        appendPart(sb, city);
        appendPart(sb, postalCode);
        appendPart(sb, country);
        return sb.toString().trim().replaceAll("^[,\\s]+|[,\\s]+$", "");
    }

    private static boolean notBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private static void appendPart(StringBuilder sb, String part) {
        if (notBlank(part)) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(part.trim());
        }
    }

    public void addItem(OrderItem item) {
        item.setOrder(this);
        this.items.add(item);
    }

    // --- Getters and setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    /** Items subtotal in cents (without delivery). */
    public int getTotalCents() { return totalCents; }
    public void setTotalCents(int totalCents) { this.totalCents = totalCents; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public String getDeliveryMethod() { return deliveryMethod; }
    public void setDeliveryMethod(String deliveryMethod) { this.deliveryMethod = deliveryMethod; }

    public int getDeliveryFeeCents() { return deliveryFeeCents; }
    public void setDeliveryFeeCents(int deliveryFeeCents) { this.deliveryFeeCents = deliveryFeeCents; }

    public String getAddressLine1() { return addressLine1; }
    public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }

    public String getAddressLine2() { return addressLine2; }
    public void setAddressLine2(String addressLine2) { this.addressLine2 = addressLine2; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getStripeSessionId() { return stripeSessionId; }
    public void setStripeSessionId(String stripeSessionId) { this.stripeSessionId = stripeSessionId; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
}
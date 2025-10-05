package com.megycakes.checkout;

import jakarta.validation.constraints.*;

public class CheckoutForm {
    @NotBlank(message = "Full name is required")
    @Size(max = 80, message = "Full name must be at most 80 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email")
    @Size(max = 120)
    private String email;

    @Size(max = 40)
    @Pattern(regexp = "^[+\\d][\\d ()-]{6,}$", message = "Please enter a valid phone")
    private String phone;

    // true = pickup, false = delivery
    private boolean pickup = true;

    // delivery fields (optional for pickup)
    @Size(max = 160)
    private String address1;

    @Size(max = 160)
    private String address2;

    @Size(max = 80)
    private String city;

    @Size(max = 20)
    private String postal;

    @Size(max = 60)
    private String country;

    // --- Conditional validation: require address fields when NOT pickup (i.e., delivery) ---
    @AssertTrue(message = "Address, city, postal and country are required for delivery")
    public boolean isDeliveryAddressValid() {
        if (pickup) return true; // pickup: delivery fields optional
        return hasText(address1) && hasText(city) && hasText(postal) && hasText(country);
    }

    private boolean hasText(String s) { return s != null && !s.trim().isEmpty(); }

    // getters/setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public boolean isPickup() { return pickup; }
    public void setPickup(boolean pickup) { this.pickup = pickup; }

    public String getAddress1() { return address1; }
    public void setAddress1(String address1) { this.address1 = address1; }

    public String getAddress2() { return address2; }
    public void setAddress2(String address2) { this.address2 = address2; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getPostal() { return postal; }
    public void setPostal(String postal) { this.postal = postal; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
}
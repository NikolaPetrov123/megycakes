package com.megycakes.checkout;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class CheckoutForm {
    @NotBlank private String name;
    @Email @NotBlank private String email;
    private String phone;

    // true = pickup, false = delivery
    private boolean pickup = true;

    // delivery fields (optional for pickup)
    private String address1;
    private String address2;
    private String city;
    private String postal;
    private String country;

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
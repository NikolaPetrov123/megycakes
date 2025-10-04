package com.megycakes.web;

import com.megycakes.cart.Cart;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(annotations = Controller.class)
public class GlobalModelAdvice {

    @ModelAttribute
    public void globalAttributes(Model model, HttpSession session, CsrfToken csrfToken) {
        // Make CSRF token available to Thymeleaf as "_csrf"
        if (csrfToken != null) {
            model.addAttribute("_csrf", csrfToken);
        }
        // Ensure a Cart is always present so the mini-cart renders safely
        Cart cart = (Cart) session.getAttribute("CART");
        if (cart == null) {
            cart = new Cart();
            session.setAttribute("CART", cart);
        }
        model.addAttribute("cart", cart);
    }
}
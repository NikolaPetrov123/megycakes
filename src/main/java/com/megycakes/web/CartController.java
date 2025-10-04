package com.megycakes.web;

import com.megycakes.cart.Cart;
import com.megycakes.catalog.Product;
import com.megycakes.catalog.ProductRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/cart")
public class CartController {
    private final ProductRepository products;

    public CartController(ProductRepository products) {
        this.products = products;
    }

    private Cart getCart(HttpSession session) {
        Cart cart = (Cart) session.getAttribute("CART");
        if (cart == null) {
            cart = new Cart();
            session.setAttribute("CART", cart);
        }
        return cart;
    }

    @GetMapping
    public String view(HttpSession session, Model model) {
        model.addAttribute("cart", getCart(session));
        return "cart";
    }

    @PostMapping("/add/{id}")
    public String add(@PathVariable("id") long id,
                      @RequestParam(name = "qty", defaultValue = "1") int qty,
                      HttpSession session,
                      Model model,
                      HttpServletRequest request) {
        // Normalize qty
        if (qty < 1) qty = 1;

        Product p = products.findById(id).orElseThrow();
        getCart(session).add(p.getId(), p.getName(), p.getPriceCents(), qty);
        model.addAttribute("cart", getCart(session));

        // If not an HTMX request, redirect to full cart page
        if (!"true".equals(request.getHeader("HX-Request"))) {
            return "redirect:/cart";
        }
        // HTMX: respond based on target
        String hxTarget = request.getHeader("HX-Target");
        if ("cart-body".equals(hxTarget)) {
            // update the main cart area
            return "cart :: cart-body";
        }
        // otherwise, update the header mini-cart
        return "fragments/cart-mini :: mini";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable("id") long id,
                         @RequestParam(name = "qty") int qty,
                         HttpSession session,
                         Model model,
                         HttpServletRequest request) {
        if (qty < 0) qty = 0; // 0 means remove
        Cart cart = getCart(session);
        if (qty == 0) {
            cart.remove(id);
        } else {
            cart.updateQty(id, qty);
        }
        model.addAttribute("cart", cart);

        if (!"true".equals(request.getHeader("HX-Request"))) {
            return "redirect:/cart";
        }
        // HTMX: respond based on target
        String hxTarget = request.getHeader("HX-Target");
        if ("cart-body".equals(hxTarget)) {
            // update the main cart area
            return "cart :: cart-body";
        }
        // otherwise, update the header mini-cart
        return "fragments/cart-mini :: mini";
    }

    @PostMapping("/remove/{id}")
    public String remove(@PathVariable("id") long id,
                         HttpSession session,
                         Model model,
                         HttpServletRequest request) {
        getCart(session).remove(id);
        model.addAttribute("cart", getCart(session));

        if (!"true".equals(request.getHeader("HX-Request"))) {
            return "redirect:/cart";
        }
        // HTMX: respond based on target
        String hxTarget = request.getHeader("HX-Target");
        if ("cart-body".equals(hxTarget)) {
            // update the main cart area
            return "cart :: cart-body";
        }
        // otherwise, update the header mini-cart
        return "fragments/cart-mini :: mini";
    }
}
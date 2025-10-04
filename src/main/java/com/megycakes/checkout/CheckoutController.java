package com.megycakes.checkout;

import com.megycakes.cart.Cart;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CheckoutController {

    private final CheckoutService checkoutService;
    private final OrderRepository orders;
    private final Cart cart;

    public CheckoutController(CheckoutService checkoutService, OrderRepository orders, Cart cart) {
        this.checkoutService = checkoutService;
        this.orders = orders;
        this.cart = cart;
    }

    @GetMapping("/checkout")
    public String checkoutForm(Model model, RedirectAttributes ra) {
        if (cart.isEmpty()) {
            ra.addFlashAttribute("message", "Your cart is empty.");
            return "redirect:/cart";
        }
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new CheckoutForm());
        }
        model.addAttribute("cart", cart);
        return "checkout";
    }

    @PostMapping("/checkout")
    public String submitCheckout(@Valid @ModelAttribute("form") CheckoutForm form,
                                 BindingResult binding,
                                 RedirectAttributes ra) {
        if (cart.isEmpty()) {
            ra.addFlashAttribute("message", "Your cart is empty.");
            return "redirect:/cart";
        }
        if (binding.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.form", binding);
            ra.addFlashAttribute("form", form);
            return "redirect:/checkout";
        }

        Order order = checkoutService.createOrderFromCart(
                form.getName(), form.getEmail(), form.getPhone(), form.isPickup(),
                form.getAddress1(), form.getAddress2(), form.getCity(), form.getPostal(), form.getCountry(),
                cart);

        cart.clear(); // important: empty session cart after order creation

        return "redirect:/order/" + order.getOrderNumber();
    }

    @GetMapping("/order/{orderNumber}")
    public String review(@PathVariable("orderNumber") String orderNumber, Model model) {
        Order order = orders.findWithItemsByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("order", order);
        return "order-review";
    }
}
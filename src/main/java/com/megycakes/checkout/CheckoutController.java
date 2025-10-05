package com.megycakes.checkout;

import com.megycakes.cart.Cart;
import com.megycakes.mail.MailService;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.megycakes.checkout.Order;
import com.megycakes.checkout.OrderStatus;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.megycakes.mail.MailService;

@Controller
public class CheckoutController {

    private final CheckoutService checkoutService;
    private final OrderRepository orders;
    private final Cart cart;
    private final MailService mailService;

    public CheckoutController(CheckoutService checkoutService, OrderRepository orders, Cart cart, MailService mailService) {
        this.checkoutService = checkoutService;
        this.orders = orders;
        this.cart = cart;
        this.mailService = mailService;
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
        return "redirect:/order/" + order.getOrderNumber();
    }

    @GetMapping("/order/{orderNumber}")
    public String review(@PathVariable("orderNumber") String orderNumber, Model model) {
        Order order = orders.findWithItemsByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("order", order);
        return "order-review";
    }

    @PostMapping("/order/{orderNumber}/confirm")
    public String confirm(@PathVariable("orderNumber") String orderNumber, RedirectAttributes ra) {
        Order order = orders.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (order.getStatus() == OrderStatus.NEW) {
            order.setStatus(OrderStatus.CONFIRMED);
            orders.save(order);
        }

        mailService.sendOrderConfirmation(order.getOrderNumber());
        // clear the session cart after confirmation
        cart.clear();

        ra.addFlashAttribute("message", "Order confirmed! A confirmation email was sent.");
        return "redirect:/order/" + order.getOrderNumber() + "/thank-you";
    }

    @GetMapping("/order/{orderNumber}/thank-you")
    public String thankYou(@PathVariable("orderNumber") String orderNumber, Model model) {
        Order order = orders.findWithItemsByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("order", order);
        return "order-thank-you";
    }
}
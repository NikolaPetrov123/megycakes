package com.megycakes.admin;

import com.megycakes.checkout.Order;
import com.megycakes.checkout.OrderRepository;
import com.megycakes.checkout.OrderStatus;
import com.megycakes.mail.MailService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final OrderRepository orderRepository;
    private final MailService mailService;

    public AdminOrderController(OrderRepository orderRepository, MailService mailService) {
        this.orderRepository = orderRepository;
        this.mailService = mailService;
    }

    @GetMapping
    public String list(@RequestParam(name = "q", required = false) String q,
                       @RequestParam(name = "page", defaultValue = "0") int page,
                       @RequestParam(name = "size", defaultValue = "20") int size,
                       Model model) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(size, 100));
        Page<Order> orders;
        if (q != null && !q.isBlank()) {
            String term = q.trim();
            orders = orderRepository
                    .findByOrderNumberContainingIgnoreCaseOrCustomerEmailContainingIgnoreCase(term, term, pageable);
        } else {
            orders = orderRepository.findAll(pageable);
        }
        model.addAttribute("orders", orders);
        model.addAttribute("q", q);
        model.addAttribute("OrderStatus", OrderStatus.values());
        return "admin/orders";
    }

    @GetMapping("/{id}")
    public String show(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Optional<Order> opt = orderRepository.findWithItemsById(id);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("error", "Order not found");
            return "redirect:/admin/orders";
        }
        model.addAttribute("order", opt.get());
        model.addAttribute("OrderStatus", OrderStatus.values());
        return "admin/order-show";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam("status") OrderStatus status,
                               RedirectAttributes ra) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            ra.addFlashAttribute("error", "Order not found");
            return "redirect:/admin/orders";
        }
        order.setStatus(status);
        orderRepository.save(order);
        ra.addFlashAttribute("success", "Status updated to " + status);
        return "redirect:/admin/orders/" + id;
    }

    @PostMapping("/{id}/resend")
    public String resend(@PathVariable Long id, RedirectAttributes ra) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            ra.addFlashAttribute("error", "Order not found");
            return "redirect:/admin/orders";
        }
        mailService.sendOrderConfirmation(order.getOrderNumber());
        ra.addFlashAttribute("success", "Confirmation email queued to " + order.getCustomerEmail());
        return "redirect:/admin/orders/" + id;
    }
}
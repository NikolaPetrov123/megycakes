package com.megycakes.mail;

import com.megycakes.checkout.Order;
import com.megycakes.checkout.OrderRepository;
import org.thymeleaf.spring6.SpringTemplateEngine;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.lang.Nullable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class MailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final OrderRepository orderRepository;
    @Nullable
    private final TaskExecutor taskExecutor; // optional async executor

    @Value("${app.mail.from:noreply@megycakes.local}")
    private String from;

    @Value("${app.base-url:http://localhost:8081}")
    private String baseUrl;

    public MailService(JavaMailSender mailSender,
                       SpringTemplateEngine templateEngine,
                       OrderRepository orderRepository,
                       @Nullable @Qualifier("mailExecutor") TaskExecutor taskExecutor) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.orderRepository = orderRepository;
        this.taskExecutor = taskExecutor;
    }

    /**
     * Queue/Send order confirmation email. We pass only the orderNumber and
     * reload the Order with items eagerly inside the mail thread to avoid
     * LazyInitializationException.
     */
    public void sendOrderConfirmation(String orderNumber) {
        Runnable job = () -> doSendOrderConfirmation(orderNumber);
        if (taskExecutor != null) {
            taskExecutor.execute(job);
        } else {
            job.run();
        }
    }

    private void doSendOrderConfirmation(String orderNumber) {
        try {
            // Ensure items are initialized (requires a fetch-join repository method)
            Order order = orderRepository.findWithItemsByOrderNumber(orderNumber)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderNumber));

            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, "UTF-8");
            helper.setTo(order.getCustomerEmail());
            helper.setFrom(from);
            helper.setSubject("Your MegyCakes order " + order.getOrderNumber());

            Context ctx = new Context();
            ctx.setVariable("order", order);
            ctx.setVariable("baseUrl", baseUrl);

            String html = templateEngine.process("mail/order-confirmation.html", ctx);
            helper.setText(html, true); // HTML

            mailSender.send(msg);
        } catch (Exception e) {
            // log and swallow: email should not break checkout
            System.err.println("Failed to send order confirmation: " + e.getMessage());
        }
    }
}
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final OrderRepository orderRepository;
    @Nullable
    private final TaskExecutor taskExecutor; // optional async executor

    @Value("${app.mail.from:noreply@megycakes.local}")
    private String from;

    @Value("${app.mail.reply-to:}")
    private String replyTo;

    @Value("${app.mail.bcc:}")
    private String bcc;

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
        // small retry loop for transient SMTP hiccups
        final int maxAttempts = 3;
        int attempt = 0;
        Exception lastFailure = null;

        while (attempt < maxAttempts) {
            attempt++;
            try {
                // Ensure items are initialized (requires a fetch-join repository method)
                Order order = orderRepository.findWithItemsByOrderNumber(orderNumber)
                        .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderNumber));

                MimeMessage msg = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");

                helper.setTo(order.getCustomerEmail());
                helper.setFrom(from);
                // Friendlier subject line
                helper.setSubject("MegyCakes – Order " + order.getOrderNumber() + " confirmed");

                // Optional Reply-To
                if (replyTo != null && !replyTo.isBlank()) {
                    helper.setReplyTo(replyTo);
                }
                // Optional BCC to shop owner
                if (bcc != null && !bcc.isBlank()) {
                    helper.setBcc(bcc);
                }

                // Thymeleaf HTML body
                Context ctx = new Context();
                ctx.setVariable("order", order);
                ctx.setVariable("baseUrl", baseUrl);
                String html = templateEngine.process("mail/order-confirmation.html", ctx);

                // Plaintext alternative (in case HTML is blocked)
                StringBuilder text = new StringBuilder();
                text.append("Thank you for your order!\n");
                text.append("Order: ").append(order.getOrderNumber()).append('\n');
                text.append("Status: ").append(order.getStatus()).append('\n');
                if (order.isPickup()) {
                    text.append("Pickup at shop\n");
                } else {
                    text.append("Delivery to: ")
                        .append(order.getAddressLine1());
                    if (order.getAddressLine2() != null && !order.getAddressLine2().isBlank()) {
                        text.append(", ").append(order.getAddressLine2());
                    }
                    text.append("\n");
                    text.append(order.getPostalCode()).append(" ").append(order.getCity()).append("\n");
                    if (order.getRegion() != null && !order.getRegion().isBlank()) {
                        text.append(order.getRegion()).append("\n");
                    }
                    if (order.getCountry() != null && !order.getCountry().isBlank()) {
                        text.append(order.getCountry()).append("\n");
                    }
                }
                text.append('\n');
                text.append("Items:\n");
                if (order.getItems() != null) {
                    order.getItems().forEach(it -> {
                        text.append(" - ")
                            .append(it.getNameSnapshot())
                            .append(" x")
                            .append(it.getQuantity())
                            .append(" = ")
                            .append(it.getPriceCentsSnapshot()/100)
                            .append(" ")
                            .append(order.getCurrency())
                            .append('\n');
                    });
                }
                text.append('\n');
                text.append("Subtotal: ").append(order.getTotalCents() - order.getDeliveryFeeCents()).append(" ").append(order.getCurrency()).append('\n');
                if (order.getDeliveryFeeCents() > 0) {
                    text.append("Delivery: ").append(order.getDeliveryFeeCents()).append(" ").append(order.getCurrency()).append('\n');
                }
                text.append("Total: ").append(order.getTotalCents()).append(" ").append(order.getCurrency()).append('\n');
                text.append("View your order: ").append(baseUrl).append("/order/").append(order.getOrderNumber()).append('\n');

                helper.setText(text.toString(), html);

                mailSender.send(msg);
                log.info("Sent order confirmation email for {} to {} (attempt {}/{})", orderNumber, order.getCustomerEmail(), attempt, maxAttempts);
                return; // success, exit retry loop
            } catch (Exception e) {
                lastFailure = e;
                log.warn("Attempt {}/{} to send order {} failed: {}", attempt, maxAttempts, orderNumber, e.toString());
                try {
                    Thread.sleep(250L * attempt); // tiny backoff
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        // all attempts failed
        if (lastFailure != null) {
            log.error("Failed to send order confirmation for {} after {} attempts", orderNumber, maxAttempts, lastFailure);
        }
    }
}
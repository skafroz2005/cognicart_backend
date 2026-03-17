package com.cognicart.cognicart_app.controller;

import com.cognicart.cognicart_app.exception.OrderException;
import com.cognicart.cognicart_app.model.Order;
import com.cognicart.cognicart_app.repository.OrderRepository;
import com.cognicart.cognicart_app.response.ApiResponse;
import com.cognicart.cognicart_app.response.PaymentLinkResponse;
import com.cognicart.cognicart_app.service.CartService;
import com.cognicart.cognicart_app.service.OrderService;
import com.cognicart.cognicart_app.service.UserService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.razorpay.Payment;
import com.razorpay.PaymentLink;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

@RestController
@RequestMapping("/api")
public class PaymentController {

    @Value("${razorpay.api.key}")
    String apiKey;

    @Value("${razorpay.api.secret}")
    String apiSecret;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartService cartService;

    @PostMapping("/payments/{orderId}")
    public ResponseEntity<PaymentLinkResponse> createPaymentLink(@PathVariable Long orderId,
                                                                 @RequestHeader("Authorization") String jwt) throws OrderException, RazorpayException, OrderException {

        Order order = orderService.findOrderById(orderId);

        try {
            // Initialize Razorpay client with your keys
            RazorpayClient razorpay = new RazorpayClient(apiKey, apiSecret);

            // Create a JSON object with the payment link request parameters
            JSONObject paymentLinkRequest = new JSONObject();
            // Razorpay accepts amounts in paise (multiply by 100)
            paymentLinkRequest.put("amount", order.getTotalDiscountedPrice() * 100);
            paymentLinkRequest.put("currency", "INR");

            // Create a JSON object with the customer details
            JSONObject customer = new JSONObject();
            customer.put("name", order.getUser().getFirstName());
            customer.put("email", order.getUser().getEmail());
            paymentLinkRequest.put("customer", customer);

            // Create a JSON object with the notification settings
            JSONObject notify = new JSONObject();
            notify.put("sms", true);
            notify.put("email", true);
            paymentLinkRequest.put("notify", notify);

            // Set the callback URL and method
//            paymentLinkRequest.put("callback_url", "http://localhost:3000/payment/success?order_id=" + orderId);
            paymentLinkRequest.put("callback_url", "http://localhost:3000/payment?order_id=" + orderId);
            paymentLinkRequest.put("callback_method", "get");

            // Create the payment link using the paymentLink.create() method
            PaymentLink payment = razorpay.paymentLink.create(paymentLinkRequest);

            String paymentLinkId = payment.get("id");
            String paymentLinkUrl = payment.get("short_url");

            PaymentLinkResponse res = new PaymentLinkResponse();
            res.setPayment_link_id(paymentLinkId);
            res.setPayment_link_url(paymentLinkUrl);

            return new ResponseEntity<PaymentLinkResponse>(res, HttpStatus.CREATED);

        } catch (Exception e) {
            throw new RazorpayException(e.getMessage());
        }
    }

    @GetMapping("/payments")
    public ResponseEntity<ApiResponse> redirect(@RequestParam(name="payment_id") String paymentId,
                                                @RequestParam(name="order_id") Long orderId) throws OrderException, RazorpayException {

        Order order = orderService.findOrderById(orderId);
        RazorpayClient razorpay = new RazorpayClient(apiKey, apiSecret);

        try {
            Payment payment = razorpay.payments.fetch(paymentId);

            if(payment.get("status").equals("captured")) {
                order.getPaymentDetails().setPaymentId(paymentId);
                order.getPaymentDetails().setStatus("COMPLETED");
                order.setOrderStatus("PLACED");
                orderRepository.save(order);

                // --- ADD THIS NEW LINE TO CLEAR THE CART ---
                cartService.clearCart(order.getUser().getId());

                ApiResponse res = new ApiResponse();
                res.setMessage("Your order has been placed successfully.");
                res.setStatus(true);
                return new ResponseEntity<>(res, HttpStatus.ACCEPTED);
            } else {
                ApiResponse res = new ApiResponse();
                res.setMessage("Payment failed or not captured.");
                res.setStatus(false);
                return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
            }
//            return new ResponseEntity<ApiResponse>(res, HttpStatus.ACCEPTED);

        } catch (Exception e) {
            throw new RazorpayException(e.getMessage());
        }
    }
}
package com.arnav.authsystem.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import jakarta.annotation.PostConstruct;

@Service
public class StripeService {

    // these come from application.properties
    @Value("${stripe.api.secret-key}")
    private String secretKey;

    @Value("${stripe.price.id}")
    private String priceId;

    @Value("${stripe.success.url}")
    private String successUrl;

    @Value("${stripe.cancel.url}")
    private String cancelUrl;

    // runs once after the bean is built — sets the Stripe API key globally
    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }

    // ---- create a Stripe Checkout Session and return its URL ----
    public String createCheckoutSession(String username) throws StripeException {

        SessionCreateParams params = SessionCreateParams.builder() 
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION )        // one-time payment (use SUBSCRIPTION for recurring)
                .setSuccessUrl(successUrl + "&session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(cancelUrl)
                // tag the session with WHO is buying, so we know who to upgrade on return
                .putMetadata("username", username)
                .setClientReferenceId(username)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPrice(priceId)     // the price_... from Stripe
                                .setQuantity(1L)
                                .build()
                )
                .build();

        Session session = Session.create(params);
        return session.getUrl();   // the hosted Stripe checkout page URL
    }

    // ---- verify a completed session (used on success redirect) ----
    // returns the username stored in metadata IF the session was actually paid
    public String verifyAndGetUsername(String sessionId) throws StripeException {
        Session session = Session.retrieve(sessionId);
        // only treat as paid if Stripe says payment_status == "paid"
        if ("complete".equals(session.getStatus())) {
            return session.getMetadata().get("username");
        }
        return null;   // not paid → don't upgrade
    }
}
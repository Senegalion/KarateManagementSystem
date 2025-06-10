package com.karate.management.karatemanagementsystem.domain.payment.api;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PayPalController {
    @GetMapping("/success")
    public String successRedirect(Model model) {
        model.addAttribute("message", "Payment successful! Your payment has been processed.");
        return "paymentSuccess";
    }

    @GetMapping("/cancel")
    public String cancelRedirect(Model model) {
        model.addAttribute("message", "Payment cancelled. No payment has been processed.");
        return "paymentCancel";
    }
}

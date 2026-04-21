package com.estatedesk.controller;

import com.estatedesk.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/customer/auth")
public class CustomerAuthController {

    @Autowired
    CustomerRepository customerRepo;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        try {
            customerRepo.createCustomer(
                body.get("firstName"),
                body.get("lastName"),
                body.get("email"),
                body.get("password"),
                body.get("phone")
            );
            return ResponseEntity.ok(Map.of("message", "Client registered successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Unable to register customer", "error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        Map<String, Object> customer = customerRepo.login(body.get("email"), body.get("password"));
        if (customer == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid credentials"));
        }
        return ResponseEntity.ok(customer);
    }
}

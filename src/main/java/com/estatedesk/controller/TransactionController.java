package com.estatedesk.controller;

import com.estatedesk.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    TransactionRepository txnRepo;

    @PostMapping("/sale")
    public ResponseEntity<?> recordSale(@RequestBody Map<String, Object> body) {
        try {
            txnRepo.insertSale(
                    (int) body.get("listingId"),
                    (int) body.get("agentId"),
                    (int) body.get("buyerId"),
                    (String) body.get("dealDate"),
                    ((Number) body.get("finalPrice")).longValue()
            );
            return ResponseEntity.ok(Map.of("message", "Sale recorded successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/rental")
    public ResponseEntity<?> recordRental(@RequestBody Map<String, Object> body) {
        try {
            txnRepo.insertRental(
                    (int) body.get("listingId"),
                    (int) body.get("agentId"),
                    (int) body.get("tenantId"),
                    (String) body.get("rentStart"),
                    (String) body.get("rentEnd"),
                    (String) body.get("dealDate"),
                    ((Number) body.get("monthlyRent")).longValue(),
                    ((Number) body.get("securityDeposit")).longValue(),
                    (String) body.get("status")
            );
            return ResponseEntity.ok(Map.of("message", "Rental recorded successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}

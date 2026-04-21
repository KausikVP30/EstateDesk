// ─── File: src/main/java/com/estatedesk/App.java ───

package com.estatedesk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.*;

@SpringBootApplication
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    // Allow the HTML file (opened from filesystem) to call the API
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "OPTIONS");
            }
        };
    }
}


// ─── File: src/main/java/com/estatedesk/controller/AuthController.java ───

package com.estatedesk.controller;

import com.estatedesk.model.Agent;
import com.estatedesk.repository.AgentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired AgentRepository agentRepo;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        Agent agent = agentRepo.findByEmailAndPassword(email, password);
        if (agent == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials"));
        }
        return ResponseEntity.ok(agent);
    }
}


// ─── File: src/main/java/com/estatedesk/controller/ReportController.java ───

package com.estatedesk.controller;

import com.estatedesk.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired TransactionRepository txnRepo;

    /**
     * GET /api/reports/sales?agentId=1&allAgents=false&from=2023-01-01&to=2023-12-31
     *
     * Returns rows from:
     *   SELECT st.*, a.first_name, a.last_name, p.house_number, p.locality, p.city, l.bhk
     *   FROM Sale_Transactions st
     *   JOIN Agent a ON st.agent_id = a.agent_id
     *   JOIN Listing l ON st.listing_id = l.listing_id
     *   JOIN Property p ON l.property_id = p.property_id
     *   WHERE (allAgents OR st.agent_id = ?)
     *   AND (?from IS NULL OR st.deal_date >= ?from)
     *   AND (?to   IS NULL OR st.deal_date <= ?to)
     *   ORDER BY st.deal_date DESC
     */
    @GetMapping("/sales")
    public List<Map<String, Object>> salesReport(
            @RequestParam int agentId,
            @RequestParam(defaultValue = "false") boolean allAgents,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        return txnRepo.getSalesReport(agentId, allAgents, from, to);
    }

    /**
     * GET /api/reports/rentals?agentId=1&allAgents=false&from=&to=
     *
     * Returns rows from:
     *   SELECT rt.*, a.first_name, a.last_name, p.house_number, p.locality, p.city, l.bhk, l.monthly_rent
     *   FROM Rental_Transactions rt
     *   JOIN Agent a ON rt.agent_id = a.agent_id
     *   JOIN Listing l ON rt.listing_id = l.listing_id
     *   JOIN Property p ON l.property_id = p.property_id
     *   WHERE (allAgents OR rt.agent_id = ?)
     *   AND (?from IS NULL OR rt.deal_date >= ?from)
     *   AND (?to   IS NULL OR rt.deal_date <= ?to)
     *   ORDER BY rt.deal_date DESC
     */
    @GetMapping("/rentals")
    public List<Map<String, Object>> rentalReport(
            @RequestParam int agentId,
            @RequestParam(defaultValue = "false") boolean allAgents,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        return txnRepo.getRentalReport(agentId, allAgents, from, to);
    }
}


// ─── File: src/main/java/com/estatedesk/controller/TransactionController.java ───

package com.estatedesk.controller;

import com.estatedesk.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired TransactionRepository txnRepo;

    /**
     * POST /api/transactions/sale
     * Body: { listingId, agentId, finalPrice, dealDate }
     *
     * Executes:
     *   INSERT INTO Sale_Transactions (listing_id, agent_id, deal_date, final_price)
     *   VALUES (?, ?, ?, ?)
     *
     * Trigger `after_sale_insert` then sets Listing.status = 'sold' automatically.
     * Trigger `before_sale_insert` + `prevent_duplicate_sale` + `before_sale_type_check`
     * will throw SQL exceptions if invalid → caught and returned as 400.
     */
    @PostMapping("/sale")
    public ResponseEntity<?> recordSale(@RequestBody Map<String, Object> body) {
        try {
            txnRepo.insertSale(
                (int) body.get("listingId"),
                (int) body.get("agentId"),
                (String) body.get("dealDate"),
                ((Number) body.get("finalPrice")).longValue()
            );
            return ResponseEntity.ok(Map.of("message", "Sale recorded successfully"));
        } catch (Exception e) {
            // MySQL SIGNAL from trigger arrives here
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * POST /api/transactions/rental
     * Body: { listingId, agentId, rentStart, rentEnd, dealDate }
     *
     * Executes:
     *   INSERT INTO Rental_Transactions (listing_id, agent_id, rent_start, rent_end, deal_date)
     *   VALUES (?, ?, ?, ?, ?)
     *
     * Trigger `after_rent_insert` sets Listing.status = 'rented'.
     */
    @PostMapping("/rental")
    public ResponseEntity<?> recordRental(@RequestBody Map<String, Object> body) {
        try {
            txnRepo.insertRental(
                (int) body.get("listingId"),
                (int) body.get("agentId"),
                (String) body.get("rentStart"),
                (String) body.get("rentEnd"),
                (String) body.get("dealDate")
            );
            return ResponseEntity.ok(Map.of("message", "Rental recorded successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}

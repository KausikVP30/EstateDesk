package com.estatedesk.controller;

import com.estatedesk.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    TransactionRepository txnRepo;

    @GetMapping("/sales")
    public List<Map<String, Object>> salesReport(
            @RequestParam int agentId,
            @RequestParam(defaultValue = "false") boolean allAgents,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        return txnRepo.getSalesReport(agentId, allAgents, from, to);
    }

    @GetMapping("/rentals")
    public List<Map<String, Object>> rentalReport(
            @RequestParam int agentId,
            @RequestParam(defaultValue = "false") boolean allAgents,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        return txnRepo.getRentalReport(agentId, allAgents, from, to);
    }
}

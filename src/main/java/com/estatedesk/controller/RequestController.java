package com.estatedesk.controller;

import com.estatedesk.repository.CustomerRequestRepository;
import com.estatedesk.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class RequestController {

    @Autowired
    CustomerRequestRepository requestRepo;

    @Autowired
    TransactionRepository txnRepo;

    @Autowired
    JdbcTemplate jdbc;

    private Integer asInt(Object value) {
        if (value == null) {
            return null;
        }
        return ((Number) value).intValue();
    }

    private Long asLong(Object value) {
        if (value == null) {
            return null;
        }
        return ((Number) value).longValue();
    }

    private Double asDouble(Object value) {
        if (value == null) {
            return null;
        }
        return ((Number) value).doubleValue();
    }

    private String asString(Object value) {
        if (value == null) {
            return null;
        }
        return String.valueOf(value);
    }

    @GetMapping("/agents")
    public List<Map<String, Object>> activeAgents() {
        String sql = """
            SELECT agent_id, first_name, last_name, email_address, status
            FROM Agent
            WHERE status = 'active'
            ORDER BY first_name, last_name
        """;
        return jdbc.queryForList(sql);
    }

    @PostMapping("/customer/requests")
    public ResponseEntity<?> createRequest(@RequestBody Map<String, Object> body) {
        try {
            Integer clientId = asInt(body.get("clientId"));
            if (clientId == null) {
                clientId = asInt(body.get("customerId"));
            }
            requestRepo.createRequest(
                clientId,
                asString(body.get("requestType")).toUpperCase(),
                asInt(body.get("listingId")),
                asLong(body.get("offeredPrice")),
                asString(body.get("rentStart")),
                asString(body.get("rentEnd")),
                asString(body.get("targetCity")),
                asString(body.get("targetLocality")),
                asString(body.get("propertyDetails")),
                asString(body.get("preferredListingType")),
                asInt(body.get("bhk")),
                asDouble(body.get("minBudget")),
                asDouble(body.get("maxBudget")),
                asString(body.get("propertyState")),
                asString(body.get("propertyPincode")),
                asString(body.get("houseNumber")),
                asString(body.get("unitType")),
                asDouble(body.get("sizeSqft")),
                asInt(body.get("bathroomCount")),
                asInt(body.get("constructionYear")),
                asString(body.get("furnishing")),
                asDouble(body.get("price")),
                asDouble(body.get("monthlyRent")),
                asDouble(body.get("securityDeposit")),
                asDouble(body.get("maintenanceCost")),
                asString(body.get("facing"))
            );
            return ResponseEntity.ok(Map.of("message", "Request submitted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Unable to submit request", "error", e.getMessage()));
        }
    }

    @GetMapping("/customer/requests/{customerId}")
    public List<Map<String, Object>> customerRequests(@PathVariable int customerId) {
        return requestRepo.getRequestsByCustomer(customerId);
    }

    @GetMapping("/admin/requests/unassigned")
    public List<Map<String, Object>> unassignedRequests() {
        return requestRepo.getUnassignedRequests();
    }

    @PostMapping("/admin/requests/{requestId}/assign")
    public ResponseEntity<?> assignAgent(@PathVariable long requestId, @RequestBody Map<String, Object> body) {
        try {
            requestRepo.assignAgent(requestId, asInt(body.get("agentId")));
            return ResponseEntity.ok(Map.of("message", "Agent assigned"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Unable to assign agent", "error", e.getMessage()));
        }
    }

    @GetMapping("/agent/requests")
    public List<Map<String, Object>> assignedToAgent(@RequestParam int agentId) {
        return requestRepo.getAssignedRequests(agentId);
    }

    @PostMapping("/agent/requests/{requestId}/approve")
    public ResponseEntity<?> approve(@PathVariable long requestId, @RequestBody Map<String, Object> body) {
        try {
            int agentId = asInt(body.get("agentId"));
            Map<String, Object> req = requestRepo.getRequestById(requestId);
            if (req == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Request not found"));
            }

            Integer assignedAgentId = (Integer) req.get("assigned_agent_id");
            if (assignedAgentId == null || assignedAgentId != agentId) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Request not assigned to this agent"));
            }

            String type = ((String) req.get("request_type")).toUpperCase();

            if ("BUY".equals(type)) {
                Integer listingId = (Integer) req.get("listing_id");
                Integer buyerId = (Integer) req.get("client_id");
                if (listingId == null) {
                    return ResponseEntity.badRequest().body(Map.of("message", "BUY request requires listingId"));
                }
                String dealDate = asString(body.get("dealDate"));
                Number finalPriceNum = (Number) (body.get("finalPrice") != null ? body.get("finalPrice") : req.get("offered_price"));
                if (dealDate == null || finalPriceNum == null) {
                    return ResponseEntity.badRequest().body(Map.of("message", "dealDate and finalPrice are required"));
                }
                txnRepo.insertSale(listingId, agentId, buyerId, dealDate, finalPriceNum.longValue());
                requestRepo.markApproved(requestId);
                return ResponseEntity.ok(Map.of("message", "BUY request approved and sale recorded"));
            }

            if ("RENT".equals(type)) {
                Integer listingId = (Integer) req.get("listing_id");
                Integer tenantId = (Integer) req.get("client_id");
                if (listingId == null) {
                    return ResponseEntity.badRequest().body(Map.of("message", "RENT request requires listingId"));
                }
                String rentStart = asString(body.get("rentStart") != null ? body.get("rentStart") : req.get("rent_start"));
                String rentEnd = asString(body.get("rentEnd") != null ? body.get("rentEnd") : req.get("rent_end"));
                String dealDate = asString(body.get("dealDate"));
                if (rentStart == null || rentEnd == null || dealDate == null) {
                    return ResponseEntity.badRequest().body(Map.of("message", "rentStart, rentEnd, dealDate are required"));
                }

                Map<String, Object> listingFinancials = txnRepo.getListingFinancials(listingId);
                Number monthlyRentNum = (Number) listingFinancials.get("monthly_rent");
                Number securityDepositNum = (Number) listingFinancials.get("security_deposit");

                txnRepo.insertRental(
                    listingId,
                    agentId,
                    tenantId,
                    rentStart,
                    rentEnd,
                    dealDate,
                    monthlyRentNum == null ? 0L : monthlyRentNum.longValue(),
                    securityDepositNum == null ? 0L : securityDepositNum.longValue(),
                    "active"
                );
                requestRepo.markApproved(requestId);
                return ResponseEntity.ok(Map.of("message", "RENT request approved and rental recorded"));
            }

            if ("LIST".equals(type)) {
                Integer clientId = (Integer) req.get("client_id");
                int ownerId = requestRepo.upsertOwnerFromClient(clientId);
                int propertyId = requestRepo.createPropertyForRequest(ownerId, req);
                int listingId = requestRepo.createListingForRequest(propertyId, agentId, req);
                requestRepo.linkListingToRequest(requestId, listingId);
                requestRepo.markApproved(requestId);
                return ResponseEntity.ok(Map.of("message", "LIST request approved and listing created", "listingId", listingId));
            }

            return ResponseEntity.badRequest().body(Map.of("message", "Unsupported request type"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Approval failed", "error", e.getMessage()));
        }
    }

    @PostMapping("/agent/requests/{requestId}/reject")
    public ResponseEntity<?> reject(@PathVariable long requestId) {
        try {
            requestRepo.markRejected(requestId);
            return ResponseEntity.ok(Map.of("message", "Request rejected"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Reject failed", "error", e.getMessage()));
        }
    }
}

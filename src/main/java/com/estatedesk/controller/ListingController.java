package com.estatedesk.controller;

import com.estatedesk.repository.ListingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/listings")
public class ListingController {

    @Autowired
    ListingRepository listingRepo;

    private Integer asInt(Object value) {
        if (value == null) {
            return null;
        }
        return ((Number) value).intValue();
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

    @GetMapping("/active")
    public List<Map<String, Object>> activeListings() {
        return listingRepo.getActiveListings();
    }

    @GetMapping("/mine")
    public List<Map<String, Object>> myListings(@RequestParam int clientId) {
        return listingRepo.getMyListings(clientId);
    }

    @PostMapping("/create-by-client")
    public ResponseEntity<?> createByClient(@RequestBody Map<String, Object> body) {
        try {
            int listingId = listingRepo.createListingByClient(
                asInt(body.get("clientId")),
                asInt(body.get("agentId")),
                asString(body.get("state")),
                asString(body.get("city")),
                asString(body.get("locality")),
                asString(body.get("pincode")),
                asString(body.get("houseNumber")),
                asString(body.get("unitType")),
                asString(body.get("notes")),
                asString(body.get("listingType")),
                asDouble(body.get("sizeSqft")),
                asInt(body.get("bhk")),
                asInt(body.get("bathroomCount")),
                asInt(body.get("constructionYear")),
                asString(body.get("furnishing")),
                asDouble(body.get("price")),
                asDouble(body.get("monthlyRent")),
                asDouble(body.get("securityDeposit")),
                asDouble(body.get("maintenanceCost")),
                asString(body.get("facing"))
            );
            return ResponseEntity.ok(Map.of("message", "Listing created", "listingId", listingId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Could not create listing", "error", e.getMessage()));
        }
    }
}

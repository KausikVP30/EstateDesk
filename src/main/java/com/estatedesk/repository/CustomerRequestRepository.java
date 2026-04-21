package com.estatedesk.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class CustomerRequestRepository {

    @Autowired
    JdbcTemplate jdbc;

    public void createRequest(
            int clientId,
            String requestType,
            Integer listingId,
            Long offeredPrice,
            String rentStart,
            String rentEnd,
            String targetCity,
            String targetLocality,
            String propertyDetails,
            String preferredListingType,
            Integer bhk,
            Double minBudget,
            Double maxBudget,
            String propertyState,
            String propertyPincode,
            String houseNumber,
            String unitType,
            Double sizeSqft,
            Integer bathroomCount,
            Integer constructionYear,
            String furnishing,
            Double price,
            Double monthlyRent,
            Double securityDeposit,
            Double maintenanceCost,
            String facing) {

        jdbc.update(
            """
            INSERT INTO CLIENT_REQUEST (
                client_id, request_type, listing_id, offered_price,
                rent_start, rent_end, target_city, target_locality,
                property_details, preferred_listing_type, bhk,
                min_budget, max_budget,
                property_state, property_pincode, house_number, unit_type,
                size_sqft, bathroom_count, construction_year, furnishing,
                price, monthly_rent, security_deposit, maintenance_cost, facing,
                status
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'pending')
            """,
            clientId, requestType, listingId, offeredPrice,
            rentStart, rentEnd, targetCity, targetLocality,
            propertyDetails, preferredListingType, bhk,
            minBudget, maxBudget,
            propertyState, propertyPincode, houseNumber, unitType,
            sizeSqft, bathroomCount, constructionYear, furnishing,
            price, monthlyRent, securityDeposit, maintenanceCost, facing
        );
    }

    public List<Map<String, Object>> getRequestsByCustomer(int clientId) {
        String sql = """
            SELECT r.request_id, r.request_type, r.listing_id, r.offered_price,
                   r.rent_start, r.rent_end, r.target_city, r.target_locality,
                   r.property_details, r.preferred_listing_type, r.bhk,
                   r.min_budget, r.max_budget, r.status, r.created_at,
                   r.assigned_agent_id,
                   CONCAT(IFNULL(a.first_name, ''), ' ', IFNULL(a.last_name, '')) AS assigned_agent_name
            FROM CLIENT_REQUEST r
            LEFT JOIN Agent a ON r.assigned_agent_id = a.agent_id
            WHERE r.client_id = ?
            ORDER BY r.created_at DESC
        """;
        return jdbc.queryForList(sql, clientId);
    }

    public List<Map<String, Object>> getUnassignedRequests() {
        String sql = """
            SELECT r.*, c.first_name, c.last_name, c.email_address, c.phone_number
            FROM CLIENT_REQUEST r
            JOIN CLIENT c ON c.client_id = r.client_id
            WHERE r.assigned_agent_id IS NULL
              AND r.status = 'pending'
            ORDER BY r.created_at DESC
        """;
        return jdbc.queryForList(sql);
    }

    public void assignAgent(long requestId, int agentId) {
        jdbc.update(
            "UPDATE CLIENT_REQUEST SET assigned_agent_id = ?, status = 'assigned', updated_at = CURRENT_TIMESTAMP WHERE request_id = ?",
            agentId, requestId
        );
    }

    public List<Map<String, Object>> getAssignedRequests(int agentId) {
        String sql = """
            SELECT r.*, c.first_name, c.last_name, c.email_address, c.phone_number
            FROM CLIENT_REQUEST r
            JOIN CLIENT c ON c.client_id = r.client_id
            WHERE r.assigned_agent_id = ?
              AND r.status IN ('assigned', 'pending')
            ORDER BY r.created_at DESC
        """;
        return jdbc.queryForList(sql, agentId);
    }

    public Map<String, Object> getRequestById(long requestId) {
        String sql = "SELECT * FROM CLIENT_REQUEST WHERE request_id = ? LIMIT 1";
        try {
            return jdbc.queryForMap(sql, requestId);
        } catch (Exception e) {
            return null;
        }
    }

    public Map<String, Object> getClientById(int clientId) {
        String sql = "SELECT * FROM CLIENT WHERE client_id = ? LIMIT 1";
        try {
            return jdbc.queryForMap(sql, clientId);
        } catch (Exception e) {
            return null;
        }
    }

    public int upsertOwnerFromClient(int clientId) {
        jdbc.update(
            """
            INSERT INTO Owner (first_name, last_name, phone_number, email_address, password)
            SELECT first_name, last_name, phone_number, email_address, password
            FROM CLIENT WHERE client_id = ?
            ON DUPLICATE KEY UPDATE
              owner_id = LAST_INSERT_ID(owner_id),
              first_name = VALUES(first_name),
              last_name = VALUES(last_name)
            """,
            clientId
        );
        Integer ownerId = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
        if (ownerId == null || ownerId == 0) {
            ownerId = jdbc.queryForObject(
                "SELECT owner_id FROM Owner WHERE email_address = (SELECT email_address FROM CLIENT WHERE client_id = ?)",
                Integer.class,
                clientId
            );
        }
        return ownerId;
    }

    public int createPropertyForRequest(int ownerId, Map<String, Object> req) {
        jdbc.update(
            """
            INSERT INTO Property (owner_id, state, city, locality, pincode, house_number, unit_type, notes)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """,
            ownerId,
            req.get("property_state"),
            req.get("target_city"),
            req.get("target_locality"),
            req.get("property_pincode"),
            req.get("house_number"),
            req.get("unit_type"),
            req.get("property_details")
        );
        Integer propertyId = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
        return propertyId == null ? 0 : propertyId;
    }

    public int createListingForRequest(int propertyId, int agentId, Map<String, Object> req) {
        jdbc.update(
            """
            INSERT INTO Listing (
                property_id, agent_id, listing_type, size_sqft, bhk, bathroom_count,
                construction_year, furnishing, price, monthly_rent, security_deposit,
                maintenance_cost, facing, listed_date, status
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURDATE(), 'active')
            """,
            propertyId,
            agentId,
            req.get("preferred_listing_type"),
            req.get("size_sqft"),
            req.get("bhk"),
            req.get("bathroom_count"),
            req.get("construction_year"),
            req.get("furnishing"),
            req.get("price"),
            req.get("monthly_rent"),
            req.get("security_deposit"),
            req.get("maintenance_cost"),
            req.get("facing")
        );
        Integer listingId = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
        return listingId == null ? 0 : listingId;
    }

    public void linkListingToRequest(long requestId, int listingId) {
        jdbc.update("UPDATE CLIENT_REQUEST SET listing_id = ? WHERE request_id = ?", listingId, requestId);
    }

    public void markApproved(long requestId) {
        jdbc.update(
            "UPDATE CLIENT_REQUEST SET status = 'approved', updated_at = CURRENT_TIMESTAMP WHERE request_id = ?",
            requestId
        );
    }

    public void markRejected(long requestId) {
        jdbc.update(
            "UPDATE CLIENT_REQUEST SET status = 'rejected', updated_at = CURRENT_TIMESTAMP WHERE request_id = ?",
            requestId
        );
    }
}

package com.estatedesk.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class ListingRepository {

    @Autowired
    JdbcTemplate jdbc;

    public List<Map<String, Object>> getActiveListings() {
        String sql = """
            SELECT l.listing_id, l.listing_type, l.size_sqft, l.bhk, l.bathroom_count,
                   l.construction_year, l.furnishing, l.price, l.monthly_rent,
                   l.security_deposit, l.maintenance_cost, l.facing, l.listed_date,
                   p.state, p.city, p.locality, p.pincode, p.house_number, p.unit_type,
                   CONCAT(IFNULL(a.first_name, ''), ' ', IFNULL(a.last_name, '')) AS agent_name,
                   a.agent_id
            FROM Listing l
            JOIN Property p ON p.property_id = l.property_id
            LEFT JOIN Agent a ON a.agent_id = l.agent_id
            WHERE l.status = 'active'
            ORDER BY l.listed_date DESC, l.listing_id DESC
        """;
        return jdbc.queryForList(sql);
    }

    public List<Map<String, Object>> getMyListings(int clientId) {
        String sql = """
            SELECT l.listing_id, l.listing_type, l.status, l.price, l.monthly_rent,
                   p.state, p.city, p.locality, p.house_number, p.unit_type,
                   CONCAT(IFNULL(a.first_name, ''), ' ', IFNULL(a.last_name, '')) AS agent_name,
                   l.listed_date
            FROM Listing l
            JOIN Property p ON p.property_id = l.property_id
            JOIN Owner o ON o.owner_id = p.owner_id
            JOIN CLIENT c ON c.email_address = o.email_address
            LEFT JOIN Agent a ON a.agent_id = l.agent_id
            WHERE c.client_id = ?
            ORDER BY l.listing_id DESC
        """;
        return jdbc.queryForList(sql, clientId);
    }

    public int createListingByClient(
            int clientId,
            Integer agentId,
            String state,
            String city,
            String locality,
            String pincode,
            String houseNumber,
            String unitType,
            String notes,
            String listingType,
            double sizeSqft,
            int bhk,
            int bathroomCount,
            int constructionYear,
            String furnishing,
            Double price,
            Double monthlyRent,
            Double securityDeposit,
            Double maintenanceCost,
            String facing) {

        jdbc.update(
            """
            INSERT INTO Owner (first_name, last_name, phone_number, email_address, password)
            SELECT first_name, last_name, phone_number, email_address, password
            FROM CLIENT WHERE client_id = ?
            ON DUPLICATE KEY UPDATE owner_id = LAST_INSERT_ID(owner_id)
            """,
            clientId
        );

        Integer ownerId = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
        if (ownerId == null || ownerId == 0) {
            ownerId = jdbc.queryForObject(
                "SELECT o.owner_id FROM Owner o JOIN CLIENT c ON c.email_address = o.email_address WHERE c.client_id = ?",
                Integer.class,
                clientId
            );
        }

        jdbc.update(
            """
            INSERT INTO Property (owner_id, state, city, locality, pincode, house_number, unit_type, notes)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """,
            ownerId, state, city, locality, pincode, houseNumber, unitType, notes
        );

        Integer propertyId = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);

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
            listingType,
            sizeSqft,
            bhk,
            bathroomCount,
            constructionYear,
            furnishing,
            price,
            monthlyRent,
            securityDeposit,
            maintenanceCost,
            facing
        );

        Integer listingId = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
        return listingId == null ? 0 : listingId;
    }
}

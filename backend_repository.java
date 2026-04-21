// ─── File: src/main/java/com/estatedesk/repository/AgentRepository.java ───

package com.estatedesk.repository;

import com.estatedesk.model.Agent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AgentRepository {

    @Autowired JdbcTemplate jdbc;

    public Agent findByEmailAndPassword(String email, String password) {
        String sql = """
            SELECT agent_id, first_name, last_name, email_address, status
            FROM Agent
            WHERE email_address = ? AND password = ? AND status = 'active'
            LIMIT 1
        """;
        try {
            return jdbc.queryForObject(sql, (rs, n) -> {
                Agent a = new Agent();
                a.setId(rs.getInt("agent_id"));
                a.setFirstName(rs.getString("first_name"));
                a.setLastName(rs.getString("last_name"));
                a.setEmail(rs.getString("email_address"));
                a.setStatus(rs.getString("status"));
                return a;
            }, email, password);
        } catch (Exception e) {
            return null;
        }
    }
}


// ─── File: src/main/java/com/estatedesk/repository/TransactionRepository.java ───

package com.estatedesk.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class TransactionRepository {

    @Autowired JdbcTemplate jdbc;

    // ── SALES REPORT ──
    public List<Map<String, Object>> getSalesReport(
            int agentId, boolean allAgents, String from, String to) {

        StringBuilder sql = new StringBuilder("""
            SELECT st.listing_id, st.agent_id,
                   CONCAT(a.first_name, ' ', IFNULL(a.last_name,'')) AS agent_name,
                   CONCAT(p.house_number, ', ', p.locality)          AS address,
                   p.city, l.bhk,
                   st.final_price, st.deal_date
            FROM Sale_Transactions st
            JOIN Agent a   ON st.agent_id   = a.agent_id
            JOIN Listing l ON st.listing_id = l.listing_id
            JOIN Property p ON l.property_id = p.property_id
            WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();

        if (!allAgents) {
            sql.append(" AND st.agent_id = ?");
            params.add(agentId);
        }
        if (from != null && !from.isBlank()) {
            sql.append(" AND st.deal_date >= ?");
            params.add(from);
        }
        if (to != null && !to.isBlank()) {
            sql.append(" AND st.deal_date <= ?");
            params.add(to);
        }
        sql.append(" ORDER BY st.deal_date DESC");

        return jdbc.queryForList(sql.toString(), params.toArray());
    }

    // ── RENTAL REPORT ──
    public List<Map<String, Object>> getRentalReport(
            int agentId, boolean allAgents, String from, String to) {

        StringBuilder sql = new StringBuilder("""
            SELECT rt.listing_id, rt.agent_id,
                   CONCAT(a.first_name, ' ', IFNULL(a.last_name,'')) AS agent_name,
                   CONCAT(p.house_number, ', ', p.locality)          AS address,
                   p.locality, p.city, l.bhk, l.monthly_rent,
                   rt.rent_start, rt.rent_end, rt.deal_date
            FROM Rental_Transactions rt
            JOIN Agent a   ON rt.agent_id   = a.agent_id
            JOIN Listing l ON rt.listing_id = l.listing_id
            JOIN Property p ON l.property_id = p.property_id
            WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();

        if (!allAgents) {
            sql.append(" AND rt.agent_id = ?");
            params.add(agentId);
        }
        if (from != null && !from.isBlank()) {
            sql.append(" AND rt.deal_date >= ?");
            params.add(from);
        }
        if (to != null && !to.isBlank()) {
            sql.append(" AND rt.deal_date <= ?");
            params.add(to);
        }
        sql.append(" ORDER BY rt.deal_date DESC");

        return jdbc.queryForList(sql.toString(), params.toArray());
    }

    // ── INSERT SALE ──
    // Triggers that fire:
    //   BEFORE: before_sale_type_check, before_sale_insert, prevent_duplicate_sale
    //   AFTER:  after_sale_insert  (sets listing status = 'sold')
    public void insertSale(int listingId, int agentId, String dealDate, long finalPrice) {
        jdbc.update(
            "INSERT INTO Sale_Transactions (listing_id, agent_id, deal_date, final_price) VALUES (?,?,?,?)",
            listingId, agentId, dealDate, finalPrice
        );
    }

    // ── INSERT RENTAL ──
    // Triggers that fire:
    //   BEFORE: before_rent_type_check, before_rent_insert, before_rent_dates
    //   AFTER:  after_rent_insert  (sets listing status = 'rented')
    public void insertRental(int listingId, int agentId, String rentStart, String rentEnd, String dealDate) {
        jdbc.update(
            "INSERT INTO Rental_Transactions (listing_id, agent_id, rent_start, rent_end, deal_date) VALUES (?,?,?,?,?)",
            listingId, agentId, rentStart, rentEnd, dealDate
        );
    }
}


// ─── File: src/main/java/com/estatedesk/model/Agent.java ───

package com.estatedesk.model;

public class Agent {
    private int id;
    private String firstName, lastName, email, status;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String v) { this.firstName = v; }
    public String getLastName() { return lastName; }
    public void setLastName(String v) { this.lastName = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
}

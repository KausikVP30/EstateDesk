package com.estatedesk.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class TransactionRepository {

    @Autowired
    JdbcTemplate jdbc;

    public List<Map<String, Object>> getSalesReport(int agentId, boolean allAgents, String from, String to) {
        StringBuilder sql = new StringBuilder("""
            SELECT st.listing_id, st.agent_id,
                   CONCAT(a.first_name, ' ', IFNULL(a.last_name,'')) AS agent_name,
                   CONCAT(p.house_number, ', ', p.locality)          AS address,
                   p.city, l.bhk,
                   st.final_price, st.deal_date
            FROM Sale_Transactions st
            JOIN Agent a   ON st.agent_id = a.agent_id
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

    public List<Map<String, Object>> getRentalReport(int agentId, boolean allAgents, String from, String to) {
        StringBuilder sql = new StringBuilder("""
            SELECT rt.listing_id, rt.agent_id,
                   CONCAT(a.first_name, ' ', IFNULL(a.last_name,'')) AS agent_name,
                   CONCAT(p.house_number, ', ', p.locality)          AS address,
                   p.locality, p.city, l.bhk, rt.monthly_rent,
                   rt.rent_start, rt.rent_end, rt.deal_date
            FROM Rental_Transactions rt
            JOIN Agent a   ON rt.agent_id = a.agent_id
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

    public void insertSale(int listingId, int agentId, int buyerId, String dealDate, long finalPrice) {
        jdbc.update(
            "INSERT INTO Sale_Transactions (listing_id, agent_id, buyer_id, deal_date, final_price) VALUES (?,?,?,?,?)",
            listingId, agentId, buyerId, dealDate, finalPrice
        );
    }

    public void insertRental(
            int listingId,
            int agentId,
            int tenantId,
            String rentStart,
            String rentEnd,
            String dealDate,
            long monthlyRent,
            long securityDeposit,
            String status) {
        jdbc.update(
            "INSERT INTO Rental_Transactions (listing_id, agent_id, tenant_id, rent_start, rent_end, deal_date, monthly_rent, security_deposit, status) VALUES (?,?,?,?,?,?,?,?,?)",
            listingId, agentId, tenantId, rentStart, rentEnd, dealDate, monthlyRent, securityDeposit, status
        );
    }

    public Map<String, Object> getListingFinancials(int listingId) {
        String sql = "SELECT monthly_rent, security_deposit FROM Listing WHERE listing_id = ? LIMIT 1";
        return jdbc.queryForMap(sql, listingId);
    }
}

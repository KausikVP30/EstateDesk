package com.estatedesk.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class CustomerRepository {

    @Autowired
    JdbcTemplate jdbc;

    public void createCustomer(String firstName, String lastName, String email, String password, String phone) {
        jdbc.update(
            "INSERT INTO CLIENT (first_name, last_name, email_address, password, phone_number) VALUES (?, ?, ?, ?, ?)",
            firstName, lastName, email, password, phone
        );
    }

    public Map<String, Object> login(String email, String password) {
        String sql = """
            SELECT client_id, first_name, last_name, email_address, phone_number
            FROM CLIENT
            WHERE email_address = ? AND password = ?
            LIMIT 1
        """;
        try {
            return jdbc.queryForMap(sql, email, password);
        } catch (Exception e) {
            return null;
        }
    }
}

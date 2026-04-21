package com.estatedesk.repository;

import com.estatedesk.model.Agent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AgentRepository {

    @Autowired
    JdbcTemplate jdbc;

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

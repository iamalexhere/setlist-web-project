package com.tubes.setlist.Events;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcEventsRepository implements EventsRepository {
    
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public List<Events> showAllEvents() {
        String sql = "SELECT * FROM events";
        List<Events> events = jdbcTemplate.query(sql, this::mapRowToEvents);
        return events;
    }

    private Events mapRowToEvents(ResultSet resultSet, int rowNum) throws SQLException {
        return new Events (
            resultSet.getInt("id_event"),
            resultSet.getInt("id_venue"),
            resultSet.getString("event_name"),
            resultSet.getString("event_date")
        );
    }

    
}

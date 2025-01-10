package com.tubes.setlist.Admin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.tubes.setlist.Admin.Model.DateNum;
import com.tubes.setlist.Admin.Model.StringNum;

@Repository
public class JdbcAdminRepository implements AdminRepository{
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public Integer getTotalEdit(){
        String sql = """
                SELECT COUNT(*) AS "edit made"
                FROM edits;
                """;
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    @Override
    public List<StringNum> getUserMostEdit(){
        String sql = """
                SELECT username AS name, activities AS num
                FROM users
                    LEFT JOIN (
                    SELECT id_user, COUNT(*) AS activities
                    FROM edits
                    GROUP BY id_user
                    ) AS countEdit
                    ON users.id_user = countEdit.id_user
                ORDER BY activities DESC
                LIMIT 10;
                """;
        return jdbcTemplate.query(sql, this::mapRowToStringNum);
    }

    @Override
    public List<StringNum> getArtistMostPopular(){
        String sql = """
                SELECT artist_name AS name, popular AS num
                FROM artists
                    LEFT JOIN (
                    SELECT id_artist, COUNT(*) AS popular
                    FROM setlists 
                    GROUP BY id_artist
                    ) AS countEdit
                    ON artists.id_artist = countEdit.id_artist
                ORDER BY popular DESC
                LIMIT 10;
                """;
        return jdbcTemplate.query(sql, this::mapRowToStringNum);
    }

    @Override
    public List<DateNum> getMonthlySetlistInsert(){
        String sql = """
                SELECT DATE_TRUNC('month', date_insert) AS date, COUNT(*) AS num
                FROM (
                    SELECT id_setlist, MIN(date_added) AS date_insert
                    FROM edits
                    GROUP BY id_setlist) AS setlist_inserts
                GROUP BY date
                ORDER BY date DESC
                """;
        return jdbcTemplate.query(sql, this::mapRowToDateNum);
    }

    private StringNum mapRowToStringNum(ResultSet resultSet, int rowNum) throws SQLException {
        return new StringNum (
            resultSet.getString("name"),
            resultSet.getLong("num")
        );
    }

    private DateNum mapRowToDateNum(ResultSet resultSet, int rowNum) throws SQLException {
        return new DateNum (
            resultSet.getTimestamp("date"),
            resultSet.getLong("num")
        );
    }

}

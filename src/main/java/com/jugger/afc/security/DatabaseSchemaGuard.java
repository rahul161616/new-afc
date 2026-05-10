package com.jugger.afc.security;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class DatabaseSchemaGuard implements CommandLineRunner {
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public DatabaseSchemaGuard(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws SQLException {
        if (!isPostgreSql()) {
            log.info("Skipping baseline database schema guard for non-PostgreSQL database");
            return;
        }

        log.info("Checking baseline database schema");
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id UUID PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    phone VARCHAR(255),
                    email VARCHAR(150),
                    password_hash VARCHAR(255),
                    role VARCHAR(30) NOT NULL,
                    leader_application_status VARCHAR(30),
                    leader_application_requested_at TIMESTAMP WITH TIME ZONE,
                    leader_application_reviewed_at TIMESTAMP WITH TIME ZONE,
                    leader_application_reviewed_by UUID,
                    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                    deleted_at TIMESTAMP WITH TIME ZONE
                )
                """);
        jdbcTemplate.execute("CREATE UNIQUE INDEX IF NOT EXISTS uk_users_email ON users (email)");

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS futsal_groups (
                    id UUID PRIMARY KEY,
                    name VARCHAR(120) NOT NULL,
                    created_by UUID NOT NULL,
                    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                    deleted_at TIMESTAMP WITH TIME ZONE
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS group_members (
                    id UUID PRIMARY KEY,
                    group_id UUID NOT NULL,
                    user_id UUID NOT NULL,
                    role VARCHAR(30) NOT NULL,
                    joined_at TIMESTAMP WITH TIME ZONE NOT NULL,
                    is_active BOOLEAN NOT NULL DEFAULT false,
                    status VARCHAR(30) NOT NULL DEFAULT 'APPROVED'
                )
                """);
        jdbcTemplate.execute("CREATE UNIQUE INDEX IF NOT EXISTS uk_group_members_group_user ON group_members (group_id, user_id)");

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS venues (
                    id UUID PRIMARY KEY,
                    group_id UUID,
                    name VARCHAR(150) NOT NULL,
                    address VARCHAR(255),
                    map_url VARCHAR(255),
                    latitude DECIMAL(10,7),
                    longitude DECIMAL(10,7),
                    is_active BOOLEAN NOT NULL,
                    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                    deleted_at TIMESTAMP WITH TIME ZONE
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS futsal_events (
                    id UUID PRIMARY KEY,
                    group_id UUID NOT NULL,
                    title VARCHAR(255) NOT NULL,
                    description VARCHAR(255),
                    venue_id UUID NOT NULL,
                    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
                    end_time TIMESTAMP WITH TIME ZONE NOT NULL,
                    max_players INT NOT NULL,
                    required_players INT NOT NULL,
                    status VARCHAR(255) NOT NULL,
                    created_by UUID NOT NULL,
                    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                    deleted_at TIMESTAMP WITH TIME ZONE
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS futsal_event_venues (
                    event_id UUID NOT NULL,
                    venue_id UUID NOT NULL,
                    PRIMARY KEY (event_id, venue_id)
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS event_responses (
                    id UUID PRIMARY KEY,
                    event_id UUID NOT NULL,
                    user_id UUID NOT NULL,
                    response_status VARCHAR(30) NOT NULL,
                    note VARCHAR(255),
                    drop_reason VARCHAR(255),
                    waitlist_position INT,
                    joined_waitlist_at TIMESTAMP WITH TIME ZONE,
                    responded_at TIMESTAMP WITH TIME ZONE NOT NULL,
                    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
                )
                """);
        jdbcTemplate.execute("CREATE UNIQUE INDEX IF NOT EXISTS uk_event_responses_event_user ON event_responses (event_id, user_id)");

        createForeignKeyIfMissing("fk_group_members_group", "group_members", "group_id", "futsal_groups", "id");
        createForeignKeyIfMissing("fk_group_members_user", "group_members", "user_id", "users", "id");
        createForeignKeyIfMissing("fk_venues_group", "venues", "group_id", "futsal_groups", "id");
        createForeignKeyIfMissing("fk_futsal_events_group", "futsal_events", "group_id", "futsal_groups", "id");
        createForeignKeyIfMissing("fk_futsal_events_venue", "futsal_events", "venue_id", "venues", "id");
        createForeignKeyIfMissing("fk_futsal_events_creator", "futsal_events", "created_by", "users", "id");
        createForeignKeyIfMissing("fk_event_venues_event", "futsal_event_venues", "event_id", "futsal_events", "id");
        createForeignKeyIfMissing("fk_event_venues_venue", "futsal_event_venues", "venue_id", "venues", "id");
        createForeignKeyIfMissing("fk_event_responses_event", "event_responses", "event_id", "futsal_events", "id");
        createForeignKeyIfMissing("fk_event_responses_user", "event_responses", "user_id", "users", "id");

        jdbcTemplate.update("""
                INSERT INTO futsal_event_venues (event_id, venue_id)
                SELECT id, venue_id
                FROM futsal_events
                WHERE NOT EXISTS (
                    SELECT 1
                    FROM futsal_event_venues
                    WHERE futsal_event_venues.event_id = futsal_events.id
                      AND futsal_event_venues.venue_id = futsal_events.venue_id
                )
                """);

        log.info("Baseline database schema is ready");
    }

    private boolean isPostgreSql() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return connection.getMetaData().getDatabaseProductName().toLowerCase().contains("postgresql");
        }
    }

    private void createForeignKeyIfMissing(
            String constraintName,
            String tableName,
            String columnName,
            String referencedTableName,
            String referencedColumnName
    ) {
        Integer existingCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pg_constraint WHERE conname = ?",
                Integer.class,
                constraintName
        );
        if (existingCount != null && existingCount > 0) {
            return;
        }

        jdbcTemplate.execute(String.format(
                "ALTER TABLE %s ADD CONSTRAINT %s FOREIGN KEY (%s) REFERENCES %s (%s)",
                tableName,
                constraintName,
                columnName,
                referencedTableName,
                referencedColumnName
        ));
    }
}

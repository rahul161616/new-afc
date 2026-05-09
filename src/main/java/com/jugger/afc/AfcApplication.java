package com.jugger.afc;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AfcApplication {

	private static final Logger log = LoggerFactory.getLogger(AfcApplication.class);

	public static void main(String[] args) {
		normalizeRenderDatabaseUrl();
		SpringApplication.run(AfcApplication.class, args);
	}

	private static void normalizeRenderDatabaseUrl() {
		String databaseUrl = System.getenv("DATABASE_URL");
		if (databaseUrl == null || databaseUrl.isBlank()) {
			log.warn("DATABASE_URL is not set; using datasource defaults from application properties");
			return;
		}

		if (databaseUrl.startsWith("jdbc:")) {
			log.info("DATABASE_URL is already in JDBC format");
			return;
		}

		if (!databaseUrl.startsWith("postgres://") && !databaseUrl.startsWith("postgresql://")) {
			log.warn("DATABASE_URL uses an unsupported scheme; datasource startup may fail");
			return;
		}

		URI uri = URI.create(databaseUrl);
		StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://")
				.append(uri.getHost())
				.append(uri.getPort() == -1 ? ":5432" : ":" + uri.getPort())
				.append(uri.getRawPath() == null ? "" : uri.getRawPath());

		if (uri.getRawQuery() != null && !uri.getRawQuery().isBlank()) {
			jdbcUrl.append("?").append(uri.getRawQuery());
		}

		System.setProperty("spring.datasource.url", jdbcUrl.toString());
		log.info(
				"Converted DATABASE_URL for PostgreSQL host={} port={} database={}",
				uri.getHost(),
				uri.getPort() == -1 ? 5432 : uri.getPort(),
				uri.getPath() == null ? "" : uri.getPath().replaceFirst("^/", "")
		);

		String userInfo = uri.getRawUserInfo();
		if (userInfo == null || userInfo.isBlank()) {
			log.warn("DATABASE_URL does not include database credentials");
			return;
		}

		int separator = userInfo.indexOf(':');
		if (separator < 0) {
			System.setProperty("spring.datasource.username", decode(userInfo));
			return;
		}

		System.setProperty("spring.datasource.username", decode(userInfo.substring(0, separator)));
		System.setProperty("spring.datasource.password", decode(userInfo.substring(separator + 1)));
		log.info("Loaded database credentials from DATABASE_URL user info");
	}

	private static String decode(String value) {
		return URLDecoder.decode(value, StandardCharsets.UTF_8);
	}

}

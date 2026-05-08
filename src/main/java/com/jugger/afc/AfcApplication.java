package com.jugger.afc;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AfcApplication {

	public static void main(String[] args) {
		normalizeRenderDatabaseUrl();
		SpringApplication.run(AfcApplication.class, args);
	}

	private static void normalizeRenderDatabaseUrl() {
		String databaseUrl = System.getenv("DATABASE_URL");
		if (databaseUrl == null || databaseUrl.isBlank() || databaseUrl.startsWith("jdbc:")) {
			return;
		}

		if (!databaseUrl.startsWith("postgres://") && !databaseUrl.startsWith("postgresql://")) {
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

		String userInfo = uri.getRawUserInfo();
		if (userInfo == null || userInfo.isBlank()) {
			return;
		}

		int separator = userInfo.indexOf(':');
		if (separator < 0) {
			System.setProperty("spring.datasource.username", decode(userInfo));
			return;
		}

		System.setProperty("spring.datasource.username", decode(userInfo.substring(0, separator)));
		System.setProperty("spring.datasource.password", decode(userInfo.substring(separator + 1)));
	}

	private static String decode(String value) {
		return URLDecoder.decode(value, StandardCharsets.UTF_8);
	}

}

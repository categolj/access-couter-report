package org.example.hellospringbatch;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Iterator;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.batch.item.ItemReader;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

public class CounterItemReader implements ItemReader<JsonNode> {

	private String cursor = null;

	private Iterator<JsonNode> iterator;

	private final RestClient restClient;

	private final LocalDate reportStartDate;

	public CounterItemReader(RestClient.Builder restClient, LocalDate reportStartDate) {
		this.restClient = restClient.build();
		this.reportStartDate = reportStartDate;
	}

	Iterator<JsonNode> loadItems() {
		OffsetDateTime from = OffsetDateTime.of(this.reportStartDate, LocalTime.of(0, 0, 0, 0), ZoneOffset.ofHours(9));
		JsonNode body = this.restClient.get().uri("https://lognroll.fly.dev/api/logs", b -> {
			UriBuilder builder = b.queryParam("query", "/api/counter kind=server -bot")
				.queryParam("size", 200)
				.queryParam("from", from.toInstant())
				.queryParam("to", from.plusWeeks(1).toInstant());
			if (StringUtils.hasText(cursor)) {
				return builder.queryParam("cursor", cursor).build();
			}
			else {
				return builder.build();
			}
		}).retrieve().body(JsonNode.class);
		return Objects.requireNonNull(body).get("logs").iterator();
	}

	@Override
	public JsonNode read() {
		if (this.iterator == null) {
			this.iterator = loadItems();
			if (!this.iterator.hasNext()) {
				return null;
			}
		}
		if (this.iterator.hasNext()) {
			JsonNode next = this.iterator.next();
			this.cursor = "%s,%s".formatted(next.get("timestamp").asText(), next.get("observedTimestamp").asText());
			return next;
		}
		else {
			this.iterator = null;
			return this.read();
		}
	}

}

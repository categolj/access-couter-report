package org.example.hellospringbatch;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.hellospringbatch.entry.Entry;
import org.example.hellospringbatch.entry.FrontMatter;
import org.example.hellospringbatch.github.Commit;
import org.example.hellospringbatch.github.Committer;
import org.example.hellospringbatch.github.CreateContentRequest;
import org.example.hellospringbatch.github.CreateContentRequestBuilder;
import org.example.hellospringbatch.github.CreateContentRequestBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import static java.util.stream.Collectors.toList;

public class CounterItemWriter implements ItemWriter<CounterItem> {

	private final RestClient githubClient;

	private final RestClient entryClient;

	private final LocalDate reportStartDate;

	private static final Logger logger = LoggerFactory.getLogger(CounterItemWriter.class);

	public CounterItemWriter(RestClient.Builder gitHubClientBuilder, RestClient.Builder entryClientBuilder,
			LocalDate reportStartDate) {
		this.githubClient = gitHubClientBuilder.build();
		this.entryClient = entryClientBuilder.build();
		this.reportStartDate = reportStartDate;
	}

	@Override
	public void write(Chunk<? extends CounterItem> chunk) throws Exception {
		logger.info("access_count: {}", chunk.getItems().size());
		Map<String, String> fromTo = Map.of("from", this.reportStartDate.toString(), "to",
				this.reportStartDate.plusDays(6).toString());
		List<? extends CounterItem> items = chunk.getItems();
		Map<OffsetDateTime, ? extends List<? extends CounterItem>> countersByDateTime = items.stream()
			.collect(Collectors.groupingBy(CounterItem::date, TreeMap::new, toList()));
		Map<Integer, ? extends List<? extends CounterItem>> countersByEntryId = items.stream()
			.collect(Collectors.groupingBy(CounterItem::entryId, TreeMap::new, toList()));
		Set<Integer> entryIds = countersByEntryId.keySet();
		List<Entry> entries = StreamSupport
			.stream(Objects.requireNonNull(this.entryClient.post()
				.uri("/graphql")
				.contentType(MediaType.APPLICATION_JSON)
				.body("""
						{
						    "query": "query getEntries($first: Int, $after: String, $tenantId: String, $entryIds: [ID]) { getEntries(first: $first, after: $after, tenantId: $tenantId, entryIds: $entryIds) { edges { node { entryId frontMatter { title } } } pageInfo { endCursor } } }",
						    "variables": {
						      "entryIds": %s
						    }
						  }
						"""
					.formatted(entryIds))
				.retrieve()
				.body(JsonNode.class)).get("data").get("getEntries").get("edges").spliterator(), false)
			.map(node -> node.get("node"))
			.map(node -> new Entry(node.get("entryId").asInt(),
					new FrontMatter(node.get("frontMatter").get("title").asText())))
			.toList();
		Map<Integer, String> titleMap = entries.stream().collect(Collectors.toMap(Entry::entryId, Entry::title));
		String countersByDateTimeCsv = "date,access\r\n" + countersByDateTime.entrySet()
			.stream()
			.map(entry -> "%s,%d".formatted(entry.getKey(), entry.getValue().size()))
			.collect(Collectors.joining("\r\n"));
		String countersByEntryIdCsv = "entryId,title,access\r\n" + countersByEntryId.entrySet()
			.stream()
			.sorted(Comparator.comparingInt(value -> -value.getValue().size()))
			.map(entry -> "%s,%s,%d".formatted(entry.getKey(), titleMap.getOrDefault(entry.getKey(), "N/A"),
					entry.getValue().size()))
			.collect(Collectors.joining("\r\n"));
		Committer committer = new Committer("making[bot]", "making[bot]@users.noreply.github.com");
		this.createOrUpdateContent("countersByDateTime.csv", countersByDateTimeCsv, fromTo, committer);
		this.createOrUpdateContent("countersByEntryId.csv", countersByEntryIdCsv, fromTo, committer);
	}

	void createOrUpdateContent(String fileName, String content, Map<String, String> fromTo, Committer committer) {
		CreateContentRequestBuilders.Optionals ccrBuilder = CreateContentRequestBuilder.createContentRequest()
			.message("Create %s (%s_%s)".formatted(fileName, fromTo.get("from"), fromTo.get("to")))
			.content(Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8)))
			.committer(committer);
		try {
			Commit lastCommit = this.githubClient.get()
				.uri("repos/categolj/access-report/contents/{from}_{to}/" + fileName, fromTo)
				.retrieve()
				.body(Commit.class);
			ccrBuilder.sha(Objects.requireNonNull(lastCommit).sha());
		}
		catch (HttpClientErrorException.NotFound notFound) {
			// ignore
		}
		this.githubClient.put()
			.uri("repos/categolj/access-report/contents/{from}_{to}/" + fileName, fromTo)
			.body(ccrBuilder.build())
			.retrieve()
			.toBodilessEntity();
	}

}

package am.ik.blog;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.batch.item.ItemProcessor;

public class CounterItemProcessor implements ItemProcessor<JsonNode, CounterItem> {

	static Pattern pattern = Pattern.compile("/entries/([0-9]+)");

	@Override
	public CounterItem process(JsonNode item) {
		Instant observedTimestamp = Instant.parse(item.get("observedTimestamp").asText());
		JsonNode refererNode = item.get("attributes").get("referer");
		if (refererNode == null) {
			return null;
		}
		String referer = refererNode.asText();
		Matcher matcher = pattern.matcher(referer);
		if (matcher.find()) {
			String entryId = matcher.group(1);
			OffsetDateTime offsetDateTime = observedTimestamp.atOffset(ZoneOffset.ofHours(9))
				.withMinute(0)
				.withSecond(0)
				.withNano(0);
			CounterItem.Lang lang = referer.endsWith("/en") ? CounterItem.Lang.EN : CounterItem.Lang.JA;
			return CounterItemBuilder.counterItem()
				.date(offsetDateTime)
				.entryId(Integer.valueOf(entryId))
				.lang(lang)
				.build();
		}
		else {
			return null;
		}
	}

}

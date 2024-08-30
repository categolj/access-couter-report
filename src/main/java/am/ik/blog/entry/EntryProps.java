package am.ik.blog.entry;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "entry")
public record EntryProps(String apiUrl) {
}

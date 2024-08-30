package am.ik.blog.lognroll;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "lognroll")
public record LognrollProps(String apiUrl, String accessToken) {
}

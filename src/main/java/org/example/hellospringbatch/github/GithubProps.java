package org.example.hellospringbatch.github;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "github")
public record GithubProps(@DefaultValue("https://api.github.com") String apiUrl, String accessToken) {
}

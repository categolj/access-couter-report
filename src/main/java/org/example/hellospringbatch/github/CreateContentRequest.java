package org.example.hellospringbatch.github;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jilt.Builder;
import org.jilt.BuilderStyle;
import org.jilt.Opt;

@Builder(style = BuilderStyle.STAGED)
public record CreateContentRequest(String message, String content,
		@JsonInclude(JsonInclude.Include.NON_EMPTY) @Opt String branch,
		@JsonInclude(JsonInclude.Include.NON_EMPTY) @Opt String sha,
		@JsonInclude(JsonInclude.Include.NON_EMPTY) @Opt Committer committer) {
}

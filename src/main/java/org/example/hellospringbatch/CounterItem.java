package org.example.hellospringbatch;

import java.time.OffsetDateTime;

import org.jilt.Builder;
import org.jilt.BuilderStyle;

@Builder(style = BuilderStyle.STAGED)
public record CounterItem(OffsetDateTime date, Integer entryId, Lang lang) {
	public enum Lang {

		JA, EN

	}
}

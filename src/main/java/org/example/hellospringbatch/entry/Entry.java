package org.example.hellospringbatch.entry;

public record Entry(Integer entryId, FrontMatter frontMatter) {

	public String title() {
		return escapeCsv(this.frontMatter.title());
	}

	/**
	 * Escapes a given string for use as a CSV column.
	 * @param input The string to be escaped.
	 * @return The escaped CSV column string.
	 */
	public static String escapeCsv(String input) {
		if (input == null) {
			return "";
		}

		boolean containsSpecialChars = input.contains(",") || input.contains("\"") || input.contains("\n");

		if (containsSpecialChars) {
			input = input.replace("\"", "\"\""); // Escape double quotes by doubling them
			return "\"" + input + "\""; // Enclose the entire string in double quotes
		}
		else {
			return input; // Return the string as is if no special characters are present
		}
	}
}

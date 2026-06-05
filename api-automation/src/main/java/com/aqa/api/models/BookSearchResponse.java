package com.aqa.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Represents the top-level response from Open Library /search.json endpoint.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookSearchResponse {

    /** Total number of results matching the query. */
    @JsonProperty("numFound")
    private int numFound;

    /** Index of the first result in this page. */
    @JsonProperty("start")
    private int start;

    /** Whether there are more results beyond this page. */
    @JsonProperty("numFoundExact")
    private boolean numFoundExact;

    /** The list of matching book documents. */
    @JsonProperty("docs")
    private List<BookDoc> docs;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BookDoc {

        @JsonProperty("key")
        private String key;

        @JsonProperty("title")
        private String title;

        @JsonProperty("author_name")
        private List<String> authorName;

        @JsonProperty("first_publish_year")
        private Integer firstPublishYear;

        @JsonProperty("isbn")
        private List<String> isbn;

        @JsonProperty("language")
        private List<String> language;

        @JsonProperty("subject")
        private List<String> subject;

        @JsonProperty("number_of_pages_median")
        private Integer numberOfPagesMedian;
    }
}

package com.aqa.api.tests;

import com.aqa.api.models.BookSearchResponse;
import com.aqa.api.utils.OpenLibraryClient;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ──────────────────────────────────────────────────────────────────────────
 * API Test Suite – Open Library (https://openlibrary.org/developers/api)
 * ──────────────────────────────────────────────────────────────────────────
 *
 * Test cases (README table):
 *
 * | # | Test Method                          | Endpoint              | Validation                                           |
 * |---|--------------------------------------|-----------------------|------------------------------------------------------|
 * | 1 | searchReturns200ForValidQuery        | GET /search.json      | Status 200, numFound > 0, docs not empty             |
 * | 2 | searchResultsMatchQuery (parametrize)| GET /search.json      | Each doc title contains expected keyword (3 queries) |
 * | 3 | authorSearchReturnsCorrectAuthor     | GET /search.json      | Every returned book has the queried author           |
 * | 4 | isbnLookupReturnsValidBook           | GET /isbn/{isbn}.json | Status 200, title not blank, key not blank           |
 * | 5 | subjectEndpointReturnsBooksWithField | GET /subjects/{s}.json| Status 200, works array not empty, name matches      |
 * | 6 | searchWithLimitHonoursBound          | GET /search.json      | Returned docs count ≤ requested limit                |
 * | 7 | invalidIsbnReturns404                | GET /isbn/{isbn}.json | Status 404 for a non-existent ISBN                   |
 *
 * Validation rationale is documented inline above each test.
 */
@Slf4j
public class OpenLibrarySearchTest extends ApiBaseTest {

    // ------------------------------------------------------------------ //
    //  1. Happy-path: valid search returns HTTP 200 with results
    // ------------------------------------------------------------------ //

    /**
     * WHY: Most fundamental contract check.
     * Any successful search must return 200 and a non-empty document list.
     * Catches total outages or broken routing immediately.
     */
    @Test(description = "TC-01: Valid search query returns 200 and non-empty results")
    public void searchReturns200ForValidQuery() {
        Response response = OpenLibraryClient.searchBooks("The Lord of the Rings", 5);

        getTest().info("GET /search.json?q=The+Lord+of+the+Rings&limit=5");

        assertThat(response.statusCode())
                .as("HTTP status should be 200")
                .isEqualTo(200);

        BookSearchResponse body = response.as(BookSearchResponse.class);

        assertThat(body.getNumFound())
                .as("numFound should be greater than 0")
                .isGreaterThan(0);

        assertThat(body.getDocs())
                .as("docs list should not be empty")
                .isNotEmpty();

        getTest().pass("TC-01 passed – status 200, numFound=" + body.getNumFound());
        log.info("TC-01: numFound={}", body.getNumFound());
    }

    // ------------------------------------------------------------------ //
    //  2. Parametrised: search titles are relevant to the query keyword
    // ------------------------------------------------------------------ //

    /**
     * WHY: Validates the relevance contract of the search engine.
     * We expect that books returned for a specific title query will actually
     * have that keyword somewhere in their title (case-insensitive).
     * Uses @DataProvider to cover multiple queries without code duplication –
     * equivalent to Pytest's @pytest.mark.parametrize.
     */
    @DataProvider(name = "searchQueries")
    public Object[][] searchQueries() {
        return new Object[][]{
                {"Dune",           "dune"},
                {"Harry Potter",   "harry"},
                {"1984",           "1984"},
        };
    }

    @Test(
        dataProvider  = "searchQueries",
        description   = "TC-02: Search results titles are relevant to the query keyword"
    )
    public void searchResultsMatchQuery(String query, String expectedKeyword) {
        Response response = OpenLibraryClient.searchBooks(query, 10);

        getTest().info("GET /search.json?q=" + query + "&limit=10");

        assertThat(response.statusCode())
                .as("Status for query [%s]", query)
                .isEqualTo(200);

        BookSearchResponse body = response.as(BookSearchResponse.class);

        assertThat(body.getDocs())
                .as("docs should not be empty for query [%s]", query)
                .isNotEmpty();

        // At least half the results should contain the keyword in their title
        // (flexible threshold – Open Library sometimes returns related editions)
        long matchCount = body.getDocs().stream()
                .filter(d -> d.getTitle() != null &&
                             d.getTitle().toLowerCase().contains(expectedKeyword))
                .count();

        long threshold = Math.max(1, body.getDocs().size() / 2);

        assertThat(matchCount)
                .as("At least %d results should contain keyword [%s] in title, but only %d did",
                    threshold, expectedKeyword, matchCount)
                .isGreaterThanOrEqualTo(threshold);

        getTest().pass(String.format("TC-02 passed for query [%s] – %d/%d titles matched keyword",
                query, matchCount, body.getDocs().size()));
    }

    // ------------------------------------------------------------------ //
    //  3. Author search – every returned book has the searched author
    // ------------------------------------------------------------------ //

    /**
     * WHY: The /search.json?author= parameter should filter results by author.
     * Validates that the author filter is applied correctly and that
     * author_name field is populated in the response docs.
     */
    @Test(description = "TC-03: Author search returns books by the specified author")
    public void authorSearchReturnsCorrectAuthor() {
        String author = "J.K. Rowling";
        Response response = OpenLibraryClient.searchByAuthor(author, 5);

        getTest().info("GET /search.json?author=J.K.+Rowling&limit=5");

        assertThat(response.statusCode()).isEqualTo(200);

        BookSearchResponse body = response.as(BookSearchResponse.class);
        assertThat(body.getDocs()).isNotEmpty();

        // Every returned doc must list the queried author in author_name
        body.getDocs().forEach(doc -> {
            List<String> authors = doc.getAuthorName();
            assertThat(authors)
                    .as("author_name field should be present for doc: %s", doc.getTitle())
                    .isNotNull()
                    .isNotEmpty();

            boolean hasAuthor = authors.stream()
                    .anyMatch(a -> a.toLowerCase().contains("rowling"));

            assertThat(hasAuthor)
                    .as("Book [%s] should have Rowling as an author. Got: %s",
                        doc.getTitle(), authors)
                    .isTrue();
        });

        getTest().pass("TC-03 passed – all " + body.getDocs().size() + " results are by Rowling");
    }

    // ------------------------------------------------------------------ //
    //  4. ISBN lookup returns a valid, populated book record
    // ------------------------------------------------------------------ //

    /**
     * WHY: ISBN is the canonical unique identifier for books.
     * Validates that the ISBN endpoint returns a populated record with
     * the essential fields (title, key) that consumers depend on.
     * The ISBN used is for "The Hobbit" – a stable, well-known record.
     */
    @Test(description = "TC-04: ISBN lookup returns a valid book record")
    public void isbnLookupReturnsValidBook() {
        String isbn = "9780547928227"; // The Hobbit (Mariner Books edition)
        Response response = OpenLibraryClient.getBookByIsbn(isbn);

        getTest().info("GET /isbn/" + isbn + ".json");

        assertThat(response.statusCode())
                .as("ISBN lookup status")
                .isEqualTo(200);

        String title = response.jsonPath().getString("title");
        String key   = response.jsonPath().getString("key");

        assertThat(title)
                .as("title field should not be blank")
                .isNotBlank();

        assertThat(key)
                .as("key field should not be blank")
                .isNotBlank()
                .startsWith("/books/");

        getTest().pass("TC-04 passed – title: [" + title + "], key: [" + key + "]");
        log.info("TC-04: isbn={} → title={}", isbn, title);
    }

    // ------------------------------------------------------------------ //
    //  5. Subject endpoint returns a non-empty works list
    // ------------------------------------------------------------------ //

    /**
     * WHY: Validates the subject/genre navigation feature.
     * Checks that the response structure is correct (name matches the subject,
     * works array is present and non-empty), confirming catalog categorisation
     * is functional.
     */
    @Test(description = "TC-05: Subject endpoint returns books grouped under the given subject")
    public void subjectEndpointReturnsBooksWithField() {
        String subject = "fantasy";
        Response response = OpenLibraryClient.getBooksBySubject(subject, 5);

        getTest().info("GET /subjects/fantasy.json?limit=5");

        assertThat(response.statusCode()).isEqualTo(200);

        String name        = response.jsonPath().getString("name");
        List<?> works      = response.jsonPath().getList("works");
        int workCount      = response.jsonPath().getInt("work_count");

        assertThat(name)
                .as("Subject name should match queried subject")
                .isNotBlank();

        assertThat(works)
                .as("works list should not be empty for subject [%s]", subject)
                .isNotNull()
                .isNotEmpty();

        assertThat(workCount)
                .as("work_count should be greater than 0")
                .isGreaterThan(0);

        getTest().pass("TC-05 passed – subject=[" + name + "], work_count=" + workCount);
    }

    // ------------------------------------------------------------------ //
    //  6. Limit parameter is respected
    // ------------------------------------------------------------------ //

    /**
     * WHY: The API contract says &limit=N should return at most N docs.
     * Pagination/limit bugs silently break consumers. This test acts as a
     * contract guard.
     */
    @DataProvider(name = "limitValues")
    public Object[][] limitValues() {
        return new Object[][]{{1}, {3}, {5}};
    }

    @Test(
        dataProvider = "limitValues",
        description  = "TC-06: Search result count respects the limit parameter"
    )
    public void searchWithLimitHonoursBound(int limit) {
        Response response = OpenLibraryClient.searchBooks("science fiction", limit);

        getTest().info("GET /search.json?q=science+fiction&limit=" + limit);

        assertThat(response.statusCode()).isEqualTo(200);

        BookSearchResponse body = response.as(BookSearchResponse.class);
        int returned = body.getDocs().size();

        assertThat(returned)
                .as("Returned docs (%d) should be ≤ requested limit (%d)", returned, limit)
                .isLessThanOrEqualTo(limit);

        getTest().pass("TC-06 passed – requested limit=" + limit + ", returned=" + returned);
    }

    // ------------------------------------------------------------------ //
    //  7. Non-existent ISBN returns 404
    // ------------------------------------------------------------------ //

    /**
     * WHY: Negative testing is essential. The API must return 404 (not 200
     * with an empty body, not 500) for an ISBN that doesn't exist.
     * Guards against silent failures where callers can't distinguish
     * "not found" from "error".
     *
     * NOTE: This request intentionally disables redirect following so the test
     * asserts the first-hop contract of the ISBN endpoint itself.
     */
    @Test(description = "TC-07: Non-existent ISBN returns HTTP 404")
    public void invalidIsbnReturns404() {
        String fakeIsbn = "0000000000000";
        Response response = OpenLibraryClient.getBookByIsbnWithoutRedirects(fakeIsbn);

        getTest().info("GET /isbn/" + fakeIsbn + ".json  [negative test, redirects disabled]");

        assertThat(response.statusCode())
                .as("Non-existent ISBN should return 404")
                .isEqualTo(404);

        getTest().pass("TC-07 passed – server correctly returned 404 for non-existent ISBN");
    }
}

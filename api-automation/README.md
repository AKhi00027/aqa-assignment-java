# API Automation – Open Library API

## Overview

REST-assured test suite targeting the **Open Library API** (`https://openlibrary.org`).  
The API is free, open-source, and requires no authentication – ideal for automated testing.

---

## Tech Stack

| Tool | Version | Purpose |
|---|---|---|
| Java | 17 | Language |
| REST-assured | 5.4.0 | HTTP client + assertions |
| TestNG | 7.9.0 | Test runner + `@DataProvider` |
| AssertJ | 3.25.3 | Fluent assertion library |
| Jackson | 2.17.0 | JSON → POJO deserialization |
| ExtentReports | 5.1.1 | HTML test report |
| Lombok | 1.18.46 | Boilerplate reduction |

---

## Project Structure

```
api-automation/
├── src/
│   ├── main/java/com/aqa/api/
│   │   ├── config/
│   │   │   └── ApiConfig.java              # Base URI + shared RequestSpec
│   │   ├── models/
│   │   │   └── BookSearchResponse.java     # POJO for /search.json response
│   │   └── utils/
│   │       └── OpenLibraryClient.java      # Thin HTTP client wrapper
│   └── test/
│       ├── java/com/aqa/api/tests/
│       │   ├── ApiBaseTest.java            # TestNG lifecycle + ExtentReports
│       │   └── OpenLibrarySearchTest.java  # All test cases
│       └── resources/
│           ├── testng.xml
│           └── logback.xml
├── test-output/ApiExtentReport.html        # HTML report (post-run)
└── pom.xml
```

---

## Test Cases

| # | Test Method | Endpoint | Validation | Why |
|---|---|---|---|---|
| TC-01 | `searchReturns200ForValidQuery` | `GET /search.json` | HTTP 200, `numFound > 0`, `docs` not empty | Fundamental contract – verifies the search engine is operational |
| TC-02 | `searchResultsMatchQuery` *(×3 via DataProvider)* | `GET /search.json` | ≥50% of returned titles contain the query keyword | Relevance contract – guards against broken relevance ranking |
| TC-03 | `authorSearchReturnsCorrectAuthor` | `GET /search.json?author=` | Every doc's `author_name` contains the queried author | Field-filter correctness – author filter must return only that author's works |
| TC-04 | `isbnLookupReturnsValidBook` | `GET /isbn/{isbn}.json` | HTTP 200, `title` not blank, `key` starts with `/books/` | ISBN uniqueness contract – canonical identifier must resolve to a complete record |
| TC-05 | `subjectEndpointReturnsBooksWithField` | `GET /subjects/{subject}.json` | HTTP 200, `works` list not empty, `work_count > 0` | Category navigation – subject taxonomy must return grouped results |
| TC-06 | `searchWithLimitHonoursBound` *(×3 via DataProvider)* | `GET /search.json?limit=N` | `docs.size() ≤ N` for limits 1, 3, 5 | Pagination contract – `limit` param must be respected by the server |
| TC-07 | `invalidIsbnReturns404` | `GET /isbn/{isbn}.json` | HTTP 404 for a non-existent ISBN | Negative test – API must clearly signal "not found", not silently fail |

### Validation Rationale Summary

- **Status codes** are checked on every test – they are the first contract a consumer relies on.
- **Field presence / non-blank** checks ensure the response is usable, not just technically valid JSON.
- **Relevance / filter correctness** (TC-02, TC-03) validates that query parameters actually influence results.
- **Limit boundary** (TC-06) is a common off-by-one regression point in paginated APIs.
- **Negative testing** (TC-07) confirms the API distinguishes "not found" from errors.

---

## `@DataProvider` Usage

`@DataProvider` is TestNG's equivalent of Pytest's `@pytest.mark.parametrize`.  
Used in TC-02 (multiple search queries) and TC-06 (multiple limit values) to avoid code duplication while maintaining full coverage:

```java
@DataProvider(name = "searchQueries")
public Object[][] searchQueries() {
    return new Object[][]{
        {"Dune",         "dune"},
        {"Harry Potter", "harry"},
        {"1984",         "1984"},
    };
}

@Test(dataProvider = "searchQueries")
public void searchResultsMatchQuery(String query, String expectedKeyword) { ... }
```

---

## Prerequisites

- Java 17+
- Maven 3.9+
- Internet access (tests call `openlibrary.org`)

---

## Running the Tests

```bash
# Run all API tests
mvn clean test

# Run in parallel (already configured in testng.xml)
mvn clean test -Dthreads=4
```

## Reports

```
test-output/ApiExtentReport.html
```

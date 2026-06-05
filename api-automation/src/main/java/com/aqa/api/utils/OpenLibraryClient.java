package com.aqa.api.utils;

import com.aqa.api.config.ApiConfig;
import io.restassured.config.RedirectConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

/**
 * Thin client wrapper for Open Library API endpoints.
 * Keeps test classes free of low-level HTTP plumbing.
 */
public class OpenLibraryClient {

    private OpenLibraryClient() {}

    /**
     * GET /search.json?q={query}&limit={limit}
     */
    public static Response searchBooks(String query, int limit) {
        return given()
                .spec(ApiConfig.baseSpec())
                .queryParam("q", query)
                .queryParam("limit", limit)
                .when()
                .get("/search.json");
    }

    /**
     * GET /search.json?author={author}&limit={limit}
     */
    public static Response searchByAuthor(String author, int limit) {
        return given()
                .spec(ApiConfig.baseSpec())
                .queryParam("author", author)
                .queryParam("limit", limit)
                .when()
                .get("/search.json");
    }

    /**
     * GET /works/{workId}.json  – retrieves full work detail
     */
    public static Response getWorkById(String workId) {
        return given()
                .spec(ApiConfig.baseSpec())
                .when()
                .get("/works/{id}.json", workId);
    }

    /**
     * GET /isbn/{isbn}.json  – lookup a book by its ISBN
     */
    public static Response getBookByIsbn(String isbn) {
        return given()
                .spec(ApiConfig.baseSpec())
                .when()
                .get("/isbn/{isbn}.json", isbn);
    }

    /**
     * GET /isbn/{isbn}.json without following redirects.
     * Useful for negative contract checks where the first-hop status matters.
     */
    public static Response getBookByIsbnWithoutRedirects(String isbn) {
        return given()
                .spec(ApiConfig.baseSpec())
                .config(RestAssuredConfig.config()
                        .redirect(RedirectConfig.redirectConfig().followRedirects(false)))
                .when()
                .get("/isbn/{isbn}.json", isbn);
    }

    /**
     * GET /subjects/{subject}.json?limit={limit}
     */
    public static Response getBooksBySubject(String subject, int limit) {
        return given()
                .spec(ApiConfig.baseSpec())
                .queryParam("limit", limit)
                .when()
                .get("/subjects/{subject}.json", subject);
    }
}

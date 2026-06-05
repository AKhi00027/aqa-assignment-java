package com.aqa.api.config;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

/**
 * Centralised RestAssured configuration for the Open Library API.
 *
 * Open Library (https://openlibrary.org/developers/api) is a free,
 * open-source, no-auth-required public API – ideal for automated testing.
 */
public class ApiConfig {

    public static final String BASE_URI = "https://openlibrary.org";

    private ApiConfig() {}

    /**
     * Shared RequestSpecification applied to every test.
     * Logs all request details at INFO level for traceability.
     */
    public static RequestSpecification baseSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(BASE_URI)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .log(LogDetail.ALL)
                .build();
    }
}

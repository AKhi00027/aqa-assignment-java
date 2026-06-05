package com.aqa.ui.config;

import org.openqa.selenium.WebDriver;

/**
 * Thread-local WebDriver store.
 * Enables safe parallel test execution without static driver references.
 */
public class DriverManager {

    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private DriverManager() {}

    public static void initDriver() {
        DRIVER.set(DriverConfig.createMobileDriver());
    }

    public static WebDriver getDriver() {
        WebDriver driver = DRIVER.get();
        if (driver == null) {
            throw new IllegalStateException(
                    "WebDriver not initialised for thread: " + Thread.currentThread().getName());
        }
        return driver;
    }

    public static void quitDriver() {
        WebDriver driver = DRIVER.get();
        if (driver != null) {
            driver.quit();
            DRIVER.remove();
        }
    }
}

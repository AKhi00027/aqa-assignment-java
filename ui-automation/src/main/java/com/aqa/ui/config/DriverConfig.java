package com.aqa.ui.config;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralised WebDriver factory.
 *
 * Mobile emulation device can be overridden via system property:
 *   -Dmobile.device="iPhone 12 Pro"
 *
 * Headless mode can be toggled via:
 *   -Dheadless=true  (default: false for local runs)
 */
public class DriverConfig {

    private static final String DEFAULT_MOBILE_DEVICE = "iPhone 12 Pro";

    private DriverConfig() {}

    public static ChromeDriver createMobileDriver() {
        WebDriverManager.chromedriver().setup();

        String device    = System.getProperty("mobile.device", DEFAULT_MOBILE_DEVICE);
        boolean headless = Boolean.parseBoolean(System.getProperty("headless", "false"));

        ChromeOptions options = buildMobileOptions(device, headless);
        return new ChromeDriver(options);
    }

    // ------------------------------------------------------------------ //
    //  Private helpers
    // ------------------------------------------------------------------ //

    private static ChromeOptions buildMobileOptions(String deviceName, boolean headless) {
        Map<String, String> mobileEmulation = new HashMap<>();
        mobileEmulation.put("deviceName", deviceName);

        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("mobileEmulation", mobileEmulation);

        // Stable flags that reduce flakiness in emulation
        options.addArguments(
                "--disable-notifications",
                "--disable-popup-blocking",
                "--disable-infobars",
                "--disable-extensions",
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--disable-gpu",
                "--window-size=390,844"    // matches iPhone 12 Pro logical resolution
        );

        if (headless) {
            options.addArguments("--headless=new");
        }

        return options;
    }
}

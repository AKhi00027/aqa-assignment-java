package com.aqa.ui.pages;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.List;

/**
 * Page Object for twitch.tv home / landing screen.
 */
@Slf4j
public class TwitchHomePage extends BasePage {

    // --- Locators --------------------------------------------------------- //
    private static final By SEARCH_ENTRYPOINT = By.cssSelector(
            "[data-a-target='nav-search-button'], " +
            "a[href='/search'], " +
            "a[href^='/search?'], " +
            "button[aria-label*='Search'], " +
            "a[aria-label*='Search']");
    private static final By COOKIE_ACCEPT_BTN = By.cssSelector("[data-a-target='consent-banner-accept']");
    private static final By MATURE_ACCEPT_BTN = By.xpath("//*[contains(text(),'Start Watching')]");

    private static final String URL            = "https://www.twitch.tv";
    private static final String SEARCH_PAGE_URL = URL + "/search";

    // --- Actions ---------------------------------------------------------- //

    public TwitchHomePage open() {
        log.info("Navigating to {}", URL);
        driver.get(URL);
        handleInitialOverlays();
        return this;
    }

    public TwitchSearchPage clickSearchIcon() {
        log.info("Opening Twitch search");
        handleInitialOverlays();

        List<WebElement> searchEntrypoints = driver.findElements(SEARCH_ENTRYPOINT);
        if (!searchEntrypoints.isEmpty()) {
            try {
                WebElement searchControl = waitForClickable(searchEntrypoints.get(0));
                log.info("Using visible search entry point");
                jsClick(searchControl);
                return new TwitchSearchPage();
            } catch (TimeoutException e) {
                log.warn("Search entry point found but not clickable, falling back to direct search URL");
            }
        } else {
            log.warn("No search entry point found on mobile header, falling back to direct search URL");
        }

        driver.get(SEARCH_PAGE_URL);
        return new TwitchSearchPage();
    }

    // ------------------------------------------------------------------ //
    //  Private helpers
    // ------------------------------------------------------------------ //

    /** Accept cookie banner and mature-content prompts that appear on first load. */
    private void handleInitialOverlays() {
        dismissIfPresent(COOKIE_ACCEPT_BTN, Duration.ofSeconds(5));
        dismissIfPresent(MATURE_ACCEPT_BTN, Duration.ofSeconds(3));
    }
}

package com.aqa.ui.pages;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;

/**
 * Page Object for an individual Twitch streamer channel page.
 *
 * Handles the following overlays that may appear:
 *  - Mature content gate        ("Start Watching")
 *  - Cookie / GDPR banner
 *  - Login/Subscribe modal      (close button)
 *  - "Turn off AdBlock" banner
 */
@Slf4j
public class TwitchStreamerPage extends BasePage {

    // --- Locators --------------------------------------------------------- //

    // Mature content
    private static final By MATURE_CONFIRM    = By.cssSelector("[data-a-target='player-overlay-mature-accept']");

    // Generic close / dismiss
    private static final By MODAL_CLOSE       = By.cssSelector(
            "[data-a-target='modal-close-button'], " +
            "button[aria-label='Close'], " +
            ".modal-close, " +
            "[data-a-target='player-overlay-click-handler']");

    // Cookie consent (may re-appear on channel pages)
    private static final By COOKIE_ACCEPT_BTN = By.cssSelector("[data-a-target='consent-banner-accept']");

    // Page loaded signal: video player or offline banner is present
    private static final By VIDEO_PLAYER      = By.cssSelector(
            "video, " +
            "[data-a-target='video-player'], " +
            ".channel-root, " +
            "[data-a-target='player-overlay-click-handler']");

    private static final By CHANNEL_USERNAME  = By.cssSelector(
            "[data-a-target='user-channel-header-item'], " +
            ".channel-header__user h1, " +
            ".channel-root__player h1");

    // --- Actions ---------------------------------------------------------- //

    /**
     * Waits for the streamer page to load fully and handles any interstitial
     * overlays that may block the video player.
     */
    public TwitchStreamerPage waitForPageLoad() {
        log.info("Waiting for streamer page to load...");

        // Wait until the DOM is settled (URL changed away from /search)
        extendedWait.until(driver ->
                !driver.getCurrentUrl().contains("/search"));

        // Handle all potential overlays in order
        handleAllOverlays();

        // Final confirmation: video player element must be present
        log.info("Waiting for video player element...");
        extendedWait.until(ExpectedConditions.presenceOfElementLocated(VIDEO_PLAYER));

        log.info("Streamer page fully loaded: {}", driver.getCurrentUrl());
        return this;
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    // ------------------------------------------------------------------ //
    //  Private helpers
    // ------------------------------------------------------------------ //

    private void handleAllOverlays() {
        Duration shortGrace = Duration.ofSeconds(5);

        // 1. Cookie banner
        dismissIfPresent(COOKIE_ACCEPT_BTN, shortGrace);

        // 2. Mature content gate
        dismissIfPresent(MATURE_CONFIRM, shortGrace);

        // 3. Generic modal close (subscribe / login prompts)
        dismissIfPresent(MODAL_CLOSE, shortGrace);

        // Small pause – let any animation finish before screenshot
        sleep(1500);
    }
}

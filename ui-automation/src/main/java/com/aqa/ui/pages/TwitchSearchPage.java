package com.aqa.ui.pages;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Page Object for Twitch search input and results.
 */
@Slf4j
public class TwitchSearchPage extends BasePage {

    // --- Locators --------------------------------------------------------- //
    private static final By SEARCH_INPUT    = By.cssSelector("input[type='search'], input[placeholder*='Search']");
    private static final By CHANNEL_CARDS   = By.xpath(
            "//section[.//*[normalize-space()='Channels']]//button[" +
            "contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'viewer')]" +
            " | " +
            "//section[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'people searching for')]" +
            "//button[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'viewer')]");

    // --- Actions ---------------------------------------------------------- //

    public TwitchSearchPage typeSearch(String query) {
        log.info("Typing search query: {}", query);
        WebElement input = waitForVisible(SEARCH_INPUT);
        input.clear();
        input.sendKeys(query);
        input.sendKeys(Keys.RETURN);
        sleep(2000); // let results settle
        return this;
    }

    /**
     * Scrolls down by ~800px for each requested scroll step,
     * waits for lazy-loaded content between each scroll.
     */
    public TwitchSearchPage scrollDown(int times) {
        for (int i = 1; i <= times; i++) {
            log.info("Scroll {} of {}", i, times);
            scrollBy(800);
            sleep(1500);
        }
        return this;
    }

    /**
     * Picks the first available streamer card in the results list.
     * Returns a {@link TwitchStreamerPage} for the selected channel.
     */
    public TwitchStreamerPage selectFirstStreamer() {
        log.info("Waiting for streamer cards to appear");
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(CHANNEL_CARDS, 0));

        List<WebElement> cards = driver.findElements(CHANNEL_CARDS);
        if (cards.isEmpty()) {
            throw new RuntimeException("No streamer cards found in search results");
        }

        WebElement firstCard = cards.get(0);
        String cardLabel = firstCard.getText().replace("\n", " | ");
        log.info("Selecting streamer card: {}", cardLabel);
        jsClick(firstCard);

        return new TwitchStreamerPage();
    }
}

package com.aqa.ui.tests;

import com.aqa.ui.pages.TwitchHomePage;
import com.aqa.ui.pages.TwitchSearchPage;
import com.aqa.ui.pages.TwitchStreamerPage;
import com.aqa.ui.utils.ExtentReportManager;
import com.aqa.ui.utils.ScreenshotUtil;
import com.aventstack.extentreports.MediaEntityBuilder;
import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Assignment Test Case – A: Web App Testing
 *
 * Steps:
 *  1. Go to Twitch
 *  2. Click the search icon
 *  3. Input "StarCraft II"
 *  4. Scroll down 2 times
 *  5. Select one streamer
 *  6. On the streamer page, wait until all is loaded and take a screenshot
 *
 * Runs in Chrome with Mobile Emulation (iPhone 12 Pro by default).
 */
@Slf4j
public class TwitchSearchTest extends BaseTest {

    private static final String SEARCH_QUERY   = "StarCraft II";
    private static final int    SCROLL_TIMES   = 2;

    @Test(description = "Search for StarCraft II, select streamer, and take screenshot")
    public void searchStarCraftIIAndCaptureStreamer() {

        // Step 1 – Open Twitch
        ExtentReportManager.getTest().info("Step 1: Navigating to Twitch");
        TwitchHomePage homePage = new TwitchHomePage().open();

        // Step 2 – Click search icon
        ExtentReportManager.getTest().info("Step 2: Clicking search icon");
        TwitchSearchPage searchPage = homePage.clickSearchIcon();

        // Step 3 – Search for StarCraft II
        ExtentReportManager.getTest().info("Step 3: Searching for " + SEARCH_QUERY);
        searchPage.typeSearch(SEARCH_QUERY);

        // Step 4 – Scroll down 2 times
        ExtentReportManager.getTest().info("Step 4: Scrolling down " + SCROLL_TIMES + " times");
        searchPage.scrollDown(SCROLL_TIMES);

        // Step 5 – Select first streamer
        ExtentReportManager.getTest().info("Step 5: Selecting first available streamer");
        TwitchStreamerPage streamerPage = searchPage.selectFirstStreamer();

        // Step 6 – Wait for full page load (handles modals/pop-ups)
        ExtentReportManager.getTest().info("Step 6: Waiting for streamer page to fully load");
        streamerPage.waitForPageLoad();

        // Capture screenshot
        String screenshotPath = ScreenshotUtil.take("twitch_streamer_starcraft2");
        Assert.assertFalse(screenshotPath.isEmpty(), "Screenshot should have been saved");

        ExtentReportManager.getTest()
                .pass("Streamer page loaded and screenshot captured",
                        MediaEntityBuilder.createScreenCaptureFromPath(screenshotPath).build());

        // Basic URL assertion to confirm we left the search results page
        String currentUrl = streamerPage.getCurrentUrl();
        log.info("Final URL: {}", currentUrl);
        Assert.assertFalse(currentUrl.contains("/search"),
                "Should be on a streamer page, not search results. Actual URL: " + currentUrl);
    }
}

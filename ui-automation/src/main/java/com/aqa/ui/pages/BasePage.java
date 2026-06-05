package com.aqa.ui.pages;

import com.aqa.ui.config.DriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Abstract base for all Page Objects.
 * Provides fluent, wait-backed helper methods so page classes stay clean.
 */
@Slf4j
public abstract class BasePage {

    protected static final Duration DEFAULT_TIMEOUT  = Duration.ofSeconds(20);
    protected static final Duration EXTENDED_TIMEOUT = Duration.ofSeconds(40);

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected WebDriverWait extendedWait;

    protected BasePage() {
        this.driver       = DriverManager.getDriver();
        this.wait         = new WebDriverWait(driver, DEFAULT_TIMEOUT);
        this.extendedWait = new WebDriverWait(driver, EXTENDED_TIMEOUT);
        PageFactory.initElements(driver, this);
    }

    // ------------------------------------------------------------------ //
    //  Element interaction helpers
    // ------------------------------------------------------------------ //

    protected WebElement waitForVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitForClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected WebElement waitForClickable(WebElement element) {
        return wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    protected void click(By locator) {
        waitForClickable(locator).click();
    }

    protected void click(WebElement element) {
        waitForClickable(element).click();
    }

    protected void sendKeys(By locator, String text) {
        WebElement el = waitForVisible(locator);
        el.clear();
        el.sendKeys(text);
    }

    protected void jsClick(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    protected void scrollBy(int pixels) {
        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, arguments[0]);", pixels);
        sleep(500);
    }

    /** Dismiss an element if present within a short grace period. */
    protected void dismissIfPresent(By locator, Duration timeout) {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, timeout);
            WebElement el = shortWait.until(ExpectedConditions.elementToBeClickable(locator));
            log.info("Dismissing overlay: {}", locator);
            jsClick(el);
        } catch (TimeoutException e) {
            log.debug("No overlay found for: {}", locator);
        }
    }

    protected boolean isElementPresent(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    protected void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}

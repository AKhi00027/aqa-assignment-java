package com.aqa.ui.utils;

import com.aqa.ui.config.DriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility for capturing and persisting screenshots.
 * Files are written to: <project-root>/screenshots/
 */
@Slf4j
public class ScreenshotUtil {

    private static final String SCREENSHOT_DIR = "screenshots";
    private static final DateTimeFormatter FMT  =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    private ScreenshotUtil() {}

    /**
     * Takes a screenshot and saves it as PNG.
     *
     * @param testName  logical label for the filename
     * @return absolute path to the saved file, or empty string on failure
     */
    public static String take(String testName) {
        try {
            ensureDirectoryExists();

            byte[] bytes = ((TakesScreenshot) DriverManager.getDriver())
                    .getScreenshotAs(OutputType.BYTES);

            String timestamp = LocalDateTime.now().format(FMT);
            String filename  = sanitise(testName) + "_" + timestamp + ".png";
            Path   target    = Paths.get(SCREENSHOT_DIR, filename);

            Files.write(target, bytes);
            log.info("Screenshot saved: {}", target.toAbsolutePath());
            return target.toAbsolutePath().toString();

        } catch (IOException e) {
            log.error("Failed to save screenshot for '{}': {}", testName, e.getMessage());
            return "";
        }
    }

    // ------------------------------------------------------------------ //

    private static void ensureDirectoryExists() throws IOException {
        Path dir = Paths.get(SCREENSHOT_DIR);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
    }

    private static String sanitise(String name) {
        return name.replaceAll("[^a-zA-Z0-9_-]", "_");
    }
}

package com.aqa.ui.tests;

import com.aqa.ui.config.DriverManager;
import com.aqa.ui.utils.ExtentReportManager;
import com.aqa.ui.utils.ScreenshotUtil;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import lombok.extern.slf4j.Slf4j;
import org.testng.ITestResult;
import org.testng.annotations.*;

/**
 * TestNG base class.
 *
 * Lifecycle:
 *   @BeforeSuite  – initialise ExtentReports
 *   @BeforeMethod – spin up a fresh WebDriver
 *   @AfterMethod  – log pass/fail + auto-screenshot on failure, quit driver
 *   @AfterSuite   – flush HTML report
 */
@Slf4j
public abstract class BaseTest {

    @BeforeSuite(alwaysRun = true)
    public void initReport() {
        ExtentReportManager.getInstance();
        log.info("ExtentReports initialised");
    }

    @BeforeMethod(alwaysRun = true)
    public void setUp(java.lang.reflect.Method method) {
        log.info("─── Starting test: {} ───", method.getName());
        DriverManager.initDriver();
        ExtentReportManager.createTest(method.getName());
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        String testName = result.getMethod().getMethodName();

        if (result.getStatus() == ITestResult.FAILURE) {
            log.error("Test FAILED: {}", testName, result.getThrowable());
            String screenshotPath = ScreenshotUtil.take(testName + "_FAILURE");
            if (!screenshotPath.isEmpty()) {
                ExtentReportManager.getTest()
                        .fail("Test failed – screenshot attached",
                                MediaEntityBuilder.createScreenCaptureFromPath(screenshotPath).build());
            }
            ExtentReportManager.getTest().fail(result.getThrowable());

        } else if (result.getStatus() == ITestResult.SUCCESS) {
            log.info("Test PASSED: {}", testName);
            ExtentReportManager.getTest().pass("Test passed");

        } else {
            log.warn("Test SKIPPED: {}", testName);
            ExtentReportManager.getTest().skip("Test skipped");
        }

        DriverManager.quitDriver();
    }

    @AfterSuite(alwaysRun = true)
    public void flushReport() {
        ExtentReportManager.flush();
        log.info("ExtentReports flushed – report available at test-output/ExtentReport.html");
    }
}

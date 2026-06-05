package com.aqa.api.tests;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import lombok.extern.slf4j.Slf4j;
import org.testng.ITestResult;
import org.testng.annotations.*;

/**
 * Base class for all API tests.
 * Manages ExtentReports lifecycle (init → create → log → flush).
 */
@Slf4j
public abstract class ApiBaseTest {

    private static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> TEST = new ThreadLocal<>();

    @BeforeSuite(alwaysRun = true)
    public void initReport() {
        ExtentSparkReporter spark = new ExtentSparkReporter("test-output/ApiExtentReport.html");
        spark.config().setTheme(Theme.DARK);
        spark.config().setDocumentTitle("API Automation Report – Open Library");
        spark.config().setReportName("Open Library API Tests");

        extent = new ExtentReports();
        extent.attachReporter(spark);
        extent.setSystemInfo("Base URI", "https://openlibrary.org");
        log.info("ExtentReports (API) initialised");
    }

    @BeforeMethod(alwaysRun = true)
    public void createTest(java.lang.reflect.Method method) {
        log.info("─── Starting API test: {} ───", method.getName());
        TEST.set(extent.createTest(method.getName()));
    }

    @AfterMethod(alwaysRun = true)
    public void logResult(ITestResult result) {
        String name = result.getMethod().getMethodName();
        switch (result.getStatus()) {
            case ITestResult.SUCCESS -> {
                log.info("PASSED: {}", name);
                TEST.get().pass("Test passed");
            }
            case ITestResult.FAILURE -> {
                log.error("FAILED: {}", name, result.getThrowable());
                TEST.get().fail(result.getThrowable());
            }
            default -> {
                log.warn("SKIPPED: {}", name);
                TEST.get().skip("Test skipped");
            }
        }
    }

    @AfterSuite(alwaysRun = true)
    public void flushReport() {
        if (extent != null) extent.flush();
        log.info("API report written to test-output/ApiExtentReport.html");
    }

    protected ExtentTest getTest() {
        return TEST.get();
    }
}

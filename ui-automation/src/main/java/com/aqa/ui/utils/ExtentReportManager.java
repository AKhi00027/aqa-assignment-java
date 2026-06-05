package com.aqa.ui.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

/**
 * Singleton wrapper for ExtentReports.
 * HTML report is written to: test-output/ExtentReport.html
 */
public class ExtentReportManager {

    private static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> TEST = new ThreadLocal<>();

    private ExtentReportManager() {}

    public static synchronized ExtentReports getInstance() {
        if (extent == null) {
            ExtentSparkReporter spark = new ExtentSparkReporter("test-output/ExtentReport.html");
            spark.config().setTheme(Theme.DARK);
            spark.config().setDocumentTitle("UI Automation Report – Twitch");
            spark.config().setReportName("Twitch Mobile Emulation Tests");

            extent = new ExtentReports();
            extent.attachReporter(spark);
            extent.setSystemInfo("Browser",  "Chrome (Mobile Emulation)");
            extent.setSystemInfo("Device",   System.getProperty("mobile.device", "iPhone 12 Pro"));
        }
        return extent;
    }

    public static void createTest(String testName) {
        TEST.set(getInstance().createTest(testName));
    }

    public static ExtentTest getTest() {
        return TEST.get();
    }

    public static void flush() {
        if (extent != null) {
            extent.flush();
        }
    }
}

# UI Automation – Twitch Mobile Emulation

## Overview

Selenium 4 test suite running in **Chrome with Mobile Emulation** (iPhone 12 Pro by default).  
Framework follows the **Page Object Model** pattern for scalability and maintainability.

---

## Tech Stack

| Tool | Version | Purpose |
|---|---|---|
| Java | 17 | Language |
| Selenium | 4.44.0 | Browser automation |
| TestNG | 7.9.0 | Test runner |
| WebDriverManager | 5.8.0 | Automatic ChromeDriver management |
| ExtentReports | 5.1.1 | HTML test report |
| Lombok | 1.18.46 | Boilerplate reduction |
| Maven | 3.9+ | Build tool |

---

## Project Structure

```
ui-automation/
├── src/
│   ├── main/java/com/aqa/ui/
│   │   ├── config/
│   │   │   ├── DriverConfig.java       # Chrome mobile emulation factory
│   │   │   └── DriverManager.java      # ThreadLocal driver (parallel-safe)
│   │   ├── pages/
│   │   │   ├── BasePage.java           # Shared Selenium helpers + waits
│   │   │   ├── TwitchHomePage.java     # Home / landing page
│   │   │   ├── TwitchSearchPage.java   # Search input + results
│   │   │   └── TwitchStreamerPage.java # Individual channel page
│   │   └── utils/
│   │       ├── ScreenshotUtil.java     # PNG capture → /screenshots
│   │       └── ExtentReportManager.java
│   └── test/
│       ├── java/com/aqa/ui/tests/
│       │   ├── BaseTest.java           # TestNG lifecycle hooks
│       │   └── TwitchSearchTest.java   # Assignment test case
│       └── resources/
│           ├── testng.xml
│           └── logback.xml
├── screenshots/                        # Auto-created at runtime
├── test-output/ExtentReport.html       # HTML report (post-run)
└── pom.xml
```

---

## Test Case

| Step | Description |
|---|---|
| 1 | Navigate to https://www.twitch.tv |
| 2 | Click the search icon |
| 3 | Type "StarCraft II" and submit |
| 4 | Scroll down 2 times |
| 5 | Select the first streamer from results |
| 6 | Wait for page to fully load, handle any blocking modal/pop-up through the framework, then capture screenshot |

### Modal / Pop-up Handling

Some streamers show an interstitial before the video becomes usable. This is handled by the framework inside `TwitchStreamerPage.waitForPageLoad()`, not by ad hoc test logic.

The framework automatically dismisses:
- Cookie / GDPR consent banners
- Mature content gates ("Start Watching")
- Subscribe / Login modals (close button)
- Generic blocking overlays before player interaction

All dismissals use a short grace timeout via `dismissIfPresent()` so they do not fail the test when absent. The page is considered ready only after overlays are handled and a player/page-ready signal is present.

---

## Prerequisites

- Java 17+
- Maven 3.9+
- Google Chrome (latest stable)

WebDriverManager downloads the matching ChromeDriver automatically – no manual setup needed.

---

## Running the Tests

```bash
# Default – visible browser, iPhone 12 Pro emulation
mvn clean test

# Headless mode (CI)
mvn clean test -Dheadless=true

# Different mobile device
mvn clean test -D"mobile.device=Pixel 5"
```

## Reports

After a run, open:
```
test-output/ExtentReport.html
```

Screenshots (pass and fail) are saved under:
```
screenshots/
```

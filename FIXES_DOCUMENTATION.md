# Fixes Documentation

This document summarizes the fixes applied to both `ui-automation` and `api-automation`, including the root cause, the change made, and the verification performed.

## 1. UI Automation Fixes

Module: `ui-automation`

### 1.1 Maven compile failure on JDK 25

#### Problem

The UI module failed during compilation with:

`java.lang.ExceptionInInitializerError: com.sun.tools.javac.code.TypeTag :: UNKNOWN`

Maven was running on JDK `25.0.2`, while the project was configured for Java `17`. The module used Lombok `1.18.32`, which was too old for the active JDK/compiler combination.

#### Fix

Updated the build configuration in:

- `ui-automation/pom.xml`
- `api-automation/pom.xml`

Changes:

- upgraded Lombok from `1.18.32` to `1.18.46`
- replaced `maven.compiler.source` / `maven.compiler.target` usage with `maven.compiler.release=17`
- configured `maven-compiler-plugin` to use:

  ` <release>${maven.compiler.release}</release> `

#### Result

The Lombok/JDK compiler crash was removed and both modules compiled successfully.

---

### 1.2 Java compile error in page object inheritance

#### Problem

`TwitchSearchPage.scrollDown(int)` returned `TwitchSearchPage`, while `BasePage.scrollDown(int)` returned `void`.

This caused a method override incompatibility:

`return type TwitchSearchPage is not compatible with void`

There was also a naming collision between:

- low-level JS scrolling helper in `BasePage`
- fluent page action in `TwitchSearchPage`

#### Fix

Updated:

- `ui-automation/src/main/java/com/aqa/ui/pages/BasePage.java`
- `ui-automation/src/main/java/com/aqa/ui/pages/TwitchSearchPage.java`

Changes:

- renamed the low-level helper from `scrollDown(int pixels)` to `scrollBy(int pixels)`
- updated `TwitchSearchPage.scrollDown(int times)` to call `scrollBy(800)`

#### Result

The compile error was resolved and the fluent API remained intact.

---

### 1.3 Twitch mobile search entry locator failure

#### Problem

The test failed waiting for the search button:

`[data-a-target='nav-search-button'], a[href='/search']`

Twitch’s current mobile UI no longer reliably exposes that old header locator.

#### Fix

Updated:

- `ui-automation/src/main/java/com/aqa/ui/pages/TwitchHomePage.java`

Changes:

- expanded the search entry selectors to try multiple mobile-compatible search controls
- added a fallback to open the search page directly:

  `https://www.twitch.tv/search`

This keeps the test working even when the mobile header DOM changes.

#### Result

The test was able to continue into the search flow instead of timing out on the home page.

---

### 1.4 Twitch search results locator failure

#### Problem

The original result selector was based on older anchor/card markup and no longer matched Twitch’s mobile search results layout.

The actual mobile results page now renders channel results primarily as buttons inside sections/shelves, not as the old anchor-based cards.

#### Fix

Updated:

- `ui-automation/src/main/java/com/aqa/ui/pages/TwitchSearchPage.java`

Changes:

- replaced the stale result selector with an XPath targeting the `Channels` section result buttons
- added a fallback path for the “People searching for...” shelf
- kept selection logic focused on visible result cards that contain viewer counts

#### Result

The test successfully selected a streamer from current Twitch mobile search results.

---

### 1.5 Selenium / Chrome CDP mismatch

#### Problem

The UI run showed warnings like:

`Unable to find CDP implementation matching 148`

The project used Selenium `4.18.1`, while the local Chrome version was `148.0.7778.181`.

#### Fix

Updated:

- `ui-automation/pom.xml`

Change:

- upgraded Selenium from `4.18.1` to `4.44.0`

This version includes `selenium-devtools-v148`, matching the installed Chrome major version.

#### Result

The CDP warning disappeared on rerun, and the UI test still passed.

---

### 1.6 UI verification performed

Verified with:

- `mvn -q -DskipTests compile` in `ui-automation`
- `mvn -Dtest=TwitchSearchTest test` in `ui-automation`

Observed result:

- test passed
- final streamer URL resolved successfully
- screenshot capture worked
- Extent report integration remained functional

---

## 2. API Automation Fixes

Module: `api-automation`

### 2.1 Failing negative ISBN test

#### Problem

The failing test was:

`OpenLibrarySearchTest.invalidIsbnReturns404`

It expected:

- `404` for a non-existent ISBN

But the actual response was:

- `200`

#### Root cause

The test used:

`9999999999999`

That value is not reliably “fake” anymore. Open Library currently responds to it with a redirect to a real book record, and Rest Assured follows that redirect, resulting in a final `200`.

#### Fix

Updated:

- `api-automation/src/test/java/com/aqa/api/tests/OpenLibrarySearchTest.java`

Changes:

- replaced the hardcoded ISBN `9999999999999` with `0000000000000`
- added an inline note explaining why arbitrary fake-looking ISBNs are unsafe for this negative test

#### Result

The test now asserts against an ISBN that currently returns `404`, which matches the intended contract check.

---

### 2.2 API verification performed

Verified with:

- direct endpoint check for:

  `https://openlibrary.org/isbn/0000000000000.json`

  Result: `404`

- targeted test run:

  `mvn -Dtest=OpenLibrarySearchTest#invalidIsbnReturns404 test`

  Result: passed

---

## 3. Report Integration Status

### UI

Extent report integration is present and working in `ui-automation`.

Current output:

- `ui-automation/test-output/ExtentReport.html`

Current behavior:

- report initialized before suite
- one Extent test created per TestNG test
- pass/fail/skip logged
- failure screenshots attached automatically
- explicit success screenshot attachment remains test-specific

### API

Extent report integration is present and working in `api-automation`.

Current output:

- `api-automation/test-output/ApiExtentReport.html`

Current behavior:

- report initialized before suite
- one Extent test created per TestNG test
- pass/fail/skip logged
- no screenshot behavior, as expected for API tests

---

## 4. Files Changed

### UI-related

- `ui-automation/pom.xml`
- `ui-automation/src/main/java/com/aqa/ui/pages/BasePage.java`
- `ui-automation/src/main/java/com/aqa/ui/pages/TwitchHomePage.java`
- `ui-automation/src/main/java/com/aqa/ui/pages/TwitchSearchPage.java`

### API-related

- `api-automation/pom.xml`
- `api-automation/src/test/java/com/aqa/api/tests/OpenLibrarySearchTest.java`

---

## 5. Recommended follow-up

The most fragile remaining API test pattern is hardcoded negative data against a live public service.

For `invalidIsbnReturns404`, the stronger long-term version is:

- disable redirect following for that request
- assert the first-hop response directly

That would make the negative test less sensitive to Open Library data changes over time.

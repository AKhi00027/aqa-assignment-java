# AQA Home Test - Java Automation Framework

This repository contains two independent Maven modules built for the assignment:

- `ui-automation`: Selenium-based UI automation against Twitch mobile web
- `api-automation`: REST-assured API automation against Open Library

Both modules use Java 17, TestNG, Maven, and ExtentReports.

## Repository Structure

```text
aqa-assignment-java/
├── ui-automation/
├── api-automation/
├── FIXES_DOCUMENTATION.md
└── README.md
```

## Modules

### UI Automation

Scope:
- Chrome mobile emulation workflow against Twitch
- Page Object Model structure
- Screenshot capture and Extent report output

Module README:
- [`ui-automation/README.md`](ui-automation/README.md)

Run:

```bash
cd ui-automation
mvn clean test
```

### API Automation

Scope:
- Open Library search, author, subject, ISBN, and negative-path checks
- Data-driven TestNG coverage
- Extent report output

Module README:
- [`api-automation/README.md`](api-automation/README.md)

Run:

```bash
cd api-automation
mvn clean test
```

## Prerequisites

- Java 17+
- Maven 3.9+
- Internet access
- Google Chrome installed for `ui-automation`

## Notes

- Fixes and verification details are documented in [`FIXES_DOCUMENTATION.md`](FIXES_DOCUMENTATION.md).
- Reports are generated under each module's `test-output/` directory after execution.

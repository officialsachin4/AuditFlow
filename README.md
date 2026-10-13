# AuditFlow Full MVP

## Run
1. Install Java 21 and Maven.
2. Open this folder in IntelliJ/VS Code.
3. Run `mvn clean spring-boot:run`.
4. Open `http://localhost:8080`.

## Current scanner
Rule-based checks for Accessibility, Security, SEO, Privacy, Performance, Technical and Mobile.

## Score
Overall score uses:
Security 20%, Performance 20%, Accessibility 15%, SEO 15%, Privacy 10%, Technical 10%, Mobile 10%.

This is a scanner MVP, not a legal compliance certification. For production, add Playwright/Lighthouse/axe-core, PostgreSQL, async jobs, authentication, multi-tenancy, full crawling, PDF reports, monitoring, rate limiting and a much larger rule catalogue.

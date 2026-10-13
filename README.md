# AuditFlow 🔍

**Automated Website Health & Compliance Monitoring**

AuditFlow is a **Spring Boot-based website auditing platform** that scans websites, identifies technical issues, and generates actionable recommendations.

### ✨ What It Checks

- 🔐 Security
- 🔎 SEO
- ♿ Accessibility
- 🔒 Privacy
- ⚡ Performance
- 🛠️ Technical Health
- 📱 Mobile Readiness

### 🚀 Key Features

- Enter any website URL and run an audit
- Automated rule-based website analysis
- Severity-based findings
- Category-wise health scores
- Overall website health score
- Detailed recommendations
- Audit history and reports
- Responsive SaaS dashboard

### 🏗️ Architecture

```text
User
 ↓
Thymeleaf UI
 ↓
Spring Boot Controller
 ↓
Website Scanner
 ↓
HTTP Client + Jsoup
 ↓
Rule Engine
 ↓
Findings & Scoring
 ↓
JPA / Database
 ↓
Audit Report
```

### 🛠️ Tech Stack

**Java 17 · Spring Boot · Spring MVC · Spring Data JPA · Hibernate · Jsoup · Thymeleaf · H2 · Maven**

### ▶️ Run Locally

```bash
git clone <your-repository-url>
cd auditflow
mvn clean spring-boot:run
```

Open **http://localhost:8080**

### 📊 Scoring

AuditFlow calculates individual scores for each category and combines them using weighted scoring to produce an overall website health score.

### 🔮 Roadmap

- 100+ audit rules
- Playwright browser scanning
- Lighthouse & axe-core integration
- PostgreSQL
- Redis & async scan workers
- Scheduled monitoring
- Email alerts
- PDF reports
- User authentication & multi-tenancy
- Production SaaS dashboard

> **Note:** The current version is an MVP based primarily on HTTP/HTML analysis and is **not a legal compliance certification**.

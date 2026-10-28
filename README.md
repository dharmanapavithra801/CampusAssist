# CampusAssist: AI-Based Student Query and Complaint Manager

A console application where students submit complaints and an AI assistant automatically
decides the **category** and **priority**. Admins track and resolve them. If the AI is
unavailable, built-in keyword rules take over, so the app never stops working.

**Tech stack:** Java 17, JDBC, MySQL, Java `HttpClient`, LLM API (Claude), Maven

## Features
- Submit a complaint (name, title, description) with input validation
- AI auto-classification into category (Academic, Hostel, Fees, Transport, Library, Infrastructure, Other) and priority (Low, Medium, High)
- Fallback to rule-based classification when the AI is unreachable or returns an unexpected answer
- View all complaints (urgent first), filter by status, view one complaint
- Admin status updates: Open, In Progress, Resolved
- Summary report using SQL `GROUP BY`

## Architecture
```
Main (menu)  ->  ComplaintService  ->  ComplaintClassifier (interface)
                       |                    |-- LlmClassifier   (calls the AI over HTTP)
                       |                    `-- KeywordClassifier (rules, used as fallback)
                       `-->  ComplaintDAO  ->  MySQL (JDBC)
```
| Layer | Package | Job |
|---|---|---|
| Model | `model` | Plain data classes and enums (`Complaint`, `Category`, `Priority`, `Status`) |
| DAO | `dao` | The only code that writes SQL |
| Service | `service` | Business rules: validate, classify, save |
| AI | `ai` | Classifier interface, LLM client, keyword fallback |
| Util / Exception | `util`, `exception` | DB connection, custom exception |

## How to run
1. Install JDK 17+, MySQL and Maven.
2. Create the database: `mysql -u root -p < sql/schema.sql`
3. Set environment variables (never put passwords or keys in code):
   ```
   export DB_URL=jdbc:mysql://localhost:3306/campusassist
   export DB_USER=root
   export DB_PASSWORD=your_password
   export ANTHROPIC_API_KEY=your_key     # optional: without it, rule-based mode is used
   ```
4. Run: `mvn compile exec:java`
5. Optional check that needs no database: compile, then run `com.campusassist.ClassifierSelfCheck` (in `src/test/java`).

## Design decisions
- **PreparedStatement everywhere** to prevent SQL injection.
- **try-with-resources** so connections always close.
- **Enums** for category, priority and status, so invalid values cannot enter the database.
- **AI output is validated** against the enums before use; the AI is never trusted blindly.
- **Prompt asks for a fixed format** (`CATEGORY|PRIORITY`) and tells the model to treat complaint text as data, which limits prompt injection.
- **Graceful fallback:** timeouts, HTTP errors, missing key or odd answers all fall back to rules.
- **API key from an environment variable**, never hardcoded.
- **Interface + constructor injection** so the AI can be swapped or faked in tests.

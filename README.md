# Customer Feedback System

A Java Swing desktop application for product feedback, backed by MySQL-compatible TiDB Cloud through MySQL Connector/J. Customers can register, sign in, browse products, submit ratings and comments, and follow administrator responses. Administrators can search and filter customer feedback, respond to or delete feedback if they are inappropriate, and review product statistics.

The desktop UI includes a database-backed dashboard, sortable feedback tables, date/rating/product filters, analytics, CSV export, connection status, and account settings. It uses only Swing and Java 8 APIs; no extra UI/chart dependency is required.

Customers can also evaluate existing feedback from **Community Feedback**. Each customer account has one vote per feedback item and may change that vote. The default verified threshold is 60%; feedback with no votes remains pending at 0%.

### Customers
- Register an account and log in
- Browse available products
- Submit a 1-5 rating and comment
- View and search their submitted feedback
- Filter feedback by product or rating
- Read administrator responses

### Administrators
- Log in using an account granted the administrator role
- View and search all customer feedback
- Filter by product or rating
- Delete feedback (its response is deleted automatically)
- Respond to feedback (a later response replaces the previous one)
- View feedback counts and average ratings per product

## Database design

Run `sql/schema.sql` to create the `feedback_db` database and its tables. The schema uses InnoDB primary and foreign keys, a unique email, a unique response per feedback item, and a 1-5 rating constraint. Feedback and response text is limited to 500 characters to match the database columns.

| Table | Why it exists | Relationships |
|---|---|---|
| `users` | Stores customer and administrator identity, unique email, role, and password hash. | One user can submit multiple feedback records. An administrator can author multiple responses. |
| `products` | Stores the products available for review. | One product can have multiple feedback records. |
| `feedback` | Stores each customer's rating, comment, and submission time. | Each row belongs to one user and one product. |
| `feedback_votes` | Stores one changeable helpful/not-helpful vote per customer and feedback item. | Foreign keys link each vote to its feedback and customer; feedback/user deletion cascades its votes. |
| `responses` | Stores the administrator's response and response time. | Each row belongs to one feedback record and one administrator. A unique feedback key allows at most one current response. |

### Community verification migration

The existing `feedback` columns already contain everything needed for credibility; vote totals are calculated from `feedback_votes` and are not stored redundantly. The project schema did not previously contain a vote table, so existing installations should apply the additive migration below. It creates the table only and does not modify or delete existing feedback:

```powershell
Get-Content sql\feedback_verification_migration.sql | mysql --host=<cluster-host> --port=4000 --user=<cluster-user> --password --ssl-mode=VERIFY_IDENTITY
```

For a local MySQL server, use the configured local host and port instead. The migration targets the existing `feedback_db` database. Before applying it, inspect any pre-existing `feedback_votes` table with `SHOW CREATE TABLE feedback_votes;`; if one exists, verify that it has the expected unique `(feedback_id, voter_id)` key and both foreign keys before deciding whether the `CREATE TABLE IF NOT EXISTS` migration is appropriate. The same table definition is included in `sql\schema.sql` for new installations.

Credibility is calculated as `helpful votes / (helpful + not-helpful votes) * 100`; zero total votes yields 0%. `src\service\FeedbackVerificationService.java` contains the sole default threshold constant (`60.0`). Change that value in one place if the application policy changes.


The existing schema does not store a feedback category or a separate email per feedback record. The interface therefore displays the linked product as the feedback category and uses the signed-in customer's name/email. Sentiment is derived from the existing rating: 4–5 positive, 3 neutral, and 1–2 negative. No schema change is needed.

## Setup (Windows)

Prerequisites: JDK 8 or newer, the MySQL Connector/J jar in `lib/`, and either a running MySQL server or a TiDB Cloud cluster.

1. Configure the existing connection. `db.properties` is ignored by Git and remains a local-only configuration file. The example targets local MySQL; the application and schema must both use the same server:

   ```powershell
   Copy-Item db.properties.example db.properties
   ```

   For local MySQL, keep the example URL (`jdbc:mysql://localhost:3306/feedback_db`). For TiDB Cloud, preserve your configured JDBC host, port, database, and TLS verification; use `sslMode=VERIFY_IDENTITY`. `DB_URL`, `DB_USER`, and `DB_PASSWORD` environment variables each override the corresponding property. Do not copy credentials into Java source or commit `db.properties`; environment variables are recommended when storing the password in a local file is undesirable.

   To use session-only environment variables in the VS Code PowerShell terminal:

   ```powershell
   $env:DB_URL = "jdbc:mysql://<cluster-host>:4000/feedback_db?sslMode=VERIFY_IDENTITY"
   $env:DB_USER = "<cluster-user>"
   $env:DB_PASSWORD = "<cluster-password>"
   ```

   These values apply to programs launched from that terminal and are not written to the repository. The application can now use these variables even if `db.properties` is absent.

2. Create the database and tables on that same server. For local MySQL:

   ```powershell
   Get-Content sql\schema.sql | mysql -u root -p
   ```

   For TiDB Cloud, substitute your cluster host and database username:

   ```powershell
   Get-Content sql\schema.sql | mysql --host=<cluster-host> --port=4000 --user=<cluster-user> --password --ssl-mode=VERIFY_IDENTITY
   ```

   Use the same cluster credentials as in `db.properties`. The database user needs permission to create the database and tables.

3. Compile and run from the VS Code terminal opened at the repository root:

   ```powershell
   javac -cp "lib\mysql-connector-j-26.7.0.jar" -sourcepath src -d out src\Main.java
   java -cp "out;lib\mysql-connector-j-26.7.0.jar" Main
   ```

   The app uses the existing Connector/J JAR in `lib/`; FlatLaf and chart libraries are not required.

   Run the standalone credibility calculation tests with:

   ```powershell
   javac -cp "out;lib\mysql-connector-j-26.7.0.jar" -d out test\service\FeedbackVerificationServiceTest.java
   java -cp "out;lib\mysql-connector-j-26.7.0.jar" service.FeedbackVerificationServiceTest
   ```

4. Register a regular account. To create the initial administrator, promote the chosen account using a database administrator account:

   ```sql
   USE feedback_db;
   UPDATE users SET role = 'ADMIN' WHERE email = 'admin@example.com';
   ```

   Then log in with that account. Public registration always creates a `CUSTOMER`; application input cannot assign administrator privileges.

## Security and implementation

- Passwords are salted and hashed with PBKDF2-HMAC-SHA-256. Plaintext passwords are not stored.
- SQL operations that use values from users use JDBC `PreparedStatement`s.
- Administrator and customer service actions check the account's stored database role.
- Feedback votes use the authenticated customer's database `user_id`, a unique database constraint prevents duplicate independent votes, and an upsert lets the customer change their vote. A manually typed name is not used as identity.
- `FeedbackVoteDAO` owns the transactional vote write, `FeedbackVerificationService` owns score and threshold rules, `FeedbackManager` retrieves vote totals, and the Swing community page handles display and refresh.
- Feedback and responses have server-side validation as well as database constraints.
- Database credentials are not committed; keep the local `db.properties` private.

The source follows the requested layout: `src\model`, `src\database`, and `src\service`, with `src\Main.java` as the Swing application entry point. `Customer` and `Admin` extend `User`; the service layer handles JDBC operations; and `Feedback` includes an optional `Response`.

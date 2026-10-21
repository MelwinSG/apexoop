# Customer Feedback System

A console-based Java application for product feedback. Customers can register, sign in, view products, rate and comment on products, and search their feedback and administrator replies. Administrators can review, search, filter, delete and respond to feedback, and view product rating statistics. Application data is persisted in MySQL.

## Features

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
| `responses` | Stores the administrator's response and response time. | Each row belongs to one feedback record and one administrator. A unique feedback key allows at most one current response. |

Feedback references users and products with foreign keys. Deleting feedback cascades to its response, while users/products referenced by feedback cannot be accidentally deleted. Three example products are inserted by the schema script.

## Setup (Windows)

Prerequisites: JDK 8 or newer, MySQL 8.0 running, and the MySQL Connector/J jar in `lib/`.

1. Create the database and tables from PowerShell:

   ```powershell
   Get-Content sql\schema.sql | mysql -u root -p
   ```

2. Copy the example settings and edit the local credentials. `db.properties` is ignored by Git:

   ```powershell
   Copy-Item db.properties.example db.properties
   ```

   `DB_URL`, `DB_USER`, and `DB_PASSWORD` environment variables can be used instead; each overrides the corresponding property.

3. Compile and run from the repository root:

   ```powershell
   javac -cp "lib\mysql-connector-j-26.7.0.jar" -sourcepath src -d out src\Main.java
   java -cp "out;lib\mysql-connector-j-26.7.0.jar" Main
   ```

4. Register a regular account. To create the initial administrator, promote the chosen account using a MySQL administrator account:

   ```sql
   USE feedback_db;
   UPDATE users SET role = 'ADMIN' WHERE email = 'admin@example.com';
   ```

   Then log in with that account. Public registration always creates a `CUSTOMER`; application input cannot assign administrator privileges.

## Security and implementation

- Passwords are salted and hashed with PBKDF2-HMAC-SHA-256. Plaintext passwords are not stored.
- SQL operations that use values from users use JDBC `PreparedStatement`s.
- Administrator and customer service actions check the account's stored database role.
- Feedback and responses have server-side validation as well as database constraints.
- Database credentials are not committed; keep the local `db.properties` private.

The source follows the requested layout: `src\model`, `src\database`, and `src\service`, with `src\Main.java` as the console entry point. `Customer` and `Admin` extend `User`; the service layer handles JDBC operations; and `Feedback` includes an optional `Response`.

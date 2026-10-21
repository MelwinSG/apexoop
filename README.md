# Customer Feedback System

A console-based Java application for collecting, storing and managing customer feedback about products. Built with **Java, Object-Oriented Programming and MySQL (JDBC)** as a second-year B.Tech CSE project.

> **Status:** In development. Sections marked *(planned)* describe the intended design and will be updated as each part is built and tested.

---

## 1. Overview

Customers can register, log in, view products, and submit a rating (1 to 5) with a comment. Administrators can review all feedback, search and filter it, delete inappropriate entries, reply to customers, and view rating statistics. All data is stored permanently in a MySQL database.

## 2. Features *(planned)*

### Customer
- Register and log in
- View available products
- Submit feedback (rating 1 to 5 and comment)
- View own feedback
- Search or filter own feedback
- View administrator responses

### Administrator
- Log in as admin
- View all feedback
- Search and filter by product or rating
- Delete inappropriate feedback
- Respond to feedback
- View average ratings and feedback statistics

## 3. Tech Stack

| Component | Technology |
|---|---|
| Language | Java |
| Database | MySQL 8.0 |
| Connectivity | JDBC with MySQL Connector/J |
| Editor | Visual Studio Code |
| OS / Terminal | Windows / PowerShell |

## 4. OOP Concepts Used *(planned)*

| Concept | Where it is applied |
|---|---|
| Encapsulation | Private fields with constructors, getters and setters in model classes |
| Inheritance | `Customer` and `Admin` extend the parent `User` class |
| Polymorphism | Overridden methods in `Customer` and `Admin` (for example, role-specific behaviour) |
| Abstraction | Abstract class or interface defining common behaviour |
| Composition | A `Feedback` object holds the `Customer` and `Product` it belongs to |

## 5. Database Design *(planned)*

Database name: `feedback_db`

| Table | Purpose |
|---|---|
| `users` | Customers and administrators (password stored as a hash, role set by the system) |
| `products` | Products that customers can review |
| `feedback` | Rating and comment, linked to a user and a product |
| `responses` | Admin replies, linked to a feedback record and an admin |

Relationships are enforced with primary and foreign keys. The exact schema will be added here after the tables are created and verified.

## 6. Project Structure *(planned)*

```
CustomerFeedbackSystem/
|-- src/
|   |-- Main.java
|   |-- model/       (User, Customer, Admin, Product, Feedback, Response)
|   |-- database/    (DBConnection)
|   |-- service/     (AuthService, FeedbackManager, ProductService, ResponseService)
|-- lib/             (MySQL Connector/J jar)
|-- README.md
```

## 7. Setup

### Prerequisites
- JDK installed (`java -version` and `javac -version` should work)
- MySQL Server 8.0 running
- MySQL Connector/J jar placed in `lib/`

### Database
1. Open the MySQL monitor: `mysql -u root -p`
2. Create the database and tables using the SQL scripts (to be added).

### Database credentials
Do **not** hardcode your password in source code. Credentials are read from a local config file or environment variables that are excluded from version control (see `.gitignore`).

### Compile and run
Commands will be added once the code is working.

## 8. Security Notes
- Passwords are stored as hashes, never as plaintext.
- All SQL that uses user input goes through `PreparedStatement`.
- Admin permissions are checked on the server side of the app, never trusted from user input.

## 9. Progress

- [x] MySQL 8.0 installed and service running
- [ ] Verify Java and `javac`
- [ ] Create `feedback_db` and tables
- [ ] Connect Java to MySQL
- [ ] OOP model classes
- [ ] Registration and login
- [ ] Products
- [ ] Customer feedback
- [ ] Admin features
- [ ] Main menu
- [ ] Testing
- [ ] Project report and viva preparation

## 10. Future Enhancements
- Feedback editing by customers
- Export reports to CSV
- A GUI or web front end

## 11. Author
Melwin, B.Tech Computer Science Engineering, RIT Kottayam

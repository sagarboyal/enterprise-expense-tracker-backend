# Enterprise Expense Tracker — Backend

A Spring Boot REST API for enterprise expense management — handles authentication, multi-level approvals, analytics, audit logs, PDF generation, and email delivery.

---
🔗 **Frontend UI:** [enterprise-expense-tracker-ui](https://github.com/sagarboyal/enterprise-expense-tracker-ui)
## Tech Stack

| | |
|---|---|
| Java + Spring Boot | Core framework |
| Spring Security + JWT | Authentication & authorization |
| Spring Data JPA + PostgreSQL | Persistence |
| Cloudinary | File & receipt storage |
| JavaMail | Email delivery |
| Docker | Containerization |
| Maven | Build tool |

---

## Features

**Auth & RBAC** — JWT-based login, role-based access for employees, managers, and admins.

**Expense Management** — Submit, edit, and track expenses with file/receipt attachments.

**Approval Workflow** — Multi-level approval with full history and audit trail.

**Analytics** — Monthly, weekly, and category-wise expense breakdowns.

**Invoice Management** — Generate and track invoices with status management.

**PDF Export** — Generate expense reports and email them automatically.

**Audit Logs** — Full activity tracking for compliance and transparency.

**Notifications** — In-app and email notifications for key events.

---

## Getting Started

**Prerequisites:** Java 17+, Maven, PostgreSQL, Docker (optional)

```bash
git clone https://github.com/sagarboyal/enterprise-expense-tracker-backend.git
cd enterprise-expense-tracker-backend
```

Create a `.env` file in the root directory:

```env
# Database
DB_URL=jdbc:postgresql://localhost:5432/postgres
DB_USERNAME=postgres
DB_PASSWORD=postgres
DRIVER_CLASS=org.postgresql.Driver
DB_DIALECT=org.hibernate.dialect.PostgreSQLDialect

# JWT
JWT_SECRET=your_jwt_secret_key
JWT_EXP_TIME=3600000

# Mail
MAIL_USERNAME=your_email@example.com
MAIL_PASSWORD=your_email_password

# Frontend
FRONT_END_URL=http://localhost:5173

# Cloudinary
CLOUD_NAME=your_cloud_name
API_KEY=your_api_key
API_SECRET=your_api_secret
MAX_FILE_SIZE=2MB
```

```bash
./mvnw spring-boot:run
```

**Using Docker:**

```bash
docker build -t expense-tracker-backend .
docker run -p 8080:8080 --env-file .env expense-tracker-backend
```

---

## Project Structure

```
src/main/java/
├── controller/       → REST endpoints (Auth, Expense, Approval, Analytics, Admin...)
├── service/          → Business logic interfaces & implementations
├── entity/           → JPA entities (User, Expense, Approval, Invoice, AuditLog...)
├── dto/              → Data transfer objects
├── payload/          → Request & response wrappers
├── repository/       → Spring Data repositories
├── specification/    → Dynamic query filters
├── security/         → SecurityConfig, JWT filter & utils
├── jwt/              → JWT auth entry point, filter, utilities
├── cloudinary/       → File upload config & service
└── utils/            → Auth utils, PDF generation, object mapping
```

---

## Roadmap

- [ ] Swagger / OpenAPI documentation
- [ ] Unit & integration tests
- [ ] Multi-currency support
- [ ] CI/CD pipeline

---

## Contact

- Email: sagarboyal.024@gmail.com
- Issues: [GitHub Issues](https://github.com/sagarboyal/enterprise-expense-tracker-backend/issues)

---

## License

[MIT](LICENSE)

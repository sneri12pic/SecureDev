# SecureDev Coursework - BankWebsite Hardening

Secure Software Development coursework based on the vulnerable `BankWebsite`
Java/Jetty application. The project demonstrates how common web application
vulnerabilities can be exploited, then mitigated, in a small online banking
system.

The application includes login, account/profile management, customer listing,
balance transfer, logout, and an admin-only password reset workflow. The final
version focuses on preserving normal banking functionality while blocking the
attacks demonstrated in the coursework report.

## Project Structure

```text
.
|-- Bank_Attack/
|   |-- build.xml
|   |-- src/
|   |   |-- TargetServer.java
|   |   |-- ViewPageServlet.java
|   |   |-- AccountPageServlet.java
|   |   |-- TransferPageServlet.java
|   |   |-- CustomersListPageServlet.java
|   |   |-- AdminPasswordResetServlet.java
|   |   |-- Database.java
|   |   |-- SecurityUtil.java
|   |   |-- create_db.sql
|   |   |-- WebContext/
|   |   `-- lib/
|   `-- built/classes/
|-- LICENSE
`-- README.md
```
## Demo

https://github.com/user-attachments/assets/f9e23ed9-dc71-4daf-ada4-493ef44a91e6

## Security Work Completed

| Area | Vulnerability | Mitigation |
| --- | --- | --- |
| Authentication | SQL injection in login/database operations | Replaced string-concatenated SQL with `PreparedStatement` queries and parameter binding. |
| Profile rendering | Stored/reflected XSS | Added `SecurityUtil.escapeHtml(...)` and escaped user-controlled output before writing HTML. |
| State-changing requests | CSRF | Added per-session CSRF tokens generated with `SecureRandom`; protected transfers, profile updates, role changes, logout, and admin reset. |
| Session handling | Session fixation/weak session controls | Invalidated old sessions after login, created a fresh authenticated session, reduced idle timeout to 15 minutes, enabled `HttpOnly`, and disabled directory listing. |
| Profile uploads | Dangerous file upload | Allowed only image MIME types/extensions, limited uploads to 2 MB, randomised filenames with UUIDs, stored files under `uploads/`, and validated stored image paths. |
| Transfer confirmation | Hidden form field trust / parameter tampering | Removed trusted hidden `fee` data from the browser; transfer details are stored server-side in the session and the fee is recalculated on confirmation. |
| Admin password reset | CWE-306 Missing Authentication for Critical Function | Restricted `/admin-reset` to authenticated admin users, added CSRF validation, and required new passwords to be at least 8 characters. |

## Requirements

- Java JDK
- Apache Ant, if rebuilding from source
- SQLite command-line tool (`sqlite3`), required by the Ant build target
- A modern web browser

The required Java libraries are already included under `Bank_Attack/src/lib`.

## Build

From the repository root:

```powershell
cd Bank_Attack
ant
```

The build compiles the Java sources into `Bank_Attack/built/classes`, copies the
static web assets, and recreates `users.db` from `src/create_db.sql`.

## Run

### Windows PowerShell

```powershell
cd Bank_Attack\built\classes
java -cp "../../src/lib/*;." TargetServer
```

### Linux/macOS

```bash
cd Bank_Attack
ant
cd built/classes
java -cp "../../src/lib/*:." TargetServer
```

Then open:

```text
http://localhost:15000/welcome
```

To stop the server, type `quit` in the terminal where `TargetServer` is running.

## Test Accounts

| Username | Password | Role | Card ID | Starting Balance |
| --- | --- | --- | --- | --- |
| `admin` | `adminPassword` | Admin | `999999` | `0` |
| `user1` | `user1Password` | Normal | `111111` | `6701` |
| `user2` | `user2Password` | Normal | `222222` | `5050` |
| `user3` | `user3Password` | Normal | `333333` | `950` |
| `user4` | `user4Password` | Normal | `444444` | `162230` |
| `user5` | `user5Password` | Normal | `555555` | `587` |

## Main Routes

| Route | Purpose | Access |
| --- | --- | --- |
| `/welcome` | Login page | Public |
| `/account` | Current user's account page | Logged-in users |
| `/account?username=<name>` | View another user's account summary | Logged-in users |
| `/transfer` | Preview and confirm balance transfers | Logged-in users |
| `/balance` | Customer list and balances/card numbers | Logged-in users |
| `/logout` | Ends the current session | CSRF-protected POST |
| `/admin-reset` | Reset a user's password | Admin only |

## Manual Verification Checklist

1. Log in with a valid account, such as `user1 / user1Password`.
2. Confirm account, customer list, transfer, profile edit, profile image upload, and logout still work normally.
3. Confirm login rejects SQL injection payloads such as `'OR 1=1;`.
4. Confirm profile text displays script payloads as text rather than executing them.
5. Confirm forged POST requests to protected actions fail without a valid `csrfToken`.
6. Confirm transfer confirmation always charges the server-side `$2` fee, even if a fake `fee` field is added in the browser.
7. Confirm non-image uploads, oversized uploads, and unsafe stored image paths are rejected.
8. Confirm unauthenticated and non-admin users receive `403 Forbidden` for `/admin-reset`.
9. Log in as `admin` and confirm `/admin-reset` works with a valid CSRF token and a new password of at least 8 characters.

## Notes

This repository is for educational security coursework. The accompanying report
documents each vulnerability using normal behaviour, exploitation before
mitigation, mitigation details, and verification after mitigation.

Do not deploy this application as a real banking system. It intentionally keeps a
small teaching-oriented architecture and stores demo credentials in a local
SQLite database.

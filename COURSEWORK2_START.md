# Coursework 2 Starter Plan

Target: around 65% or above by focusing on working mitigations, clear before/after evidence, and the mandatory video for the two additional vulnerabilities.

## Mark Strategy

The coursework is marked as:

- 30 marks: mitigate five existing BankWebsite vulnerabilities.
- 30 marks: implement two additional vulnerabilities.
- 30 marks: mitigate the two additional vulnerabilities.
- 10 marks: integrate the two additional vulnerabilities into one app or BankWebsite.

For a realistic 65+ target, the safest route is:

- Make the five BankWebsite mitigations practical and easy to evidence.
- Use the CW1 planned extra vulnerabilities: `CWE-22 Path Traversal` and `CWE-306 Missing Authentication for Critical Function`.
- Integrate both into BankWebsite, because that protects the 10 integration marks and avoids the separate-application cap.
- Record the mandatory video. Without it, the two additional sections can receive 0.

## BankWebsite Mitigations Started

The first patch has started the five required BankWebsite mitigations:

| Vulnerability | Files | Mitigation direction |
| --- | --- | --- |
| SQL Injection | `Database.java`, `CustomersListPageServlet.java` | Prepared statements for login and role update; validate role values; admin-only role update. |
| XSS | `SecurityUtil.java`, `AccountPageServlet.java`, `CustomersListPageServlet.java`, `TransferPageServlet.java`, `Account.js` | HTML output encoding and client-side `textContent` instead of `innerHTML`. |
| CSRF | `SecurityUtil.java`, `AccountPageServlet.java`, `TransferPageServlet.java`, `CustomersListPageServlet.java`, `LogoutServlet.java`, `Account.js` | Per-session CSRF token in forms/AJAX and server-side token checks on state-changing POST requests. |
| Session vulnerabilities | `TargetServer.java`, `ViewPageServlet.java` | Shorter session timeout, HttpOnly session cookie, session regeneration after login, directory listing disabled. |
| Dangerous file upload | `AccountPageServlet.java`, `Database.java`, `Account.js` | Server-side image extension/MIME/size checks, random stored names, uploads under `uploads/`, stored-path validation, image rendering instead of iframe. |

## Evidence Checklist For The Report

For each of the five BankWebsite mitigations, capture:

- Before mitigation: screenshot or request showing the exploit works.
- Vulnerable code snippet: short snippet from the original code.
- Mitigation: short snippet from the changed code.
- After mitigation: screenshot or request showing the same attack fails.
- Normal functionality: screenshot showing the feature still works normally.
- Reference: use relevant CWE plus OWASP guidance where possible.

Suggested evidence:

- SQL Injection: login with `' OR 1=1;--` fails after prepared statement.
- Role update: non-admin `fetch('/balance', ...)` receives `403 Forbidden`; admin update still works if you add a legitimate admin form/request with CSRF token.
- XSS: `/account?username=<script>alert(1)</script>` renders as text or does not execute.
- Persistent XSS: profile description containing `<script>alert(1)</script>` displays safely as text.
- CSRF: POST to `/transfer` or `/account` without `csrfToken` receives `403 Forbidden`; normal in-site transfer/profile update still works.
- Upload: `.html` file upload is rejected; normal `.png` or `.jpg` upload still works.
- Session: after login, session is regenerated, cookie is HttpOnly, timeout is 15 minutes, and directory listing no longer works.

## Additional Vulnerabilities To Build Next

Use the CW1 plan:

1. `CWE-22 Path Traversal`
   - Add a receipt feature to BankWebsite.
   - Normal behaviour: logged-in user views a transfer receipt from a receipts folder.
   - Vulnerable behaviour: attacker changes a file parameter such as `../../users.db` or `../create_db.sql` and reads a file outside the receipts folder.
   - Mitigation: canonical path check, allow-list receipt IDs, block path separators, store/read only from a fixed receipts directory.

2. `CWE-306 Missing Authentication for Critical Function`
   - Add an admin password reset page.
   - Normal behaviour: logged-in admin resets a user's password.
   - Vulnerable behaviour: attacker sends a direct POST to the reset endpoint without a valid authenticated admin session.
   - Mitigation: require session, require admin role, require CSRF token, validate target user.

## Video Script

Keep the video under 6 minutes total:

- Path Traversal, 3 minutes max:
  - Show normal receipt view.
  - Show attack payload and exposed file.
  - Explain canonical path/allow-list mitigation.
  - Repeat attack and show failure.
  - Show normal receipt view still works.

- Missing Authentication, 3 minutes max:
  - Show admin reset works normally.
  - Show unauthenticated/direct request resets a password in vulnerable version.
  - Explain session/admin/CSRF mitigation.
  - Repeat direct request and show `403 Forbidden`.
  - Show admin reset still works normally.

## Report Structure

Use `template.docx` and keep this order:

1. Summary checklist table from the template.
2. Vulnerability Mitigations of BankWebsite.
3. Additional Attacks Implementation.
4. Mitigation Strategies and Implementations for the two additional vulnerabilities.
5. Reflection.
6. References.

References to include:

- CWE-89 SQL Injection
- CWE-79 Cross-Site Scripting
- CWE-352 Cross-Site Request Forgery
- CWE-384 Session Fixation
- CWE-434 Dangerous File Upload
- CWE-22 Path Traversal
- CWE-306 Missing Authentication for Critical Function
- OWASP Cheat Sheet Series for SQL Injection Prevention, XSS Prevention, CSRF Prevention, File Upload, Authentication, and Session Management

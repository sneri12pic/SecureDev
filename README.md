# SecureDev Coursework 2

Secure Software Development coursework based on the BankWebsite application.

## Overview

This project demonstrates identification, exploitation, and mitigation of web application vulnerabilities in a Java/Jetty banking application.

The Coursework 2 implementation covers:

- Mitigation of five existing BankWebsite vulnerabilities:
  - SQL Injection
  - Cross-Site Scripting (XSS)
  - Cross-Site Request Forgery (CSRF)
  - Session vulnerabilities
  - Upload of dangerous files

- Implementation and mitigation of two additional vulnerabilities:
  - Hidden form field trust / parameter tampering in the transfer confirmation workflow
  - CWE-306 Missing Authentication for Critical Function in the admin password reset workflow

## Running the Application

From Windows PowerShell:

```powershell
cd Bank_Attack\built\classes
java -cp "../../src/lib/*;." TargetServer
```

Then open:

```text
http://localhost:15000/welcome
```

On Linux:

```bash
cd Bank_Attack
ant
cd built/classes
./run.sh
```

## Test Accounts

```text
admin / adminPassword
user1 / user1Password
user2 / user2Password
user3 / user3Password
```

## Main Pages

```text
/welcome
/account
/transfer
/balance
/admin-reset
```

## Notes

The report and video evidence demonstrate normal behaviour, successful exploitation before mitigation, and failed exploitation after mitigation.

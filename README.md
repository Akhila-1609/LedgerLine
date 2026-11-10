



# LedgerLine - Financial Transaction Management System



## Overview



LedgerLine is a backend financial transaction management application developed using Java, Spring Boot, Spring Data JPA, and PostgreSQL.



It provides REST APIs to create accounts, deposit money, withdraw money, transfer funds, retrieve account balances, and view transaction history.



## Technologies Used



\- Java 21

\- Spring Boot

\- Spring Data JPA

\- PostgreSQL

\- Maven

\- Spring Security

\- REST APIs



## Features



\- Create bank accounts

\- Deposit and withdraw money

\- Transfer funds between accounts

\- Retrieve account details and balances

\- View transaction history

\- Validate transaction amounts

\- Prevent duplicate transaction references

\- Prevent insufficient-balance transactions

\- Secure API endpoints using HTTP Basic Authentication

\- Use database transactions and pessimistic locking



## API Endpoints



| Method | Endpoint | Description |

|---|---|---|

| POST | /api/accounts | Create account |

| POST | /api/accounts/{accountNumber}/deposit | Deposit |

| POST | /api/accounts/{accountNumber}/withdraw | Withdraw |

| POST | /api/transfers | Transfer |

| GET | /api/accounts/{accountNumber} | Account details |

| GET | /api/accounts/{accountNumber}/transactions | Transaction history |



## Database Configuration



This project uses PostgreSQL.



Create a database named `ledgerline\_db`.



Set these environment variables in PowerShell:



```powershell

$env:DB\_PASSWORD = "your\_database\_password"

$env:APP\_USERNAME = "your\_username"

$env:APP\_PASSWORD = "your\_application\_password"

```



## Running the Application



Start the application:



```powershell

.\\mvnw.cmd spring-boot:run

```



The application runs at http://localhost:8080.



## Authentication



The application uses HTTP Basic Authentication. Credentials are configured through environment variables. Never commit real passwords to GitHub.



## Testing



Run automated tests:



```powershell

.\\mvnw.cmd clean test

```



Build the application:



```powershell

.\\mvnw.cmd clean package

```



## Project Status



Core account management and transaction APIs have been implemented and manually tested. The automated Spring Boot test suite passes.



## Author



Mekala Akhila



Backend Engineering Project





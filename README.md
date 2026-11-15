# Ecommerce Assistant

Spring Boot sample application with two separate LangChain4j agents and an in-memory H2 database.

## Request flow

```text
HTTP request
    -> AssistantController
       -> /chat  -> Assistant (general chat agent)
       -> /order -> OrderAgent (ecommerce agent)
                      -> OrderTools
                          -> OrderService
                              -> CustomerOrderRepository
                                  -> H2
```

The `agent` package contains the two AI agent interfaces. Only `OrderAgent` is configured with the `OrderTools` component. `tools` holds callable order actions, `service` contains order business logic, `repository` handles JPA persistence, `entity` contains persisted JPA types, and `config` initializes sample data.

## Launch the application

Set a Gemini API key in PowerShell, then run Spring Boot:

```powershell
$env:GEMINI_API_KEY = "your-gemini-api-key"
.\mvnw.cmd spring-boot:run
```

The application starts at `http://localhost:8080`. Chat endpoints:

- General assistant: `GET http://localhost:8080/api/assistant/chat?message=Hello`
- Ecommerce order agent: `GET http://localhost:8080/api/assistant/order?message=Check%20order%20ORD-1001`

The order agent can create orders, check their status, and cancel orders that have not shipped. Seed orders include `ORD-1001`, `ORD-1002`, and `ORD-1003`.

## View database details in H2

H2 is configured as an in-memory database at `jdbc:h2:mem:ecommerce`. Hibernate creates the schema when the application starts and drops it when the application stops. The sample order data is inserted at startup, so it does not persist across restarts.

1. Keep the application running.
2. Open `http://localhost:8080/h2-console`.
3. Use these connection settings:

   | Setting | Value |
   | --- | --- |
   | Driver Class | `org.h2.Driver` |
   | JDBC URL | `jdbc:h2:mem:ecommerce` |
   | User Name | `sa` |
   | Password | Leave blank |

4. Click **Connect**, then run `SELECT * FROM CUSTOMER_ORDERS;` in the SQL console.

The order, payment, and shipping fields are stored as columns in `CUSTOMER_ORDERS`.

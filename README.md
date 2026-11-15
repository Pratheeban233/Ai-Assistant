# AI Assistant

Spring Boot learning project with two separate LangChain4j agents and an in-memory H2 database. The sample order workflow demonstrates agent tools, services, repositories, and JPA persistence.

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

The Gemini model is configured to return and resend its thinking data. Gemini 3 uses a `thought_signature` to continue a tool call; without these settings, follow-up requests after order tools can fail.

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

## API errors

Controller errors use Spring's RFC 9457 `ProblemDetail` format. AI provider failures return HTTP `502 Bad Gateway`, while invalid requests return HTTP `400 Bad Request`. For example:

```json
{
  "type": "about:blank",
  "title": "Bad Gateway",
  "status": 502,
  "detail": "The AI provider could not complete the request. Please try again.",
  "instance": "/api/assistant/order",
  "code": "AI_PROVIDER_ERROR",
  "timestamp": "2026-11-15T10:00:00Z"
}
```

Provider error details are logged by the application for diagnosis, but are not returned to API clients.

# AI Assistant

Spring Boot learning project with two separate LangChain4j agents and an in-memory H2 database. The sample order workflow demonstrates agent tools, services, repositories, and JPA persistence.

## Request flow

```text
HTTP request
    -> AssistantController
       -> /chat  -> Assistant (general chat agent) -> assistant chat memory
       -> /order -> OrderAgent (ecommerce agent) -> order chat memory
                      -> OrderTools
                          -> OrderService
                              -> CustomerOrderRepository
                                  -> H2
```

The `agent` package contains the two AI agent interfaces. Only `OrderAgent` is configured with the `OrderTools` component. `tools` holds callable order actions, `service` contains order business logic, `repository` handles JPA persistence, `entity` contains persisted JPA types, and `config` initializes sample data.

The Gemini model is configured to return and resend its thinking data. Gemini 3 uses a `thought_signature` to continue a tool call; without these settings, follow-up requests after order tools can fail.

## Prompt template example (Java)

LangChain4j supports dynamic system-message templates using `{{variable}}` placeholders and `@V` parameters. This resembles using Jinja to fill in prompt context, but it is LangChain4j's template syntax rather than Python/Jinja itself.

The personalized chat agent declares the template and its value:

```java
@SystemMessage("""
        You are a polite assistant.
        The customer's name is {{customerName}}.
        Use that name naturally when appropriate.
        """)
String personalizedChat(
        @MemoryId String conversationId,
        @UserMessage String userMessage,
        @V("customerName") String customerName);
```

Call the example endpoint with JSON:

```powershell
curl.exe -X POST http://localhost:8080/api/assistant/personalized-chat `
  -H "Content-Type: application/json" `
  -d '{\"conversationId\":\"demo-1\",\"message\":\"How is my day?\",\"customerName\":\"Avery\"}'
```

The controller passes `message` as the user message and `customerName` as template context. LangChain4j replaces `{{customerName}}` before sending the system message to Gemini.

## Launch the application

Set a Gemini API key in PowerShell, then run Spring Boot:

```powershell
$env:GEMINI_API_KEY = "your-gemini-api-key"
.\mvnw.cmd spring-boot:run
```

The application starts at `http://localhost:8080`. Chat endpoints:

- General assistant: `POST /api/assistant/chat`
- Ecommerce order agent: `POST /api/assistant/order`

Both endpoints accept a JSON body containing a `conversationId` and `message`. For example:

```powershell
curl.exe -X POST http://localhost:8080/api/assistant/chat `
  -H "Content-Type: application/json" `
  -d '{\"conversationId\":\"demo-1\",\"message\":\"Hello\"}'

curl.exe -X POST http://localhost:8080/api/assistant/order `
  -H "Content-Type: application/json" `
  -d '{\"conversationId\":\"orders-1\",\"message\":\"Check order ORD-1001\"}'
```

## Conversation memory

`AssistantAgent` and `OrderAgent` each have a named `ChatMemoryProvider` in `ChatMemoryConfiguration`. LangChain4j calls the provider when it first sees a conversation ID, then reuses that conversation's memory for later calls to the same agent. `@MemoryId` connects the ID in the request to that memory. On each turn, LangChain4j includes the retained messages in the model request and adds the new user and assistant messages to the history.

Each conversation uses `MessageWindowChatMemory` with a 20-message window, so older messages are removed as the window fills. The agents have separate providers: the same ID sent to `/chat` and `/order` does not mix their histories. The general and personalized assistant endpoints also use separate internal ID namespaces because their system prompts differ. Reuse an ID on the same endpoint to continue that conversation, and use a different ID to start another.

This memory is in-process and temporary: restarting the application clears it, and multiple application instances do not share it. A deployed app that needs durable or shared history should configure a persistent/shared `ChatMemoryStore`. This learning app has no authentication; in a real app, authenticate users and verify that each conversation ID belongs to the requesting user.

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

# Macroeconomics AI

An AI agent built with **Spring AI** that answers questions about U.S. macroeconomics using real, up-to-date data from **FRED** (Federal Reserve Economic Data). The agent uses tool calling to query live series (inflation, unemployment, interest rates, GDP, S&P 500, M2 money supply, and more) and produces structured, analyst-style reports.

> 📘 Built while studying the *Spring AI* book — a hands-on companion project to the concepts covered in its chapters on `ChatClient`, tool calling, and model configuration.

## ⚠️ Disclaimer

This is a **personal study project**, not a production system. The generated reports are AI-written analyses based on real economic data, but they are **not financial or investment advice**. Do not use this project as a basis for investment decisions.

## Features

- 🤖 **Tool-calling agent** built with Spring AI's `ChatClient` and `@Tool` annotations
- 📊 **Real economic data** pulled live from the FRED API (no hardcoded or invented numbers)
- 📈 Supports key U.S. indicators out of the box:
    - Inflation (CPI, year-over-year % change)
    - Unemployment rate
    - Federal Funds Rate
    - GDP
    - Any custom FRED series by ID (e.g. `SP500`, `DGS10`, `M2SL`, `PAYEMS`)
- 📝 Generates **structured, analyst-style reports** in Markdown (headline, key figures table, trend & context, historical perspective)
- 🔁 Multi-tool chaining — the agent can combine multiple indicators to answer comparative questions (e.g. "How does the S&P 500 relate to inflation and interest rates?")
- ⚡ Series metadata (title, unit) is cached in memory to avoid redundant FRED calls

## Tech Stack

- **Java 25**
- **Spring Boot 4.1.0**
- **Spring AI 2.0.0**
- **Google Gemini API** (`spring-ai-starter-model-google-genai`) — free tier
- **FRED API** (Federal Reserve Bank of St. Louis)
- Virtual Threads (Project Loom) enabled

## Architecture Overview

```
User request (GET /chat?message=...)
        │
        ▼
  ChatController
        │
        ▼
  ChatClient (Spring AI)
        │
        ├── System prompt: analyst persona + report formatting rules
        │
        ▼
  MacroeconomicsTools (@Tool methods)
        │
        ▼
  MacroeconomicsClient (RestClient)
        │
        ▼
  FRED API (observations + series metadata)
```

The LLM decides which tools to call based on the user's question, retrieves real data from FRED, and synthesizes a structured report — citing exact values, dates, and units returned by the tools.

## Getting Started

### Prerequisites

- Java 25+
- A [FRED API key](https://fred.stlouisfed.org/docs/api/api_key.html) (free, no credit card required)
- A [Google AI Studio API key](https://aistudio.google.com/) (free tier, no credit card required)

### Configuration

Set the following environment variables:

```bash
export FRED_API_KEY=your_fred_api_key
export GEMINI_API_KEY=your_gemini_api_key
```

`application.yml`:

```yaml
spring:
  application:
    name: macroeconomics-ai
  threads:
    virtual:
      enabled: true

  ai:
    google:
      genai:
        api-key: ${GEMINI_API_KEY}
        chat:
          options:
            model: ${GEMINI_MODEL:gemini-3.1-flash-lite}
            temperature: 0.2
            max-output-tokens: 2048

fred:
  api:
    key: ${FRED_API_KEY}
    base-url: https://api.stlouisfed.org/fred

server:
  port: 8080
```

### Running

```bash
./mvnw spring-boot:run
```

### Usage

```bash
curl "http://localhost:8080/chat?message=What is the latest U.S. inflation rate, and how has it changed over the last 6 months?"
```

## Example Questions

- `What is the latest U.S. inflation rate, and how has it changed over the last 6 months?`
- `What is the current U.S. unemployment rate compared to a year ago?`
- `Compare the current inflation rate with the unemployment rate — does this look like a period of stagflation risk?`
- `Compare the 10-Year Treasury yield (DGS10) with the current Federal Funds Rate — what does the yield curve suggest about recession risk?`
- `How has the S&P 500 (SP500) performed over the last 6 months relative to the trend in inflation and interest rates?`
- `What has happened to the M2 money supply (M2SL) over the past year, and how does that relate to current inflation trends?`

## Lessons Learned

A few things worth noting from building this:

- **Free-tier LLM models deprecate fast.** Over the course of this project, `qwen/qwen-2.5-72b-instruct:free` and `gemini-2.5-flash` both stopped being available to new users within days of each other.
- **Raw index ≠ rate.** FRED's CPI series (`CPIAUCSL`) is a raw index (base 1982–1984 = 100), not a percentage. Getting the actual inflation *rate* requires the `units=pc1` transformation parameter on the FRED API — no manual calculation needed.
- **Tool-chaining needs concrete examples, not abstract rules.** Telling the model "always call all relevant tools" in the system prompt wasn't reliable. Giving it one concrete worked example (a specific question mapped to specific tool calls) fixed multi-tool chaining consistently.
- **Formatting bugs aren't always the model's fault.** A "missing spaces" bug turned out to be the API client's Markdown preview renderer, not the LLM output itself — always check the raw response before blaming the model.

## License

This is a personal study project. Feel free to fork and adapt it for your own learning.
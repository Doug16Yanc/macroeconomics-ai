# Macroeconomics AI

An AI agent built with **Spring AI** that answers questions about U.S. and Brazilian
macroeconomics using real, up-to-date data from **FRED** (Federal Reserve Economic
Data — U.S. market), **BCB/SGS** (Banco Central do Brasil) and **SIDRA/IBGE**
(Sistema IBGE de Recuperação Automática — Brazilian market). The agent uses tool
calling to query live series (inflation, unemployment, interest rates, GDP, exchange
rate, labor market by sector, and more) and produces structured, analyst-style
reports — in English or Portuguese, matching the language of the question.

**Spring Batch** is the backbone of data ingestion: it pulls and processes
high-volume data from each of these sources — FRED, BCB/SGS, and SIDRA/IBGE — into
a local Postgres cache, so the agent always answers from fresh, pre-processed data
instead of hitting external APIs on every question.

> 📘 Built while studying the *Spring AI* book — a hands-on companion project to the
> concepts covered in its chapters on `ChatClient`, tool calling, and model
> configuration. Evolved into a multi-source, hexagonal-architecture project as a
> vehicle for practicing ports-and-adapters design with real external data sources.

## ⚠️ Disclaimer

This is a **personal study project**, not a production system. The generated reports
are AI-written analyses based on real economic data, but they are **not financial or
investment advice**. Do not use this project as a basis for investment decisions.

## Features

- 🤖 **Tool-calling agent** built with Spring AI's `ChatClient` and `@Tool` annotations
- 🌎 **Multi-country coverage** — U.S. data via FRED, Brazilian data via BCB/SGS and SIDRA/IBGE
- 📊 **Real economic data**, no hardcoded or invented numbers, sourced from official
  central bank and statistical institute APIs
- 📈 U.S. indicators out of the box (FRED):
    - Inflation (CPI, year-over-year % change)
    - Unemployment rate
    - Federal Funds Rate
    - GDP
    - Labor market series (payrolls, jobless claims, job openings)
    - Any custom FRED series by ID (e.g. `SP500`, `DGS10`, `M2SL`, `PAYEMS`)
- 📈 Brazilian indicators out of the box (BCB/SGS + SIDRA/IBGE):
    - Selic rate (base interest rate)
    - IPCA inflation
    - USD/BRL exchange rate
    - Employment by economic activity (SIDRA) — including sectoral breakdown of services
    - Any custom BCB/SGS series by numeric code
- 🇺🇸🇧🇷 **Cross-country comparisons** — the agent queries sources independently
  and never assumes a trend from one country applies to the other
- 🗣️ **Responds in the question's language** (English or Portuguese)
- 📝 Generates **structured, analyst-style reports** in Markdown (headline, key
  figures table, trend & context, historical perspective)
- 🔁 Multi-tool chaining — the agent combines multiple indicators and multiple
  sources/countries to answer comparative questions
- 💾 **Spring Batch as the ingestion and processing engine** — high-volume series
  from FRED, BCB/SGS, and SIDRA/IBGE are pulled, transformed, and persisted into
  Postgres on demand, so the agent reads from a local cache-first table instead of
  hitting external APIs on every question
- ⚡ Series metadata (title, unit) is cached in memory to avoid redundant external calls

## Tech Stack

- **Java 25**
- **Spring Boot 4.1.0**
- **Spring AI 2.0.0**
- **Spring Batch** — ingestion and processing engine for all three data sources
- **PostgreSQL** + **Flyway** — versioned schema, local observation cache
- **Google Gemini API** (`spring-ai-starter-model-google-genai`) — free tier
- **FRED API** (Federal Reserve Bank of St. Louis)
- **BCB/SGS API** (Banco Central do Brasil — Sistema Gerenciador de Séries Temporais)
- **SIDRA/IBGE API** (Sistema IBGE de Recuperação Automática)
- Virtual Threads (Project Loom) enabled

## Architecture Overview

The project follows a **hexagonal (ports and adapters)** architecture. Each external
data source is a swappable adapter behind a domain port, so adding a new source
(or a new country) means adding an adapter — not touching the agent or the domain.

```
domain/
  model/    → MonetaryIndicator, LaborMarketIndicator, FredObservation, ...
  port/     → MonetaryDataPort, LaborMarketDataPort

infrastructure/
  fred/     → FredMonetaryAdapter, FredLaborAdapter, FredRestClientConfig
  bcb/      → BcbMonetaryAdapter, BcbRestClientConfig
  sidra/    → SidraLaborAdapter, SidraRestClientConfig
  batch/    → ingestion jobs (Fred + Bcb + Sidra), reader/processor/writer per source
  ai/       → ChatClientConfig, FredTools, BcbTools, SidraTools (@Tool methods)
  web/      → ChatController, ingestion trigger controllers
```

```
User request (GET /chat?message=...)
        │
        ▼
  ChatController
        │
        ▼
  ChatClient (Spring AI)
        │
        ├── System prompt: multi-country analyst persona,
        │   tool-chaining rules, language-matching rule
        │
        ▼
  FredTools / BcbTools / SidraTools (@Tool methods)
        │
        ▼
  MonetaryDataPort / LaborMarketDataPort   ← domain ports
        │
        ▼
  FredMonetaryAdapter / BcbMonetaryAdapter / SidraLaborAdapter  ← infrastructure adapters
        │
        ▼
  FRED API  /  BCB SGS API  /  IBGE SIDRA
```

Separately, a **Spring Batch** pipeline ingests observations into Postgres
(`fred_observations`, `bcb_observations`, `sidra_observations`) on demand via REST
trigger — one job per source, each with its own reader/processor/writer — giving
the agent a local, idempotent cache to query instead of re-hitting external APIs on
every question. This is the layer that handles volume: pagination, batching, and
chunk-oriented processing for series that can span years of daily or monthly
observations across three independent sources.

The LLM decides which tools to call based on the user's question, retrieves real
data from the relevant source(s), and synthesizes a structured report — citing
exact values, dates, and units returned by the tools.

## Getting Started

### Prerequisites

- Java 25+
- PostgreSQL (local instance, no Docker required)
- A [FRED API key](https://fred.stlouisfed.org/docs/api/api_key.html) (free, no
  credit card required)
- A [Google AI Studio API key](https://aistudio.google.com/) (free tier, no credit
  card required)
- No API key required for BCB/SGS or SIDRA/IBGE — both are public, unauthenticated endpoints

### Configuration

Set the following environment variables:

```bash
export FRED_API_KEY=your_fred_api_key
export GEMINI_API_KEY=your_gemini_api_key
export DB_URL=jdbc:postgresql://localhost:5432/macroeconomics_ai
export DB_USERNAME=your_db_user
export DB_PASSWORD=your_db_password
```

`application.yml`:

```yaml
spring:
  application:
    name: macroeconomics-ai
  threads:
    virtual:
      enabled: true

  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver

  batch:
    jdbc:
      initialize-schema: always
    job:
      enabled: false

  flyway:
    enabled: true
    locations: classpath:db/migration

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

bcb:
  api:
    base-url: https://api.bcb.gov.br/dados/serie

sidra:
  api:
    base-url: https://apisidra.ibge.gov.br/values

server:
  port: 8080
```

### Running

```bash
./mvnw spring-boot:run
```

### Ingesting data

Before asking the agent questions, trigger ingestion so the local cache is
populated — one Spring Batch job per source:

```bash
curl -X POST http://localhost:8080/jobs/fred-ingestion
curl -X POST http://localhost:8080/jobs/bcb-ingestion
curl -X POST http://localhost:8080/jobs/sidra-ingestion
```

Check job status:

```bash
curl http://localhost:8080/jobs/fred-ingestion/{jobExecutionId}
curl http://localhost:8080/jobs/bcb-ingestion/{jobExecutionId}
curl http://localhost:8080/jobs/sidra-ingestion/{jobExecutionId}
```

### Usage

```bash
curl "http://localhost:8080/chat?message=What is the latest U.S. inflation rate, and how has it changed over the last 6 months?"
curl "http://localhost:8080/chat?message=Qual é a taxa Selic atual e como ela variou nos últimos 6 meses?"
```

## Example Questions

**U.S. (English):**
- `What is the latest U.S. inflation rate, and how has it changed over the last 6 months?`
- `Compare the current inflation rate with the unemployment rate — does this look like a period of stagflation risk?`
- `Is the slowdown in tech hiring a broad economic phenomenon or something isolated to the sector?`

**Brazil (Portuguese):**
- `Qual é a taxa Selic atual e como ela variou nos últimos 6 meses?`
- `Como está o câmbio dólar/real nos últimos 3 meses, e isso tem relação com a inflação brasileira (IPCA) no mesmo período?`
- `Como está o emprego no setor de serviços segundo o SIDRA, e isso é coerente com a trajetória da Selic?`

**Cross-country:**
- `Compare a política monetária dos EUA e do Brasil: como estão a Fed Funds Rate e a Selic atualmente, e o que essa diferença sugere?`
  

## Lessons Learned

A few things worth noting from building this:

- **Free-tier LLM models deprecate fast.** Over the course of this project,
  `qwen/qwen-2.5-72b-instruct:free` and `gemini-2.5-flash` both stopped being
  available to new users within days of each other.
- **Raw index ≠ rate.** FRED's CPI series (`CPIAUCSL`) is a raw index (base
  1982–1984 = 100), not a percentage. Getting the actual inflation *rate* requires
  the `units=pc1` transformation parameter on the FRED API — no manual calculation
  needed.
- **Tool-chaining needs concrete examples, not abstract rules.** Telling the model
  "always call all relevant tools" in the system prompt wasn't reliable. Giving it
  one concrete worked example (a specific question mapped to specific tool calls)
  fixed multi-tool chaining consistently — this held true again when extending the
  rule to cross-country and cross-source tool calls.
- **A shared port signature can hide a source-specific assumption.**
  `LaborMarketDataPort` was originally designed with CAGED's sector/region
  dimensions in mind; retrofitting FRED (which has no sector/region breakdown)
  onto that same signature required adding an explicit `indicator` parameter and
  making `sector`/`region` nullable — a reminder that abstracting behind an
  interface too early, before a second real implementation exists, risks baking in
  the first source's shape. Adding SIDRA as a third implementation validated the
  port again, since its sector-breakdown shape maps more naturally to the original
  signature than FRED's did.
- **Formatting bugs aren't always the model's fault.** A "missing spaces" bug
  turned out to be the API client's Markdown preview renderer, not the LLM output
  itself — always check the raw response before blaming the model.
- **Precision assumptions don't transfer between sources.** Copying the FRED
  observations schema (`NUMERIC(20,4)`) for the new BCB and SIDRA tables would have
  silently truncated Selic values like `0.052531` to `0.0525` — each source's
  decimal precision needs to be checked, not assumed.
- **Batch jobs need to be genuinely per-source, not one generic job.** FRED, BCB,
  and SIDRA each paginate, rate-limit, and format dates/decimals differently.
  Keeping a separate Spring Batch job (reader/processor/writer) per source — rather
  than a single "generic ingestion job" — kept each adapter's quirks isolated
  instead of leaking into shared step logic.

## License

This is a personal study project. Feel free to fork and adapt it for your own
learning.

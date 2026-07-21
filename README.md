# FRED AI Project

Agente de IA em Java que responde perguntas sobre macroeconomia americana consultando dados reais da API do FRED (Federal Reserve Bank of St. Louis), usando **Spring AI 2.0** + **Spring Boot 4** + **Java 25**.

## Stack

- Java 25
- Spring Boot 4.0.1
- Spring AI 2.0.0 (GA)
- Modelo: Anthropic Claude (trocavel facilmente por OpenAI, Gemini, Ollama etc.)

## Pre-requisitos

1. **JDK 25** instalado (`java -version` deve mostrar 25).
2. **Chave da API do FRED** (gratuita): crie em https://fred.stlouisfed.org/docs/api/api_key.html
3. **Chave da API da Anthropic** (ou troque o starter no `pom.xml` pelo provedor de sua preferencia — veja a secao "Trocando de modelo" abaixo).

## Configuracao

Defina as variaveis de ambiente antes de rodar:

```bash
export FRED_API_KEY=sua_chave_fred_aqui
export ANTHROPIC_API_KEY=sua_chave_anthropic_aqui
```

## Rodando o projeto

```bash
./mvnw spring-boot:run
```

Ou, se preferir empacotar:

```bash
./mvnw clean package
java -jar target/fred-ai-project-0.0.1-SNAPSHOT.jar
```

## Testando

Com a aplicacao rodando em `http://localhost:8080`, chame o endpoint de chat:

```bash
curl -G "http://localhost:8080/chat" --data-urlencode "mensagem=Como esta a inflacao americana nos ultimos meses?"
```

Outros exemplos de pergunta:
- "Qual a taxa de desemprego atual dos Estados Unidos?"
- "Como esta o PIB americano nos ultimos trimestres?"
- "Qual a taxa de juros do FED agora, e como ela mudou nos ultimos meses?"
- "Me traga os ultimos 5 valores do S&P 500 (series_id SP500)"

O modelo vai decidir sozinho qual `@Tool` chamar (definidas em `FredTools.java`), buscar os dados reais na API do FRED, e formular a resposta.

## Estrutura do projeto

```
src/main/java/com/example/fredai/
├── FredAiApplication.java       # classe principal
├── client/FredClient.java       # chamada HTTP para a API do FRED (RestClient)
├── config/ChatClientConfig.java # configuracao do ChatClient + system prompt + tools
├── controller/ChatController.java # endpoint REST /chat
├── dto/                          # records de resposta do FRED
└── tool/FredTools.java           # @Tool methods expostos ao modelo de IA
```

## Series do FRED usadas de exemplo

| Serie      | Descricao                          |
|------------|-------------------------------------|
| GDP        | PIB dos EUA (trimestral)            |
| CPIAUCSL   | CPI / inflacao (mensal)             |
| UNRATE     | Taxa de desemprego (mensal)         |
| FEDFUNDS   | Taxa de juros do FED (mensal)       |

Ha tambem uma tool generica (`serieFredPersonalizada`) que aceita qualquer `series_id` do FRED (ex: `SP500`, `DGS10`, `PAYEMS`, `M2SL`).

## Trocando de modelo (Anthropic -> OpenAI/Gemini/Ollama)

No `pom.xml`, troque a dependencia:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-openai</artifactId>
</dependency>
```

E no `application.yml`, ajuste a secao correspondente (`spring.ai.openai.api-key`, etc). O restante do codigo (tools, controller) nao muda nada — essa e a vantagem da abstracao do Spring AI.

## Proximos passos sugeridos (para continuar aprendendo)

1. Adicionar **memoria de conversa** (`MessageChatMemoryAdvisor`) para o agente lembrar do contexto entre perguntas.
2. Criar um segundo endpoint que gera um **resumo/relatorio** comparando inflacao vs. juros vs. desemprego nos ultimos 12 meses.
3. Migrar o mesmo projeto para **LangChain4j** como exercicio comparativo (usando `AiServices` no lugar do `ChatClient`).
4. Adicionar cache simples para nao bater na API do FRED a cada pergunta repetida.

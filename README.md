# GameCoach Backend

REST API that analyzes League of Legends and Valorant matches 
using Riot Games API and generates AI coaching with Groq.

## Stack
- Java 21 + Spring Boot
- Riot Games API
- Groq API (LLaMA 3.3)

## Endpoints
GET /api/analyze/lol/{gameName}/{tagLine}
GET /api/analyze/valorant/{gameName}/{tagLine}
GET /api/health

## Setup
1. Clone the repo
2. Copy `application.properties.example` → `application.properties`
3. Fill in your API keys
4. Run with `./mvnw spring-boot:run`
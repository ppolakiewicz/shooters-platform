---
project: shooters-platform
researched_at: 2026-05-21
recommended_platform: Railway
runner_up: Render
context_type: brownfield
tech_stack:
  frontend: Angular 21 SPA
  backend: Java 25, Spring Boot 4.0.6, Gradle
  database: PostgreSQL
  e2e: Playwright
decision_inputs:
  persistent_connections: no
  cost_sensitivity: free or very cheap
  platform_familiarity: none
  reach: Europe/Poland is enough
  co_location_preference: prefer co-located on one platform
inputs_used:
  - context/foundation/stack-assessment.md
  - AGENTS.md
missing_inputs:
  - context/foundation/tech-stack.md
  - context/foundation/prd.md
---

# Infrastructure Recommendation

## Recommendation

Use Railway for the MVP deployment.

The deployment shape should be one Railway project in the EU West Metal region (`europe-west4-drams3a`, Amsterdam), with:

- One Spring Boot web service.
- One Railway PostgreSQL service.
- Angular built into static assets and served by the Spring Boot service for the first MVP deploy.

Serving the Angular build from Spring Boot is the cheapest co-located shape for this repository because it avoids paying for a second always-on frontend service while keeping `/api` and browser routing under one origin. The repo can still keep the local developer split (`npm run frontend:start` plus `bootRun`), but production should start as a single web process.

Railway is the best fit because it has a current official Spring Boot guide, supports deploys from CLI, GitHub, and Dockerfile, provides PostgreSQL as a service in the same project, supports a European region, has monorepo guidance, exposes pre-deploy commands for migrations, and now exposes both local and hosted MCP support for agent operations. Current Railway Hobby pricing is $5/month with $5 of included usage; the Free tier has $1 credit and tighter resource limits, so MVP production should assume Hobby rather than "free forever."

## Runner-Up

Render is the runner-up.

Render is more explicit about free web services and has Frankfurt as a region, Docker builds, managed PostgreSQL, infrastructure-as-code, a CLI, an API, and a hosted MCP server for Codex-compatible agent operations. The problem is database durability on the free tier: Free Render Postgres expires 30 days after creation. A real MVP with persistent bookings therefore starts at roughly the paid web service plus paid Postgres floor, not truly free.

## Hard Filters

The current hard stack is Java 25 Spring Boot, Angular, and PostgreSQL. Platforms that cannot host a normal Spring Boot JVM process without replacing the backend architecture are not acceptable as the primary full-stack host.

Cloudflare, Vercel, and Netlify remain useful future options for static frontend hosting, CDN, or edge functions, but they should not be the primary MVP platform for this repository unless the backend is moved to another host or rewritten. Current official runtime support does not make a Spring Boot service a first-class deployment target:

- Cloudflare Workers first-class languages are JavaScript, TypeScript, Python Workers, and Rust, with other languages only via Wasm.
- Vercel official function runtimes include Node.js, Bun, Python, Rust, Go, Ruby, Wasm, and Edge, not Java.
- Netlify Functions currently support TypeScript, JavaScript, and Go.

## Platform Comparison

| Platform | Stack fit | CLI-first | Managed/serverless | Agent-readable docs | Scriptable deploy API | MCP / agent integration | Cost fit | EU fit | Co-location fit | Verdict |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Railway | Pass | Pass | Pass | Pass | Pass | Pass | Pass | Pass | Pass | Recommended |
| Render | Pass | Pass | Pass | Pass | Pass | Pass | Partial | Pass | Pass | Runner-up |
| Fly.io | Pass | Pass | Partial | Pass | Pass | Partial | Fail | Pass | Partial | Good runtime, poor cheap-DB fit |
| Vercel | Fail | Pass | Pass | Pass | Pass | Partial | Pass | Pass | Fail | Frontend-only for this repo |
| Netlify | Fail | Pass | Pass | Pass | Pass | Partial | Pass | Partial | Fail | Frontend-only for this repo |
| Cloudflare | Fail | Pass | Pass | Pass | Pass | Partial | Pass | Pass | Fail | Edge/static-only for this repo |

## Notes By Platform

### Railway

Railway has an official Spring Boot deployment guide updated on 2026-05-21, including CLI, GitHub, and Dockerfile deploy paths. It also has a PostgreSQL service based on an SSL-enabled Postgres image, a monorepo guide, pre-deploy commands for migrations, rollback/redeploy actions, and public/custom domains. Railway's EU West Metal region is Amsterdam. Railway for Agents, checked 2026-05-21, documents CLI, local MCP, hosted MCP, and agent skills support.

Cost posture: assume Hobby at $5/month. Free has only $1/month credit and 0.5 GB RAM per service, which is too tight for a reliable Java + PostgreSQL MVP. Add a budget alert/spend limit before deploy.

### Render

Render supports Docker web services, managed Postgres, static sites, private networking, environment variables, monorepo support, deploy hooks, pre-deploy commands, instant rollbacks, a CLI, a REST API, and a hosted MCP server. Frankfurt is available for services and datastores. Free web services spin down after 15 minutes idle, and Free Postgres expires after 30 days, so the free tier is suitable for demos but not a durable booking MVP.

Cost posture: durable MVP starts above free. Check the current web-service and Postgres pricing during deploy planning because Render has recently changed workspace and Postgres plan language.

### Fly.io

Fly.io can run a Dockerized Spring Boot service well and has strong CLI deploy mechanics (`fly deploy`, rolling/canary/blue-green strategies) plus European Postgres regions including Amsterdam and Frankfurt. It is less aligned with the "free or very cheap plus managed co-location" requirement because managed Postgres starts at $38/month before storage. A cheaper self-managed Postgres-on-volume shape is possible but increases operational burden and backup risk.

### Vercel

Vercel is excellent for frontend deployments and has a beta official MCP server, checked 2026-05-21. It is not a good primary host for this repository because Java is not an official function runtime and a persistent Spring Boot service is outside the normal platform model.

### Netlify

Netlify is strong for static frontend hosting, deploy previews, and serverless functions, but Netlify Functions currently support TypeScript, JavaScript, and Go. That excludes the existing Spring Boot backend as a first-class target.

### Cloudflare

Cloudflare is strong for edge/static workloads, has agent-readable docs and Cloudflare MCP/documentation support, and would be a good later CDN/DNS choice. Workers do not fit the current Spring Boot backend without a rewrite or unusual Wasm/native approach, so it is not the MVP full-stack host.

## Anti-Bias Cross-Check

### Devil's Advocate Against Railway

1. The cheap Railway story can become misleading because $5/month is a subscription floor plus usage accounting, not a hard cap for an always-on Java service and Postgres.
2. Java 25 may require a Dockerfile to make runtime selection explicit; relying on automatic detection could create deploy drift.
3. Serving Angular from Spring Boot reduces cost, but it couples frontend releases to backend deploys and requires careful browser-history fallback configuration.
4. Railway EU West is Amsterdam, not Poland. That is acceptable for the stated requirement, but it is still not local Polish hosting.
5. Railway MCP and agent operations are powerful enough to mutate live infrastructure, so token scope and human confirmation matter from day one.

### Pre-Mortem

Six months after launch, the Railway decision failed because the project treated a cheap prototype setup like a production boundary. The Spring Boot service was deployed without a Dockerfile, so a platform build change caused a Java runtime mismatch during a routine redeploy. The Angular assets were bundled into the backend, which kept costs low, but frontend fixes now waited on backend builds and Flyway migration checks. The database grew modestly, but logs, builds, and always-on memory pushed usage above the expected floor. Nobody had configured budget alerts or a spend ceiling. Agent access was added through MCP for convenience, but the API token was too broad, so routine troubleshooting became uncomfortable around production secrets and variables. The root mistake was not choosing Railway; it was failing to write down the exact production shape, resource assumptions, deploy commands, rollback path, and permission boundary before first deploy.

### Unknown Unknowns

1. Railway's Java autodetection path may lag behind Java 25 expectations; Dockerfile-based deployment is the safer first production contract.
2. The cheapest viable memory size for Spring Boot 4 plus PostgreSQL client pools is not known until measured under the real app jar.
3. Angular-on-Spring production routing may expose missing fallback behavior that local Angular dev-server proxying hides.
4. Railway's agent tooling is current and useful, but practical least-privilege controls for agent sessions need to be verified during setup, not assumed.

## Operational Story

| Axis | Railway MVP answer |
| --- | --- |
| Preview | Use Railway environments only after the first production deploy is stable. For the first deploy, keep GitHub Actions as the quality gate and deploy `main` manually. |
| Secrets | Store `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, and any auth secrets in Railway variables. Do not commit secrets or place them in MCP config. |
| Database | Use a Railway PostgreSQL service in the same project and EU West region. Let Flyway run at app startup for MVP; move to Railway pre-deploy command if migrations become risky or slow. |
| Build | Add a production Dockerfile that builds Angular, copies the built assets into Spring Boot static resources, builds the backend jar, and runs one JVM process. |
| Deploy | Use Railway CLI for the first deploy: link the project/service, set variables, then deploy the Dockerfile-backed service. Use GitHub auto-deploy only after manual deploy is verified. |
| Rollback | Use Railway deployment rollback to restore the previous successful image and variables. Treat database migrations as forward-only; test destructive migrations locally before production. |
| Logs | Start with Railway service logs. Add structured JSON logging later if debugging production issues becomes slow. |
| Approval | Production deploys and variable changes require a human approval step. Agent can prepare commands and inspect logs; destructive database actions remain human-only. |

## Risk Register

| Risk | Source lens | Impact | Mitigation |
| --- | --- | --- | --- |
| Railway usage exceeds the expected cheap MVP floor. | Devil's advocate | Medium | Configure spending controls before deploy and record the initial monthly budget. |
| Java 25 support drifts in automatic build detection. | Unknown unknowns | High | Use a Dockerfile with an explicit Java 25 base image. |
| Serving Angular through Spring Boot breaks SPA fallback routing. | Pre-mortem | Medium | Add production route fallback and verify browser refresh on deep routes. |
| Flyway runs a risky migration during app startup. | Research finding | High | Keep migrations additive for MVP; move to pre-deploy migration command before destructive changes. |
| Agent/MCP permissions are too broad. | Devil's advocate | High | Use scoped tokens, keep secrets in environment variables, and require human confirmation for mutations. |
| Render looks cheaper than it is because Free Postgres expires. | Research finding | Medium | Treat Render free as demo-only; compare against paid durable floor. |
| Fly.io appears operationally elegant but managed Postgres violates the cheap requirement. | Research finding | Medium | Do not pick Fly.io unless cost preference changes or self-managed DB is accepted. |

## Source Checks

- Railway Spring Boot deployment guide, checked 2026-05-21: https://docs.railway.com/guides/spring-boot
- Railway PostgreSQL service docs, checked 2026-05-21: https://docs.railway.com/databases/postgresql
- Railway pricing plans, checked 2026-05-21: https://docs.railway.com/pricing/plans
- Railway regions, checked 2026-05-21: https://docs.railway.com/deployments/regions
- Railway for Agents and MCP docs, checked 2026-05-21: https://docs.railway.com/agents and https://docs.railway.com/cli/mcp
- Render free tier, pricing, regions, deploy, and MCP docs, checked 2026-05-21: https://render.com/docs/free, https://render.com/pricing, https://render.com/docs/regions, https://render.com/docs/deploys, https://render.com/docs/mcp-server
- Fly.io deploy, pricing, and managed Postgres docs, checked 2026-05-21: https://fly.io/docs/launch/deploy/, https://fly.io/docs/about/pricing/, https://fly.io/docs/mpg/
- Vercel runtimes and MCP docs, checked 2026-05-21: https://vercel.com/docs/functions/runtimes and https://vercel.com/docs/agent-resources/vercel-mcp
- Netlify Functions docs, checked 2026-05-21: https://docs.netlify.com/build/functions/overview/
- Cloudflare Workers languages docs, checked 2026-05-21: https://developers.cloudflare.com/workers/languages/

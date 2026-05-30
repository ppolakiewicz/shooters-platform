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
  cost_sensitivity: darmowo albo bardzo tanio
  platform_familiarity: brak
  reach: Europa/Polska wystarczy
  co_location_preference: preferowana kolokacja na jednej platformie
inputs_used:
  - context/foundation/stack-assessment.md
  - AGENTS.md
missing_inputs:
  - context/foundation/tech-stack.md
  - context/foundation/prd.md
---

# Rekomendacja Infrastruktury

## Rekomendacja

Użyj Railway do wdrożenia MVP.

Docelowy kształt wdrożenia powinien być jednym projektem Railway w regionie EU West Metal (`europe-west4-drams3a`,
Amsterdam), z:

- Jedną usługą webową Spring Boot.
- Jedną usługą Railway PostgreSQL.
- Angularem zbudowanym do statycznych assetów i serwowanym przez usługę Spring Boot dla pierwszego wdrożenia MVP.

Serwowanie buildu Angulara ze Spring Boot to najtańszy kolokowany kształt dla tego repozytorium, bo unika opłacania
drugiej zawsze uruchomionej usługi frontendowej, a jednocześnie utrzymuje `/api` i routing przeglądarkowy pod jednym
originem. Repo nadal może zachować lokalny podział developerski (`npm run frontend:start` plus `bootRun`), ale produkcja
powinna zacząć jako jeden proces webowy.

Railway jest najlepszym dopasowaniem, ponieważ ma aktualny oficjalny przewodnik Spring Boot, wspiera wdrożenia z CLI,
GitHuba i Dockerfile, dostarcza PostgreSQL jako usługę w tym samym projekcie, wspiera region europejski, ma wskazówki
dla monorepo, udostępnia pre-deploy commands dla migracji oraz obecnie udostępnia zarówno lokalne, jak i hostowane
wsparcie MCP dla operacji agentów. Obecna cena Railway Hobby to $5/miesiąc z $5 wliczonego użycia; Free tier ma $1
kredytu i ciaśniejsze limity zasobów, więc produkcyjne MVP powinno zakładać Hobby zamiast "free forever".

## Drugi Wybór

Render jest drugim wyborem.

Render bardziej jednoznacznie opisuje darmowe usługi webowe i ma Frankfurt jako region, buildy Docker, zarządzany
PostgreSQL, infrastructure-as-code, CLI, API i hostowany serwer MCP dla operacji agentowych kompatybilnych z Codex.
Problemem jest trwałość bazy danych na darmowym tierze: Free Render Postgres wygasa 30 dni po utworzeniu. Prawdziwe MVP
z trwałymi rezerwacjami startuje więc mniej więcej od kosztu płatnej usługi webowej plus płatnego Postgresa, a nie jest
naprawdę darmowe.

## Twarde Filtry

Obecny twardy stos to Java 25 Spring Boot, Angular i PostgreSQL. Platformy, które nie mogą hostować normalnego procesu
JVM Spring Boot bez wymiany architektury backendu, nie są akceptowalne jako główny host full-stack.

Cloudflare, Vercel i Netlify pozostają użytecznymi przyszłymi opcjami dla statycznego hostingu frontendu, CDN albo edge
functions, ale nie powinny być główną platformą MVP dla tego repozytorium, chyba że backend zostanie przeniesiony do
innego hosta albo przepisany. Obecne oficjalne wsparcie runtime nie czyni usługi Spring Boot pierwszoklasowym celem
wdrożeniowym:

- Języki pierwszej klasy Cloudflare Workers to JavaScript, TypeScript, Python Workers i Rust, a inne języki tylko przez
  Wasm.
- Oficjalne runtime'y funkcji Vercel obejmują Node.js, Bun, Python, Rust, Go, Ruby, Wasm i Edge, ale nie Java.
- Netlify Functions obecnie wspiera TypeScript, JavaScript i Go.

## Porównanie Platform

| Platforma  | Dopasowanie stosu | CLI-first | Managed/serverless | Dokumentacja czytelna dla agentów | Skryptowalne API deployu | MCP / integracja agentowa | Dopasowanie kosztu | Dopasowanie EU | Dopasowanie kolokacji | Werdykt                         |
|------------|-------------------|-----------|--------------------|-----------------------------------|--------------------------|---------------------------|--------------------|----------------|-----------------------|---------------------------------|
| Railway    | Pass              | Pass      | Pass               | Pass                              | Pass                     | Pass                      | Pass               | Pass           | Pass                  | Rekomendowana                   |
| Render     | Pass              | Pass      | Pass               | Pass                              | Pass                     | Pass                      | Partial            | Pass           | Pass                  | Drugi wybór                     |
| Fly.io     | Pass              | Pass      | Partial            | Pass                              | Pass                     | Partial                   | Fail               | Pass           | Partial               | Dobry runtime, słabe tanie DB   |
| Vercel     | Fail              | Pass      | Pass               | Pass                              | Pass                     | Partial                   | Pass               | Pass           | Fail                  | Tylko frontend dla tego repo    |
| Netlify    | Fail              | Pass      | Pass               | Pass                              | Pass                     | Partial                   | Pass               | Partial        | Fail                  | Tylko frontend dla tego repo    |
| Cloudflare | Fail              | Pass      | Pass               | Pass                              | Pass                     | Partial                   | Pass               | Pass           | Fail                  | Tylko edge/static dla tego repo |

## Notatki Według Platform

### Railway

Railway ma oficjalny przewodnik wdrożenia Spring Boot zaktualizowany 2026-05-21, obejmujący ścieżki deployu przez CLI,
GitHub i Dockerfile. Ma też usługę PostgreSQL opartą o obraz Postgres z włączonym SSL, przewodnik monorepo, pre-deploy
commands dla migracji, akcje rollback/redeploy oraz domeny publiczne/custom. Region EU West Metal Railway to Amsterdam.
Railway for Agents, sprawdzone 2026-05-21, dokumentuje CLI, lokalne MCP, hostowane MCP i wsparcie agent skills.

Postawa kosztowa: zakładaj Hobby za $5/miesiąc. Free ma tylko $1/miesiąc kredytu i 0.5 GB RAM na usługę, co jest zbyt
ciasne dla niezawodnego MVP Java + PostgreSQL. Dodaj alert budżetowy/limit wydatków przed deployem.

### Render

Render wspiera web services Docker, zarządzany Postgres, static sites, private networking, zmienne środowiskowe,
wsparcie monorepo, deploy hooks, pre-deploy commands, natychmiastowe rollbacki, CLI, REST API i hostowany serwer MCP.
Frankfurt jest dostępny dla usług i datastore'ów. Darmowe usługi webowe usypiają po 15 minutach bezczynności, a Free
Postgres wygasa po 30 dniach, więc darmowy tier nadaje się do demo, ale nie do trwałego MVP rezerwacji.

Postawa kosztowa: trwałe MVP startuje powyżej free. Sprawdź aktualne ceny web-service i Postgres podczas planowania
deployu, ponieważ Render ostatnio zmieniał język planów workspace i Postgres.

### Fly.io

Fly.io może dobrze uruchomić zdockeryzowaną usługę Spring Boot i ma mocne mechanizmy deployu CLI (`fly deploy`,
strategie rolling/canary/blue-green) oraz europejskie regiony Postgres, w tym Amsterdam i Frankfurt. Jest mniej
dopasowane do wymagania "darmowo albo bardzo tanio plus zarządzana kolokacja", ponieważ managed Postgres zaczyna się
od $38/miesiąc przed storage. Tańszy kształt self-managed Postgres-on-volume jest możliwy, ale zwiększa ciężar
operacyjny i ryzyko backupu.

### Vercel

Vercel jest świetny dla wdrożeń frontendu i ma beta oficjalny serwer MCP, sprawdzone 2026-05-21. Nie jest dobrym głównym
hostem dla tego repozytorium, ponieważ Java nie jest oficjalnym runtime'em funkcji, a trwała usługa Spring Boot wychodzi
poza normalny model platformy.

### Netlify

Netlify jest mocne dla statycznego hostingu frontendu, deploy previews i funkcji serverless, ale Netlify Functions
obecnie wspiera TypeScript, JavaScript i Go. To wyklucza istniejący backend Spring Boot jako pierwszoklasowy cel.

### Cloudflare

Cloudflare jest mocne dla zadań edge/static, ma dokumentację czytelną dla agentów i wsparcie Cloudflare
MCP/dokumentacji, a później byłoby dobrym wyborem dla CDN/DNS. Workers nie pasują do obecnego backendu Spring Boot bez
przepisu albo nietypowego podejścia Wasm/native, więc nie jest to host full-stack dla MVP.

## Kontrola Anty-Bias

### Devil's Advocate Przeciw Railway

1. Tania opowieść Railway może być myląca, ponieważ $5/miesiąc to podłoga subskrypcji plus rozliczenie użycia, a nie
   twardy limit dla zawsze uruchomionej usługi Java i Postgres.
2. Java 25 może wymagać Dockerfile, żeby jawnie wybrać runtime; poleganie na automatycznym wykrywaniu może spowodować
   dryf deployu.
3. Serwowanie Angulara ze Spring Boot obniża koszt, ale wiąże wydania frontendu z deployami backendu i wymaga ostrożnej
   konfiguracji fallbacku historii przeglądarki.
4. Railway EU West to Amsterdam, nie Polska. To akceptowalne dla deklarowanego wymagania, ale nadal nie jest lokalnym
   polskim hostingiem.
5. Railway MCP i operacje agentów są na tyle silne, że mogą mutować żywą infrastrukturę, więc zakres tokenów i
   potwierdzenie człowieka mają znaczenie od pierwszego dnia.

### Pre-Mortem

Sześć miesięcy po starcie decyzja o Railway zawiodła, bo projekt potraktował tanie ustawienie prototypowe jak granicę
produkcyjną. Usługa Spring Boot została wdrożona bez Dockerfile, więc zmiana buildu platformy spowodowała niedopasowanie
runtime'u Java podczas rutynowego redeployu. Assety Angulara były bundlowane do backendu, co trzymało koszty nisko, ale
poprawki frontendu zaczęły czekać na buildy backendu i kontrole migracji Flyway. Baza danych urosła umiarkowanie, ale
logi, buildy i pamięć always-on wypchnęły użycie ponad oczekiwaną podłogę. Nikt nie skonfigurował alertów budżetowych
ani limitu wydatków. Dostęp agentowy dodano przez MCP dla wygody, ale token API był zbyt szeroki, więc rutynowe
debugowanie stało się niekomfortowe wokół sekretów produkcyjnych i zmiennych. Głównym błędem nie był wybór Railway; było
nim niezapisanie dokładnego kształtu produkcji, założeń zasobów, komend deployu, ścieżki rollbacku i granicy uprawnień
przed pierwszym deployem.

### Nieznane Niewiadome

1. Ścieżka autodetekcji Java w Railway może opóźniać się względem oczekiwań Java 25; wdrożenie oparte o Dockerfile jest
   bezpieczniejszym pierwszym kontraktem produkcyjnym.
2. Najtańszy realny rozmiar pamięci dla Spring Boot 4 plus pule klientów PostgreSQL nie jest znany, dopóki nie zostanie
   zmierzony na prawdziwym jarze aplikacji.
3. Produkcyjny routing Angular-on-Spring może ujawnić brakujące zachowanie fallback, które lokalne proxy dev-servera
   Angular ukrywa.
4. Narzędzia agentowe Railway są aktualne i użyteczne, ale praktyczne kontrole least-privilege dla sesji agentów trzeba
   zweryfikować podczas konfiguracji, a nie zakładać.

## Historia Operacyjna

| Oś          | Odpowiedź Railway MVP                                                                                                                                                                                                 |
|-------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Preview     | Użyj Railway environments dopiero po ustabilizowaniu pierwszego deployu produkcyjnego. Dla pierwszego deployu zachowaj GitHub Actions jako bramę jakości i deployuj `main` ręcznie.                                   |
| Sekrety     | Przechowuj `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` i wszystkie sekrety auth w zmiennych Railway. Nie commituj sekretów i nie umieszczaj ich w konfiguracji MCP.           |
| Baza danych | Użyj usługi Railway PostgreSQL w tym samym projekcie i regionie EU West. Pozwól Flyway działać przy starcie aplikacji dla MVP; przenieś do Railway pre-deploy command, jeśli migracje staną się ryzykowne albo wolne. |
| Build       | Dodaj produkcyjny Dockerfile, który buduje Angulara, kopiuje zbudowane assety do zasobów statycznych Spring Boot, buduje jar backendu i uruchamia jeden proces JVM.                                                   |
| Deploy      | Użyj Railway CLI dla pierwszego deployu: połącz projekt/usługę, ustaw zmienne, a potem wdroż usługę opartą o Dockerfile. Włącz GitHub auto-deploy dopiero po zweryfikowaniu ręcznego deployu.                         |
| Rollback    | Użyj rollbacku deploymentu Railway, aby przywrócić poprzedni udany obraz i zmienne. Traktuj migracje bazy jako forward-only; testuj destrukcyjne migracje lokalnie przed produkcją.                                   |
| Logi        | Zacznij od logów usług Railway. Dodaj ustrukturyzowane logowanie JSON później, jeśli debugowanie problemów produkcyjnych stanie się powolne.                                                                          |
| Akceptacja  | Deploye produkcyjne i zmiany zmiennych wymagają kroku akceptacji człowieka. Agent może przygotować komendy i analizować logi; destrukcyjne akcje na bazie danych pozostają wyłącznie dla człowieka.                   |

## Rejestr Ryzyk

| Ryzyko                                                                               | Soczewka źródłowa   | Wpływ  | Mitygacja                                                                                                               |
|--------------------------------------------------------------------------------------|---------------------|--------|-------------------------------------------------------------------------------------------------------------------------|
| Użycie Railway przekracza oczekiwaną tanią podłogę MVP.                              | Devil's advocate    | Medium | Skonfiguruj kontrolę wydatków przed deployem i zapisz początkowy miesięczny budżet.                                     |
| Wsparcie Java 25 dryfuje w automatycznej detekcji buildu.                            | Nieznane niewiadome | High   | Użyj Dockerfile z jawnym obrazem bazowym Java 25.                                                                       |
| Serwowanie Angulara przez Spring Boot psuje fallback routing SPA.                    | Pre-mortem          | Medium | Dodaj produkcyjny fallback tras i zweryfikuj odświeżanie przeglądarki na głębokich trasach.                             |
| Flyway uruchamia ryzykowną migrację przy starcie aplikacji.                          | Wynik researchu     | High   | Trzymaj migracje addytywne dla MVP; przenieś do pre-deploy migration command przed destrukcyjnymi zmianami.             |
| Uprawnienia Agent/MCP są zbyt szerokie.                                              | Devil's advocate    | High   | Używaj zakresowanych tokenów, trzymaj sekrety w zmiennych środowiskowych i wymagaj potwierdzenia człowieka dla mutacji. |
| Render wygląda taniej niż jest, bo Free Postgres wygasa.                             | Wynik researchu     | Medium | Traktuj Render free jako tylko demo; porównuj z płatną trwałą podłogą.                                                  |
| Fly.io wygląda operacyjnie elegancko, ale managed Postgres łamie wymaganie taniości. | Wynik researchu     | Medium | Nie wybieraj Fly.io, chyba że zmieni się preferencja kosztowa albo zaakceptowany zostanie self-managed DB.              |

## Sprawdzone Źródła

- Railway Spring Boot deployment guide, sprawdzone 2026-05-21: https://docs.railway.com/guides/spring-boot
- Railway PostgreSQL service docs, sprawdzone 2026-05-21: https://docs.railway.com/databases/postgresql
- Railway pricing plans, sprawdzone 2026-05-21: https://docs.railway.com/pricing/plans
- Railway regions, sprawdzone 2026-05-21: https://docs.railway.com/deployments/regions
- Railway for Agents i dokumentacja MCP, sprawdzone 2026-05-21: https://docs.railway.com/agents
  oraz https://docs.railway.com/cli/mcp
- Render free tier, ceny, regiony, deploy i dokumentacja MCP, sprawdzone
  2026-05-21: https://render.com/docs/free, https://render.com/pricing, https://render.com/docs/regions, https://render.com/docs/deploys, https://render.com/docs/mcp-server
- Fly.io deploy, ceny i dokumentacja managed Postgres, sprawdzone
  2026-05-21: https://fly.io/docs/launch/deploy/, https://fly.io/docs/about/pricing/, https://fly.io/docs/mpg/
- Vercel runtimes i dokumentacja MCP, sprawdzone 2026-05-21: https://vercel.com/docs/functions/runtimes
  oraz https://vercel.com/docs/agent-resources/vercel-mcp
- Netlify Functions docs, sprawdzone 2026-05-21: https://docs.netlify.com/build/functions/overview/
- Cloudflare Workers languages docs, sprawdzone 2026-05-21: https://developers.cloudflare.com/workers/languages/

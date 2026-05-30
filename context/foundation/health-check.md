---
project: shooters-platform
checked_at: 2026-05-19T22:11:59.5899917+02:00
health_status: needs-attention
context_type: brownfield
language_family: multi
stack_assessment_available: true
checks_run:
  - lockfile
  - dependency_audit
  - outdated_deps
  - test_runner
  - ci_cd
  - configuration
audit_findings:
  critical: 0
  high: 0
  moderate: 0
  low: 0
test_runner_detected: true
ci_provider: GitHub Actions
recommended_fixes: 5
---

## Kondycja Zależności

### Lockfile

Status: obecny (`package-lock.json`, `backend/gradle.lockfile`)
Menedżer pakietów: npm workspaces i Gradle Wrapper

Główny npm workspace jest przypięty przez `package-lock.json`. Backend używa blokowania zależności Gradle przez
`backend/gradle.lockfile`, więc obie wykryte powierzchnie zarządzania pakietami mają odtwarzalny stan zależności.

### Audyt Bezpieczeństwa

Narzędzie: `npm.cmd audit --json`; audyt Java pominięty - w tabeli dispatch tego health-checku nie istnieje wbudowana
komenda audytu Java.
Podsumowanie: 0 CRITICAL, 0 HIGH, 0 MODERATE, 0 LOW
Bezpośrednie vs przechodnie: npm nie zgłosił podatności wśród 677 zależności.

Rekomendowane zewnętrzne narzędzie Java: repozytorium ma już `.github/workflows/osv-scanner.yml`, które uruchamia OSV
Scanner rekurencyjnie na pull requestach, merge queue, pushach do `main`, tygodniowym harmonogramie i ręcznym wywołaniu.

### Nieaktualne Zależności

Pakiety z lukami major version: nie udało się ustalić w tym uruchomieniu.

Próbowano `npm.cmd outdated --json`, ale prośba o eskalację została odrzucona, ponieważ wysłałaby metadane prywatnych
pakietów workspace do zewnętrznego rejestru npm. Kontrola jest zapisana jako pominięta zamiast ponawiana inną ścieżką.

## Zestaw Testów

Test runner: Vitest, Spock/JUnit Platform, Playwright
Znalezione testy: 34 testy frontendu, 81 testów backendu, 4 testy e2e
Wykonanie testów: frontend i backend przechodzą; enumeracja e2e przechodzi, pełnego wykonania e2e nie próbowano w tym
lokalnym health-checku

Konfiguracja: `frontend/angular.json`, `backend/build.gradle`, `e2e/playwright.config.ts`
Framework: Angular unit-test builder z Vitest 4.1.5, Spock 2.4 na JUnit Platform, Playwright 1.59.1

Uruchomione komendy weryfikacyjne:

```powershell
npm.cmd run test --workspace frontend
.\gradlew.bat test --no-daemon
npm.cmd exec --workspace e2e -- playwright test --list
```

Wyniki:

- Frontend: przeszło 13 plików testowych, 34 testy.
- Backend: Gradle `test` zakończył się sukcesem; istniejące raporty JUnit XML pokazują 18 suite'ów, 81 testów, 0
  failure, 0 error.
- E2E: wykryto 4 testy Playwright w 3 plikach.

## CI/CD

Dostawca: GitHub Actions
Konfiguracja: `.github/workflows/ci.yml`, `.github/workflows/osv-scanner.yml`

| Etap       | Status  | Notatki                                                                                                                                                                           |
|------------|---------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Lint       | missing | `frontend/package.json` ma `lint`, ale CI go nie uruchamia. Nie skonfigurowano osobnego backendowego zadania wyłącznie style/lint poza Error Prone i NullAway podczas kompilacji. |
| Test       | present | CI uruchamia testy backendu, testy frontendu i testy e2e Playwright.                                                                                                              |
| Build      | present | CI uruchamia `backend/gradlew build` i `npm run frontend:build`.                                                                                                                  |
| Type check | present | Build Angular egzekwuje rygorystyczne sprawdzanie TypeScript/szablonów; kompilacja backendu egzekwuje kompilację Java, Error Prone i NullAway.                                    |
| Security   | present | Osobny workflow OSV Scanner uruchamia skany rekurencyjne.                                                                                                                         |

## Konfiguracja

### Wysoka ważność

Nie wykryto luk konfiguracyjnych wysokiej ważności.

### Średnia ważność

- **Konfiguracja formatera** - nie znaleziono `.prettierrc*` ani `biome.json`. Ma to znaczenie, ponieważ edycje agentów
  będą bardziej spójne, gdy formatowanie jest jawne. Naprawa: dodaj konfigurację formatera, na przykład
  `npm.cmd install --save-dev prettier` i główny `.prettierrc.json`, albo przyjmij Biome, jeśli chcesz jedno narzędzie
  do formatowania i lintingu.
- **Pokrycie lint w CI** - CI nie uruchamia istniejącego skryptu lint frontendu. Ma to znaczenie, ponieważ edycje
  agentów mogą przechodzić testy, a nadal naruszać styl albo reguły statyczne. Naprawa: dodaj krok GitHub Actions taki
  jak `npm run lint --workspace frontend`.

### Niska ważność

- **Szablon środowiska** - nie znaleziono `.env.example` ani `.env.template`. Ma to znaczenie, ponieważ agenci i nowi
  kontrybutorzy potrzebują stabilnego źródła wymaganych zmiennych lokalnych. Naprawa: dodaj `.env.example` dokumentujący
  lokalne zmienne PostgreSQL i aplikacji bez sekretów.

Obecne i zdrowe: `.editorconfig`, `.gitignore`, `AGENTS.md`, rygorystyczny `frontend/tsconfig.json`, konfiguracja ESLint
frontendu, lokalny `docker-compose.yml`, npm lockfile i Gradle lockfile.

## Odniesienie Do Oceny Stosu

Ocena stosu: `context/foundation/stack-assessment.md`
Gotowość agentowa (ze stack-assess): ready

| Luka bramy jakości                                         | Ustalenie health-checku                                                                                                                                        | Status     |
|------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------|------------|
| brak                                                       | Ocena stosu wykazała 20 zaliczonych bram i 0 niezaliczonych.                                                                                                   | Wzmocnione |
| rekomendowane instrukcje agentowe specyficzne dla projektu | `AGENTS.md` istnieje, ale wcześniejsza ocena stosu zauważa, że dokumentuje głównie workflow 10x zamiast konwencji Angular/Spring specyficznych dla codebase'u. | Follow-up  |

## Rekomendowane Naprawy

### Napraw przed pracą agentową (Kategoria A)

### 1. Dodaj jawną konfigurację formatera

**Wpływ**: Zmiany generowane przez agentów będą łatwiejsze do review i mniej prawdopodobne, że stworzą szum stylu.
**Ważność**: średnia
**Nakład**: umiarkowany (15-30 min)
**Naprawa**:

```powershell
npm.cmd install --save-dev prettier
```

Potem dodaj główny `.prettierrc.json` i skrypt formatowania albo wybierz `biome.json`, jeśli chcesz połączony
formatter/linter.

### 2. Uruchamiaj lint w CI

**Wpływ**: Agenci mogą obecnie polegać na testach i buildach, ale nie na feedbacku lint egzekwowanym przez CI.
**Ważność**: średnia
**Nakład**: szybki (< 5 min)
**Naprawa**:

Dodaj to po kroku testów frontendu w `.github/workflows/ci.yml`:

```yaml
- name: Lint frontend
  run: npm run lint --workspace frontend
```

### 3. Zdecyduj, jak obsługiwać kontrole świeżości zależności

**Wpływ**: Audyt bezpieczeństwa jest czysty, ale to uruchomienie nie mogło porównać zainstalowanych wersji pakietów z
najnowszymi wersjami w rejestrze bez eksportowania metadanych prywatnych pakietów.
**Ważność**: niska
**Nakład**: szybki (< 5 min)
**Naprawa**:

Uruchom `npm.cmd outdated --json` dopiero po jawnej akceptacji dostępu do zewnętrznego rejestru albo polegaj na zaufanym
wewnętrznym narzędziu do śledzenia świeżości zależności.

### 4. Dodaj szablon środowiska

**Wpływ**: Agenci i nowi kontrybutorzy mogą wywnioskować lokalny setup z `docker-compose.yml`, ale jawny szablon
ogranicza ponowne odkrywanie.
**Ważność**: niska
**Nakład**: szybki (< 5 min)
**Naprawa**:

Utwórz `.env.example` z niesekretnymi lokalnymi wartościami domyślnymi i udokumentuj, które wartości są wymagane przez
backend i frontend.

### Zaadresowane w nadchodzących lekcjach (Kategoria B)

### Instrukcje AI assistant specyficzne dla projektu

**Lekcja
**: [Agent Onboarding: Agents.md, AI Rules i feedback loops (M1L4)](https://platforma.przeprogramowani.pl/external/10xdevs-3/m1-l4)
**Co tam zrobisz**: rozszerzysz istniejący `AGENTS.md` o konwencje Angular, Spring Boot, testowania i granic specyficzne
dla codebase'u, zamiast teraz generować generyczny stub.

## Podsumowanie

Status zdrowia: needs-attention

Projekt jest w solidnym stanie operacyjnym dla developmentu wspieranego agentami: zależności są zablokowane, npm audit
jest czysty, rygorystyczne typowanie jest włączone, testy frontendu i backendu przechodzą, testy e2e są wykrywalne, a
GitHub Actions obejmuje build/test/security. Główne luki to dopracowanie workflow, a nie blokery: brak jawnego
formatera, brak kroku lint w CI, brak szablonu środowiska i brak zaakceptowanej zewnętrznej kontroli świeżości
zależności dla tego uruchomienia.

Następny krok: zaadresować lekkie naprawy kategorii A powyżej, a potem przejść do agent onboarding.

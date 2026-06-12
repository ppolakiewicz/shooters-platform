# CRUD szablonów szkoleń dla Organizatorów

## Przegląd

Zastępujemy istniejące, częściowe pojęcie `TrainingEnrollment` pełnym CRUD-em `TrainingTemplate`, ograniczonym do
zasobów właściciela i dostępnym wyłącznie dla użytkowników z rolą `ORGANIZER`. Zmiana uzupełnia schemat i API, oddziela
zarządzanie szablonami od istniejącego ekranu terminów oraz dodaje osobne, dwujęzyczne widoki Angular bez zmiany
uprawnień istniejących operacji rezerwacyjnych.

## Analiza stanu obecnego

- Backend obsługuje już tworzenie, listowanie i aktualizację w pakiecie `bookings.trainingenrollment`, ale nie ma
  szczegółów ani usuwania z kontrolą właściciela. Domena sprawdza tylko dolne granice wartości liczbowych i nie zawiera
  `defaultStartTime` (
  `backend/src/main/java/com/shootersplatform/backend/bookings/trainingenrollment/domain/TrainingEnrollment.java:11`).
- Repozytorium sortuje obecnie szablony po `createdAt`, podczas gdy nowy kontrakt wymaga malejącego `updatedAt` (
  `backend/src/main/java/com/shootersplatform/backend/bookings/trainingenrollment/infrastructure/JpaTrainingEnrollmentRepository.java:25`).
- Kontroler używa `/api/bookings/training-enrollments` i wymaga wyłącznie roli `USER` (
  `backend/src/main/java/com/shootersplatform/backend/bookings/web/BookingTrainingEnrollmentsController.java:20`).
- Model tożsamości i początkowa migracja zawierają wyłącznie rolę `USER` (
  `backend/src/main/java/com/shootersplatform/backend/identity/domain/UserRole.java:3`,
  `backend/src/main/resources/db/migration/V1__create_identity_tables.sql:13`).
- Komponent Angular `/bookings` ładuje jednocześnie szablony i terminy, a następnie kopiuje wybrany szablon do
  formularza terminu (`frontend/src/app/bookings/booking-admin.component.ts:122`,
  `frontend/src/app/bookings/booking-admin.component.ts:217`). Zabezpieczenie API szablonów rolą `ORGANIZER` przed
  usunięciem tej zależności zepsułoby ekran dla użytkownika posiadającego tylko `USER`.
- Odpowiedzi uwierzytelniania przekazują już nazwy ról do Angulara, ale routing ma tylko guard sprawdzający
  uwierzytelnienie (`frontend/src/app/identity/auth.service.ts:6`, `frontend/src/app/identity/auth.guard.ts:6`).
- Frontend nie ma ustalonego wzorca routowanego CRUD-u, guarda `CanDeactivate`, dialogu Material ani powiadomień
  snackbar. Należy je wprowadzić w wydzielonej funkcji szablonów zamiast rozbudowywać obecny komponent administracji
  rezerwacjami.
- Istniejące testy Playwright tworzą `TrainingEnrollment` przez obecny ekran `/bookings` i stare API. Playwright jest
  jawnie poza zakresem tej zmiany, dlatego scenariusze te mogą wymagać późniejszego dostosowania przy planowaniu
  publikowania terminów.

## Oczekiwany stan końcowy

Uwierzytelniony użytkownik posiadający role `USER` i `ORGANIZER` może przejść do chronionej sekcji szablonów szkoleń,
wyświetlić wyłącznie własne szablony, utworzyć szablon, zobaczyć jego szczegóły, edytować go i trwale usunąć. Użytkownik
bez `ORGANIZER` nie widzi nawigacji do szablonów, po bezpośrednim wejściu na trasę szablonów jest przekierowywany na
stronę główną, a każda operacja API szablonów zwraca mu `403 Forbidden`.

Backend udostępnia dla tego zasobu wyłącznie `/api/bookings/training-templates`, zwraca spójne `404 Not Found` dla
brakujących i cudzych szablonów, waliduje cały kontrakt oraz zapisuje model w `booking_training_templates`. Uprawnienia
i zachowanie terminów, rezerwacji oraz listy rezerwowej pozostają bez zmian.

### Kluczowe ustalenia

- Przemianowanie jest rzeczywistą migracją domeny, API i schematu, a nie zmianą etykiety. Odwołania obejmują kod
  produkcyjny, fixture'y testów backendu, modele i serwisy Angular, tłumaczenia oraz Playwright (
  `backend/src/main/java/com/shootersplatform/backend/bookings/trainingenrollment`,
  `frontend/src/app/bookings/booking.models.ts:17`, `e2e/tests/bookings.spec.ts:20`).
- Baza danych przechowuje już dane wielokrotnego użytku w `booking_training_enrollments`. Migracja V4 jest niezmienna,
  więc nowa migracja musi przemianować i zaostrzyć istniejącą tabelę (
  `backend/src/main/resources/db/migration/V4__create_booking_tables.sql:1`).
- Konfiguracja bezpieczeństwa dopuszcza obecnie wszystkich uwierzytelnionych użytkowników `USER` do `/api/bookings/**`.
  Reguła szablonów musi być bardziej restrykcyjna bez zmiany zachowania pozostałych endpointów (
  `backend/src/main/java/com/shootersplatform/backend/shared/config/security/SecurityConfiguration.java:57`).
- Moduł bookings ma już wymagany układ `web -> usecase -> domain/infrastructure`, dlatego przemianowany podmoduł
  `trainingtemplate` powinien go zachować i dodać jedną publiczną klasę use case na operację CRUD.

## Czego nie robimy

- Nie tworzymy, nie publikujemy i nie edytujemy terminów poza usunięciem ich frontendowej zależności od szablonów.
- Nie wysyłamy `templateId` do API terminów i nie zapisujemy relacji szablon-termin.
- Nie kopiujemy danych szablonu do formularza terminu w tej zmianie.
- Nie zmieniamy autoryzacji terminów, rezerwacji, listy rezerwowej ani innych istniejących operacji bookings.
- Nie budujemy interfejsu administracyjnego do zarządzania rolami i nie nadajemy automatycznie `ORGANIZER`.
- Nie dodajemy duplikowania, współdzielenia, wersjonowania, wersji roboczych, archiwum, historii, paginacji,
  wyszukiwania ani filtrowania szablonów.
- Nie dodajemy wyszukiwarki adresów, integracji z mapą ani obsługi stref czasowych innych niż `Europe/Warsaw`.
- Nie dodajemy ani nie przepisujemy scenariuszy Playwright w ramach tej zmiany.

## Podejście implementacyjne

Zmianę realizujemy w czterech fazach z ręczną bramą po każdej z nich. Najpierw ustanawiamy końcowy schemat i słownik
ról. Następnie przemianowujemy i uzupełniamy zasób backendowy, zachowując granice modułu bookings. Przed wprowadzeniem
frontendu chronionego nową rolą usuwamy zależność starego `/bookings` od szablonów, aby zwykłe konta `USER` nadal miały
działający ekran terminów. Na końcu dodajemy wydzieloną funkcję Angular `training-templates` z routingiem ról,
współdzielonym formularzem, potwierdzeniem usuwania, powiadomieniami i tłumaczeniami.

Stare API i nazwy `TrainingEnrollment` w Javie oraz TypeScript zostają usunięte bez aliasów. Jest to bezpieczne,
ponieważ opis zmiany potwierdza brak danych biznesowych i wymogu kompatybilności. Flyway nadal wykonuje przemianowanie
tabeli w miejscu, aby zachować poprawną historię migracji.

## Krytyczne szczegóły implementacyjne

Migracja i przemianowanie aplikacji muszą trafić do tej samej wdrażalnej fazy: po zmianie nazwy tabeli przez Flyway
żadna encja produkcyjna nie może nadal wskazywać `booking_training_enrollments`. Frontend `/bookings` musi także
przestać pobierać szablony, zanim aplikacja będzie używana przez konta bez `ORGANIZER`; inaczej równoległe ładowanie
początkowe zakończy się błędem mimo zachowania uprawnień do terminów.

## Faza 1: Schemat, rola i konfiguracja administracyjna

### Przegląd

Wprowadzamy trwałą rolę `ORGANIZER`, migrujemy tabelę danych wielokrotnego użytku do końcowego schematu szablonów i
dokumentujemy administracyjne nadawanie roli bez zmiany zachowania rejestracji.

### Wymagane zmiany

#### 1. Migracja Flyway

**Plik**: `backend/src/main/resources/db/migration/V9__rename_training_templates_and_add_organizer_role.sql`

**Cel**: Przemianować tabelę i obiekty bazy, dodać wymaganą lokalną godzinę rozpoczęcia, zaostrzyć ograniczenia zgodnie
z domeną i dodać nową rolę.

**Kontrakt**: Przemianować `booking_training_enrollments` na `booking_training_templates`; przemianować
`ix_booking_training_enrollments_owner` i wszystkie ograniczenia `ck_booking_training_enrollments_*`; zmienić indeks
właściciela na `(owner_user_id, updated_at desc)`; dodać niepuste `default_start_time time` z tymczasową wartością
domyślną potrzebną do migracji, a następnie usunąć tę wartość domyślną. Ograniczyć pojemność do `1..10`, dni anulowania
do `0..365`, czas trwania do `30..1440` i wielokrotności 30, godzinę rozpoczęcia do kwadransów bez sekund, szerokość do
`-90..90`, długość do `-180..180`, a poziom do istniejących wartości enum. Dodać `ORGANIZER` do `roles` w sposób
idempotentny.

#### 2. Słownik ról tożsamości

**Pliki**:

- `backend/src/main/java/com/shootersplatform/backend/identity/domain/UserRole.java`
- `backend/src/test/groovy/com/shootersplatform/backend/identity/domain/IdentityServiceSpec.groovy`
- `backend/src/test/groovy/com/shootersplatform/backend/identity/usecase/RegisterUserUseCaseSpec.groovy`
- `backend/src/test/groovy/com/shootersplatform/backend/identity/infrastructure/JpaIdentityIntegrationSpec.groovy`
- `backend/src/test/groovy/com/shootersplatform/backend/identity/web/AuthControllerSecuritySpec.groovy`

**Cel**: Uczynić `ORGANIZER` rozpoznawaną rolą i udowodnić, że zwykła rejestracja nadal nadaje wyłącznie `USER`, a
uwierzytelnianie zwraca wszystkie zapisane role.

**Kontrakt**: `UserRole` zawiera `USER` i `ORGANIZER`. Sygnatura rejestracji ani domyślne role nie zmieniają się. Test
integracyjny zapisuje i odczytuje użytkownika z obiema rolami bez założenia, że konto ma tylko jedną rolę.

#### 3. Dokumentacja administracyjnego nadawania roli

**Plik**: `README.md`

**Cel**: Udokumentować wspierane polecenie PostgreSQL nadające `ORGANIZER` istniejącemu kontu.

**Kontrakt**: Dodać krótką sekcję z idempotentnym poleceniem
`insert into user_account_roles ... select ... where email = ... on conflict do nothing` oraz informacją, że normalna
rejestracja nadaje wyłącznie `USER`.

### Kryteria sukcesu

#### Weryfikacja automatyczna

- Testy migracji i tożsamości przechodzą: z `backend/` uruchomić `.\gradlew.bat test`.
- Pełne kontrole backendu, w tym Flyway, Error Prone, NullAway i testy integracyjne, przechodzą: z `backend/` uruchomić
  `.\gradlew.bat build`.
- Testy integracyjne przechodzą przez use case'y i potwierdzają, że końcowy schemat obsługuje zapis wartości
  granicznych, `default_start_time`, sortowanie po aktualizacji, własność i usuwanie; testy tożsamości potwierdzają
  obsługę roli `ORGANIZER`.

#### Weryfikacja ręczna

- Po migracji lokalnej bazy sprawdzić, że `booking_training_templates` zawiera przemianowane kolumny i
  `default_start_time`.
- Wykonać udokumentowane SQL dwa razy dla konta testowego i potwierdzić dokładnie role `USER` i `ORGANIZER` bez błędu
  duplikatu.
- Zarejestrować nowe konto i potwierdzić, że otrzymuje tylko `USER`.

**Uwaga implementacyjna**: Po tej fazie zatrzymać się w celu ręcznego potwierdzenia migracji i administracyjnego
nadawania roli na lokalnym PostgreSQL.

---

## Faza 2: Backendowy CRUD szablonów z kontrolą właściciela

### Przegląd

Przemianowujemy istniejący podmoduł backendu i uzupełniamy go do pełnego CRUD-u z końcową walidacją, własnością,
sortowaniem, kontraktem HTTP i persystencją.

### Wymagane zmiany

#### 1. Przemianowanie domeny i walidacja

**Pliki**:

- `backend/src/main/java/com/shootersplatform/backend/bookings/trainingtemplate/domain/package-info.java`
- `backend/src/main/java/com/shootersplatform/backend/bookings/trainingtemplate/domain/TrainingTemplate.java`
- `backend/src/main/java/com/shootersplatform/backend/bookings/trainingtemplate/domain/TrainingTemplateId.java`
- `backend/src/main/java/com/shootersplatform/backend/bookings/trainingtemplate/domain/TrainingTemplateRepository.java`
- `backend/src/main/java/com/shootersplatform/backend/bookings/trainingtemplate/domain/TrainingTemplateService.java`
-
`backend/src/main/java/com/shootersplatform/backend/bookings/trainingtemplate/domain/TrainingTemplateNotFoundException.java`
-
`backend/src/main/java/com/shootersplatform/backend/bookings/trainingtemplate/domain/TrainingTemplateValidationException.java`
- usunąć zastępowane pliki produkcyjne `bookings/trainingenrollment/domain`

**Cel**: Ustanowić kanoniczny model `TrainingTemplate` i operacje biznesowe, zachowując prywatność właściciela oraz
granicę serwisu domenowego.

**Kontrakt**: Rekord zawiera id, ownerId, nazwę, znormalizowany opis, poziom, lokalizację, pojemność, dni anulowania,
czas trwania w minutach, `LocalTime defaultStartTime`, createdAt i updatedAt. Walidacja wymusza nazwę `1..120`, opis
`0..2048`, wszystkie reguły lokalizacji, pojemność `1..10`, anulowanie `0..365`, czas `30..1440` podzielny przez 30 oraz
start bez sekund i nanosekund, z minutą podzielną przez 15. Serwis udostępnia `list(ownerId)`, `get(ownerId, id)`,
`create(...)`, `update(ownerId, id, ...)` i `delete(ownerId, id)`. Obcy i brakujący identyfikator powodują
`TrainingTemplateNotFoundException`.

#### 2. Przemianowanie persystencji i obsługa usuwania

**Pliki**:

- `backend/src/main/java/com/shootersplatform/backend/bookings/trainingtemplate/infrastructure/package-info.java`
-
`backend/src/main/java/com/shootersplatform/backend/bookings/trainingtemplate/infrastructure/TrainingTemplateEntity.java`
-
`backend/src/main/java/com/shootersplatform/backend/bookings/trainingtemplate/infrastructure/SpringDataTrainingTemplateRepository.java`
-
`backend/src/main/java/com/shootersplatform/backend/bookings/trainingtemplate/infrastructure/JpaTrainingTemplateRepository.java`
- usunąć zastępowane pliki produkcyjne `bookings/trainingenrollment/infrastructure`

**Cel**: Mapować nową tabelę i godzinę startu, sortować listy właściciela według ostatniej aktualizacji i obsłużyć
trwałe usuwanie bez ujawniania mechaniki persystencji use case'om.

**Kontrakt**: Encja wskazuje `booking_training_templates`; `default_start_time` mapuje się na `LocalTime`; lista
właściciela sortuje po `updatedAt` malejąco; port repozytorium zapewnia wyszukiwanie z kontrolą właściciela i trwałe
usunięcie. Adapter mapuje wszystkie pola i nie odczytuje ani nie usuwa rekordu innego właściciela.

#### 3. Osobny use case dla każdej operacji

**Pliki**:

- `backend/src/main/java/com/shootersplatform/backend/bookings/usecase/ListTrainingTemplatesUseCase.java`
- `backend/src/main/java/com/shootersplatform/backend/bookings/usecase/GetTrainingTemplateUseCase.java`
- `backend/src/main/java/com/shootersplatform/backend/bookings/usecase/CreateTrainingTemplateUseCase.java`
- `backend/src/main/java/com/shootersplatform/backend/bookings/usecase/UpdateTrainingTemplateUseCase.java`
- `backend/src/main/java/com/shootersplatform/backend/bookings/usecase/DeleteTrainingTemplateUseCase.java`
- usunąć `CreateTrainingEnrollmentUseCase.java`, `ListTrainingEnrollmentsUseCase.java` i
  `UpdateTrainingEnrollmentUseCase.java`

**Cel**: Zapewnić osobną granicę transakcji dla każdej operacji biznesowej zgodnie ze wzorcem modułu bookings i zakazem
wstrzykiwania repozytoriów do use case'ów.

**Kontrakt**: Każdy use case deleguje wyłącznie do `TrainingTemplateService`. Odczyty używają transakcji read-only,
zapisy zwykłych transakcji. Proste operacje zwracają `TrainingTemplate`, a delete nie zwraca danych domenowych.

#### 4. Końcowe API web

**Pliki**:

- `backend/src/main/java/com/shootersplatform/backend/bookings/web/BookingTrainingTemplatesController.java`
- `backend/src/main/java/com/shootersplatform/backend/bookings/web/TrainingTemplateRequest.java`
- `backend/src/main/java/com/shootersplatform/backend/bookings/web/TrainingTemplateResponse.java`
- `backend/src/main/java/com/shootersplatform/backend/bookings/web/BookingExceptionHandler.java`
- usunąć `BookingTrainingEnrollmentsController.java`, `TrainingEnrollmentRequest.java` i
  `TrainingEnrollmentResponse.java`

**Cel**: Udostępnić pięć operacji API z autoryzacją organizatora, kontrolą właściciela, poprawnymi statusami i
istniejącym formatem `application/problem+json`.

**Kontrakt**: Ścieżka bazowa to `/api/bookings/training-templates`; kontroler wymaga `hasRole('ORGANIZER')`; endpointy
to lista `GET`, szczegóły `GET /{templateId}`, tworzenie `POST` z `201`, aktualizacja `PUT /{templateId}` z `200` i
usunięcie `DELETE /{templateId}` z `204`. Request używa `durationMinutes` i lokalnego ISO `defaultStartTime` w formacie
`HH:mm`. Bean Validation odzwierciedla granice domeny i kroki tam, gdzie da się je wyrazić adnotacjami. Handler wyjątków
mapuje brak szablonu na `404`, a walidację domenową na `400`.

#### 5. Reguły bezpieczeństwa

**Pliki**:

- `backend/src/main/java/com/shootersplatform/backend/shared/config/security/SecurityConfiguration.java`
- `backend/src/test/groovy/com/shootersplatform/backend/bookings/web/BookingControllerSecuritySpec.groovy`

**Cel**: Odrzucać uwierzytelnionych użytkowników bez roli organizatora na granicy API szablonów, zachowując aktualną
autoryzację `USER` dla wszystkich pozostałych endpointów bookings.

**Kontrakt**: Szczegółowa reguła `/api/bookings/training-templates/**` dla organizatora poprzedza ogólną regułę
`/api/bookings/**` dla `USER` albo równoważne method security zapewnia identyczne zachowanie. Żądania anonimowe nadal
zwracają `401`, uwierzytelnione konta `USER` otrzymują `403`, a konta z obiema rolami przechodzą dalej.

#### 6. Przemianowanie i rozszerzenie testów backendu

**Pliki**:

- `backend/src/test/groovy/com/shootersplatform/backend/bookings/trainingtemplate/**`
- `backend/src/test/groovy/com/shootersplatform/backend/bookings/usecase/TrainingTemplateUseCasesSpec.groovy`
- `backend/src/test/groovy/com/shootersplatform/backend/bookings/usecase/BookingUseCaseTestContext.groovy`
- `backend/src/test/groovy/com/shootersplatform/backend/bookings/web/TrainingTemplateApiClient.java`
- nowe lub zmienione testy web/infrastructure w `backend/src/test/groovy/com/shootersplatform/backend/bookings/**`
- usunąć zastępowane fixture'y i testy `TrainingEnrollment*`

**Cel**: Pokryć reguły domenowe, orkiestrację, własność, sortowanie, usuwanie, statusy i granicę roli w odpowiednich
warstwach.

**Kontrakt**: Testy domeny obejmują wszystkie granice liczbowe, błędne kroki, normalizację tekstu, aktualizację
timestampów i precyzję czasu. Testy use case obejmują pięć operacji, duplikaty nazw, sortowanie po `updatedAt`,
semantykę `404` dla zasobu obcego i brakującego oraz trwałe usunięcie. Testy web obejmują `401`, `403`, `404`, `201`,
`200`, `204`, walidację requestu i problem JSON. Testy infrastruktury potwierdzają nową tabelę i mapowanie
`default_start_time`.

### Kryteria sukcesu

#### Weryfikacja automatyczna

- Brak produkcyjnych i testowych odwołań backendu do `TrainingEnrollment`, `training-enrollments` lub
  `booking_training_enrollments` poza niezmiennymi migracjami V4/V7 i asercjami migracji:
  `rg -n "TrainingEnrollment|training-enrollments|booking_training_enrollments" backend`.
- Testy domeny, use case, web i security przechodzą z `backend/`: `.\gradlew.bat test`.
- Pełny build backendu przechodzi z `backend/`: `.\gradlew.bat build`.

#### Weryfikacja ręczna

- W sesji organizatora wykonać wszystkie pięć operacji API i potwierdzić zmianę kolejności listy po edycji starszego
  szablonu.
- W sesji zwykłego `USER` potwierdzić `403` dla każdego endpointu szablonów oraz dalsze działanie endpointów terminów.
- Jako organizator A wykonać detail, update i delete dla szablonu organizatora B i potwierdzić taki sam format `404` jak
  dla losowego ID.

**Uwaga implementacyjna**: Po tej fazie zatrzymać się w celu ręcznej weryfikacji API i własności przed zmianami
Angulara.

---

## Faza 3: Oddzielenie istniejącego zarządzania terminami od szablonów

### Przegląd

Usuwamy ładowanie i kopiowanie szablonów z istniejącego komponentu zarządzania rezerwacjami, aby obecny przepływ `USER`
działał po ograniczeniu API szablonów do organizatorów.

### Wymagane zmiany

#### 1. Uporządkowanie modeli i serwisu bookings

**Pliki**:

- `frontend/src/app/bookings/booking.models.ts`
- `frontend/src/app/bookings/booking.service.ts`
- `frontend/src/app/bookings/booking.service.spec.ts`

**Cel**: Usunąć stary kontrakt klienta `TrainingEnrollment` z ogólnego serwisu bookings i uniezależnić tworzenie terminu
od identyfikatora szablonu.

**Kontrakt**: Usunąć `TrainingEnrollment`, `UpsertTrainingEnrollment` i stare metody serwisu enrollment. `UpsertTerm`
staje się jawnym payloadem terminu zawierającym pola szkolenia i `startsAt`; nie zawiera `templateId` ani
`enrollmentId`.

#### 2. Uproszczenie komponentu administracji rezerwacjami

**Pliki**:

- `frontend/src/app/bookings/booking-admin.component.ts`
- `frontend/src/app/bookings/booking-admin.component.html`
- `frontend/src/app/bookings/booking-admin.component.css`
- `frontend/src/app/bookings/booking-admin.component.spec.ts`

**Cel**: Zachować ręczne tworzenie terminów, listę terminów, rezerwacje i listę rezerwową, usuwając formularz szablonu,
wybór szablonu, początkowe żądanie szablonów i kopiowanie danych.

**Kontrakt**: Ładowanie początkowe pobiera tylko terminy właściciela. Formularz terminu zaczyna z użytecznymi
wartościami domyślnymi i waliduje własne dane bez `enrollmentId`. Tworzenie wysyła ten sam kontrakt terminu co
wcześniej. Uprawnienia `/bookings` nie zmieniają się.

#### 3. Uporządkowanie tłumaczeń

**Pliki**:

- `frontend/src/app/shared/i18n/translations.pl.ts`
- `frontend/src/app/shared/i18n/translations.en.ts`

**Cel**: Usunąć nieaktualne etykiety i błędy `TrainingEnrollment` ze słownika zarządzania rezerwacjami bez
przedwczesnego dodawania tłumaczeń nowej funkcji.

**Kontrakt**: Istniejące klucze terminów, rezerwacji i listy rezerwowej pozostają poprawne w obu językach. Klucze
używane wyłącznie przez usunięty przepływ inline są kasowane lub przenoszone w fazie 4 do przestrzeni
`trainingTemplates.*`.

### Kryteria sukcesu

#### Weryfikacja automatyczna

- Testy serwisu bookings i komponentu admin przechodzą: `npm run test --workspace frontend`.
- Produkcyjny build Angular przechodzi rygorystyczne sprawdzanie typów i szablonów: `npm run frontend:build`.
- Lint frontendu przechodzi: `npm run lint --workspace frontend`.
- Nie pozostają stare odwołania klienta enrollment:
  `rg -n "TrainingEnrollment|training-enrollments|enrollmentId|enrollments\\(" frontend/src/app`.

#### Weryfikacja ręczna

- Zalogować się jako zwykły `USER`, otworzyć `/bookings` i potwierdzić ładowanie terminów bez żądania szablonów
  kończącego się `403`.
- Utworzyć termin przez ręczne wypełnienie pól i potwierdzić jego obecność na liście.
- Otworzyć rezerwacje i listę rezerwową istniejącego terminu oraz potwierdzić brak regresji.

**Uwaga implementacyjna**: Po tej fazie zatrzymać się i potwierdzić działanie istniejącego ekranu bookings dla konta bez
roli organizatora.

---

## Faza 4: Funkcja Angular do zarządzania szablonami Organizatora

### Przegląd

Dodajemy osobny, chroniony rolą interfejs CRUD z routowanymi widokami, pełną walidacją klienta, ochroną niezapisanych
zmian, potwierdzeniem usuwania, powiadomieniami, responsywną prezentacją i dwujęzyczną treścią.

### Wymagane zmiany

#### 1. Typowany klient szablonów

**Pliki**:

- `frontend/src/app/training-templates/training-template.models.ts`
- `frontend/src/app/training-templates/training-template.service.ts`
- `frontend/src/app/training-templates/training-template.service.spec.ts`

**Cel**: Odizolować kontrakt nowego API od ogólnego serwisu bookings i scentralizować CSRF, obsługę problem response i
operacje CRUD.

**Kontrakt**: Modele odzwierciedlają API, w tym `durationMinutes` i `defaultStartTime: string`. Mapowanie formularza
przelicza godziny na całkowite minuty wyłącznie dla wartości co pół godziny. Metody serwisu to `list`, `get`, `create`,
`update` i `delete` pod `/api/bookings/training-templates`; delete oczekuje pustej odpowiedzi `204`.

#### 2. Guard Organizatora i nawigacja

**Pliki**:

- `frontend/src/app/identity/organizer.guard.ts`
- `frontend/src/app/identity/organizer.guard.spec.ts`
- `frontend/src/app/app.routes.ts`
- `frontend/src/app/home/home.component.ts`
- `frontend/src/app/home/home.component.html`
- odpowiednie style i testy home, jeśli będą potrzebne

**Cel**: Udostępnić nawigację i trasy szablonów wyłącznie użytkownikom, których załadowany stan uwierzytelnienia zawiera
`ORGANIZER`.

**Kontrakt**: Dodać chronione trasy `/training-templates`, `/training-templates/new`, `/training-templates/:id` i
`/training-templates/:id/edit`. Guard ładuje bieżącego użytkownika, gdy stan auth jest nieznany, przepuszcza użytkownika
z `ORGANIZER`, przekierowuje uwierzytelnionego użytkownika bez tej roli na `/` i zachowuje normalne zachowanie dla
anonimowych. Nawigacja home pokazuje link do szablonów tylko organizatorom.

#### 3. Współdzielony formularz i guard niezapisanych zmian

**Pliki**:

- `frontend/src/app/training-templates/training-template-form.component.ts`
- `frontend/src/app/training-templates/training-template-form.component.html`
- `frontend/src/app/training-templates/training-template-form.component.css`
- `frontend/src/app/training-templates/training-template-form.component.spec.ts`
- `frontend/src/app/training-templates/pending-training-template-changes.guard.ts`
- `frontend/src/app/training-templates/pending-training-template-changes.guard.spec.ts`

**Cel**: Użyć jednego kontraktu formularza dla tworzenia i edycji, egzekwując wszystkie reguły domenowe i ostrzegając
dopiero po zabrudzeniu kontrolek.

**Kontrakt**: Pola obejmują nazwę, opcjonalny opis, poziom, pełną lokalizację, pojemność, dni anulowania, czas w
godzinach i domyślną godzinę startu. Czas jest polem liczbowym `0.5..24` z krokiem `0.5` i mapuje się dokładnie na
`30..1440` minut. Start używa `type="time"` z krokiem 15 minut i dodatkową walidacją klienta. Pojemność to `1..10`,
anulowanie `0..365`, a współrzędne i limity tekstu odpowiadają backendowi. Kontrakt `CanDeactivate` wywołuje
przetłumaczone `window.confirm` tylko wtedy, gdy formularz zgłasza dirty i udany zapis lub reset nie wyczyścił tego
stanu. Zgodnie z zaakceptowaną decyzją ręczne przywrócenie wartości nie musi usuwać dirty.

#### 4. Widok listy

**Pliki**:

- `frontend/src/app/training-templates/training-template-list.component.ts`
- `frontend/src/app/training-templates/training-template-list.component.html`
- `frontend/src/app/training-templates/training-template-list.component.css`
- `frontend/src/app/training-templates/training-template-list.component.spec.ts`

**Cel**: Zapewnić zwięzły, prywatny punkt wejścia umożliwiający odróżnienie szablonów o takich samych nazwach bez
przeciążania listy szczegółami.

**Kontrakt**: Lista zachowuje kolejność API i pokazuje wyłącznie nazwę, przetłumaczony poziom oraz zlokalizowane
`updatedAt`. Wiersz lub nazwa prowadzi do szczegółów, a główna akcja do tworzenia. Stany ładowania, pusty i błędu są
przetłumaczone i responsywne.

#### 5. Widoki tworzenia i edycji

**Pliki**:

- `frontend/src/app/training-templates/training-template-create.component.ts`
- `frontend/src/app/training-templates/training-template-create.component.html`
- `frontend/src/app/training-templates/training-template-create.component.spec.ts`
- `frontend/src/app/training-templates/training-template-edit.component.ts`
- `frontend/src/app/training-templates/training-template-edit.component.html`
- `frontend/src/app/training-templates/training-template-edit.component.spec.ts`

**Cel**: Orkiestrować współdzielony formularz dla nowych i istniejących szablonów, zachować dane po błędzie i wracać na
listę po udanym zapisie.

**Kontrakt**: Create wysyła zmapowany payload; edit pobiera szablon z ID trasy i wysyła pełny PUT. Sukces czyści dirty,
przechodzi do `/training-templates` i pokazuje przetłumaczony snackbar Material. Błąd zapisu pokazuje snackbar oraz
pozostawia formularz i bieżącą trasę bez zmian. Początkowe `404` edycji renderuje wspólny stan not-found zamiast
przekierowania.

#### 6. Szczegóły i potwierdzenie usunięcia

**Pliki**:

- `frontend/src/app/training-templates/training-template-detail.component.ts`
- `frontend/src/app/training-templates/training-template-detail.component.html`
- `frontend/src/app/training-templates/training-template-detail.component.css`
- `frontend/src/app/training-templates/training-template-detail.component.spec.ts`
- `frontend/src/app/training-templates/training-template-delete-dialog.component.ts`
- `frontend/src/app/training-templates/training-template-delete-dialog.component.html`
- `frontend/src/app/training-templates/training-template-delete-dialog.component.spec.ts`
- opcjonalna wspólna prezentacja not-found wewnątrz funkcji

**Cel**: Pokazać wszystkie zapisane pola w widoku tylko do odczytu i wymagać jawnego potwierdzenia z nazwą przed trwałym
usunięciem.

**Kontrakt**: Szczegóły oferują akcje Edytuj i Usuń. Dialog Material zawiera nazwę szablonu oraz przetłumaczone akcje
Anuluj/Usuń. Udane usunięcie zamyka dialog, przechodzi na listę, odświeża ją przez zwykłe ładowanie i pokazuje snackbar
sukcesu. Błąd usunięcia pozostawia bieżący element widoczny i pokazuje snackbar błędu. Początkowe `404` szczegółów
pokazuje przetłumaczony stan not-found oraz akcję powrotu do listy.

#### 7. Snackbary i tłumaczenia

**Pliki**:

- `frontend/src/app/shared/i18n/translations.pl.ts`
- `frontend/src/app/shared/i18n/translations.en.ts`
- komponenty funkcji używające `MatSnackBar`
- konfiguracja providerów Angular, jeśli wymaga tego snackbar lub dialog Material

**Cel**: Zapewnić pełne polskie i angielskie tłumaczenia etykiet, walidacji, potwierdzeń, sukcesów, błędów, pustych
stanów i not-found.

**Kontrakt**: Dodać spójny zestaw kluczy `trainingTemplates.*` oraz powiązanych kluczy walidacji i błędów do obu map.
Snackbary obsługują sukces create/update/delete i błędy operacyjne, w tym `404` podczas zapisu lub usuwania. Angielskie
szczegóły błędu z backendu nie są standardowym tekstem widocznym dla użytkownika.

#### 8. Pokrycie regresji frontendu

**Pliki**:

- wszystkie pliki `*.spec.ts` wymienione w tej fazie
- istniejące testy auth, home, routingu i tłumaczeń zmienione przez obsługę ról

**Cel**: Zweryfikować routing, widoczność zależną od roli, mapowanie API, walidację, dirty navigation, stany listy i
szczegółów, dialog oraz powiadomienia bez Playwright.

**Kontrakt**: Testy obejmują przepuszczenie organizatora, przekierowanie zwykłego użytkownika na home, ukrytą nawigację,
pola i kolejność listy, początkowe `404`, mapowanie czasu i kroków, wszystkie granice walidacji, potwierdzenie tylko dla
dirty, zachowanie formularza po błędzie zapisu, nawigację i snackbar po sukcesie, dialog z nazwą, zachowanie elementu po
błędzie delete oraz klucze obu języków.

### Kryteria sukcesu

#### Weryfikacja automatyczna

- Wszystkie testy Angular przechodzą: `npm run test --workspace frontend`.
- Produkcyjny build Angular przechodzi rygorystyczne sprawdzanie TypeScript i szablonów: `npm run frontend:build`.
- Lint Angular przechodzi: `npm run lint --workspace frontend`.
- Pełny build backendu nadal przechodzi: z `backend/` uruchomić `.\gradlew.bat build`.
- W aktywnym kodzie nie ma starych odwołań poza niezmiennymi migracjami i wyłączonymi z zakresu testami Playwright:
  `rg -n "TrainingEnrollment|training-enrollments|training_enrollments" backend/src/main backend/src/test frontend/src/app`.

#### Weryfikacja ręczna

- Jako zwykły `USER` potwierdzić brak nawigacji do szablonów i przekierowanie każdej bezpośredniej trasy szablonów na
  `/`.
- Jako organizator utworzyć szablon z czasem `0.5` godziny i startem o pełnym kwadransie, a następnie potwierdzić nazwę,
  poziom i zlokalizowaną datę aktualizacji na liście.
- Otworzyć szczegóły, edytować szablon, zapisać i potwierdzić kolejność listy wynikającą z nowego `updatedAt`.
- Zmienić kontrolkę formularza i spróbować opuścić stronę, aby zobaczyć natywne potwierdzenie; opuścić nietknięty
  formularz bez ostrzeżenia.
- Raz anulować usunięcie, następnie je potwierdzić i sprawdzić zniknięcie wiersza po powrocie na listę.
- Wywołać błędy ładowania, zapisu i usuwania oraz potwierdzić przetłumaczone snackbary, zachowanie danych formularza,
  pozostawienie kontekstu listy/szczegółów i dedykowany stan not-found.
- Przełączyć język polski i angielski oraz sprawdzić wszystkie nowe widoki, walidacje, tekst dialogu i powiadomienia.

**Uwaga implementacyjna**: Po tej fazie zatrzymać się w celu pełnych testów akceptacyjnych interfejsu dla organizatora i
zwykłego użytkownika. Playwright pozostaje poza zakresem, a uszkodzony starszy scenariusz e2e należy zapisać do
późniejszej zmiany publikowania terminów zamiast rozszerzać ten zakres.

---

## Strategia testowania

### Testy jednostkowe

- Testy granic domenowych każdego pola, normalizacji, aktualizacji, timestampów i dozwolonych kroków czasu.
- Testy use case z prawdziwym serwisem domenowym i repozytorium in-memory dla CRUD-u, ukrywania cudzej własności,
  sortowania i trwałego usuwania.
- Testy serwisu Angular dla metod, URL, CSRF, konwersji payloadu i odpowiedzi `204`.
- Testy komponentów i guardów Angular dla ról, tras, walidacji, dirty form, wyniku dialogu, snackbarów i zachowania
  stanu po błędzie.

### Testy integracyjne

- Testy use case używają rzeczywistych serwisów domenowych i implementacji portów in-memory do weryfikacji orkiestracji,
  własności i sortowania. Integracyjne testy web uruchamiają pełny stos Spring, adapter JPA i PostgreSQL oraz odtwarzają
  główny cykl życia użytkownika: tworzenie, listowanie, szczegóły, edycję, zmianę kolejności, usunięcie i prywatność
  zasobu.
- Integracja MockMvc potwierdza uwierzytelnianie, autoryzację organizatora, owner-scoped `404`, wszystkie statusy
  sukcesu, walidację problem JSON i brak regresji pozostałych endpointów bookings.
- Pełny build backendu oraz build/test/lint Angular są końcową bramą automatyczną. Playwright jest celowo wyłączony.

### Kroki testów ręcznych

1. Zmigrować lokalną bazę, nadać kontu `ORGANIZER` i zalogować się ponownie, aby sesja zawierała obie role.
2. Najpierw sprawdzić zwykłe konto `USER`: `/bookings` działa, nawigacja do szablonów jest ukryta, a bezpośrednie trasy
   przekierowują na home.
3. Sprawdzić pełny CRUD organizatora w obu językach, w tym duplikaty nazw i sortowanie po edycji.
4. Sprawdzić wszystkie limity, konwersję półgodzinnego czasu, kwadranse startu, potwierdzenie dirty form i anulowanie
   tego potwierdzenia.
5. Sprawdzić anulowanie delete, udane trwałe usunięcie i zachowanie przy błędzie.
6. Potwierdzić nierozróżnialność obcych i brakujących ID w API oraz UI.

## Wydajność

- Lista właściciela celowo nie ma paginacji ze względu na małą oczekiwaną liczbę danych. Zachować indeks
  `(owner_user_id, updated_at desc)`, aby jedno indeksowane zapytanie realizowało filtrowanie i sortowanie.
- Nie dodawać cache klienta ani optymistycznych zapisów. Ponowne pobranie małej listy po routowanej mutacji upraszcza
  kod i zapobiega nieaktualnej kolejności.
- Natywne pola czasu i liczb unikają renderowania 96 opcji godzin lub 48 wartości czasu trwania.

## Uwagi migracyjne

- Nie modyfikować V4 ani V7. V9 musi działać dla instalacji, która wykonała już obie migracje.
- Opis zmiany potwierdza brak zapisanych szablonów i terminów, ale migracja nadal powinna być strukturalnie bezpieczna:
  użyć tymczasowego defaultu dla `default_start_time`, a następnie go usunąć, aby przyszłe zapisy musiały podawać
  wartość.
- Ograniczenia ze zmienionymi predykatami należy usunąć i utworzyć ponownie, a nie tylko przemianować.
- Stare API HTTP i nazwy kodu są usuwane bez aliasów kompatybilności.
- Wycofanie wymaga odtworzenia bazy albo kolejnej migracji naprawczej. Wykonanych migracji Flyway nie edytujemy.

## Referencje

- Definicja zmiany: `context/changes/training-template-crud/change.md`
- Wymagania produktu: `context/foundation/prd.md`
- Kontekst roadmapy: `context/foundation/roadmap.md`
- Istniejący model domenowy:
  `backend/src/main/java/com/shootersplatform/backend/bookings/trainingenrollment/domain/TrainingEnrollment.java:11`
- Istniejące API:
  `backend/src/main/java/com/shootersplatform/backend/bookings/web/BookingTrainingEnrollmentsController.java:20`
- Istniejący schemat: `backend/src/main/resources/db/migration/V4__create_booking_tables.sql:1`
- Ogólna reguła bezpieczeństwa:
  `backend/src/main/java/com/shootersplatform/backend/shared/config/security/SecurityConfiguration.java:57`
- Powiązany przepływ Angular: `frontend/src/app/bookings/booking-admin.component.ts:122`
- Role auth na froncie: `frontend/src/app/identity/auth.service.ts:6`

## Postęp

> Konwencja: `- [ ]` oczekuje, `- [x]` wykonane. Po wdrożeniu kroku dopisz ` - <commit sha>`. Nie zmieniaj nazw kroków.

### Faza 1: Schemat, rola i konfiguracja administracyjna

#### Automatyczne

- [x] 1.1 Testy migracji i tożsamości backendu przechodzą
- [x] 1.2 Pełne kontrole jakości backendu przechodzą
- [x] 1.3 Testy integracyjne potwierdzają zachowanie końcowego schematu i roli

#### Ręczne

- [x] 1.4 Lokalny schemat po migracji zawiera końcowe obiekty szablonów
- [x] 1.5 Administracyjne SQL nadawania roli jest idempotentne
- [x] 1.6 Nowa rejestracja nadal nadaje wyłącznie USER

### Faza 2: Backendowy CRUD szablonów z kontrolą właściciela

#### Automatyczne

- [x] 2.1 Stare odwołania backendu do training-enrollment usunięto poza niezmienną historią migracji
- [x] 2.2 Testy CRUD-u, własności i bezpieczeństwa przechodzą
- [x] 2.3 Pełny build backendu przechodzi

#### Ręczne

- [x] 2.4 Organizator wykonuje pięć operacji API, a lista zmienia kolejność po edycji
- [x] 2.5 Zwykły USER otrzymuje 403 wyłącznie dla endpointów szablonów
- [x] 2.6 Obcy i brakujący szablon zwracają 404

### Faza 3: Oddzielenie istniejącego zarządzania terminami od szablonów

#### Automatyczne

- [x] 3.1 Testy serwisu bookings i komponentu admin przechodzą
- [x] 3.2 Produkcyjny build Angular przechodzi
- [x] 3.3 Lint frontendu przechodzi
- [x] 3.4 Stare odwołania klienta enrollment usunięto

#### Ręczne

- [x] 3.5 Zwykły USER ładuje zarządzanie rezerwacjami bez żądania szablonów
- [x] 3.6 Ręczne tworzenie terminu działa
- [x] 3.7 Zarządzanie rezerwacjami i listą rezerwową pozostaje bez zmian

### Faza 4: Funkcja Angular do zarządzania szablonami Organizatora

#### Automatyczne

- [x] 4.1 Wszystkie testy Angular przechodzą
- [x] 4.2 Produkcyjny build Angular przechodzi
- [x] 4.3 Lint Angular przechodzi
- [x] 4.4 Pełny build backendu nadal przechodzi
- [x] 4.5 Nie pozostały aktywne odwołania do training-enrollment

#### Ręczne

- [x] 4.6 Zwykły USER nie widzi nawigacji, a chronione trasy przekierowują na home
- [x] 4.7 Organizator tworzy i widzi poprawny szablon na liście
- [x] 4.8 Organizator wyświetla, edytuje i obserwuje zmianę kolejności listy
- [x] 4.9 Potwierdzenie opuszczenia pojawia się tylko po interakcji z formularzem
- [x] 4.10 Anulowanie i potwierdzenie usunięcia działają poprawnie
- [x] 4.11 Stany błędu i not-found zachowują uzgodniony kontekst
- [x] 4.12 Pokrycie polskie i angielskie jest kompletne

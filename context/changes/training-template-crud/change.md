---
change_id: training-template-crud
title: Training template CRUD for organizers
status: new
created: 2026-06-09
updated: 2026-06-09
archived_at: null
---

## Notes

### Historyjka użytkownika

Jako użytkownik z rolą Organizatora chcę tworzyć, zapisywać, odczytywać, edytować i usuwać prywatne szablony
szkoleń, aby móc później wykorzystać ich dane podczas przygotowania konkretnego terminu szkolenia.

### Zakres

- Pełny CRUD szablonów szkoleń: tworzenie, lista, szczegóły, edycja i trwałe usunięcie.
- Osobny chroniony widok „Szablony szkoleń” dostępny wyłącznie dla Organizatora.
- Przemianowanie istniejącego modelu `TrainingEnrollment` na `TrainingTemplate`.
- Przemianowanie API z `training-enrollments` na `training-templates`.
- Przemianowanie tabeli `booking_training_enrollments` na `booking_training_templates`.
- Dodanie roli `ORGANIZER` jako dodatkowej roli obok `USER`.
- Migracja Flyway, backendowy CRUD, frontend Angular, testy oraz tłumaczenia polskie i angielskie.
- Dokumentacja polecenia SQL do administracyjnego nadawania roli `ORGANIZER`.

### Własność i autoryzacja

- Szablon jest prywatną własnością Organizatora, który go utworzył.
- Organizator może odczytywać i modyfikować wyłącznie własne szablony.
- CRUD szablonów wymaga roli `ORGANIZER`.
- Organizator posiada jednocześnie role `USER` i `ORGANIZER`.
- Zwykła rejestracja nadal nadaje wyłącznie rolę `USER`.
- Rola `ORGANIZER` jest nadawana administracyjnie poza interfejsem aplikacji.
- Istniejący użytkownicy pozostają z rolą `USER`; migracja nie nadaje im automatycznie roli `ORGANIZER`.
- Próba odczytu, edycji lub usunięcia cudzego albo nieistniejącego szablonu zwraca `404 Not Found`.
- Brak roli `ORGANIZER` zwraca `403 Forbidden`.
- W tej zmianie rola `ORGANIZER` ogranicza wyłącznie CRUD szablonów. Uprawnienia istniejących funkcji
  organizatorskich pozostają bez zmian.

### Model szablonu

Szablon zawiera:

- nazwę,
- opcjonalny opis,
- poziom szkolenia,
- lokalizację: nazwę miejsca, adres, szerokość i długość geograficzną,
- pojemność,
- liczbę dni przed terminem, do kiedy uczestnik może anulować,
- czas trwania,
- domyślną godzinę rozpoczęcia,
- `createdAt` i `updatedAt`.

Reguły:

- Nazwa jest obowiązkowa i ma maksymalnie 120 znaków.
- Opis jest opcjonalny, normalizowany do pustego tekstu i ma maksymalnie 2048 znaków.
- Poziom to `BASIC`, `INTERMEDIATE` albo `ADVANCED`.
- Wszystkie pola lokalizacji są obowiązkowe.
- Szerokość geograficzna mieści się w `[-90, 90]`, a długość w `[-180, 180]`.
- Pojemność mieści się w zakresie `1–10`.
- Termin anulowania mieści się w zakresie `0–365` dni.
- Czas trwania mieści się w zakresie `0,5–24` godzin, w krokach co 30 minut.
- API i backend przechowują czas trwania jako całkowitą liczbę minut w polu `durationMinutes`.
- Frontend prezentuje i przyjmuje czas trwania w godzinach, również jako wartości połówkowe.
- `defaultStartTime` jest obowiązkowe i przesyłane jako lokalny czas ISO `HH:mm`.
- Godzina rozpoczęcia mieści się w zakresie `00:00–23:45`, w krokach co 15 minut, bez sekund.
- Backend mapuje `defaultStartTime` na `LocalTime`.
- System działa wyłącznie w strefie `Europe/Warsaw`; obsługa innych stref czasowych jest poza zakresem.
- Szablon może opisywać szkolenie kończące się następnego dnia.
- Nazwy szablonów nie muszą być unikalne dla jednego Organizatora.
- Szablon nie ma stanów roboczy/aktywny/archiwalny.
- Nie ma wersjonowania ani historii zmian; `updatedAt` ma charakter informacyjny.
- Przy równoczesnej edycji obowiązuje zasada „ostatni zapis wygrywa”.

### Kontrakt API

- `GET /api/bookings/training-templates` zwraca wszystkie własne szablony, malejąco po `updatedAt`.
- `GET /api/bookings/training-templates/{templateId}` zwraca szczegóły własnego szablonu.
- `POST /api/bookings/training-templates` tworzy szablon i zwraca `201 Created`.
- `PUT /api/bookings/training-templates/{templateId}` aktualizuje szablon i zwraca `200 OK`.
- `DELETE /api/bookings/training-templates/{templateId}` trwale usuwa szablon i zwraca `204 No Content`.
- API używa istniejącego formatu błędów `application/problem+json`.
- Komunikaty backendu pozostają po angielsku, a frontend prezentuje własne przetłumaczone komunikaty.
- Lista nie wymaga paginacji, wyszukiwania ani filtrowania.

### Interfejs użytkownika

- Nawigacja do szablonów jest widoczna wyłącznie dla użytkownika z rolą `ORGANIZER`.
- Bezpośrednia próba wejścia na chronioną trasę bez tej roli przekierowuje na stronę główną.
- Lista pokazuje dane pozwalające rozróżnić szablony o takich samych nazwach.
- Szczegóły szablonu są osobnym widokiem tylko do odczytu z akcjami „Edytuj” i „Usuń”.
- Tworzenie i edycja używają formularzy z walidacją zgodną z regułami domenowymi.
- Próba opuszczenia zmienionego, niezapisanego formularza wymaga potwierdzenia.
- Po utworzeniu lub edycji Organizator wraca na listę i widzi komunikat sukcesu.
- Błąd zapisu pozostawia użytkownika w formularzu i zachowuje wprowadzone dane.
- Usunięcie wymaga potwierdzenia w modalu zawierającym nazwę szablonu oraz akcje „Anuluj” i „Usuń”.
- Po usunięciu pozycja znika z listy bez pełnego przeładowania strony i pojawia się komunikat sukcesu.
- Błąd usunięcia pozostawia szablon na liście i pokazuje komunikat błędu.
- Wszystkie etykiety, walidacje, potwierdzenia i komunikaty są dostępne po polsku i angielsku.

### Migracja i nazewnictwo

- Nie modyfikujemy wykonanej migracji `V4`.
- Nowa migracja Flyway zmienia nazwę tabeli na `booking_training_templates`.
- Migracja zmienia również nazwy powiązanych indeksów i ograniczeń z `training_enrollments` na
  `training_templates`.
- Migracja dodaje obowiązkową kolumnę `default_start_time`.
- Obecnie nie ma zapisanych szablonów ani terminów, więc nie jest wymagane przenoszenie istniejących danych
  biznesowych ani warstwa kompatybilności starego API.

### Relacja z terminami

- Obsługa terminów jest poza zakresem tej zmiany.
- Docelowo kopiowanie danych szablonu do formularza terminu odbywa się wyłącznie na frontendzie.
- Backend tworzenia terminu nie otrzymuje `templateId` i nie wie, że dane pochodzą z szablonu.
- Termin otrzymuje niezależną kopię danych i nie przechowuje referencji do szablonu.
- Edycja lub usunięcie szablonu nie wpływa na wcześniej utworzone terminy.

### Kryteria akceptacji

- Użytkownik z `ORGANIZER` może utworzyć poprawny szablon i zobaczyć go na swojej liście.
- Organizator widzi wyłącznie własne szablony, posortowane malejąco po `updatedAt`.
- Organizator może otworzyć szczegóły, edytować i trwale usunąć własny szablon.
- Użytkownik bez `ORGANIZER` nie widzi nawigacji do szablonów, nie otworzy chronionej trasy i otrzymuje `403`
  z API.
- Organizator otrzymuje `404` podczas operacji na cudzym albo nieistniejącym szablonie.
- Walidacja backendu i frontendu egzekwuje ustalone limity, krok 30 minut dla czasu trwania oraz krok 15 minut
  dla domyślnej godziny rozpoczęcia.
- Ostrzeżenie przed opuszczeniem formularza pojawia się tylko dla niezapisanych zmian.
- Modal usunięcia pokazuje nazwę szablonu i wymaga jawnego potwierdzenia.
- Interfejs działa w języku polskim i angielskim.
- Dokumentacja zawiera polecenie SQL do nadania istniejącemu użytkownikowi roli `ORGANIZER`.

### Testy

- Testy domenowe obejmują walidację i aktualizację pojedynczego szablonu.
- Testy use case obejmują CRUD, własność zasobu, sortowanie listy i trwałe usunięcie.
- Testy web obejmują kontrakty HTTP, rolę `ORGANIZER`, odpowiedzi `403`, `404`, `201`, `200` i `204`.
- Testy infrastruktury obejmują mapowanie nowej tabeli i pola `default_start_time`.
- Testy Angular obejmują guard roli, listę, szczegóły, formularze, walidację, niezapisane zmiany, modal usunięcia
  oraz komunikaty sukcesu i błędów.
- Testy Playwright pozostają poza zakresem tej zmiany.

### Poza zakresem

- Tworzenie, publikowanie i edycja terminów.
- Powiązanie terminu z identyfikatorem szablonu.
- Synchronizacja zmian szablonu z terminami.
- Panel administracyjny do nadawania roli `ORGANIZER`.
- Automatyczne nadawanie `ORGANIZER` przy rejestracji lub migracji.
- Archiwizacja, wersje robocze i historia zmian szablonu.
- Duplikowanie szablonu.
- Współdzielenie szablonów między Organizatorami.
- Paginacja, wyszukiwanie i filtrowanie.
- Integracja lokalizacji z mapą lub wyszukiwarką adresów.
- Obsługa stref czasowych innych niż `Europe/Warsaw`.

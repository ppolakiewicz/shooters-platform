# CRUD szablonów szkoleń dla Organizatorów - Skrót planu

> Pełny plan: `context/changes/training-template-crud/plan.md`

## Co i dlaczego

Budujemy kompletny, prywatny CRUD szablonów szkoleń dla Organizatorów, aby dane kursu przeznaczone do ponownego użycia
mogły być zarządzane niezależnie przed utworzeniem konkretnego terminu. Zmiana zastępuje mylące, częściowe pojęcie
`TrainingEnrollment`, wprowadza jawną granicę autoryzacji `ORGANIZER` i pozostawia istniejące operacje rezerwacyjne bez
zmian.

## Punkt wyjścia

Backend obsługuje już tworzenie, listowanie i aktualizację danych szkolenia, ale używa starych nazw, niepełnej walidacji
i autoryzacji `USER`, a także nie ma szczegółów ani usuwania. Obecny ekran Angular `/bookings` miesza tworzenie
szablonów z zarządzaniem terminami, dlatego musi zostać rozdzielony przed ograniczeniem API szablonów do Organizatorów.

## Oczekiwany stan końcowy

Organizator może zarządzać wyłącznie własnymi szablonami przez osobne trasy listy, tworzenia, szczegółów i edycji oraz
trwale usuwać je po potwierdzeniu. Zwykły użytkownik nie widzi i nie otwiera tych tras, otrzymuje `403` z API szablonów
i zachowuje dotychczasowy dostęp do terminów oraz rezerwacji.

## Kluczowe decyzje

| Obszar                 | Wybór                                                              | Uzasadnienie                                                                  |
|------------------------|--------------------------------------------------------------------|-------------------------------------------------------------------------------|
| Istniejący `/bookings` | Usunąć zależność od szablonów i zachować ręczne tworzenie terminów | Zachowuje uprawnienia `USER` i pozostawia publikowanie terminów poza zakresem |
| Routing CRUD           | Osobne trasy listy, tworzenia, szczegółów i edycji                 | Zapewnia czytelną nawigację, bezpośrednie linki i proste guardy               |
| Początkowe `404`       | Stan not-found z akcją powrotu do listy                            | Wyjaśnia sytuację bez ujawniania właściciela zasobu                           |
| Powiadomienia          | Snackbary Angular Material                                         | Obsługują sukces po nawigacji i nieblokujące błędy                            |
| Niezapisane zmiany     | `CanDeactivate` z przetłumaczonym `window.confirm`                 | Chroni każdą zmianę trasy przy małej złożoności                               |
| Definicja dirty        | Stan dirty kontrolek Angular                                       | Odpowiada zaakceptowanemu zachowaniu nawet po ręcznym przywróceniu wartości   |
| Zachowanie po delete   | Przejście na listę i ponowne pobranie                              | Nie pozostawia użytkownika pod URL-em usuniętego zasobu                       |
| `404` zapisu/delete    | Zachować formularz lub element i pokazać snackbar                  | Chroni dane oraz kontekst przy równoczesnym usunięciu                         |
| Czas trwania           | Pole godzin `0.5..24`, krok `0.5`                                  | Jest naturalne dla Organizatora i zachowuje minuty całkowite w API            |
| Godzina startu         | Natywne pole czasu i jawna walidacja kwadransów                    | Zapewnia wygodne pole mobilne bez selecta z 96 wartościami                    |
| Pola listy             | Nazwa, poziom i data aktualizacji                                  | Utrzymuje zwięzłość i pomaga odróżnić popularne duplikaty nazw                |
| Kompatybilność         | Usunąć stare API i nazwy kodu                                      | Brak danych biznesowych i wymogu warstwy zgodności                            |

## Zakres

**W zakresie:**

- Przemianowanie tabeli i obiektów Flyway, ograniczenia, `default_start_time` i rola `ORGANIZER`.
- Pełny backendowy CRUD z kontrolą właściciela oraz `/api/bookings/training-templates`.
- Dokumentacja administracyjnego SQL nadającego rolę.
- Oddzielenie ładowania szablonów od istniejącego zarządzania terminami.
- Osobne trasy Angular CRUD, guard, formularze, potwierdzenie, snackbary, tłumaczenia PL/EN oraz testy jednostkowe i
  integracyjne.

**Poza zakresem:**

- Tworzenie i publikowanie terminów z szablonów.
- Referencje szablonów w terminach i synchronizacja między nimi.
- Interfejs zarządzania rolami, automatyczne nadawanie organizatora, współdzielenie, duplikowanie, historia,
  wyszukiwanie, paginacja i mapy.
- Nowe pokrycie Playwright.

## Architektura i podejście

Flyway i identity ustanawiają końcowy schemat oraz rolę. Backend zachowuje przepływ
`web -> usecase -> trainingtemplate.domain/infrastructure`, z jednym publicznym use case'em na operację CRUD i kontrolą
właściciela w serwisie domenowym. Angular najpierw usuwa stare powiązanie z `bookings`, a następnie wprowadza wydzieloną
funkcję `training-templates`, której trasy są chronione rolami zwracanymi już przez `/api/auth/me`.

## Fazy w skrócie

| Faza                       | Rezultat                                                              | Główne ryzyko                                                      |
|----------------------------|-----------------------------------------------------------------------|--------------------------------------------------------------------|
| 1. Schemat i rola          | Końcowa tabela, ograniczenia, godzina startu, rola i dokumentacja SQL | Przemianowanie obiektów migracji i wymiana ograniczeń              |
| 2. Backend CRUD            | Kanoniczny model, operacje właściciela, API Organizatora i testy      | Ukrywanie obcych zasobów przy zachowaniu innych uprawnień bookings |
| 3. Oddzielenie UI terminów | `/bookings` działa bez dostępu do szablonów                           | Regresja ręcznego tworzenia terminów lub zarządzania rezerwacjami  |
| 4. Angular CRUD            | Kompletny, dwujęzyczny interfejs wyłącznie dla Organizatora           | Koordynacja dirty form, powiadomień po nawigacji i błędów delete   |

**Wymagania wstępne:** Lokalny PostgreSQL do sprawdzenia migracji oraz konta testowe `USER` i `USER + ORGANIZER`.

**Szacowany wysiłek:** Około 4 sesji implementacyjnych, po jednej na fazę, oraz ręczna weryfikacja na każdej granicy.

## Ryzyka i założenia

- Starsze testy Playwright tworzą obecnie inline `TrainingEnrollment`. Są poza zakresem i mogą pozostać niesprawne do
  czasu późniejszego dostosowania przepływu publikowania terminów. Należy to zapisać zamiast niejawnie rozszerzać
  zmianę.
- Migracja zakłada instalację z wykonanymi V4 i V7. Nie wolno edytować tych historycznych migracji.
- Zaakceptowana definicja dirty może ostrzec po zmianie i ręcznym przywróceniu wartości, ponieważ kontrolka Angular
  nadal pozostaje dirty.
- Zachowanie natywnego pola czasu różni się między przeglądarkami, dlatego kontrakt 15 minut musi być egzekwowany
  niezależną walidacją.

## Podsumowanie kryteriów sukcesu

- Organizator wykonuje pięć operacji na wyłącznie własnych szablonach, z poprawnym sortowaniem, walidacją i dwujęzycznym
  UI.
- Zwykły użytkownik nie widzi nawigacji, jest przekierowywany z tras szablonów, otrzymuje API `403` i nadal zarządza
  terminami w istniejącym przepływie.
- Build backendu, build/test/lint Angular, kontrole migracji i wszystkie testy integracyjne w zakresie przechodzą bez
  aktywnych odwołań do starego API lub modelu training-enrollment.

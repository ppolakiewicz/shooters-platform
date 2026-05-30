# Przepływ Rejestracji Uczestnika I Listy Rezerwowej - Plan Implementacji

## Przegląd

Ukończyć element roadmapy S-02: uczestnicy mogą przeglądać opublikowane sesje, rezerwować, gdy są dostępne miejsca, i dołączać do listy rezerwowej dopiero po zapełnieniu pojemności.

To zmiana o średniej złożoności, ponieważ główna domena rezerwacji już istnieje, ale implementacja musi doprecyzować zachowanie w use case'ach backendu, publicznym UI Angular i pokryciu e2e. Plan zależy od S-01 jako docelowego źródła terminów "opublikowanych z informacji o kursie do ponownego użycia"; dopóki S-01 nie wyląduje, istniejący enrollment szkolenia i tworzenie terminu są poprawnymi fixture'ami do planowania i testów.

## Analiza Obecnego Stanu

- Publiczne odkrywanie istnieje przez `ListPublicTermsUseCase`, który listuje przyszłe publiczne terminy i wylicza dostępne miejsca z zajętych rezerwacji (`backend/src/main/java/com/shootersplatform/backend/bookings/usecase/ListPublicTermsUseCase.java:23`).
- Publiczne szczegóły terminu istnieją przez `GetPublicTermUseCase` i `TermResponse`, w tym `availablePlaces` (`backend/src/main/java/com/shootersplatform/backend/bookings/web/TermResponse.java:8`).
- Tworzenie rezerwacji już blokuje termin, odrzuca zduplikowane aktywne e-maile, tworzy potwierdzoną rezerwację, dopóki pojemność pozostaje dostępna, oraz tworzy wpis na listę rezerwową, gdy termin jest pełny (`backend/src/main/java/com/shootersplatform/backend/bookings/usecase/CreateReservationUseCase.java:71`).
- Publiczna lista Angular jest chronologiczna i już przełącza treść przycisku listy na listę rezerwową, gdy `availablePlaces <= 0` (`frontend/src/app/bookings/booking-public-list.component.ts:28`, `frontend/src/app/bookings/booking-public-list.component.ts:54`).
- Strona szczegółów Angular używa jednego formularza i już renderuje wyniki rezerwacji oraz listy rezerwowej, ale jej nagłówek formularza i treść przycisku są nadal zorientowane na rezerwację (`frontend/src/app/bookings/booking-public-detail.component.html:75`, `frontend/src/app/bookings/booking-public-detail.component.html:113`).
- Istniejące testy backendu i e2e obejmują wiele zachowań rezerwacji, w tym rezerwację, listę rezerwową, anulowanie i promocję (`backend/src/test/groovy/com/shootersplatform/backend/bookings/web/ReservationUserPathIntegrationSpec.groovy:48`, `e2e/tests/bookings.spec.ts:3`).

## Decyzje

1. Publiczne odkrywanie to lista chronologiczna plus bezpośrednie linki do szczegółów terminu. Wyszukiwanie i filtrowanie są poza S-02.
2. Pełne terminy używają tego samego formularza uczestnika, z nagłówkiem, przyciskiem i wynikiem specyficznym dla listy rezerwowej.
3. Udany wpis na listę rezerwową pokazuje pozycję i token anulowania.
4. Rezerwacja gościa pozostaje obsługiwana, z opcjonalnym utworzeniem konta w tym samym przepływie.
5. Weryfikacja backendu musi obejmować jawne pokrycie współbieżności pojemności.
6. Istniejące zachowanie promocji z listy rezerwowej jest chronione regresyjnie, ale nie jest rozszerzane.
7. Akceptacja e2e to jedna publiczna ścieżka obejmująca rezerwację i listę rezerwową.

## Zakres

### W Zakresie

- Potwierdzić i utwardzić zachowanie dostępności publicznej listy/szczegółów terminów.
- Potwierdzić i utwardzić zachowanie tworzenia rezerwacji vs listy rezerwowej.
- Dodać backendowe pokrycie ukierunkowane na współbieżność dla egzekwowania pojemności.
- Dopracować treść i stan publicznego UI szczegółów dla pełnych terminów.
- Zachować opcjonalne tworzenie konta podczas rezerwacji.
- Utrzymać widoczność tokenu anulowania po rezerwacji albo wpisie na listę rezerwową.
- Zaktualizować testy frontendu dla treści pełnego terminu i renderowania wyniku listy rezerwowej.
- Zaktualizować jeden przepływ e2e, aby obejmował akceptację rezerwacji i listy rezerwowej od listy do szczegółów.

### Poza Zakresem

- Wyszukiwanie, filtry albo zaawansowane publiczne odkrywanie.
- Nowe zachowanie powiadomień dla promocji z listy rezerwowej.
- Zaplanowana komunikacja ze szczegółami sesji.
- Potwierdzanie obecności.
- Płatności albo billing.
- Szeroki refactor modułu `bookings`.

## Faza 1: Utwardzenie Kontraktu Backendu

### Cel

Uczynić kontrakt backendu S-02 jawnym i trudnym do regresji: publiczne terminy wystawiają dokładną dostępność, żądania rezerwacji nigdy nie przekraczają pojemności, a pełne terminy tworzą wpisy na listę rezerwową.

### Wymagane Zmiany

#### Testy use case'ów backendu

- Plik: `backend/src/test/groovy/com/shootersplatform/backend/bookings/usecase/ReservationUseCasesSpec.groovy`
    - **Intencja:** Dodać albo wzmocnić test dowodzący, że termin z jednym miejscem nigdy nie tworzy dwóch potwierdzonych rezerwacji przy współbieżnych próbach rezerwacji.
    - **Kontrakt:** Test powinien asercjami potwierdzać, że liczba potwierdzonych rezerwacji dla terminu nigdy nie przekracza pojemności, a nadmiarowi uczestnicy trafiają na listę rezerwową albo otrzymują kontrolowany wynik walidacyjny. Użyj istniejącego kontekstu testów in-memory tylko wtedy, gdy potrafi wiernie modelować locking; w przeciwnym razie umieść test wyścigu w warstwie integracyjnej, gdzie działa blokada wiersza bazy danych.

- Plik: `backend/src/test/groovy/com/shootersplatform/backend/bookings/web/ReservationUserPathIntegrationSpec.groovy`
    - **Intencja:** Zachować zachowanie publicznego API dla rezerwacji gościa i tworzenia listy rezerwowej przez HTTP.
    - **Kontrakt:** Istniejące testy powinny nadal asercjami potwierdzać `RESERVATION` dla dostępnej pojemności i `WAITLIST_ENTRY` dla pełnej pojemności, bez wycieku tajnego tokenu przez listy zarządzania właściciela.

#### Kontrakt dostępności backendu

- Plik: `backend/src/main/java/com/shootersplatform/backend/bookings/usecase/ListPublicTermsUseCase.java`
    - **Intencja:** Zachować dostępność na publicznej liście jako źródło prawdy dla treści publicznej listy.
    - **Kontrakt:** `AvailableTerm.availablePlaces` pozostaje nieujemne i wynika z pojemności minus liczba zajętych rezerwacji.

- Plik: `backend/src/main/java/com/shootersplatform/backend/bookings/usecase/GetPublicTermUseCase.java`
    - **Intencja:** Zachować spójność dostępności publicznych szczegółów z dostępnością publicznej listy.
    - **Kontrakt:** Odpowiedzi szczegółów wystawiają tę samą semantykę `availablePlaces` co odpowiedzi listy.

- Plik: `backend/src/main/java/com/shootersplatform/backend/bookings/usecase/CreateReservationUseCase.java`
    - **Intencja:** Zachować jedną ścieżkę komendy zarówno dla rezerwacji, jak i wpisu na listę rezerwową.
    - **Kontrakt:** Jeśli zajęte miejsca są poniżej pojemności, zwróć `CreatedBooking.reservation`; w przeciwnym razie zwróć `CreatedBooking.waitlistEntry`. Zduplikowane aktywne e-maile rezerwacji/listy rezerwowej pozostają odrzucane przed utworzeniem kolejnego wpisu.

### Kryteria Sukcesu

#### Weryfikacja Automatyczna

- `.\gradlew.bat test --tests "*ReservationUseCasesSpec"` przechodzi z `backend/`.
- `.\gradlew.bat test --tests "*ReservationUserPathIntegrationSpec"` przechodzi z `backend/`.
- Nowe albo istniejące testy dowodzą, że termin z jednym miejscem nigdy nie ma więcej potwierdzonych rezerwacji niż pojemność.

#### Weryfikacja Ręczna

- Przejrzeć nazwy i asercje testów backendu: powinny czytać się jak reguły biznesowe, nie jak szczegóły implementacji.

## Faza 2: Dopracowanie Publicznego UI

### Cel

Dopasować UI widoczne dla uczestnika do zaakceptowanego zachowania MVP: chronologiczne odkrywanie, bezpośrednia nawigacja do szczegółów, jeden adaptacyjny formularz oraz jasna treść wyniku dla rezerwacji albo listy rezerwowej.

### Wymagane Zmiany

#### Publiczna lista

- Plik: `frontend/src/app/bookings/booking-public-list.component.ts`
    - **Intencja:** Zachować kolejność chronologiczną i etykiety akcji oparte o pojemność.
    - **Kontrakt:** `sortedTerms` pozostaje posortowane po dacie rozpoczęcia, a `isWaitlistTerm(term)` pozostaje jedynym predykatem dla treści listy rezerwowej na liście.

- Plik: `frontend/src/app/bookings/booking-public-list.component.html`
    - **Intencja:** Zachować bezpośrednią nawigację z każdego publicznego wiersza do szczegółów terminu.
    - **Kontrakt:** Każdy wiersz linkuje do `/booking-terms/:id`; gdy `availablePlaces <= 0`, widoczna akcja używa treści listy rezerwowej.

#### Formularz szczegółów publicznych

- Plik: `frontend/src/app/bookings/booking-public-detail.component.ts`
    - **Intencja:** Dodać stan pochodny na poziomie szczegółów, który mówi, czy obecny termin zostanie wysłany jako rezerwacja, czy jako lista rezerwowa na podstawie `availablePlaces`.
    - **Kontrakt:** Komponent wystawia stabilny predykat albo computed value równoważne `term()?.availablePlaces <= 0`; nie decyduje lokalnie o finalnym typie booking po wysłaniu, bo odpowiedź backendu pozostaje autorytatywna.

- Plik: `frontend/src/app/bookings/booking-public-detail.component.html`
    - **Intencja:** Dostosować nagłówek, treść objaśniającą i przycisk submit do trybu rezerwacji vs dołączenia do listy rezerwowej.
    - **Kontrakt:** Ten sam formularz wysyła oba tryby. Tekst przycisku to `common.reserve`, gdy miejsca pozostają dostępne, i `bookings.public.joinWaitlist`, gdy termin jest pełny. Panele wyniku nadal rozgałęziają się po `booking().type`.

- Pliki: `frontend/src/app/shared/i18n/translations.en.ts`, `frontend/src/app/shared/i18n/translations.pl.ts`
    - **Intencja:** Dodać brakujące zlokalizowane etykiety dla adaptacyjnego formularza szczegółów i jaśniejszej treści wyniku listy rezerwowej.
    - **Kontrakt:** Istniejące klucze pozostają stabilne, chyba że test zostanie zaktualizowany dla celowej poprawy treści.

#### Testy frontendu

- Plik: `frontend/src/app/bookings/booking-public-detail.component.spec.ts`
    - **Intencja:** Pokryć zachowanie szczegółów pełnego terminu i renderowanie wyniku listy rezerwowej.
    - **Kontrakt:** Testy powinny asercjami potwierdzać, że `availablePlaces: 0` zmienia treść formularza na tryb listy rezerwowej oraz że wynik `WAITLIST_ENTRY` wyświetla pozycję i token anulowania.

- Plik: `frontend/src/app/bookings/booking-public-list.component.spec.ts`
    - **Intencja:** Zachować chronologiczną listę i treść akcji listy rezerwowej.
    - **Kontrakt:** Istniejące testy sortowania i treści listy dla pełnego terminu pozostają zielone; dodaj pokrycie tylko wtedy, gdy obecne asercje pomijają finalny kontrakt treści.

### Kryteria Sukcesu

#### Weryfikacja Automatyczna

- `npm run test --workspace frontend -- bookings` przechodzi albo przechodzi najbliższe wspierane skupione wywołanie Vitest dla speców booking.
- `npm run lint --workspace frontend` przechodzi.
- `npm run frontend:build` przechodzi.

#### Weryfikacja Ręczna

- Na `/booking-terms` przyszłe terminy pojawiają się chronologicznie, a pełne terminy pokazują treść akcji listy rezerwowej.
- Na `/booking-terms/:id` pełny termin pokazuje nagłówek/przycisk listy rezerwowej, używając tego samego formularza uczestnika.
- Udana rezerwacja wyświetla treść potwierdzonej rezerwacji i token anulowania.
- Udane dołączenie do listy rezerwowej wyświetla pozycję na liście i token anulowania.

## Faza 3: Akceptacja End-to-End

### Cel

Udowodnić zachowanie gwiazdy północnej S-02 przez ścieżkę na poziomie przeglądarki zaczynającą się od publicznej powierzchni uczestnika.

### Wymagane Zmiany

#### Scenariusz E2E

- Plik: `e2e/tests/bookings.spec.ts`
    - **Intencja:** Utrzymać jeden scenariusz e2e, który tworzy fixture terminu, odwiedza publiczny przepływ listy/szczegółów, potwierdza rezerwację pierwszego uczestnika i potwierdza wpis na listę rezerwową drugiego uczestnika.
    - **Kontrakt:** Scenariusz powinien nawigować przez publiczną listę albo bezpośredni link szczegółów, używać widocznych kontrolek formularza uczestnika, asercjami potwierdzać `RESERVATION` dla pierwszego booking, asercjami potwierdzać `WAITLIST_ENTRY` i pozycję dla drugiego booking oraz unikać rozszerzania w zaparkowaną pracę nad powiadomieniami.

- Plik: `e2e/tests/bookings.spec.ts`
    - **Intencja:** Utrzymać istniejące asercje promocji jako pokrycie regresyjne.
    - **Kontrakt:** Jeśli promocja pozostaje w ścieżce e2e, nie może stać się głównym kryterium akceptacji S-02. Główna asercja to zachowanie rezerwacji/listy rezerwowej uczestnika.

#### Akceptacja międzywarstwowa

- Plik: `frontend/src/app/bookings/booking.service.ts`
    - **Intencja:** Zachować publiczny kontrakt serwisu używany przez ścieżkę e2e.
    - **Kontrakt:** `publicTerms`, `publicTerm` i `createReservation` pozostają punktami wejścia klienta dla S-02.

### Kryteria Sukcesu

#### Weryfikacja Automatyczna

- `npm run e2e:test` przechodzi w standardowym setupie workspace.
- Komendy backend, frontend build/test/lint i e2e równoważne CI przechodzą uruchomione razem.

#### Weryfikacja Ręczna

- Używając działającej aplikacji, utwórz albo użyj opublikowanego przyszłego terminu o pojemności 1.
- Jako publiczny uczestnik zarezerwuj pierwsze miejsce i zobacz wynik potwierdzonej rezerwacji.
- Jako drugi publiczny uczestnik wyślij ten sam formularz i zobacz pozycję 1 na liście rezerwowej.
- Wróć do publicznej listy i zweryfikuj, że termin pokazuje brak dostępnych miejsc / treść akcji listy rezerwowej.

## Strategia Testowania

### Testy Jednostkowe I Komponentowe

- Komponent publicznej listy: kolejność chronologiczna, treść badge'a dostępności, treść akcji dla pełnego terminu.
- Komponent publicznych szczegółów: adaptacyjna treść formularza rezerwacja/lista rezerwowa, opcjonalne pola konta, wynik rezerwacji, wynik listy rezerwowej.
- Serwis booking: ciało żądania `createReservation` i zachowanie CSRF pozostają nienaruszone.

### Testy Backendu

- Testy use case'ów: rezerwacja vs lista rezerwowa, duplikaty, tworzenie konta gościa, odrzucenie po deadline i ochrona współbieżności/pojemności.
- Testy integracyjne: publiczne API zwraca dostępność, tworzenie rezerwacji zwraca poprawny typ `CreatedBooking`, API zarządzania właściciela nie wycieka tajnych tokenów.

### Testy E2E

- Jedna ścieżka przeglądarkowa obejmuje setup fixture organizatora, nawigację publiczna lista/szczegóły, potwierdzoną rezerwację pierwszego uczestnika i wpis na listę rezerwową drugiego uczestnika.

## Kwestie Wydajności

- Publiczna dostępność obecnie liczy zajęte miejsca per termin. To akceptowalne dla skali MVP, ale w tej zmianie unikaj dodawania client-side polling albo powtarzających się pętli odświeżania.
- Ochrona współbieżności powinna opierać się na istniejącym zachowaniu transakcji/lockingu, a nie na optymistycznych założeniach UI.

## Notatki Migracyjne

Nie oczekuje się migracji schematu dla S-02. Jeśli implementacja odkryje, że dostępność albo status listy rezerwowej wymaga nowego utrwalanego pola, zatrzymaj się i przedefiniuj zakres, ponieważ rozszerzyłoby to zmianę poza obecny plan.

## Plan Rollbacku

- Zmiany UI można cofnąć niezależnie, jeśli zachowanie backendu pozostaje stabilne.
- Zmiany backendu powinny zachować istniejący kształt odpowiedzi publicznego API. Jeśli utwardzenie backendu psuje kompatybilność, cofnij tę zmianę i zachowaj testy jako czerwony sygnał do dalszego planowania.
- Zmiany e2e można tymczasowo zawęzić do asercji opartych na API, jeśli flake przeglądarkowy zablokuje dostarczenie, ale finalna akceptacja S-02 powinna przywrócić ścieżkę przeglądarkową.

## Referencje

- PRD: `context/foundation/prd.md`
- Roadmapa: `context/foundation/roadmap.md`
- Use case publicznej listy:
  `backend/src/main/java/com/shootersplatform/backend/bookings/usecase/ListPublicTermsUseCase.java`
- Use case rezerwacji:
  `backend/src/main/java/com/shootersplatform/backend/bookings/usecase/CreateReservationUseCase.java`
- Komponent publicznej listy: `frontend/src/app/bookings/booking-public-list.component.ts`
- Komponent publicznych szczegółów: `frontend/src/app/bookings/booking-public-detail.component.html`
- Ścieżka e2e booking: `e2e/tests/bookings.spec.ts`

## Postęp

> Konwencja: `- [ ]` oczekuje, `- [x]` zrobione. Dodaj ` - <commit sha>`, gdy krok wyląduje. Nie zmieniaj nazw kroków.

### Faza 1: Utwardzenie Kontraktu Backendu

#### Automatyczne

- [ ] 1.1 `.\gradlew.bat test --tests "*ReservationUseCasesSpec"` przechodzi z `backend/`.
- [ ] 1.2 `.\gradlew.bat test --tests "*ReservationUserPathIntegrationSpec"` przechodzi z `backend/`.
- [ ] 1.3 Nowe albo istniejące testy dowodzą, że termin z jednym miejscem nigdy nie ma więcej potwierdzonych rezerwacji niż pojemność.

#### Ręczne

- [ ] 1.4 Przejrzeć nazwy i asercje testów backendu pod kątem jasności reguł biznesowych.

### Faza 2: Dopracowanie Publicznego UI

#### Automatyczne

- [ ] 2.1 `npm run test --workspace frontend -- bookings` przechodzi albo przechodzi najbliższe wspierane skupione wywołanie Vitest dla speców booking.
- [ ] 2.2 `npm run lint --workspace frontend` przechodzi.
- [ ] 2.3 `npm run frontend:build` przechodzi.

#### Ręczne

- [ ] 2.4 Na `/booking-terms` przyszłe terminy pojawiają się chronologicznie, a pełne terminy pokazują treść akcji listy rezerwowej.
- [ ] 2.5 Na `/booking-terms/:id` pełny termin pokazuje nagłówek/przycisk listy rezerwowej, używając tego samego formularza uczestnika.
- [ ] 2.6 Udana rezerwacja wyświetla treść potwierdzonej rezerwacji i token anulowania.
- [ ] 2.7 Udane dołączenie do listy rezerwowej wyświetla pozycję na liście i token anulowania.

### Faza 3: Akceptacja End-to-End

#### Automatyczne

- [ ] 3.1 `npm run e2e:test` przechodzi w standardowym setupie workspace.
- [ ] 3.2 Komendy backend, frontend build/test/lint i e2e równoważne CI przechodzą uruchomione razem.

#### Ręczne

- [ ] 3.3 Używając działającej aplikacji, utwórz albo użyj opublikowanego przyszłego terminu o pojemności 1.
- [ ] 3.4 Jako publiczny uczestnik zarezerwuj pierwsze miejsce i zobacz wynik potwierdzonej rezerwacji.
- [ ] 3.5 Jako drugi publiczny uczestnik wyślij ten sam formularz i zobacz pozycję 1 na liście rezerwowej.
- [ ] 3.6 Wróć do publicznej listy i zweryfikuj, że termin pokazuje brak dostępnych miejsc / treść akcji listy rezerwowej.

---
project: "Shooters platform"
version: 1
status: draft
created: 2026-05-27
updated: 2026-05-27
prd_version: 1
main_goal: market-feedback
top_blocker: decisions
---

# Roadmapa

## Przypomnienie Wizji

Shooters Platform ogranicza powtarzalną pracę organizatorów i niepewność uczestników wokół dostępności kursów strzeleckich. MVP powinno udowodnić, że organizator może ponownie użyć informacji o kursie dla datowanej sesji, opublikować ją i pozwolić uczestnikowi zarejestrować się albo dołączyć do listy rezerwowej bez przekraczania pojemności.

Istniejący codebase ma już główne warstwy aplikacji, więc roadmapa skupia się na domykaniu luk produktowych względem PRD zamiast na budowaniu szerokich fundamentów technicznych.

## Gwiazda Północna

Gwiazda północna oznacza najmniejszy widoczny dla użytkownika przepływ end-to-end, który dowodzi, że produkt działa. Dla tego PRD ten przepływ to: organizator publikuje datowaną sesję z informacji o kursie nadających się do ponownego użycia, a uczestnik znajduje ją i albo rejestruje się, gdy są dostępne miejsca, albo dołącza do listy rezerwowej, gdy sesja jest pełna.

- Element roadmapy: S-02
- Dlaczego to dowodzi produktu: uruchamia przepływ konfiguracji organizatora, publiczne odkrywanie, egzekwowanie pojemności i regułę listy rezerwowej w jednej ścieżce uczestnika.
- Referencje PRD: US-01, FR-001, FR-002, FR-003, FR-004, FR-005, FR-006

## W Skrócie

| ID   | Rezultat                                                                                                      | Change ID                              | Wymagania wstępne | Referencje PRD                 | Status   |
|------|---------------------------------------------------------------------------------------------------------------|----------------------------------------|-------------------|-------------------------------|----------|
| S-01 | Organizator może tworzyć informacje o kursie do ponownego użycia i publikować z nich edytowalną datowaną sesję. | course-template-session-publishing     | -                 | FR-001, FR-002, FR-003        | ready    |
| S-02 | Uczestnik może przeglądać opublikowaną sesję i zarejestrować się albo dołączyć do listy rezerwowej według reguł pojemności. | participant-registration-waitlist-flow | S-01              | US-01, FR-004, FR-005, FR-006 | proposed |
| S-03 | Zarejestrowani uczestnicy otrzymują zaplanowaną komunikację ze szczegółami sesji, a organizatorzy widzą status wysyłki. | scheduled-session-communication        | S-02              | FR-007                        | blocked  |
| S-04 | Organizator albo instruktor może potwierdzić obecność przed kursem.                                           | pre-course-attendance-confirmation     | S-02              | US-01, FR-008                 | proposed |

## Strumienie

| Strumień         | Łańcuch    | Cel                                                                                                  |
|------------------|------------|-------------------------------------------------------------------------------------------------------|
| Rdzeń rezerwacji | S-01, S-02 | Dowodzi ścieżki od szablonu przez sesję do uczestnika oraz reguły pojemności.                         |
| Operacje kursu   | S-03, S-04 | Dodaje komunikację przed kursem i pracę organizatora nad potwierdzeniem po ustabilizowaniu rezerwacji. |

## Stan Bazowy

- Frontend: obecny. Istnieją routowane ekrany rezerwacji i tożsamości, w tym publiczna lista/szczegóły rezerwacji i
  zarządzanie rezerwacjami przez organizatora.
- Backend/API: obecne. API zawiera już identity, bookings, kontrolery web, use case'y, serwisy domenowe i adaptery
  persystencji.
- Dane: obecne. Schemat bazy danych jest zarządzany migracjami, z tabelami identity i booking już reprezentowanymi.
- Auth: obecny. Rejestracja i logowanie e-mail/hasło, obsługa sesji, CSRF i autoryzacja na poziomie tras są zaimplementowane.
- Deploy/infra: częściowe. Istnieją lokalna konfiguracja bazy danych i CI z kontrolami backendu, frontendu oraz end-to-end; nie znaleziono produkcyjnego celu wdrożenia.
- Obserwowalność: częściowa. Istnieje readiness przez Spring Boot Actuator i logowanie aplikacji; nie znaleziono
  dedykowanych metryk, tracingu ani raportowania błędów.

## Fundamenty

Nie są potrzebne osobne elementy fundamentów przed pierwszym wycinkiem roadmapy. Codebase ma już fundamenty frontendu, backendu, danych i auth wymagane do rozpoczęcia przepływu produktowego zgodnego z PRD.

## Wycinki

### S-01: Organizator może tworzyć informacje o kursie do ponownego użycia i publikować z nich edytowalną datowaną sesję.

- Rezultat: Organizator może utworzyć informacje o kursie do ponownego użycia, użyć ich do wstępnego wypełnienia datowanej sesji, edytować skopiowane szczegóły i opublikować sesję.
- Change ID: course-template-session-publishing
- Referencje PRD: FR-001, FR-002, FR-003
- Wymagania wstępne: -
- Równolegle z: -
- Blokery: -
- Niewiadome:
    - Czy istniejące pojęcie kursu do ponownego użycia jest kanonicznym szablonem kursu, czy język produktu i model powinny zostać dostosowane? Właściciel: user/product. Blokuje: nie.
- Ryzyko: To idzie jako pierwsze, ponieważ ścieżka rezerwacji uczestnika zależy od opublikowanej datowanej sesji z wiarygodnie skopiowanymi szczegółami.
- Status: ready

### S-02: Uczestnik może przeglądać opublikowaną sesję i zarejestrować się albo dołączyć do listy rezerwowej według reguł pojemności.

- Rezultat: Uczestnik może przeglądać albo znajdować opublikowane sesje, rejestrować się tylko wtedy, gdy miejsca są dostępne, i dołączać do listy rezerwowej dopiero po zapełnieniu pojemności.
- Change ID: participant-registration-waitlist-flow
- Referencje PRD: US-01, FR-004, FR-005, FR-006
- Wymagania wstępne: S-01
- Równolegle z: -
- Blokery: -
- Niewiadome:
    - Jaka publiczna powierzchnia odkrywania wystarczy dla MVP: tylko przegląd chronologiczny, wyszukiwanie, filtry czy bezpośrednie linki? Właściciel: user/product. Blokuje: nie.
- Ryzyko: To centralny dowód produktu; powinno nastąpić po S-01, aby ścieżka uczestnika testowała realne sesje opublikowane przez organizatora, a nie dane syntetyczne.
- Status: proposed

### S-03: Zarejestrowani uczestnicy otrzymują zaplanowaną komunikację ze szczegółami sesji, a organizatorzy widzą status wysyłki.

- Rezultat: Zarejestrowani uczestnicy otrzymują szablonowe szczegóły sesji zgodnie z konfiguracją sesji, a organizatorzy widzą, czy komunikacja została wysłana.
- Change ID: scheduled-session-communication
- Referencje PRD: FR-007
- Wymagania wstępne: S-02
- Równolegle z: S-04
- Blokery: -
- Niewiadome:
    - Co liczy się jako status wysłania dla MVP: próba wysyłki, akceptacja przez dostawcę dostarczania czy ręczne oznaczenie jako wysłane? Właściciel: user/product. Blokuje: tak.
    - Czy MVP powinno od razu użyć realnego kanału dostarczania, czy najpierw zapisać workflow i później wymienić adapter dostarczania? Właściciel: user/technical. Blokuje: tak.
- Ryzyko: Sekwencjonowanie tego po S-02 pozwala uniknąć budowania komunikacji wokół niestabilnych stanów rezerwacji, ale decyzja o statusie wysyłki musi zostać podjęta przed planowaniem wycinka.
- Status: blocked

### S-04: Organizator albo instruktor może potwierdzić obecność przed kursem.

- Rezultat: Organizator albo instruktor może oznaczyć, czy zarejestrowany uczestnik jest potwierdzony do obecności przed kursem.
- Change ID: pre-course-attendance-confirmation
- Referencje PRD: US-01, FR-008
- Wymagania wstępne: S-02
- Równolegle z: S-03
- Blokery: -
- Niewiadome:
    - Jakie statusy wystarczą dla MVP: niepotwierdzony i potwierdzony, czy także odmówił/brak odpowiedzi? Właściciel: user/product. Blokuje: nie.
- Ryzyko: To powinno nastąpić po ścieżce rezerwacji, ponieważ potwierdzanie obecności wymaga stabilnej listy zarejestrowanych uczestników, ale po ukończeniu S-02 może iść niezależnie od zaplanowanej komunikacji.
- Status: proposed

## Przekazanie Backlogu

| ID roadmapy | Change ID                              | Rekomendowana umiejętność                         | Notatka przekazania                                                                                              |
|------------|----------------------------------------|---------------------------------------------------|------------------------------------------------------------------------------------------------------------------|
| S-01       | course-template-session-publishing     | /10x-plan course-template-session-publishing      | Wyrównać informacje o kursie do ponownego użycia z publikowaniem datowanych sesji i edytowalnymi skopiowanymi szczegółami. |
| S-02       | participant-registration-waitlist-flow | /10x-plan participant-registration-waitlist-flow  | Ukończyć ścieżkę przeglądania, rejestracji, pojemności i listy rezerwowej uczestnika względem sesji opublikowanych przez organizatora. |
| S-03       | scheduled-session-communication        | /10x-plan scheduled-session-communication         | Planować dopiero po rozstrzygnięciu decyzji o statusie wysyłki i kanale dostarczania.                            |
| S-04       | pre-course-attendance-confirmation     | /10x-plan pre-course-attendance-confirmation      | Dodać potwierdzanie przez organizatora/instruktora dla zarejestrowanych uczestników.                              |

## Otwarte Pytania Roadmapy

1. **Mapowanie szablonu kursu** - Czy istniejące pojęcie kursu do ponownego użycia jest kanonicznym szablonem kursu, czy przed S-01 należy dostosować język i zachowanie produktu? Właściciel: user/product. Blokuje: nic.
2. **Zakres publicznego odkrywania** - Czy przegląd chronologiczny wystarczy dla MVP, czy FR-004 wymaga wyszukiwania albo filtrowania w pierwszym wycinku? Właściciel: user/product. Blokuje: nic.
3. **Status wysyłki komunikacji** - Jaki status wystarczy dla MVP: próba wysyłki, akceptacja przez dostawcę czy ręczne oznaczenie jako wysłane? Właściciel: user/product. Blokuje: S-03.
4. **Ścieżka dostarczania komunikacji** - Czy MVP powinno od razu użyć realnego kanału dostarczania, czy najpierw zapisać workflow i później wymienić dostarczanie? Właściciel: user/technical. Blokuje: S-03.
5. **Słownik statusów obecności** - Jakie statusy wystarczą przed kursem? Właściciel: user/product. Blokuje: nic.

## Zaparkowane

- Promowanie z listy rezerwowej po anulowaniu przez uczestnika. Powód: PRD oznacza to jako pożądane, ale poza głównym przepływem MVP.
- Powiadomienie listy rezerwowej po ręcznym usunięciu uczestnika przez organizatora. Powód: PRD oznacza to jako pożądane, ale poza głównym przepływem MVP.
- Moduł płatności albo billingowy. Powód: PRD jawnie wyklucza płatności; organizator ręcznie potwierdza obecność.
- Zaawansowane zarządzanie administracyjne. Powód: PRD zachowuje tylko role Organizator i Uczestnik dla MVP.

## Ukończone

Żaden element roadmapy nie jest jeszcze ukończony.

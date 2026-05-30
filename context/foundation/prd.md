---
project: "Shooters platform"
version: 1
status: draft
created: 2026-05-27
context_type: greenfield
product_type: web-app
target_scale:
  users: medium
  qps: low
  data_volume: small
timeline_budget:
  mvp_weeks: 3
  hard_deadline: null
  after_hours_only: true
---

# Dokument Wymagań Produktowych

## Wizja I Opis Problemu

Organizatorzy szkoleń powtarzają pracę związaną z przygotowaniem kursów i potwierdzaniem udziału, a uczestnicy chcący zapisać się na szkolenie muszą ręcznie śledzić dostępność, gdy sesja jest pełna. Problem pojawia się, gdy organizatorzy tworzą nową datowaną sesję szkoleniową od zera zamiast użyć szablonu, gdy uczestnicy nie mogą dołączyć do listy rezerwowej i otrzymać powiadomienia po zwolnieniu miejsca oraz gdy instruktorzy muszą ręcznie wysyłać e-maile przed każdą sesją, aby potwierdzić obecność.

Dzisiejszy koszt to wielokrotne wpisywanie opisu kursu, gdy zmienia się tylko data, utracone rejestracje, ręczna praca e-mailowa przed sesją i niepewność uczestników wokół dostępności. Kluczowy wniosek jest taki, że szablony kursów, aktywne powiadomienia listy rezerwowej i automatyczne potwierdzanie obecności przed sesją razem usuwają obecną pracę ręczną i ryzyko utraconych rejestracji.

## Użytkownik I Persona

### Główne persony

- Organizator szkolenia: tworzy i zarządza sesjami kursów strzeleckich, ponownie używa informacji o kursie między terminami, zarządza rejestracjami i listami rezerwowymi oraz potrzebuje potwierdzenia obecności przed każdą sesją.
- Uczestnik: szuka dostępnych sesji kursów strzeleckich, zapisuje się, dołącza do listy rezerwowej, gdy sesja jest pełna, i musi wiedzieć, kiedy zwolni się miejsce.

## Kryteria Sukcesu

### Główne

- MVP działa, gdy organizator może utworzyć albo ponownie użyć szablonu kursu, opublikować datowaną sesję z edytowalnymi, wstępnie wypełnionymi danymi, a uczestnik może zarejestrować się tylko wtedy, gdy są dostępne miejsca.
- Jeśli nie ma dostępnych miejsc, uczestnik może dołączyć do listy rezerwowej; uczestnik nie może dołączyć do listy rezerwowej, gdy miejsca są nadal dostępne.
- Zarejestrowani uczestnicy otrzymują szablonowy e-mail ze szczegółami sesji określoną liczbę dni przed sesją, a instruktor albo organizator może potwierdzić obecność przed kursem.

### Drugorzędne

- Promowanie z listy rezerwowej po anulowaniu jest pożądane, ale nie należy do głównego przepływu MVP.
- Powiadomienie listy rezerwowej po ręcznym usunięciu uczestnika przez organizatora jest pożądane, ale nie należy do głównego przepływu MVP.

### Ograniczenia Bezpieczeństwa

- Pojemność sesji nie może zostać przekroczona.

## Historie Użytkownika

### US-01: Uczestnik rejestruje się na datowany kurs strzelecki

- **Given** organizator opublikował datowaną sesję z szablonu kursu
- **When** uczestnik znajduje sesję i są dostępne miejsca
- **Then** uczestnik może zarejestrować się na sesję i jest wliczany do pojemności sesji

#### Kryteria Akceptacji

- Uczestnik nie może się zarejestrować, jeśli pojemność sesji jest już pełna.
- Uczestnik nie może dołączyć do listy rezerwowej, gdy miejsca są nadal dostępne.
- Uczestnik może dołączyć do listy rezerwowej, gdy nie ma dostępnych miejsc.
- Zarejestrowani uczestnicy otrzymują e-mail ze szczegółami sesji na podstawie szablonu e-maila przypisanego do sesji.
- Instruktor albo organizator może potwierdzić obecność przed kursem.

## Wymagania Funkcjonalne

### Konfiguracja kursu

- FR-001: Organizator może tworzyć szablony kursów. Priorytet: must-have
  > Socrates: Rozważony kontrargument: szablony mogą być nadmiarowe, jeśli jest tylko kilka typów kursów albo jeśli skopiowanie poprzedniej sesji jest prostsze. Rozstrzygnięcie: utrzymane; sesja jest innym pojęciem biznesowym z innym cyklem życia, a ta sama sesja może mieć wiele szablonów zależnie od sezonu.
- FR-002: Organizator może ponownie użyć szablonu kursu przy tworzeniu datowanej sesji. Priorytet: must-have
  > Socrates: Rozważony kontrargument: ponowne użycie szablonu może utworzyć nieaktualne szczegóły sesji albo kolidować z dostosowaniem konkretnej sesji. Rozstrzygnięcie: utrzymane; nie będzie dużo dostosowania specyficznego dla sesji, a użycie szablonu jest szybsze: wybrać szablon, ustawić datę, zatwierdzić i utworzyć nową sesję.
- FR-003: Organizator może edytować wstępnie wypełnione dane sesji przed publikacją. Priorytet: must-have
  > Socrates: Rozważony kontrargument: edycja skopiowanych danych może powodować niespójności między szablonem i sesją albo zmniejszać wartość szablonów. Rozstrzygnięcie: utrzymane; edycja jest potrzebna do szybkich korekt.

### Rejestracja i lista rezerwowa

- FR-004: Uczestnik może przeglądać albo znaleźć sesję. Priorytet: must-have
  > Socrates: Rozważony kontrargument: uczestnicy mogą trafiać przez bezpośrednie linki, a wyszukiwanie albo przeglądanie może być zbyt szerokie dla MVP. Rozstrzygnięcie: utrzymane; udostępnianie linków uczestnikom jest obecnie niewygodne, a uczestnicy powinni móc samodzielnie wyszukiwać bez interakcji z organizatorem.
- FR-005: Uczestnik może zarejestrować się tylko wtedy, gdy są dostępne miejsca. Priorytet: must-have
  > Socrates: Rozważony kontrargument: overbooking mógłby być użyteczny przy ryzyku nieobecności, albo przed zaliczeniem kogoś jako zarejestrowanego może być potrzebna akceptacja organizatora. Rozstrzygnięcie: utrzymane; sesje strzeleckie mają ograniczoną pojemność ze względów bezpieczeństwa i overbooking nie jest dozwolony.
- FR-006: Uczestnik może dołączyć do listy rezerwowej tylko wtedy, gdy nie ma dostępnych miejsc. Priorytet: must-have
  > Socrates: Rozważony kontrargument: niektórzy uczestnicy mogą preferować listę rezerwową nawet przy dostępnych miejscach albo organizatorzy mogą chcieć ręcznie kontrolować, czy lista rezerwowa jest otwarta. Rozstrzygnięcie: utrzymane; nie ma powodu, by dołączać do listy rezerwowej, gdy dostępne jest miejsce.

### Komunikacja sesji i obecność

- FR-007: System wysyła zarejestrowanym uczestnikom szablonowy e-mail ze szczegółami sesji określoną liczbę dni przed sesją. Priorytet: must-have
  > Socrates: Rozważony kontrargument: przed wysłaniem oficjalnych szczegółów kursu może być potrzebny ręczny przegląd albo problemy z dostarczaniem e-maili mogą czynić automatyczny e-mail ryzykownym jako must-have. Rozstrzygnięcie: utrzymane; szablon e-maila jest przypisany do szablonu sesji i kopiowany do sesji przy tworzeniu, jego treść jest stabilna, a aplikacja musi wiedzieć, że e-mail został wysłany, aby organizator mógł ręcznie skontaktować się z uczestnikiem, jeśli ten nie potwierdzi udziału.
- FR-008: Instruktor albo organizator może potwierdzić obecność przed kursem. Priorytet: must-have
  > Socrates: Rozważony kontrargument: samodzielne potwierdzenie przez uczestnika mogłoby być bardziej użyteczne albo potwierdzanie obecności mogłoby należeć po kursie. Rozstrzygnięcie: utrzymane; udział jest potwierdzany przelewem, a ponieważ MVP nie ma modułu billingowego ani płatności, organizator musi ręcznie potwierdzić obecność na podstawie otrzymanej płatności.

## Wymagania Niefunkcjonalne

- Organizatorzy mogą zobaczyć, czy zaplanowane e-maile do uczestników zostały wysłane.
- Dane kontaktowe i rejestracyjne uczestnika są widoczne tylko dla upoważnionych organizatorów i danego uczestnika.
- Produkt nigdy nie wyświetla ani nie potwierdza rejestracji ponad skonfigurowaną pojemność sesji.
- Uczestnicy mogą przeglądać i rejestrować się zarówno z telefonu, jak i z desktopu.

## Logika Biznesowa

Aplikacja egzekwuje reguły pojemności sesji przez rejestrowanie uczestników tylko wtedy, gdy są dostępne miejsca, dopuszczanie wejścia na listę rezerwową dopiero po zapełnieniu pojemności i wyzwalanie zaplanowanej komunikacji z uczestnikami przed sesją.

Reguła zużywa skonfigurowaną pojemność sesji, bieżącą liczbę zarejestrowanych uczestników, próbę rejestracji uczestnika oraz skonfigurowany timing komunikacji sesji. Jej wynikiem jest informacja, czy uczestnik zostaje zarejestrowany, może dołączyć do listy rezerwowej, czy jest zablokowany przed wejściem na listę rezerwową, ponieważ miejsca są nadal dostępne.

Użytkownik napotyka tę regułę podczas przeglądania i rejestrowania się na sesję, gdy pełna sesja oferuje wejście na listę rezerwową zamiast rejestracji oraz gdy zarejestrowani uczestnicy otrzymują szczegóły sesji przed kursem.

## Kontrola Dostępu

Użytkownicy logują się e-mailem i hasłem.

- Organizator: może zarządzać szablonami kursów, datowanymi sesjami, rejestracjami, listami rezerwowymi i potwierdzaniem obecności.
- Uczestnik: może przeglądać sesje, rejestrować się, dołączać do list rezerwowych i potwierdzać obecność.

## Poza Zakresem

- Brak modułu płatności albo billingowego: organizator potwierdza obecność ręcznie na podstawie przelewu.
- Brak zaawansowanego zarządzania administracyjnego: MVP zachowuje tylko role Organizator i Uczestnik.

## Otwarte Pytania

Nie zapisano otwartych pytań w shape notes.

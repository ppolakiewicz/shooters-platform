---
change_id: participant-registration-waitlist-flow
title: Participant registration and waitlist flow
status: new
created: 2026-05-31
updated: 2026-05-31
archived_at: null
---

## Notes

### Historyjka użytkownika

Jako uczestnik chcę zapisać się na opublikowany termin szkolenia, niezależnie od tego, czy jestem zalogowany, aby mieć
potwierdzone miejsce albo trafić na listę rezerwową, gdy termin jest pełny.

### Zakres

- Publiczny widok terminu pokazuje aktualną liczbę wolnych miejsc.
- Gdy są wolne miejsca, użytkownik widzi akcję `Zapisz się`.
- Gdy nie ma wolnych miejsc, ale lista rezerwowa nie jest pełna, użytkownik widzi akcję `Dołącz do listy rezerwowej`.
- Gdy termin i lista rezerwowa są pełne, użytkownik nie widzi aktywnej akcji zapisu.
- Formularz zapisu obsługuje użytkownika zalogowanego i niezalogowanego.
- Niezalogowany użytkownik może opcjonalnie utworzyć konto podczas zapisu, ale nie jest to główny cel historyjki.
- Organizator widzi utworzoną rezerwację na liście uczestników albo wpis na liście rezerwowej, jeśli istniejące widoki
  organizatora już to wspierają.

### Reguły biznesowe

- Jeden zapis dotyczy jednej osoby i jednego miejsca.
- Minimalne dane uczestnika: imię, nazwisko, e-mail i numer telefonu.
- Zapis jest możliwy tylko na publiczny, nadal rezerwowalny termin.
- System blokuje duplikaty po adresie e-mail w ramach jednego terminu.
- Uczestnik nie może mieć jednocześnie aktywnej rezerwacji i aktywnego wpisu na liście rezerwowej dla tego samego
  terminu i e-maila.
- Jeśli `availablePlaces > 0`, poprawny zapis tworzy potwierdzoną rezerwację.
- Jeśli `availablePlaces = 0` i `waitlistSize < 10`, poprawny zapis tworzy wpis na liście rezerwowej.
- Jeśli `availablePlaces = 0` i `waitlistSize >= 10`, system odrzuca zapis bez utworzenia rezerwacji ani wpisu na liście
  rezerwowej.
- Limit listy rezerwowej wynosi 10 aktywnych wpisów na termin i jest stałą regułą systemową.
- Wpis na listę rezerwową nie zajmuje miejsca na szkoleniu.
- Kolejność listy rezerwowej wynika z czasu utworzenia wpisu; najstarsze wpisy są wcześniej w kolejce.
- System nie pozwala, aby liczba aktywnych rezerwacji zajmujących miejsce przekroczyła pojemność terminu.
- Reguły pojemności szkolenia i limitu listy rezerwowej muszą być egzekwowane atomowo po stronie backendu/bazy.

### Kryteria akceptacji

- Dla terminu z wolnymi miejscami użytkownik może wysłać formularz zapisu i otrzymuje potwierdzenie rezerwacji.
- Po udanej rezerwacji liczba wolnych miejsc zmniejsza się o 1.
- Dla pełnego terminu z listą rezerwową krótszą niż 10 użytkownik może dołączyć do listy rezerwowej i otrzymuje ekranowe
  potwierdzenie.
- Po dodaniu do listy rezerwowej liczba wolnych miejsc pozostaje bez zmian.
- Dla pełnego terminu z 10 aktywnymi wpisami na liście rezerwowej UI informuje, że termin i lista rezerwowa są pełne, a
  API odrzuca próbę zapisu.
- Gdy użytkownik widział wolne miejsce, ale w międzyczasie ktoś zajął ostatnie miejsce, system zapisuje go na listę
  rezerwową, jeśli limit listy nie został osiągnięty.
- Dla terminu z `capacity = 1` dwa równoczesne poprawne zapisy kończą się maksymalnie jedną potwierdzoną rezerwacją;
  drugi zapis trafia na listę rezerwową, jeśli jest dostępna.
- Dla pełnego terminu z 9 aktywnymi wpisami na liście rezerwowej dwa równoczesne zapisy kończą się maksymalnie jednym
  nowym wpisem na liście; drugi zapis jest odrzucony z powodu pełnej listy.
- Próba zapisu na termin nierezerwowalny kończy się błędem i nie zmienia danych.
- Próba ponownego zapisu tym samym e-mailem na ten sam termin kończy się błędem i nie tworzy duplikatu.
- Po zapisie wynik jest widoczny w danych organizatora: jako rezerwacja albo wpis na liście rezerwowej.

### Poza zakresem

- Zgłoszenia grupowe.
- Zaawansowane wyszukiwanie, filtrowanie, sortowanie i rekomendacje terminów.
- Pokazywanie użytkownikowi dokładnej pozycji na liście rezerwowej.
- Anulowanie rezerwacji przez uczestnika.
- Anulowanie rezerwacji przez organizatora.
- Wypisanie się z listy rezerwowej.
- Promowanie z listy rezerwowej po zwolnieniu miejsca.
- Oferta miejsca z listy rezerwowej, termin ważności oferty i potwierdzanie oferty.
- SMS.
- Dostarczalność wiadomości e-mail jako warunek akceptacji tej historyjki.

### Uwagi techniczne i testowe

- Backend może zachować jeden przepływ decyzyjny, który zwraca wynik typu `RESERVATION` albo `WAITLIST_ENTRY`.
- UI powinno nazywać akcję zgodnie ze stanem terminu, aby użytkownik nie był zaskoczony wynikiem.
- Testy powinny objąć regułę antyduplikacyjną po e-mailu, zapis gościa, zapis zalogowanego użytkownika, przejście na
  listę rezerwową przy wyścigu o ostatnie miejsce oraz atomowe egzekwowanie limitu 10 wpisów listy rezerwowej.

# Przepływ Rejestracji Uczestnika I Listy Rezerwowej - Skrót Planu

> Pełny plan: `context/changes/participant-registration-waitlist-flow/plan.md`
> Roadmapa: `context/foundation/roadmap.md`
> PRD: `context/foundation/prd.md`

## Co I Dlaczego

Ta zmiana domyka uczestniczący w rezerwacji przepływ dla opublikowanych sesji kursów strzeleckich. Uczestnik musi móc
przeglądać chronologiczne publiczne terminy, otworzyć bezpośrednią stronę szczegółów, zarezerwować miejsce, gdy
pojemność nadal jest dostępna, albo dołączyć do listy rezerwowej, gdy pojemność jest pełna.

## Punkt Startowy

Większość backendu i UI już istnieje: publiczne API terminów wystawia dostępność, tworzenie rezerwacji przechodzi do
listy rezerwowej, gdy termin jest pełny, a testy e2e już obejmują ścieżkę rezerwacji. Pozostała praca polega na tym,
żeby zachowanie było jawne, dopracowane i odporne na regresje względem kontraktu PRD.

## Oczekiwany Stan Końcowy

Publiczni użytkownicy mogą przejść z listy terminów na stronę szczegółów i wysłać jeden adaptacyjny formularz. Gdy są
dostępne miejsca, otrzymują potwierdzoną rezerwację; gdy miejsc nie ma, ten sam przepływ jasno umieszcza ich na liście
rezerwowej i pokazuje pozycję oraz token anulowania. Pojemność nie może zostać przekroczona, także przy współbieżnych
żądaniach.

## Kluczowe Decyzje

| Decyzja                | Wybór                                               | Dlaczego                                                                                   | Źródło          |
|------------------------|-----------------------------------------------------|--------------------------------------------------------------------------------------------|-----------------|
| Zakres odkrywania      | Lista chronologiczna plus bezpośrednie linki        | Pasuje do istniejącego publicznego API/UI i wystarcza do walidacji MVP.                    | Plan            |
| UX pełnego terminu     | Ten sam formularz, treść listy rezerwowej           | Utrzymuje prosty przepływ i spełnia FR-006.                                                | Plan            |
| Wynik listy rezerwowej | Pokaż pozycję i token anulowania                    | Ponownie używa istniejącego kontraktu odpowiedzi i zachowuje samodzielne anulowanie.       | Plan            |
| Tworzenie konta        | Zachowaj opcjonalny checkbox konta                  | Wspiera rezerwację gościa bez usuwania obecnego przekazania do konta.                      | Plan            |
| Utwardzenie pojemności | Dodaj albo utrzymaj współbieżne pokrycie backendu   | Chroni najważniejsze ograniczenie: brak potwierdzeń ponad pojemność.                       | Plan            |
| Zakres promocji        | Tylko regresja                                      | Istniejące promowanie nie powinno się zepsuć, ale rozszerzenie jest zaparkowane przez PRD. | Roadmapa / Plan |
| Pokrycie akceptacyjne  | Jedna ścieżka e2e dla rezerwacji i listy rezerwowej | Weryfikuje przepływ gwiazdy północnej end-to-end.                                          | Plan            |

## Zakres

**W zakresie:**

- Publiczne chronologiczne odkrywanie terminów i bezpośrednia nawigacja do szczegółów.
- Rejestracja uczestnika, gdy są dostępne miejsca.
- Dołączenie do listy rezerwowej, gdy pojemność jest pełna.
- Opcjonalne tworzenie konta podczas rezerwacji.
- Jasna treść wyników rezerwacji/listy rezerwowej.
- Pokrycie backendowej ochrony współbieżności pojemności.
- Skupione testy frontendu, backendu i e2e.

**Poza zakresem:**

- Wyszukiwanie, filtrowanie albo full-text discovery.
- Płatności, billing albo potwierdzanie obecności.
- Nowe zachowanie promocji/powiadomień listy rezerwowej.
- Zaplanowany e-mail ze szczegółami sesji.
- Wdrożenie produkcyjne albo prace obserwowalności.

## Architektura / Podejście

Zachowaj istniejący pionowy wycinek: publiczne komponenty rezerwacji Angular wywołują publiczne API rezerwacji, które
wchodzą do use case'ów rezerwacji blokujących termin, liczących zajęte miejsca i tworzących albo potwierdzoną
rezerwację, albo wpis na listę rezerwową. Implementacja powinna utwardzać kontrakty i treści wokół tej ścieżki, a nie
wprowadzać nowy moduł.

## Fazy W Skrócie

| Faza                              | Co dostarcza                                                                                     | Kluczowe ryzyko                                                               |
|-----------------------------------|--------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------|
| 1. Utwardzenie kontraktu backendu | Inwarianty pojemności/listy rezerwowej są jawne i testowane, także pod naciskiem współbieżności. | Testy wyścigów mogą być kruche, jeśli granice transakcji nie są kontrolowane. |
| 2. Dopracowanie publicznego UI    | Publiczna lista i formularz szczegółów jasno komunikują rezerwację vs listę rezerwową.           | Treść i stan mogą odpłynąć od typu wyniku backendu.                           |
| 3. Akceptacja end-to-end          | Jeden test przeglądarkowy dowodzi rezerwacji i listy rezerwowej przez publiczny przepływ.        | Setup e2e zależy od tworzenia terminu organizatora z wymaganego wycinka.      |

**Wymagania wstępne:** S-01 powinno dostarczyć sesje opublikowane przez organizatora. Dopóki to nie wyląduje, użyj
istniejącego przepływu enrollment i tworzenia terminu jako fixture testowego.

**Szacowany nakład:** nieoszacowany; wykonywać faza po fazie.

## Otwarte Ryzyka I Założenia

- Założenie: przegląd chronologiczny spełnia FR-004 dla MVP; wyszukiwanie i filtry zostają poza zakresem.
- Założenie: istniejące publiczne endpointy terminów pozostają publicznym kontraktem po tym, jak S-01 wyrówna
  terminologię.
- Ryzyko: obecne pokrycie e2e tworzy terminy przez UI zarządzania; jeśli S-01 zmieni ten przepływ, testy muszą podążyć
  za nową ścieżką użytkownika.

## Podsumowanie Kryteriów Sukcesu

- Publiczny uczestnik może zarezerwować niepełny termin i widzi wynik potwierdzonej rezerwacji.
- Publiczny uczestnik może wysłać ten sam formularz dla pełnego terminu i widzi pozycję na liście rezerwowej oraz token
  anulowania.
- Testy backendu dowodzą, że pojemność nie jest przekraczana, także pod naciskiem współbieżnych rezerwacji.

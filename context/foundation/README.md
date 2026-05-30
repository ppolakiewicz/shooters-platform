# Dokumenty Fundamentów

Żywe dokumenty przekrojowe, wspólne dla wielu zmian. Każdy projekt wybiera, których dokumentów fundamentów potrzebuje, np. wymagań produktowych, stosu technologicznego, roadmapy, glosariusza albo stosu testowego. Dokumenty fundamentów są własnością umiejętności, które je czytają i zapisują; ten README opisuje konwencje wspólne dla wszystkich takich dokumentów.

## Konwencja Aktualizacji

**Edytuj w miejscu.** Dokumenty fundamentów rozwijają się przez cały czas życia projektu. Gdy coś zmienia się przyrostowo, na przykład dochodzi nowa zależność, doprecyzowany cel produktowy albo przesunięty kamień milowy, edytuj istniejący plik. Nie twórz kopii z datą.

## Konwencja Archiwizacji

Gdy dokument fundamentów zostanie w pełni zastąpiony, czyli wymieniony przez nowe podejście zamiast doprecyzowany, przenieś go do `foundation/archive/YYYY-MM-DD-<doc>.md` i zapisz następcę pod pierwotną ścieżką. Folder archiwum jest zapisem historycznym; nic rutynowo z niego nie czyta.

## Antywzorzec

**Nie** umieszczaj tutaj dokumentów ograniczonych do jednej zmiany. Wszystko, co dotyczy pojedynczej zmiany, czyli jej plan, research, review albo inne artefakty, należy do `context/changes/<change-id>/`. Fundamenty są na to, co przeżywa więcej niż jedną zmianę.

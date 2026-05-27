# Participant Registration and Waitlist Flow - Implementation Plan

## Overview

Complete roadmap item S-02: participants can browse published sessions, reserve while places are available, and join the
waitlist only after capacity is full.

This is a medium-complexity change because the main booking domain already exists, but the implementation must tighten
behavior across backend use cases, Angular public UI, and e2e coverage. The plan depends on S-01 for the final "
published from reusable course information" source of terms; until S-01 lands, existing training enrollment and term
creation are valid fixtures for planning and tests.

## Current State Analysis

- Public discovery exists through `ListPublicTermsUseCase`, which lists future public terms and computes available
  places from occupied reservations (
  `backend/src/main/java/com/shootersplatform/backend/bookings/usecase/ListPublicTermsUseCase.java:23`).
- Public term detail exists through `GetPublicTermUseCase` and `TermResponse`, including `availablePlaces` (
  `backend/src/main/java/com/shootersplatform/backend/bookings/web/TermResponse.java:8`).
- Reservation creation already locks a term, rejects duplicate active emails, creates a confirmed reservation while
  capacity remains, and creates a waitlist entry when full (
  `backend/src/main/java/com/shootersplatform/backend/bookings/usecase/CreateReservationUseCase.java:71`).
- The Angular public list is chronological and already switches list-button copy to waitlist when
  `availablePlaces <= 0` (`frontend/src/app/bookings/booking-public-list.component.ts:28`,
  `frontend/src/app/bookings/booking-public-list.component.ts:54`).
- The Angular detail page uses one form and already renders both reservation and waitlist results, but its form heading
  and button copy are still reservation-oriented (`frontend/src/app/bookings/booking-public-detail.component.html:75`,
  `frontend/src/app/bookings/booking-public-detail.component.html:113`).
- Existing backend and e2e tests cover many booking behaviors, including reservation, waitlist, cancellation, and
  promotion (
  `backend/src/test/groovy/com/shootersplatform/backend/bookings/web/ReservationUserPathIntegrationSpec.groovy:48`,
  `e2e/tests/bookings.spec.ts:3`).

## Decisions

1. Public discovery is a chronological list plus direct term detail links. Search and filtering are out of S-02.
2. Full terms use the same participant form, with waitlist-specific heading/button/result copy.
3. A successful waitlist entry shows position and cancellation token.
4. Guest reservation remains supported, with optional account creation in the same flow.
5. Backend verification must include explicit concurrent-capacity coverage.
6. Existing waitlist promotion behavior is regression-protected but not expanded.
7. E2E acceptance is one public flow that covers reservation and waitlist.

## Scope

### In Scope

- Confirm and harden public term listing/detail availability behavior.
- Confirm and harden reservation vs waitlist creation behavior.
- Add concurrency-oriented backend coverage for capacity enforcement.
- Polish public detail UI copy/state for full terms.
- Keep optional account creation during reservation.
- Maintain existing cancellation token visibility after reservation or waitlist entry.
- Update frontend tests for full-term waitlist copy and result behavior.
- Update one e2e flow to cover list-to-detail reservation and waitlist acceptance.

### Out of Scope

- Search, filters, or advanced public discovery.
- New notification behavior for waitlist promotion.
- Scheduled session-details communication.
- Attendance confirmation.
- Payment or billing.
- Broad refactoring of the bookings module.

## Phase 1: Backend Contract Hardening

### Goal

Make the S-02 backend contract explicit and hard to regress: public terms expose accurate availability, reservation
requests never exceed capacity, and full terms create waitlist entries.

### Changes Required

#### Backend use case tests

- File: `backend/src/test/groovy/com/shootersplatform/backend/bookings/usecase/ReservationUseCasesSpec.groovy`
    - **Intent:** Add or strengthen a test proving that a one-seat term never creates two confirmed reservations under
      concurrent reservation attempts.
    - **Contract:** The test should assert that confirmed reservations for the term never exceed capacity and surplus
      participants become waitlist entries or receive a controlled validation outcome. Use existing in-memory test
      context only if it can model locking faithfully; otherwise place the race test in the integration layer where
      database row locking participates.

- File: `backend/src/test/groovy/com/shootersplatform/backend/bookings/web/ReservationUserPathIntegrationSpec.groovy`
    - **Intent:** Preserve public API behavior for guest reservation and waitlist creation through HTTP.
    - **Contract:** Existing tests should continue to assert `RESERVATION` for available capacity and `WAITLIST_ENTRY`
      for full capacity, with no secret token leakage through owner management lists.

#### Backend availability contract

- File: `backend/src/main/java/com/shootersplatform/backend/bookings/usecase/ListPublicTermsUseCase.java`
    - **Intent:** Keep public list availability as the source of truth for public list copy.
    - **Contract:** `AvailableTerm.availablePlaces` remains non-negative and derives from capacity minus occupied
      reservation count.

- File: `backend/src/main/java/com/shootersplatform/backend/bookings/usecase/GetPublicTermUseCase.java`
    - **Intent:** Keep public detail availability consistent with public list availability.
    - **Contract:** Detail responses expose the same `availablePlaces` semantics as list responses.

- File: `backend/src/main/java/com/shootersplatform/backend/bookings/usecase/CreateReservationUseCase.java`
    - **Intent:** Keep the single command path for both reservation and waitlist entry.
    - **Contract:** If occupied places are below capacity, return `CreatedBooking.reservation`; otherwise return
      `CreatedBooking.waitlistEntry`. Duplicate active reservation/waitlist emails stay rejected before creating another
      entry.

### Success Criteria

#### Automated Verification

- `.\gradlew.bat test --tests "*ReservationUseCasesSpec"` passes from `backend/`.
- `.\gradlew.bat test --tests "*ReservationUserPathIntegrationSpec"` passes from `backend/`.
- New or existing tests prove a one-seat term never has more confirmed reservations than capacity.

#### Manual Verification

- Review the backend test names and assertions: they should read as business rules, not implementation details.

## Phase 2: Public UI Polish

### Goal

Make the participant-facing UI match the accepted MVP behavior: chronological discovery, direct detail navigation, one
adaptive form, and clear result copy for reservation or waitlist.

### Changes Required

#### Public list

- File: `frontend/src/app/bookings/booking-public-list.component.ts`
    - **Intent:** Preserve chronological ordering and capacity-based action labels.
    - **Contract:** `sortedTerms` remains ordered by start date, and `isWaitlistTerm(term)` remains the sole predicate
      for waitlist list copy.

- File: `frontend/src/app/bookings/booking-public-list.component.html`
    - **Intent:** Keep direct navigation from each public row to term detail.
    - **Contract:** Each row links to `/booking-terms/:id`; when `availablePlaces <= 0`, the visible action uses
      waitlist copy.

#### Public detail form

- File: `frontend/src/app/bookings/booking-public-detail.component.ts`
    - **Intent:** Add a detail-level derived state for whether the current term will submit as reservation or waitlist
      based on `availablePlaces`.
    - **Contract:** The component exposes a stable predicate or computed value equivalent to
      `term()?.availablePlaces <= 0`; it does not decide final booking type locally after submission, because the
      backend response remains authoritative.

- File: `frontend/src/app/bookings/booking-public-detail.component.html`
    - **Intent:** Adapt heading, explanatory copy, and submit button to reserve vs join-waitlist mode.
    - **Contract:** The same form submits both modes. Button text is `common.reserve` when places remain and
      `bookings.public.joinWaitlist` when full. Result panels continue to branch on `booking().type`.

- Files: `frontend/src/app/shared/i18n/translations.en.ts`, `frontend/src/app/shared/i18n/translations.pl.ts`
    - **Intent:** Add any missing localized labels for the adaptive detail form and clearer waitlist result copy.
    - **Contract:** Existing keys stay stable unless a test is updated for a deliberate copy improvement.

#### Frontend tests

- File: `frontend/src/app/bookings/booking-public-detail.component.spec.ts`
    - **Intent:** Cover full-term detail behavior and waitlist result rendering.
    - **Contract:** Tests should assert that `availablePlaces: 0` changes form copy to waitlist mode and that a
      `WAITLIST_ENTRY` result displays position and cancellation token.

- File: `frontend/src/app/bookings/booking-public-list.component.spec.ts`
    - **Intent:** Preserve chronological list and waitlist action copy.
    - **Contract:** Existing tests for sorting and full-term list copy remain passing; add coverage only if current
      assertions miss the final copy contract.

### Success Criteria

#### Automated Verification

- `npm run test --workspace frontend -- bookings` passes, or the closest supported focused Vitest invocation for booking
  specs passes.
- `npm run lint --workspace frontend` passes.
- `npm run frontend:build` passes.

#### Manual Verification

- On `/booking-terms`, future terms appear chronologically and full terms show waitlist action copy.
- On `/booking-terms/:id`, a full term shows waitlist heading/button copy while using the same participant form.
- Successful reservation displays confirmed reservation copy and cancellation token.
- Successful waitlist entry displays waitlist position and cancellation token.

## Phase 3: End-to-End Acceptance

### Goal

Prove the S-02 north-star behavior through a browser-level path that starts from a public participant surface.

### Changes Required

#### E2E scenario

- File: `e2e/tests/bookings.spec.ts`
    - **Intent:** Keep one e2e scenario that creates a term fixture, visits the public list/detail flow, confirms
      reservation for the first participant, and confirms waitlist entry for the second participant.
    - **Contract:** The scenario should navigate through the public list or direct detail link, use visible participant
      form controls, assert `RESERVATION` for the first booking, assert `WAITLIST_ENTRY` and position for the second
      booking, and avoid expanding into parked notification work.

- File: `e2e/tests/bookings.spec.ts`
    - **Intent:** Keep existing promotion assertions as regression coverage only.
    - **Contract:** If promotion remains in the e2e path, it must not become the main acceptance criterion for S-02. The
      primary assertion is participant reservation/waitlist behavior.

#### Cross-layer acceptance

- File: `frontend/src/app/bookings/booking.service.ts`
    - **Intent:** Preserve the public service contract used by the e2e path.
    - **Contract:** `publicTerms`, `publicTerm`, and `createReservation` remain the client entry points for S-02.

### Success Criteria

#### Automated Verification

- `npm run e2e:test` passes in the standard workspace setup.
- CI-equivalent backend, frontend build/test/lint, and e2e commands pass when run together.

#### Manual Verification

- Using the running app, create or use a published future term with capacity 1.
- As a public participant, reserve the first place and see confirmed reservation result.
- As a second public participant, submit the same form and see waitlist position 1.
- Return to the public list and verify the term shows no available places / waitlist action copy.

## Testing Strategy

### Unit and Component Tests

- Public list component: chronological ordering, availability badge copy, full-term action copy.
- Public detail component: adaptive reserve/waitlist form copy, optional account fields, reservation result, waitlist
  result.
- Booking service: `createReservation` request body and CSRF behavior remain intact.

### Backend Tests

- Use case tests: reservation vs waitlist, duplicates, guest account creation, deadline rejection, and
  concurrency/capacity guardrail.
- Integration tests: public API returns availability, reservation creation returns the correct `CreatedBooking` type,
  owner management APIs do not leak secret tokens.

### E2E Tests

- One browser path covers organizer fixture setup, public list/detail navigation, first participant confirmed
  reservation, and second participant waitlist entry.

## Performance Considerations

- Public availability currently counts occupied places per term. This is acceptable for MVP scale, but avoid adding
  client-side polling or repeated refresh loops in this change.
- The concurrency guard should rely on existing transaction/locking behavior rather than optimistic UI assumptions.

## Migration Notes

No schema migration is expected for S-02. If implementation discovers that availability or waitlist status requires a
new persisted field, stop and re-scope because that would expand this change beyond the current plan.

## Rollback Plan

- UI changes can be reverted independently if backend behavior remains stable.
- Backend changes should preserve the existing public API response shape. If a backend hardening change breaks
  compatibility, revert that change and keep the tests as a failing signal for follow-up planning.
- E2E changes can be temporarily narrowed to API-backed assertions if browser flake blocks delivery, but the final S-02
  acceptance should restore the browser path.

## References

- PRD: `context/foundation/prd.md`
- Roadmap: `context/foundation/roadmap.md`
- Public list use case:
  `backend/src/main/java/com/shootersplatform/backend/bookings/usecase/ListPublicTermsUseCase.java`
- Reservation use case:
  `backend/src/main/java/com/shootersplatform/backend/bookings/usecase/CreateReservationUseCase.java`
- Public list component: `frontend/src/app/bookings/booking-public-list.component.ts`
- Public detail component: `frontend/src/app/bookings/booking-public-detail.component.html`
- E2E booking path: `e2e/tests/bookings.spec.ts`

## Progress

> Convention: `- [ ]` pending, `- [x]` done. Append ` - <commit sha>` when a step lands. Do not rename step titles.

### Phase 1: Backend Contract Hardening

#### Automated

- [ ] 1.1 `.\gradlew.bat test --tests "*ReservationUseCasesSpec"` passes from `backend/`.
- [ ] 1.2 `.\gradlew.bat test --tests "*ReservationUserPathIntegrationSpec"` passes from `backend/`.
- [ ] 1.3 New or existing tests prove a one-seat term never has more confirmed reservations than capacity.

#### Manual

- [ ] 1.4 Review backend test names and assertions for business-rule clarity.

### Phase 2: Public UI Polish

#### Automated

- [ ] 2.1 `npm run test --workspace frontend -- bookings` passes, or the closest supported focused Vitest invocation for
  booking specs passes.
- [ ] 2.2 `npm run lint --workspace frontend` passes.
- [ ] 2.3 `npm run frontend:build` passes.

#### Manual

- [ ] 2.4 On `/booking-terms`, future terms appear chronologically and full terms show waitlist action copy.
- [ ] 2.5 On `/booking-terms/:id`, a full term shows waitlist heading/button copy while using the same participant form.
- [ ] 2.6 Successful reservation displays confirmed reservation copy and cancellation token.
- [ ] 2.7 Successful waitlist entry displays waitlist position and cancellation token.

### Phase 3: End-to-End Acceptance

#### Automated

- [ ] 3.1 `npm run e2e:test` passes in the standard workspace setup.
- [ ] 3.2 CI-equivalent backend, frontend build/test/lint, and e2e commands pass when run together.

#### Manual

- [ ] 3.3 Using the running app, create or use a published future term with capacity 1.
- [ ] 3.4 As a public participant, reserve the first place and see confirmed reservation result.
- [ ] 3.5 As a second public participant, submit the same form and see waitlist position 1.
- [ ] 3.6 Return to the public list and verify the term shows no available places / waitlist action copy.

# Participant Registration and Waitlist Flow - Plan Brief

> Full plan: `context/changes/participant-registration-waitlist-flow/plan.md`
> Roadmap: `context/foundation/roadmap.md`
> PRD: `context/foundation/prd.md`

## What & Why

This change completes the participant-facing booking flow for published shooting-course sessions. A participant must be
able to browse chronological public terms, open a direct detail page, reserve a place while capacity remains, or join
the waitlist when capacity is full.

## Starting Point

Most of the backend and UI already exist: public term APIs expose availability, reservation creation falls back to
waitlist when full, and e2e tests already cover a booking path. The remaining work is to make the behavior explicit,
polished, and regression-resistant against the PRD contract.

## Desired End State

Public users can move from the term list to the detail page and submit one adaptive form. When places remain, they
receive a confirmed reservation; when no places remain, the same flow clearly places them on the waitlist and shows
their position plus cancellation token. Capacity cannot be exceeded, including under concurrent requests.

## Key Decisions Made

| Decision            | Choice                                      | Why                                                                               | Source         |
|---------------------|---------------------------------------------|-----------------------------------------------------------------------------------|----------------|
| Discovery scope     | Chronological list plus direct links        | Matches existing public API/UI and is enough for MVP validation.                  | Plan           |
| Full-term UX        | Same form, waitlist copy                    | Keeps the flow simple while satisfying FR-006.                                    | Plan           |
| Waitlist result     | Show position and cancellation token        | Reuses existing response contract and preserves self-cancellation.                | Plan           |
| Account creation    | Keep optional account checkbox              | Supports guest booking without removing current account handoff.                  | Plan           |
| Capacity hardening  | Add or maintain concurrent backend coverage | Protects the highest-value guardrail: no over-capacity confirmations.             | Plan           |
| Promotion scope     | Regression only                             | Existing promotion behavior should not break, but expansion is parked by the PRD. | Roadmap / Plan |
| Acceptance coverage | One e2e path for reservation and waitlist   | Verifies the north-star flow end to end.                                          | Plan           |

## Scope

**In scope:**

- Public chronological term discovery and direct detail navigation.
- Participant reservation while available places remain.
- Waitlist entry when capacity is full.
- Optional account creation during reservation.
- Clear reservation/waitlist result copy.
- Backend concurrency guardrail coverage.
- Focused frontend, backend, and e2e tests.

**Out of scope:**

- Search, filtering, or full-text discovery.
- Payment, billing, or attendance confirmation.
- New waitlist promotion/notification behavior.
- Scheduled session-details email.
- Production deployment or observability work.

## Architecture / Approach

Keep the existing vertical slice: public Angular booking components call public booking APIs, which enter booking use
cases that lock the term, calculate occupied places, and create either a confirmed reservation or waitlist entry. The
implementation should tighten contracts and copy around this path, not introduce a new module.

## Phases at a Glance

| Phase                         | What it delivers                                                                      | Key risk                                                                  |
|-------------------------------|---------------------------------------------------------------------------------------|---------------------------------------------------------------------------|
| 1. Backend contract hardening | Capacity/waitlist invariants are explicit and tested, including concurrency pressure. | Race tests can be brittle if transaction boundaries are not controlled.   |
| 2. Public UI polish           | The public list and detail form communicate reservation vs waitlist clearly.          | Copy and state can drift from backend result type.                        |
| 3. End-to-end acceptance      | One browser test proves reservation and waitlist through the public flow.             | E2E setup depends on organizer term creation from the prerequisite slice. |

**Prerequisites:** S-01 should provide organizer-published sessions. Until that lands, use the existing training
enrollment and term creation flow as the test fixture.

**Estimated effort:** Not estimated; execute phase by phase.

## Open Risks & Assumptions

- Assumption: chronological browse satisfies FR-004 for MVP; search and filters stay out.
- Assumption: existing public term endpoints remain the public contract after S-01 aligns terminology.
- Risk: current e2e coverage creates terms through management UI; if S-01 changes that flow, tests must follow the new
  user path.

## Success Criteria Summary

- A public participant can reserve a non-full term and sees a confirmed reservation result.
- A public participant can submit the same form for a full term and sees waitlist position plus cancellation token.
- Backend tests prove capacity is not exceeded, including concurrent reservation pressure.

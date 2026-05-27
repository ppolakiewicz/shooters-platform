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

# Roadmap

## Vision recap

Shooters Platform reduces repeated organizer work and participant uncertainty around shooting-course availability. The
MVP should prove that an organizer can reuse course information for a dated session, publish it, and let a participant
register or join the waitlist without exceeding capacity.

The existing codebase already has the main application layers in place, so the roadmap focuses on closing the product
gaps against the PRD rather than creating broad technical foundations.

## North star

North star means the smallest end-to-end user-visible flow that proves the product works. For this PRD, that flow is: an
organizer publishes a dated session from reusable course information, and a participant finds it and either registers
when places are available or joins the waitlist when the session is full.

- Roadmap item: S-02
- Why this proves the product: it exercises the organizer setup flow, public discovery, capacity enforcement, and
  waitlist rule in one participant-facing path.
- PRD refs: US-01, FR-001, FR-002, FR-003, FR-004, FR-005, FR-006

## At a glance

| ID   | Outcome                                                                                                     | Change ID                              | Prerequisites | PRD refs                      | Status   |
|------|-------------------------------------------------------------------------------------------------------------|----------------------------------------|---------------|-------------------------------|----------|
| S-01 | Organizer can create reusable course information and publish an editable dated session from it.             | course-template-session-publishing     | -             | FR-001, FR-002, FR-003        | ready    |
| S-02 | Participant can browse a published session and register or join the waitlist under capacity rules.          | participant-registration-waitlist-flow | S-01          | US-01, FR-004, FR-005, FR-006 | proposed |
| S-03 | Registered participants receive scheduled session-details communication and organizers can see send status. | scheduled-session-communication        | S-02          | FR-007                        | blocked  |
| S-04 | Organizer or instructor can confirm attendance before the course.                                           | pre-course-attendance-confirmation     | S-02          | US-01, FR-008                 | proposed |

## Streams

| Stream            | Chain      | Purpose                                                                                         |
|-------------------|------------|-------------------------------------------------------------------------------------------------|
| Booking core      | S-01, S-02 | Proves the template-to-session-to-participant path and capacity rule.                           |
| Course operations | S-03, S-04 | Adds pre-course communication and organizer confirmation work after the booking path is stable. |

## Baseline

- Frontend: present. Routed booking, identity, and training screens exist, including public booking list/detail and
  organizer booking management.
- Backend/API: present. The API already contains identity, training, bookings, health, web controllers, use cases,
  domain services, and persistence adapters.
- Data: present. Database schema is managed through migrations, with identity and booking tables already represented.
- Auth: present. Email/password registration and login, session handling, CSRF, and route-level authorization are
  implemented.
- Deploy/infra: partial. Local database setup and CI with backend, frontend, and end-to-end checks exist; no production
  deployment target was found.
- Observability: partial. Health checks and application logging exist; dedicated metrics, tracing, and error reporting
  were not found.

## Foundations

No separate foundation items are needed before the first roadmap slice. The codebase already has the frontend, backend,
data, and auth foundations required to start the PRD-aligned product flow.

## Slices

### S-01: Organizer can create reusable course information and publish an editable dated session from it.

- Outcome: Organizer can create reusable course information, use it to prefill a dated session, edit copied details, and
  publish the session.
- Change ID: course-template-session-publishing
- PRD refs: FR-001, FR-002, FR-003
- Prerequisites: -
- Parallel with: -
- Blockers: -
- Unknowns:
    - Is the existing reusable course concept the canonical course template, or should the product language and model be
      adjusted? Owner: user/product. Block: no.
- Risk: This comes first because the participant booking path depends on having a published dated session with trusted
  copied details.
- Status: ready

### S-02: Participant can browse a published session and register or join the waitlist under capacity rules.

- Outcome: Participant can browse or find published sessions, register only while places are available, and join the
  waitlist only after capacity is full.
- Change ID: participant-registration-waitlist-flow
- PRD refs: US-01, FR-004, FR-005, FR-006
- Prerequisites: S-01
- Parallel with: -
- Blockers: -
- Unknowns:
    - What public discovery surface is sufficient for MVP: chronological browse only, search, filters, or direct links?
      Owner: user/product. Block: no.
- Risk: This is the central product proof; it should follow S-01 so the participant path tests real organizer-published
  sessions rather than synthetic data.
- Status: proposed

### S-03: Registered participants receive scheduled session-details communication and organizers can see send status.

- Outcome: Registered participants receive templated session details according to session configuration, and organizers
  can see whether communication was sent.
- Change ID: scheduled-session-communication
- PRD refs: FR-007
- Prerequisites: S-02
- Parallel with: S-04
- Blockers: -
- Unknowns:
    - What counts as sent status for MVP: attempted, accepted by delivery provider, or manually marked as sent? Owner:
      user/product. Block: yes.
    - Should MVP use a real delivery channel immediately or record the workflow first and replace the delivery adapter
      later? Owner: user/technical. Block: yes.
- Risk: Sequencing this after S-02 avoids building communication around unstable booking states, but the send-status
  decision must be made before planning the slice.
- Status: blocked

### S-04: Organizer or instructor can confirm attendance before the course.

- Outcome: Organizer or instructor can mark whether a registered participant is confirmed for attendance before the
  course.
- Change ID: pre-course-attendance-confirmation
- PRD refs: US-01, FR-008
- Prerequisites: S-02
- Parallel with: S-03
- Blockers: -
- Unknowns:
    - Which statuses are enough for MVP: unconfirmed and confirmed, or also declined/no response? Owner: user/product.
      Block: no.
- Risk: This should follow the booking path because attendance confirmation needs a stable list of registered
  participants, but it can proceed independently from scheduled communication once S-02 is done.
- Status: proposed

## Backlog Handoff

| Roadmap ID | Change ID                              | Recommended skill                                | Handoff note                                                                                                     |
|------------|----------------------------------------|--------------------------------------------------|------------------------------------------------------------------------------------------------------------------|
| S-01       | course-template-session-publishing     | /10x-plan course-template-session-publishing     | Align reusable course information with dated session publishing and editable copied details.                     |
| S-02       | participant-registration-waitlist-flow | /10x-plan participant-registration-waitlist-flow | Complete the participant browse, registration, capacity, and waitlist path against organizer-published sessions. |
| S-03       | scheduled-session-communication        | /10x-plan scheduled-session-communication        | Plan only after send-status and delivery-channel decisions are resolved.                                         |
| S-04       | pre-course-attendance-confirmation     | /10x-plan pre-course-attendance-confirmation     | Add organizer/instructor confirmation over registered participants.                                              |

## Open Roadmap Questions

1. **Course template mapping** - Is the existing reusable course concept the canonical course template, or should
   product language and behavior be adjusted before S-01? Owner: user/product. Blocks: none.
2. **Public discovery scope** - Is chronological browse enough for MVP, or does FR-004 require search or filtering in
   the first slice? Owner: user/product. Blocks: none.
3. **Communication send status** - What status is enough for MVP: attempted, accepted by delivery provider, or manually
   marked as sent? Owner: user/product. Blocks: S-03.
4. **Communication delivery path** - Should MVP use a real delivery channel immediately or record the workflow first and
   replace delivery later? Owner: user/technical. Blocks: S-03.
5. **Attendance status vocabulary** - Which statuses are enough before the course? Owner: user/product. Blocks: none.

## Parked

- Waitlist promotion after participant cancellation. Reason: PRD marks it desirable but outside the primary MVP flow.
- Waitlist notification after organizer manual participant removal. Reason: PRD marks it desirable but outside the
  primary MVP flow.
- Payment or billing module. Reason: PRD explicitly excludes payments; organizer confirms attendance manually.
- Advanced admin management. Reason: PRD keeps only Organizer and Participant roles for MVP.

## Done

No roadmap items are done yet.

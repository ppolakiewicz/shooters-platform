---
project: "Shooters platform"
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
created: 2026-05-18
updated: 2026-05-18
checkpoint:
  current_phase: 8
  phases_completed: [1, 2, 3, 4, 5, 6, 7]
  gray_areas_resolved:
    - topic: "context type"
      decision: "greenfield; user overrode automatic brownfield detection"
    - topic: "primary persona"
      decision: "both organizer and participant are first-class in MVP"
    - topic: "main pain category"
      decision: "mix of workflow friction, coordination overhead, and missing capability"
    - topic: "core insight"
      decision: "course templates, active waitlist notifications, and automated pre-session attendance confirmation are all central"
    - topic: "auth strategy"
      decision: "login with email and password"
    - topic: "role model"
      decision: "Organizer and Participant roles"
    - topic: "MVP scope"
      decision: "primary MVP flow is template-based dated session publishing, capacity-aware registration/waitlist entry, templated pre-session email, and attendance confirmation; waitlist promotion flows are secondary"
    - topic: "timeline budget"
      decision: "3 weeks after-hours for primary MVP flow"
    - topic: "product type"
      decision: "mobile-first web application that communicates with a backend application"
    - topic: "target scale"
      decision: "dozens to a hundred users; MVP session-capacity and waitlist rule does not change at 100x scale"
    - topic: "non-goals"
      decision: "no payment or billing module; no advanced admin management"
  frs_drafted: 8
  quality_check_status: accepted
---

# Shape Notes

Seed idea: I developed an app to announce the dates of shooting courses and manage registrations for those courses.

## Vision & Problem Statement

Training organizers repeat course setup and confirmation work, while participants looking to sign up for a training session must manually track availability when a session is full. The pain appears when organizers create a new dated training session from scratch instead of reusing a template, when participants cannot join a waiting list and get notified when a spot opens, and when instructors must manually send emails before each training session to confirm attendance.

The cost today is repeated course-description entry when only the date changes, missed registrations, manual pre-session email work, and participant uncertainty around availability.

The key insight is that course templates, active waitlist notifications, and automated pre-session attendance confirmation together remove the current manual work and missed-registration risk.

## User & Persona

### Primary personas

- Training organizer: creates and manages shooting-course sessions, reuses course information across dates, manages registrations and waitlists, and needs attendance confirmation before each session.
- Participant: looks for available shooting-course sessions, signs up, joins a waiting list when a session is full, and needs to know when a spot opens.

## Access Control

Users log in with email and password.

- Organizer: can manage course templates, dated sessions, registrations, waitlists, and attendance confirmation.
- Participant: can browse sessions, register, join waitlists, and confirm attendance.

## Success Criteria

### Primary

- The MVP works when an organizer can create or reuse a course template, publish a dated session with editable prefilled data, and a participant can register only when places are available.
- If no places are available, the participant can join the waitlist; a participant cannot join the waitlist while places are still available.
- Registered participants receive a templated session-details email a configured number of days before the session, and the instructor or organizer can confirm attendance before the course.

### Secondary

- Waitlist promotion after cancellation is desirable but not part of the primary MVP flow.
- Waitlist notification after organizer manual participant removal is desirable but not part of the primary MVP flow.

### Guardrails

- Session capacity must not be exceeded.

## Functional Requirements

### Course setup

- FR-001: Organizer can create course templates. Priority: must-have
  > Socrates: Counter-argument considered: templates may be overkill if there are only a few course types or if copying a previous session is simpler. Resolution: kept; session is a different business concept with a different lifecycle, and the same session can have multiple templates based on season.
- FR-002: Organizer can reuse a course template when creating a dated session. Priority: must-have
  > Socrates: Counter-argument considered: template reuse could create stale session details or conflict with session-specific customization. Resolution: kept; there will not be much session-specific customization, and using a template is faster: select template, set date, approve, and create the new session.
- FR-003: Organizer can edit prefilled session data before publishing. Priority: must-have
  > Socrates: Counter-argument considered: editing copied data could cause inconsistencies between template and session, or make templates less valuable. Resolution: kept; editing is needed for quick adjustments.

### Registration and waitlist

- FR-004: Participant can browse or find a session. Priority: must-have
  > Socrates: Counter-argument considered: participants may arrive through direct links, and search or browse may be too broad for MVP. Resolution: kept; sharing links with participants is currently inconvenient, and participants should be able to search on their own without organizer interaction.
- FR-005: Participant can register only when places are available. Priority: must-have
  > Socrates: Counter-argument considered: overbooking might be useful for no-show risk, or organizer approval may be needed before counting someone as registered. Resolution: kept; shooting sessions have limited capacity for safety reasons and overbooking is not allowed.
- FR-006: Participant can join the waitlist only when no places are available. Priority: must-have
  > Socrates: Counter-argument considered: some participants may prefer waitlist even when places are available, or organizers may want manual control over whether waitlist opens. Resolution: kept; there is no reason to join the waitlist when a place is available.

### Session communication and attendance

- FR-007: System sends registered participants a templated session-details email a configured number of days before the session. Priority: must-have
  > Socrates: Counter-argument considered: manual review may be needed before official course details are sent, or email delivery issues may make automated email risky as a must-have. Resolution: kept; the email template is assigned to the session template and copied to the session when created, its content is stable, and the application must know the email was sent so the organizer can manually follow up if the participant does not confirm participation.
- FR-008: Instructor or organizer can confirm attendance before the course. Priority: must-have
  > Socrates: Counter-argument considered: participant self-confirmation might be more useful, or attendance confirmation might belong after the course. Resolution: kept; participation is confirmed by wire transfer, and because the MVP has no billing or payment module, the organizer must manually confirm attendance based on received payment.

## User Stories

### US-01: Participant registers for a dated shooting course

- **Given** an organizer has published a dated session from a course template
- **When** a participant finds the session and places are available
- **Then** the participant can register for the session and is counted against session capacity

#### Acceptance Criteria

- Participant cannot register if session capacity is already full.
- Participant cannot join the waitlist while places are still available.
- Participant can join the waitlist when no places are available.
- Registered participants receive the session-details email based on the session's assigned email template.
- Instructor or organizer can confirm attendance before the course.

## Business Logic

The application enforces session-capacity rules by registering participants only while places are available, allowing waitlist entry only after capacity is full, and triggering scheduled participant communication before the session.

The rule consumes the configured session capacity, the current number of registered participants, the participant's registration attempt, and the session's configured communication timing. Its output is whether the participant becomes registered, is allowed to join the waitlist, or is blocked from waitlist entry because places are still available.

The user encounters this rule when browsing and registering for a session, when a full session offers waitlist entry instead of registration, and when registered participants receive session details before the course.

## Non-Functional Requirements

- Organizers can see whether scheduled participant emails were sent.
- Participant contact and registration data is visible only to authorized organizers and the participant.
- The product never displays or confirms registrations above the configured session capacity.
- Participants can browse and register from a phone as well as desktop.

## Product Framing

- Product type: mobile-first web application that communicates with a backend application.
- Target scale: dozens to a hundred users.
- Scale note: at 100x scale, the MVP session-capacity and waitlist rule does not change.
- Timeline: no hard deadline; after-hours work; MVP budget is 3 weeks.

## Non-Goals

- No payment or billing module: organizer confirms attendance manually based on wire transfer.
- No advanced admin management: MVP keeps only Organizer and Participant roles.

## Quality Cross-Check

- Access Control: present.
- Business Logic: present.
- Project artifacts: present.
- Timeline-cost acknowledgment: present; 3-week MVP budget.
- Non-Goals: present.
- Preserved behavior: n/a for greenfield.

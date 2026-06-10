package com.shootersplatform.backend.bookings.trainingtemplate.domain

import com.shootersplatform.backend.bookings.location.domain.Location
import com.shootersplatform.backend.bookings.traininglevel.domain.TrainingLevel
import com.shootersplatform.backend.identity.domain.UserId
import spock.lang.Specification

import java.time.Clock
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneOffset

class TrainingTemplateServiceSpec extends Specification {

    private UserId owner = UserId.newId()
    private InMemoryTrainingTemplateRepository repository
    private TrainingTemplateService service

    def setup() {
        repository = new InMemoryTrainingTemplateRepository()
        service = new TrainingTemplateService(
                repository,
                Clock.fixed(Instant.parse("2026-05-08T10:00:00Z"), ZoneOffset.UTC)
        )
    }

    def "creates template with normalized details and default start time"() {
        when: "A template is created with surrounding whitespace"
            def template = create()

        then: "Text is normalized and the template is stored"
            template.name() == "Basic pistol"
            template.description() == "Safety and stance"
            template.defaultStartTime() == LocalTime.parse("09:15")
            repository.findByOwner(owner)*.id() == [template.id()]
    }

    def "normalizes null description to empty text"() {
        expect: "A missing optional description is stored as empty text"
            service.create(owner, "Basic", null, TrainingLevel.BASIC, location(), 1, 0, 30, LocalTime.MIDNIGHT)
                    .description() == ""
    }

    def "accepts text and numeric boundary values"(
            String name,
            String description,
            int capacity,
            int cancellationDays,
            int durationMinutes,
            LocalTime startTime
    ) {
        when: "A template is created with values on accepted boundaries"
            def template = service.create(
                    owner,
                    name,
                    description,
                    TrainingLevel.BASIC,
                    location(),
                    capacity,
                    cancellationDays,
                    durationMinutes,
                    startTime
            )

        then: "The boundary values are preserved"
            template.name() == name
            template.description() == description
            template.capacity() == capacity
            template.cancellationDeadlineDays() == cancellationDays
            template.durationMinutes() == durationMinutes
            template.defaultStartTime() == startTime

        where:
            name            | description      | capacity | cancellationDays | durationMinutes | startTime
            "a"             | ""               | 1        | 0                | 30              | LocalTime.MIDNIGHT
            "n".repeat(120) | "d".repeat(2048) | 10       | 365              | 1440            | LocalTime.parse("23:45")
    }

    def "rejects invalid text values"(String name, String description) {
        when: "A template is created with invalid text"
            service.create(owner, name, description, TrainingLevel.BASIC, location(), 1, 0, 30, LocalTime.NOON)

        then: "Domain validation rejects the value"
            thrown(TrainingTemplateValidationException)

        where:
            name            | description
            null            | ""
            ""              | ""
            "   "           | ""
            "n".repeat(121) | ""
            "Basic"         | "d".repeat(2049)
    }

    def "rejects invalid numeric values"(int capacity, int cancellationDays, int durationMinutes) {
        when: "A template is created with an invalid numeric value"
            service.create(owner, "Basic", "", TrainingLevel.BASIC, location(), capacity, cancellationDays, durationMinutes, LocalTime.NOON)

        then: "Domain validation rejects the value"
            thrown(TrainingTemplateValidationException)

        where:
            capacity | cancellationDays | durationMinutes
            0        | 0                | 30
            11       | 0                | 30
            1        | -1               | 30
            1        | 366              | 30
            1        | 0                | 29
            1        | 0                | 31
            1        | 0                | 1470
    }

    def "rejects default start time outside quarter-hour precision"(LocalTime startTime) {
        when: "A template is created with unsupported time precision"
            service.create(owner, "Basic", "", TrainingLevel.BASIC, location(), 1, 0, 30, startTime)

        then: "Domain validation rejects the value"
            thrown(TrainingTemplateValidationException)

        where:
            startTime << [LocalTime.parse("09:01"), LocalTime.parse("09:15:01"), LocalTime.parse("09:15:00.000000001")]
    }

    def "updates fields while preserving identity and creation timestamp"() {
        given: "An existing template"
            def template = create()

        when: "All editable values are updated"
            def updated = service.update(
                    owner,
                    template.id(),
                    "Advanced",
                    "Updated",
                    TrainingLevel.ADVANCED,
                    location(),
                    10,
                    365,
                    1440,
                    LocalTime.parse("23:45")
            )

        then: "Identity and creation time are preserved while editable values change"
            updated.id() == template.id()
            updated.createdAt() == template.createdAt()
            updated.name() == "Advanced"
            updated.defaultStartTime() == LocalTime.parse("23:45")
    }

    def "hides missing and foreign templates"() {
        given: "A template owned by another user"
            def template = create()

        when: "A different owner requests the template"
            service.get(UserId.newId(), template.id())

        then: "The template is reported as missing"
            thrown(TrainingTemplateNotFoundException)

        when: "The owner requests a random identifier"
            service.get(owner, TrainingTemplateId.newId())

        then: "The template is reported as missing"
            thrown(TrainingTemplateNotFoundException)
    }

    def "deletes owned template permanently"() {
        given: "An owned template"
            def template = create()

        when: "The owner deletes the template"
            service.delete(owner, template.id())

        then: "The template is no longer listed"
            repository.findByOwner(owner).isEmpty()
    }

    private TrainingTemplate create() {
        service.create(
                owner,
                " Basic pistol ",
                " Safety and stance ",
                TrainingLevel.INTERMEDIATE,
                location(),
                8,
                2,
                90,
                LocalTime.parse("09:15")
        )
    }

    private static Location location() {
        new Location("Range A", "Range Street 1", 52.2297d, 21.0122d)
    }
}

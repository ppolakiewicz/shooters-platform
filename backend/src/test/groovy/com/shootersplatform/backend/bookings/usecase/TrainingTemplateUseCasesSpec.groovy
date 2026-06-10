package com.shootersplatform.backend.bookings.usecase

import com.shootersplatform.backend.bookings.location.domain.Location
import com.shootersplatform.backend.bookings.traininglevel.domain.TrainingLevel
import com.shootersplatform.backend.bookings.trainingtemplate.domain.TrainingTemplateNotFoundException
import com.shootersplatform.backend.identity.domain.UserId
import spock.lang.Specification
import spock.util.time.MutableClock

import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneOffset

class TrainingTemplateUseCasesSpec extends Specification {

    private UserId owner = UserId.newId()
    private UserId otherOwner = UserId.newId()
    private MutableClock clock
    private BookingUseCaseTestContext booking

    def setup() {
        clock = new MutableClock(Instant.parse("2026-05-08T10:00:00Z"), ZoneOffset.UTC)
        booking = new BookingUseCaseTestContext(clock)
    }

    def "supports owner-scoped create list get update and delete"() {
        when: "The owner creates a template"
            def created = create(owner, "Basic pistol")

        then: "The template can be listed and read by its owner"
            booking.listTrainingTemplates.list(owner)*.id() == [created.id()]
            booking.getTrainingTemplate.get(owner, created.id()) == created

        when: "The owner updates the template later"
            clock.instant = Instant.parse("2026-05-08T11:00:00Z")
            def updated = booking.updateTrainingTemplate.update(
                    owner,
                    created.id(),
                    "Advanced pistol",
                    "Updated",
                    TrainingLevel.ADVANCED,
                    location(),
                    10,
                    3,
                    120,
                    LocalTime.parse("10:30")
            )

        then: "The edited values and update timestamp change"
            updated.name() == "Advanced pistol"
            updated.updatedAt() == clock.instant

        when: "The owner deletes the template"
            booking.deleteTrainingTemplate.delete(owner, created.id())

        then: "The template disappears from the owner list"
            booking.listTrainingTemplates.list(owner).isEmpty()
    }

    def "owner manages boundary templates without seeing another owner's data"() {
        when: "Two owners create templates and the first uses lower and upper boundary values"
            def lowerBoundary = booking.createTrainingTemplate.create(
                    owner,
                    "a",
                    "",
                    TrainingLevel.BASIC,
                    new Location("Range A", "Range Street 1", 52.2297d, 21.0122d),
                    1,
                    0,
                    30,
                    LocalTime.MIDNIGHT
            )
            clock.plus(Duration.ofMinutes(1))
            def upperBoundary = booking.createTrainingTemplate.create(
                    owner,
                    "n".repeat(120),
                    "d".repeat(2048),
                    TrainingLevel.ADVANCED,
                    new Location("Range B", "Range Street 2", -90d, 180d),
                    10,
                    365,
                    1440,
                    LocalTime.parse("23:45")
            )
            booking.createTrainingTemplate.create(
                    otherOwner,
                    "Private",
                    "",
                    TrainingLevel.INTERMEDIATE,
                    new Location("Range C", "Range Street 3", 0d, 0d),
                    5,
                    1,
                    60,
                    LocalTime.NOON
            )

        then: "The first owner reads every boundary value and sees only their templates"
            def loaded = booking.getTrainingTemplate.get(owner, upperBoundary.id())
            loaded.name() == "n".repeat(120)
            loaded.description() == "d".repeat(2048)
            loaded.trainingLevel() == TrainingLevel.ADVANCED
            loaded.location().latitude() == -90d
            loaded.location().longitude() == 180d
            loaded.capacity() == 10
            loaded.cancellationDeadlineDays() == 365
            loaded.durationMinutes() == 1440
            loaded.defaultStartTime() == LocalTime.parse("23:45")
            booking.listTrainingTemplates.list(owner)*.id() == [upperBoundary.id(), lowerBoundary.id()]

        when: "The owner edits the older template"
            clock.plus(Duration.ofMinutes(1))
            booking.updateTrainingTemplate.update(
                    owner,
                    lowerBoundary.id(),
                    "Updated",
                    "Updated description",
                    TrainingLevel.INTERMEDIATE,
                    new Location("Updated range", "Updated address", 90d, -180d),
                    7,
                    7,
                    120,
                    LocalTime.parse("10:30")
            )

        then: "Every edited value changes and the template moves to the front"
            def updated = booking.getTrainingTemplate.get(owner, lowerBoundary.id())
            updated.name() == "Updated"
            updated.description() == "Updated description"
            updated.trainingLevel() == TrainingLevel.INTERMEDIATE
            updated.location().latitude() == 90d
            updated.location().longitude() == -180d
            updated.capacity() == 7
            updated.cancellationDeadlineDays() == 7
            updated.durationMinutes() == 120
            updated.defaultStartTime() == LocalTime.parse("10:30")
            booking.listTrainingTemplates.list(owner)*.id() == [lowerBoundary.id(), upperBoundary.id()]

        when: "The owner deletes the edited template"
            booking.deleteTrainingTemplate.delete(owner, lowerBoundary.id())

        then: "Only the remaining owned template is listed"
            booking.listTrainingTemplates.list(owner)*.id() == [upperBoundary.id()]
    }

    def "allows duplicate names and sorts by most recent update"() {
        given: "Two templates with the same name created at different times"
            def first = create(owner, "Repeated")
            clock.instant = Instant.parse("2026-05-08T10:01:00Z")
            def second = create(owner, "Repeated")

        expect: "The newest template appears first"
            booking.listTrainingTemplates.list(owner)*.id() == [second.id(), first.id()]

        when: "The older template is updated"
            clock.instant = Instant.parse("2026-05-08T10:02:00Z")
            booking.updateTrainingTemplate.update(
                    owner,
                    first.id(),
                    first.name(),
                    first.description(),
                    first.trainingLevel(),
                    first.location(),
                    first.capacity(),
                    first.cancellationDeadlineDays(),
                    first.durationMinutes(),
                    first.defaultStartTime()
            )

        then: "The updated template moves to the front"
            booking.listTrainingTemplates.list(owner)*.id() == [first.id(), second.id()]
    }

    def "hides foreign templates for get update and delete"() {
        given: "A template owned by another user"
            def template = create(owner, "Private")

        when: "A different owner reads the template"
            booking.getTrainingTemplate.get(otherOwner, template.id())
        then: "The read reports not found"
            thrown(TrainingTemplateNotFoundException)

        when: "A different owner updates the template"
            booking.updateTrainingTemplate.update(otherOwner, template.id(), "Changed", "", TrainingLevel.BASIC, location(), 1, 0, 30, LocalTime.NOON)
        then: "The update reports not found"
            thrown(TrainingTemplateNotFoundException)

        when: "A different owner deletes the template"
            booking.deleteTrainingTemplate.delete(otherOwner, template.id())
        then: "The delete reports not found"
            thrown(TrainingTemplateNotFoundException)
    }

    private create(UserId ownerId, String name) {
        booking.createTrainingTemplate.create(
                ownerId,
                name,
                "",
                TrainingLevel.BASIC,
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

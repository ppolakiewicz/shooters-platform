package com.shootersplatform.backend.bookings.usecase

import com.shootersplatform.backend.bookings.location.domain.Location
import com.shootersplatform.backend.bookings.trainingenrollment.domain.TrainingEnrollmentNotFoundException
import com.shootersplatform.backend.bookings.traininglevel.domain.TrainingLevel
import com.shootersplatform.backend.identity.domain.UserId
import spock.lang.Specification

class TrainingEnrollmentUseCasesSpec extends Specification {

    private UserId owner = UserId.newId()
    private UserId otherOwner = UserId.newId()
    private BookingUseCaseTestContext booking

    def setup() {
        booking = new BookingUseCaseTestContext()
    }

    def "creates and lists training enrollments for the owner"() {
        when: "The instructor creates a training enrollment template"
            def enrollment = booking.createTrainingEnrollment.create(owner, " Basic pistol ", " Safety and stance ", TrainingLevel.INTERMEDIATE, location(), 8, 2, 90)

        then: "The template is returned through the owner list"
            enrollment.name() == "Basic pistol"
            enrollment.trainingLevel() == TrainingLevel.INTERMEDIATE
            booking.listTrainingEnrollments.list(owner)*.id() == [enrollment.id()]
            booking.listTrainingEnrollments.list(owner)*.trainingLevel() == [TrainingLevel.INTERMEDIATE]
            booking.listTrainingEnrollments.list(owner)*.capacity() == [8]
    }

    def "lists only the requested owner's training enrollments"() {
        given: "Two owners have enrollment templates"
            def ownerEnrollment = booking.createTrainingEnrollment.create(owner, "Basic pistol", "", TrainingLevel.BASIC, location(), 8, 2, 90)
            booking.createTrainingEnrollment.create(otherOwner, "Other pistol", "", TrainingLevel.BASIC, location(), 8, 2, 90)

        expect: "Only the requested owner's templates are returned"
            booking.listTrainingEnrollments.list(owner)*.id() == [ownerEnrollment.id()]
    }

    def "updates training enrollment owned by the instructor"() {
        given: "A training enrollment exists"
            def enrollment = booking.createTrainingEnrollment.create(owner, "Basic pistol", "", TrainingLevel.BASIC, location(), 8, 2, 90)

        when: "The owner updates it"
            def updated = booking.updateTrainingEnrollment.update(owner, enrollment.id(), "Advanced pistol", "Updated", TrainingLevel.ADVANCED, location(), 10, 3, 120)

        then: "The updated enrollment is visible through the owner list"
            updated.name() == "Advanced pistol"
            updated.trainingLevel() == TrainingLevel.ADVANCED
            updated.capacity() == 10
            booking.listTrainingEnrollments.list(owner).first().name() == "Advanced pistol"
            booking.listTrainingEnrollments.list(owner).first().trainingLevel() == TrainingLevel.ADVANCED
            booking.listTrainingEnrollments.list(owner).first().durationMinutes() == 120
    }

    def "rejects update by a different owner"() {
        given: "A training enrollment exists"
            def enrollment = booking.createTrainingEnrollment.create(owner, "Basic pistol", "", TrainingLevel.BASIC, location(), 8, 2, 90)

        when: "Another owner tries to update it"
            booking.updateTrainingEnrollment.update(otherOwner, enrollment.id(), "Advanced pistol", "", TrainingLevel.ADVANCED, location(), 10, 3, 120)

        then: "The enrollment is hidden from the other owner"
            thrown(TrainingEnrollmentNotFoundException)
    }

    private static Location location() {
        new Location("Range A", "Range Street 1", 52.2297d, 21.0122d)
    }
}

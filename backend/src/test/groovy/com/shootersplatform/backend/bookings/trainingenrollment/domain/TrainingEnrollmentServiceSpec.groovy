package com.shootersplatform.backend.bookings.trainingenrollment.domain


import com.shootersplatform.backend.bookings.location.domain.Location
import com.shootersplatform.backend.identity.domain.UserId
import spock.lang.Specification

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class TrainingEnrollmentServiceSpec extends Specification {

  private UserId owner = UserId.newId()
  private InMemoryTrainingEnrollmentRepository repository
  private TrainingEnrollmentService service

  def setup() {
    repository = new InMemoryTrainingEnrollmentRepository()
    service = new TrainingEnrollmentService(repository, Clock.fixed(Instant.parse("2026-05-08T10:00:00Z"), ZoneOffset.UTC))
  }

  def "creates training enrollment with normalized details"() {
    when: "The instructor creates a training enrollment template"
        def enrollment = service.create(owner, " Basic pistol ", " Safety and stance ", location(), 8, 2, 90)

    then: "The template is stored with normalized fields"
        enrollment.name() == "Basic pistol"
        enrollment.description() == "Safety and stance"
        enrollment.location().placeName() == "Range A"
        enrollment.capacity() == 8
        repository.findByOwner(owner)*.id() == [enrollment.id()]
  }

  def "rejects invalid capacity"() {
    when: "The instructor creates a training enrollment without seats"
        service.create(owner, "Basic pistol", "", location(), 0, 2, 90)

    then: "The domain rejects the template"
        thrown(TrainingEnrollmentValidationException)
  }

  def "reports missing training enrollment with dedicated exception"() {
    when: "The instructor updates a missing training enrollment"
        service.update(owner, TrainingEnrollmentId.newId(), "Basic pistol", "", location(), 8, 2, 90)

    then: "The module reports a training enrollment specific not found error"
        thrown(TrainingEnrollmentNotFoundException)
  }

  private static Location location() {
    new Location("Range A", "Range Street 1", 52.2297d, 21.0122d)
  }
}

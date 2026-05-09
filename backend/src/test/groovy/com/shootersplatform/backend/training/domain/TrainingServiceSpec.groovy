package com.shootersplatform.backend.training.domain

import com.shootersplatform.backend.identity.domain.UserId
import com.shootersplatform.backend.training.InMemoryTrainingRepository
import spock.lang.Specification

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class TrainingServiceSpec extends Specification {

  private UserId owner = UserId.newId()
  private InMemoryTrainingRepository repository
  private TrainingService service

  def setup() {
    repository = new InMemoryTrainingRepository()
    service = new TrainingService(repository, Clock.fixed(Instant.parse("2026-05-07T12:00:00Z"), ZoneOffset.UTC))
  }

  def "creates training draft with future date and normalized optional description"() {
    when: "The owner creates a training planned for the future"
        def training = service.create(owner, "  Morning drills  ", "  Range A  ", "  Accuracy block  ", LocalDate.parse("2026-06-01"), WeaponType.PISTOL, ScoringType.IDPA)

    then: "The training is stored as an owned draft without tasks"
        training.name() == "Morning drills"
        training.place() == "Range A"
        training.description() == "Accuracy block"
        training.performedOn() == LocalDate.parse("2026-06-01")
        training.tasks().empty
        repository.count() == 1
  }

  def "rejects invalid training text fields"() {
    when: "The owner creates a training without a meaningful name"
        service.create(owner, "   ", "Range A", "", LocalDate.parse("2026-05-07"), WeaponType.PISTOL, ScoringType.IDPA)

    then: "The domain rejects the request"
        thrown(TrainingValidationException)
  }

  def "adds tasks with stable run numbers and leaves gaps after deletion"() {
    given: "A training exists"
        def training = service.create(owner, "Practice", "Range A", "", LocalDate.parse("2026-05-07"), WeaponType.PISTOL, ScoringType.IDPA)

    and: "Two tasks are added"
        def withFirst = service.addTask(owner, training.id(), WeaponType.PISTOL, idpaScore(["alpha": 2, "miss": 1]), 624)
        def withSecond = service.addTask(owner, training.id(), WeaponType.RIFLE, targetScore(["0": 1, "10": 2]), 711)

    when: "The first task is deleted and another task is added"
        service.deleteTask(owner, training.id(), withFirst.tasks().first().id())
        def updated = service.addTask(owner, training.id(), WeaponType.SHOTGUN, idpaScore(["alpha": 1, "charlie": 1]), 455)

    then: "The next run number is based on the historical maximum"
        withSecond.tasks()*.runNumber() == [1, 2]
        updated.tasks()*.runNumber() == [2, 3]
  }

  def "updates task weapon scoring and duration without changing run number"() {
    given: "A training with an IDPA task exists"
        def training = service.create(owner, "Practice", "Range A", "", LocalDate.parse("2026-05-07"), WeaponType.PISTOL, ScoringType.IDPA)
        def withTask = service.addTask(owner, training.id(), WeaponType.PISTOL, idpaScore(["alpha": 1]), 100)
        def task = withTask.tasks().first()

    when: "The task is updated to target scoring"
        def updated = service.updateTask(owner, training.id(), task.id(), WeaponType.RIFLE, targetScore(["0": 1, "10": 4]), 250)

    then: "The domain keeps the run number and stores the new task values"
        updated.tasks().first().runNumber() == 1
        updated.tasks().first().weaponType() == WeaponType.RIFLE
        updated.tasks().first().scoringType() == ScoringType.TARGET
        updated.tasks().first().durationTenths() == 250
  }

  def "rejects task without any hit or miss"() {
    given: "A training exists"
        def training = service.create(owner, "Practice", "Range A", "", LocalDate.parse("2026-05-07"), WeaponType.PISTOL, ScoringType.IDPA)

    when: "The owner adds a task without score counters"
        service.addTask(owner, training.id(), WeaponType.PISTOL, HitScore.empty(ScoringType.IDPA), 100)

    then: "The domain rejects the empty score"
        thrown(TrainingValidationException)
  }

  def "does not expose trainings owned by another user"() {
    given: "A training exists for the owner"
        def training = service.create(owner, "Practice", "Range A", "", LocalDate.parse("2026-05-07"), WeaponType.PISTOL, ScoringType.IDPA)

    when: "Another user tries to load it"
        service.get(UserId.newId(), training.id())

    then: "The domain reports it as unavailable"
        thrown(TrainingNotFoundException)
  }

  private static HitScore idpaScore(Map<String, Integer> hits) {
    new HitScore(ScoringType.IDPA, hits)
  }

  private static HitScore targetScore(Map<String, Integer> hits) {
    new HitScore(ScoringType.TARGET, hits)
  }
}

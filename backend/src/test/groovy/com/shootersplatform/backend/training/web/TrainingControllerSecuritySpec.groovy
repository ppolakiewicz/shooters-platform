package com.shootersplatform.backend.training.web

import com.jayway.jsonpath.JsonPath
import com.shootersplatform.backend.AbstractIntegrationSpec
import com.shootersplatform.backend.identity.web.AuthApiClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mock.web.MockHttpSession
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import java.time.LocalDate

import static org.hamcrest.Matchers.contains
import static org.hamcrest.Matchers.hasSize
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class TrainingControllerSecuritySpec extends AbstractIntegrationSpec {

    @Autowired
    WebApplicationContext context

    MockMvc mockMvc
    AuthApiClient auth
    TrainingApiClient trainings

    def setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build()
        auth = new AuthApiClient(mockMvc)
        trainings = new TrainingApiClient(mockMvc)
    }

    def "training endpoints require authenticated USER role"() {
        when: "The training list is requested without a session"
        def anonymousResult = mockMvc.perform(get("/api/trainings"))

        then: "The API rejects it as unauthenticated"
        anonymousResult.andExpect(status().isUnauthorized())

        when: "An authenticated principal without USER role requests the list"
        def wrongRoleResult = mockMvc.perform(get("/api/trainings").with(user("admin").roles("ADMIN")))

        then: "Spring Security rejects access"
        wrongRoleResult.andExpect(status().isForbidden())
    }

    def "creates lists updates and deletes owned training with future date"() {
        given: "A registered user has a session"
        def session = registerSession()

        when: "The user creates a planned training"
        def createResult = trainings.create(session, "Planned drills", "Range A", "Prepare classifier", LocalDate.parse("2026-06-01"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath('$.performedOn').value('2026-06-01'))
                .andExpect(jsonPath('$.description').value('Prepare classifier'))
                .andReturn()
        def trainingId = json(createResult, '$.id') as String

        then: "The training appears in the owner list"
        trainings.list(session)
                .andExpect(status().isOk())
                .andExpect(jsonPath('$', hasSize(1)))
                .andExpect(jsonPath('$[0].name').value('Planned drills'))

        when: "The training details are updated"
        def updateResult = trainings.update(session, UUID.fromString(trainingId), "Updated drills", "Range B", "Updated description")

        then: "The API stores the new values without tasks"
        updateResult.andExpect(status().isOk())
                .andExpect(jsonPath('$.name').value('Updated drills'))
                .andExpect(jsonPath('$.place').value('Range B'))
                .andExpect(jsonPath('$.tasks', hasSize(0)))

        when: "The user deletes the training"
        def deleteResult = trainings.deleteTraining(session, UUID.fromString(trainingId))

        then: "The training is removed"
        deleteResult.andExpect(status().isNoContent())
        trainings.list(session).andExpect(status().isOk()).andExpect(jsonPath('$', hasSize(0)))
    }

    def "supports task CRUD with run number gaps and target zero scoring"() {
        given: "A registered user owns a training"
        def session = registerSession()
        def trainingId = UUID.fromString(json(trainings.create(session, "Practice", "Range A", "", LocalDate.parse("2026-05-07")).andReturn(), '$.id') as String)

        and: "Two tasks exist"
        def firstResult = trainings.addIdpaTask(session, trainingId, 2, 0, 0, 1, 624).andExpect(status().isCreated()).andReturn()
        def firstTaskId = UUID.fromString(json(firstResult, '$.tasks[0].id') as String)
        trainings.addTargetTaskWithZero(session, trainingId)
                .andExpect(status().isCreated())
                .andExpect(jsonPath('$.tasks[*].runNumber', contains(1, 2)))
                .andExpect(jsonPath('$.tasks[1].score["0"]').value(1))

        when: "The first task is updated and deleted before another task is added"
        trainings.updateIdpaTask(session, trainingId, firstTaskId)
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.tasks[0].runNumber').value(1))
                .andExpect(jsonPath('$.tasks[0].weaponType').value('SHOTGUN'))
        trainings.deleteTask(session, trainingId, firstTaskId).andExpect(status().isOk())
        def afterGap = trainings.addIdpaTask(session, trainingId, 1, 0, 0, 0, 300)

        then: "The new task uses the next historical run number"
        afterGap.andExpect(status().isCreated())
                .andExpect(jsonPath('$.tasks[*].runNumber', contains(2, 3)))
    }

    def "does not expose trainings across users"() {
        given: "Two users are registered"
        def ownerSession = registerSession()
        def otherSession = registerSession()

        and: "The first user owns a training"
        def trainingId = UUID.fromString(json(trainings.create(ownerSession, "Private practice", "Range A", "", LocalDate.parse("2026-05-07")).andReturn(), '$.id') as String)

        expect: "The other user cannot access or modify it"
        trainings.get(otherSession, trainingId).andExpect(status().isNotFound())
        trainings.update(otherSession, trainingId, "Nope", "Range B", "").andExpect(status().isNotFound())
        trainings.deleteTraining(otherSession, trainingId).andExpect(status().isNotFound())
        trainings.list(otherSession).andExpect(status().isOk()).andExpect(jsonPath('$', hasSize(0)))
    }

    def "mutating training requests require csrf"() {
        given: "A registered user has a session"
        def session = registerSession()

        expect: "Spring Security rejects a create request without CSRF"
        trainings.createWithoutCsrf(session).andExpect(status().isForbidden())
    }

    private MockHttpSession registerSession() {
        def result = auth.register(uniqueEmail(), uniqueUsername(), "correct horse battery").andExpect(status().isCreated()).andReturn()
        result.request.getSession(false) as MockHttpSession
    }

    private static String uniqueEmail() {
        "training-${UUID.randomUUID()}@example.com"
    }

    private static String uniqueUsername() {
        "Training_${UUID.randomUUID().toString().replace("-", "").substring(0, 12)}"
    }

    private static Object json(result, String path) {
        JsonPath.parse(result.response.contentAsString).read(path)
    }
}

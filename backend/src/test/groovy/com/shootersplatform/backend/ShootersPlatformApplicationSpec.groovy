package com.shootersplatform.backend

class ShootersPlatformApplicationSpec extends AbstractIntegrationSpec {

    def "loads application context"() {
        expect: "The Spring application context starts successfully"
        true
    }
}

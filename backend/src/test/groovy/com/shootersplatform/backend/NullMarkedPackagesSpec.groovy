package com.shootersplatform.backend

import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

class NullMarkedPackagesSpec extends Specification {

    def "all main Java packages declare a JSpecify nullness default"() {
        given: "All packages containing main Java source files"
            Path mainJava = Path.of("src/main/java")
            def sourcePackages = Files.walk(mainJava).withCloseable { paths ->
                paths
                        .filter { Files.isRegularFile(it) }
                        .filter { it.fileName.toString().endsWith(".java") }
                        .filter { it.fileName.toString() != "package-info.java" }
                        .map { it.parent }
                        .distinct()
                        .toList()
            }

        when: "Package metadata is checked for JSpecify annotations"
            def packagesWithoutNullnessDefault = sourcePackages.findAll { sourcePackage ->
                Path packageInfo = sourcePackage.resolve("package-info.java")
                !isProjectRootPackage(sourcePackage) && (!Files.exists(packageInfo) || !declaresNullnessDefault(packageInfo))
            }

        then: "Each package is explicitly null-marked or intentionally null-unmarked"
            packagesWithoutNullnessDefault == []
    }

    private static boolean declaresNullnessDefault(Path packageInfo) {
        String content = Files.readString(packageInfo)
        content.contains("@NullMarked") || content.contains("@NullUnmarked")
    }

    private static boolean isProjectRootPackage(Path sourcePackage) {
        sourcePackage == Path.of("src/main/java/com/shootersplatform/backend")
    }
}

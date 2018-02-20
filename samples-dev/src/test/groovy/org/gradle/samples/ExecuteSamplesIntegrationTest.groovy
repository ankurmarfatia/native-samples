package org.gradle.samples

import org.gradle.internal.os.OperatingSystem
import org.gradle.samples.fixtures.NativeSample
import org.gradle.samples.fixtures.Samples
import org.gradle.testkit.runner.GradleRunner
import org.junit.Assume
import spock.lang.Specification
import spock.lang.Unroll

abstract class ExecuteSamplesIntegrationTest extends Specification {

    def runSetupFor(NativeSample sample) {
        def docs = sample.documentation
        docs.setupSteps.each { command ->
            println "Running setup step " + command + " in " + docs.workingDir
            GradleRunner.create()
            .withProjectDir(sample.workingDir)
            .withArguments(command.split().drop(1))
            .build()
        }
    }

    boolean cmakeAvailable() {
         OperatingSystem.current().findInPath("cmake") != null
    }

    boolean notWindows() {
        return !OperatingSystem.current().isWindows()
    }

    @Unroll
    def "can run help for '#sample.name' without running any setup steps"() {
        // TODO - remove this when instruction parsing is smarter
        Assume.assumeTrue(sample.sampleName != 'swift-package-manager-publish')
        Assume.assumeTrue(sample.sampleName != 'cmake-library')
        Assume.assumeTrue(sample.sampleName != 'cmake-source-dependencies')
        Assume.assumeTrue(sample.sampleName != 'autotools-library')
        Assume.assumeTrue(sample.sampleName != 'library-with-tests')
        // Tool chains can only be provision on Linux for Swift and Linux and macOS for C++
        Assume.assumeFalse(sample.languageName == 'swift' && sample.sampleName == 'provisionable-tool-chains' && OperatingSystem.current().macOsX)
        Assume.assumeFalse(sample.sampleName == 'provisionable-tool-chains' && OperatingSystem.current().windows)

        given:
        sample.clean()

        expect:
        new File(sample.workingDir, "gradlew").file
        new File(sample.workingDir, "gradlew.bat").file
        new File(sample.workingDir, "settings.gradle").file

        GradleRunner.create()
                .withProjectDir(sample.workingDir)
                .withArguments("help")
                .build()

        where:
        sample << Samples.getSamples(getSampleLanguage())
    }

    @Unroll
    def "can generate build scan for '#sample.name'"() {
        given:
        expect:
        GradleRunner.create()
                .withProjectDir(sample.workingDir)
                .withArguments("tasks",
                "-I", "../../build-scan/buildScanAccept.gradle",
                "-I", "../../build-scan/buildScanUserData.gradle",
                "--scan")
                .forwardOutput()
                .build()

        where:
        sample << Samples.getSamples(getSampleLanguage())
    }

    abstract String getSampleLanguage()
}

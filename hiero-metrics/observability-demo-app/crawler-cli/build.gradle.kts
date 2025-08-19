// SPDX-License-Identifier: Apache-2.0
plugins {
    id("org.hiero.gradle.module.application")
    id("org.hiero.gradle.feature.test-timing-sensitive")
}

mainModuleInfo {
    runtimeOnly("com.swirlds.config.impl")
    runtimeOnly("org.hiero.metrics.openmetrics.http")

    runtimeOnly("org.hiero.metrics.demo.crawler.threadpool")
    runtimeOnly("org.hiero.metrics.demo.crawler.http")
    runtimeOnly("org.hiero.metrics.demo.crawler.cache.guava")
}

timingSensitiveModuleInfo { requires("org.junit.jupiter.api") }

application.mainClass = "org.hiero.metrics.demo.crawler.cli.CliMain"

tasks.named<JavaExec>("run") { standardInput = System.`in` }

val cleanAppData =
    tasks.register<Delete>("cleanData") {
        description = "Clean up application output data directory"
        group = "application"
        delete(layout.projectDirectory.dir("out"))
    }

tasks.clean { dependsOn(cleanAppData) }

tasks.register<Exec>("startObserving") {
    description = "Starts docker containers with Prometheus, Grafana, etc"
    group = "docker"

    workingDir(layout.projectDirectory.dir("../../docker"))
    commandLine("/usr/local/bin/docker", "compose", "up")
}

tasks.register<Exec>("stopObserving") {
    description = "Stops running docker containers with Prometheus, Grafana, etc"
    group = "docker"

    workingDir(layout.projectDirectory.dir("../../docker"))
    commandLine("/usr/local/bin/docker", "compose", "stop")
}

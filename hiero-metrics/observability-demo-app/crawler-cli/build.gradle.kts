// SPDX-License-Identifier: Apache-2.0
plugins { id("org.hiero.gradle.module.application") }

mainModuleInfo {
    runtimeOnly("com.swirlds.config.impl")
    runtimeOnly("org.hiero.metrics.openmetrics.http")

    runtimeOnly("org.hiero.metrics.demo.crawler.threadpool")
    runtimeOnly("org.hiero.metrics.demo.crawler.http")
    runtimeOnly("org.hiero.metrics.demo.crawler.cache.guava")
}

application.mainClass = "org.hiero.metrics.demo.crawler.cli.CliMain"

tasks.named<JavaExec>("run") { standardInput = System.`in` }

tasks.register<Exec>("startDocker") {
    description = "Starts docker with observability backends"
    group = "docker"

    // dependsOn(updateDockerEnvTask)
    workingDir(layout.projectDirectory.dir("../../docker"))
    commandLine("/usr/local/bin/docker", "compose", "up")
}

tasks.register<Exec>("stopDocker") {
    description = "Stops running docker of observability backends"
    group = "docker"

    // dependsOn(updateDockerEnvTask)
    workingDir(layout.projectDirectory.dir("../../docker"))
    commandLine("/usr/local/bin/docker", "compose", "stop")
}

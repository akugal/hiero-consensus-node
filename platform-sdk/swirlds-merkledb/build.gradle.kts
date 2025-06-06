// SPDX-License-Identifier: Apache-2.0
plugins {
    id("org.hiero.gradle.module.library")
    id("org.hiero.gradle.feature.publish-artifactregistry")
    id("org.hiero.gradle.feature.benchmark")
    id("org.hiero.gradle.feature.test-fixtures")
    id("org.hiero.gradle.feature.test-hammer")
    id("org.hiero.gradle.feature.test-timing-sensitive")
}

// Remove the following line to enable all 'javac' lint checks that we have turned on by default
// and then fix the reported issues.
tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Xlint:-exports,-lossy-conversions")
}

mainModuleInfo { annotationProcessor("com.swirlds.config.processor") }

jmhModuleInfo {
    requires("jmh.core")
    requires("com.swirlds.base.test.fixtures")
}

testModuleInfo {
    requires("com.google.common")
    requires("com.swirlds.common.test.fixtures")
    requires("com.swirlds.config.extensions.test.fixtures")
    requires("com.swirlds.merkledb.test.fixtures")
    requires("org.apache.commons.lang3")
    requires("org.apache.logging.log4j.core")
    requires("org.hiero.base.utility.test.fixtures")
    requires("org.hiero.consensus.model")
    requires("org.junit.jupiter.api")
    requires("org.junit.jupiter.params")
    requires("org.mockito")
}

hammerModuleInfo {
    requires("com.swirlds.common")
    requires("com.swirlds.merkledb")
    requires("com.swirlds.merkledb.test.fixtures")
    requires("com.swirlds.config.api")
    requires("org.apache.logging.log4j")
    requires("org.apache.logging.log4j.core")
    requires("org.junit.jupiter.api")
    requires("org.junit.jupiter.params")
    runtimeOnly("com.swirlds.common.test.fixtures")
    runtimeOnly("com.swirlds.config.impl")
}

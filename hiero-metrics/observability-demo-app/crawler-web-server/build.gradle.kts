// SPDX-License-Identifier: Apache-2.0
plugins { id("org.hiero.gradle.module.application") }

mainModuleInfo {
    runtimeOnly("org.hiero.metrics.openmetrics.http")
}

application.mainClass = "org.hiero.metrics.demo.crawler.webserver.CrawlerWebServerApplication"
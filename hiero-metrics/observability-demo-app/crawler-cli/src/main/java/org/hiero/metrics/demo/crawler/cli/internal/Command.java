// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.cli.internal;

import java.io.PrintStream;

public interface Command {

    String name();

    String description();

    String help();

    void execute(PrintStream out, String... args) throws Exception;
}

// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.cli.internal;

import java.io.PrintStream;

public abstract class AbstractCommand implements Command {

    private final String name;
    private final String description;

    public AbstractCommand(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Override
    public final String name() {
        return name;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public final void execute(PrintStream out, String... args) throws Exception {
        if (args.length == 1 && args[0].equals("--help")) {
            out.println(help());
        } else {
            executeCommand(out, args);
        }
    }

    protected abstract void executeCommand(PrintStream out, String... args) throws Exception;
}

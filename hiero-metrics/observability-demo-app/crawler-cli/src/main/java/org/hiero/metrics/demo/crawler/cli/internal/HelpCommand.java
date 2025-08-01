// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.cli.internal;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HelpCommand extends AbstractCommand {

    private final String help;

    public HelpCommand(List<Command> commands) {
        super("help", "Displays help for available commands");

        Set<String> duplicateNames = new HashSet<>();
        StringBuilder helpBuilder =
                new StringBuilder("Enter 'help {command_name}' or {command_name} --help for help.\n");
        helpBuilder.append("Available commands:\n");

        for (Command command : commands) {
            if (name().equals(command.name())) {
                throw new IllegalArgumentException("Cannot have command 'help'");
            }
            if (!duplicateNames.add(command.name())) {
                throw new IllegalStateException("Duplicate command name: " + name());
            }

            helpBuilder
                    .append("  ")
                    .append(command.name())
                    .append(" - ")
                    .append(command.description())
                    .append('\n');
        }

        help = helpBuilder.toString();
    }

    @Override
    public String help() {
        return help;
    }

    @Override
    protected void executeCommand(PrintStream out, String... args) {
        out.println(help());
    }
}

// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.cli.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandManager {

    private final Map<String, Command> commandMap = new HashMap<>();

    public CommandManager(List<Command> commands) {
        HelpCommand helpCommand = new HelpCommand(commands);
        commandMap.put(helpCommand.name(), helpCommand);

        for (Command command : commands) {
            if (commandMap.put(command.name(), command) != null) {
                throw new IllegalStateException("Duplicate command name: " + command.name());
            }
        }
    }

    public Command getCommand(String commandName) {
        Command command = commandMap.get(commandName);
        if (command == null) {
            throw new IllegalArgumentException("Unknown command: " + commandName);
        }
        return command;
    }
}

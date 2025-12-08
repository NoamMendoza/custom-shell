package com.codecrafters.shell.commands;

import com.codecrafters.shell.CommandRegistry;
import com.codecrafters.shell.ExecutionResult;
import com.codecrafters.shell.utils.PathUtils;
import java.io.File;
import java.util.List;

/**
 * Comando builtin 'type' que indica cómo se interpretaría un comando.
 * Distingue entre comandos builtin y ejecutables externos.
 */
public class TypeCommand implements BuiltinCommand {
    private final CommandRegistry registry;

    public TypeCommand(CommandRegistry registry) {
        this.registry = registry;
    }

    @Override
    public String getName() {
        return "type";
    }

    @Override
    public ExecutionResult execute(List<String> args) {
        if (args.isEmpty()) {
            return new ExecutionResult(null, null, 0);
        }
        String commandName = args.get(0);

        if (registry.isBuiltin(commandName)) {
            return new ExecutionResult(commandName + " is a shell builtin", null, 0);
        } else {
            File file = PathUtils.findExecutable(commandName);
            if (file != null) {
                return new ExecutionResult(commandName + " is " + file.getAbsolutePath(), null, 0);
            } else {
                return new ExecutionResult(commandName + ": not found", null, 1);
            }
        }
    }
}

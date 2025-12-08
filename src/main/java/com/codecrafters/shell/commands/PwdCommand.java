package com.codecrafters.shell.commands;

import com.codecrafters.shell.ExecutionResult;
import java.util.List;

/**
 * Comando builtin 'pwd' que imprime el directorio de trabajo actual.
 */
public class PwdCommand implements BuiltinCommand {
    @Override
    public String getName() {
        return "pwd";
    }

    @Override
    public ExecutionResult execute(List<String> args) {
        return new ExecutionResult(System.getProperty("user.dir"), null, 0);
    }
}

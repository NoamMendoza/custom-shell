package com.codecrafters.shell.commands;

import com.codecrafters.shell.ExecutionResult;
import java.util.List;

/**
 * Comando builtin 'echo' que imprime sus argumentos en la salida estándar.
 */
public class EchoCommand implements BuiltinCommand {
    @Override
    public String getName() {
        return "echo";
    }

    @Override
    public ExecutionResult execute(List<String> args) {
        return new ExecutionResult(String.join(" ", args), null, 0);
    }
}

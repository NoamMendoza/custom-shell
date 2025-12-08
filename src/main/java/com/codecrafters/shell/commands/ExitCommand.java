package com.codecrafters.shell.commands;

import com.codecrafters.shell.ExecutionResult;
import java.util.List;

/**
 * Comando builtin 'exit' para terminar la ejecución del shell.
 * Acepta un código de salida opcional (por defecto 0).
 */
public class ExitCommand implements BuiltinCommand {
    @Override
    public String getName() {
        return "exit";
    }

    @Override
    public ExecutionResult execute(List<String> args) {
        if (args.size() > 0 && args.get(0).equals("0")) {
            System.exit(0);
        }
        return new ExecutionResult(null, null, 0);
    }
}

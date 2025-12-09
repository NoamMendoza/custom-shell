package com.codecrafters.shell;

import com.codecrafters.shell.commands.*;
import com.codecrafters.shell.parser.CommandParser;
import com.codecrafters.shell.parser.RedirectionInfo;
import com.codecrafters.shell.utils.PathUtils;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Clase principal que orquesta el ciclo de vida del shell.
 * Maneja la lectura de comandos, parseo, ejecución y redirección de entrada/salida.
 */
public class Shell {
    private final CommandRegistry registry;
    private final CommandParser parser;
    private final ExternalCommandExecutor executor;

    /**
     * Constructor que inicializa los componentes del shell y registra los comandos builtin.
     */
    public Shell() {
        this.registry = new CommandRegistry();
        this.parser = new CommandParser();
        this.executor = new ExternalCommandExecutor();
        initializeCommands();
    }

    private void initializeCommands() {
        registry.register(new ExitCommand());
        registry.register(new EchoCommand());
        registry.register(new PwdCommand());
        registry.register(new CdCommand());
        registry.register(new TypeCommand(registry));
    }

    /**
     * Inicia el bucle principal del shell (REPL).
     * Lee la entrada del usuario, la procesa y ejecuta los comandos correspondientes.
     * 
     * @throws Exception Si ocurre un error fatal durante la ejecución.
     */
    public void run() throws Exception {
        // Prepare commands for autocompletion
        List<String> builtinNames = new ArrayList<>(Arrays.asList("echo", "type", "exit", "pwd", "cd"));
        List<String> pathExecutables = PathUtils.getExecutablesFromPath();
        List<String> allCommands = new ArrayList<>(builtinNames);
        allCommands.addAll(pathExecutables);

        Terminal terminal = TerminalBuilder.builder()
                .system(true)
                .build();

        ShellCompleter completer = new ShellCompleter(allCommands);

        LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(completer)
                .variable("columns", 80)
                .build();

        while (true) {
            String input;
            try {
                input = reader.readLine("$ ");
            } catch (Exception e) {
                break;
            }

            if (input == null || input.trim().isEmpty()) {
                continue;
            }

            RedirectionInfo redirectInfo = parser.parseRedirection(input);
            String commandInput = redirectInfo.getCommand();

            List<String> args = parser.parseArguments(commandInput);
            if (args.isEmpty()) {
                continue;
            }

            String commandName = args.get(0);
            List<String> commandArgs = args.subList(1, args.size());

            String output = null;
            String errorOutput = null;

            if (registry.isBuiltin(commandName)) {
                try {
                    ExecutionResult result = registry.getBuiltin(commandName).get().execute(commandArgs);
                    output = result.stdout;
                    errorOutput = result.stderr;
                    if (result.exitCode != 0 && errorOutput == null) {
                         // If exit code is non-zero and no error message, maybe we should print something?
                         // But builtins usually return error message in stderr.
                    }
                } catch (Exception e) {
                    errorOutput = "Error executing builtin: " + e.getMessage();
                }
            } else {
                ExecutionResult result = executor.execute(args);
                output = result.stdout;
                errorOutput = result.stderr;
            }

            // Handle stdout redirection
            if (redirectInfo.hasStdoutRedirection()) {
                writeToFile(redirectInfo.getStdoutFile(), output == null ? "" : output, redirectInfo.isStdoutAppend());
            } else if (output != null && !output.isEmpty()) {
                System.out.println(output);
            }

            // Handle stderr redirection
            if (redirectInfo.hasStderrRedirection()) {
                writeToFile(redirectInfo.getStderrFile(), errorOutput == null ? "" : errorOutput, redirectInfo.isStderrAppend());
            } else if (errorOutput != null && !errorOutput.isEmpty()) {
                System.err.println(errorOutput);
            }
        }
        
        // Close terminal if needed, though JLine usually handles it.
    }

    private void writeToFile(String filename, String content, boolean append) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, append))) {
            if (content != null && !content.isEmpty()) {
                writer.write(content);
                if (!content.endsWith("\n")) {
                    writer.write("\n");
                }
            }
        } catch (IOException e) {
            System.err.println("Error: cannot write to file: " + filename);
        }
    }
}

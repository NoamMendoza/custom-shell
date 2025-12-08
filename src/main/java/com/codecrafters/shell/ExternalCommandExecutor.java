package com.codecrafters.shell;

import com.codecrafters.shell.utils.PathUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Ejecutor de comandos externos del sistema.
 * Se encarga de buscar el ejecutable en el PATH y ejecutarlo en un proceso separado.
 */
public class ExternalCommandExecutor {

    /**
     * Ejecuta un comando externo con los argumentos dados.
     * Captura stdout y stderr en hilos separados para evitar bloqueos.
     * 
     * @param commandArgs Lista de argumentos, donde el primero es el nombre del comando.
     * @return ExecutionResult con la salida y el código de retorno.
     */
    public ExecutionResult execute(List<String> commandArgs) {
        String commandName = commandArgs.get(0);
        File executable = PathUtils.findExecutable(commandName);

        if (executable == null) {
            return new ExecutionResult(null, commandName + ": command not found", 127);
        }

        try {
            ProcessBuilder pb = new ProcessBuilder(commandArgs);
            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            StringBuilder errors = new StringBuilder();

            Thread outputThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            Thread errorThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errors.append(line).append("\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            outputThread.start();
            errorThread.start();

            int exitCode = process.waitFor();
            outputThread.join();
            errorThread.join();

            String stdout = output.length() > 0 ? output.substring(0, output.length() - 1) : "";
            String stderr = errors.length() > 0 ? errors.substring(0, errors.length() - 1) : "";

            return new ExecutionResult(stdout, stderr, exitCode);

        } catch (IOException | InterruptedException e) {
            return new ExecutionResult(null, "Error executing command: " + e.getMessage(), 1);
        }
    }
}

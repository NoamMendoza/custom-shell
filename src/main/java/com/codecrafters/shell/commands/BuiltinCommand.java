package com.codecrafters.shell.commands;

import com.codecrafters.shell.ExecutionResult;
import java.util.List;

/**
 * Interfaz para comandos internos del shell.
 */
public interface BuiltinCommand {
    /**
     * Obtiene el nombre del comando.
     * @return Nombre del comando (ej. "cd", "echo").
     */
    String getName();

    /**
     * Ejecuta el comando con los argumentos dados.
     * @param args Lista de argumentos.
     * @return Resultado de la ejecución.
     * @throws Exception Si ocurre un error durante la ejecución.
     */
    ExecutionResult execute(List<String> args) throws Exception;
}

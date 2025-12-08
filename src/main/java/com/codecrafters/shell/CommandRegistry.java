package com.codecrafters.shell;

import com.codecrafters.shell.commands.BuiltinCommand;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Registro central para comandos builtin.
 * Permite registrar y buscar comandos disponibles internamente en el shell.
 */
public class CommandRegistry {
    private final Map<String, BuiltinCommand> builtins = new HashMap<>();

    /**
     * Registra un nuevo comando builtin.
     * @param command El comando a registrar.
     */
    public void register(BuiltinCommand command) {
        builtins.put(command.getName(), command);
    }

    /**
     * Obtiene un comando builtin por su nombre.
     * @param name Nombre del comando.
     * @return Optional conteniendo el comando si existe.
     */
    public Optional<BuiltinCommand> getBuiltin(String name) {
        return Optional.ofNullable(builtins.get(name));
    }

    /**
     * Verifica si un comando es builtin.
     * @param name Nombre del comando.
     * @return true si es builtin, false en caso contrario.
     */
    public boolean isBuiltin(String name) {
        return builtins.containsKey(name);
    }
}

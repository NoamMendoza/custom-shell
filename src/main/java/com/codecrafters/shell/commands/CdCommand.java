package com.codecrafters.shell.commands;

import com.codecrafters.shell.ExecutionResult;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Comando builtin 'cd' para cambiar el directorio de trabajo actual.
 * Soporta rutas absolutas, relativas y el atajo '~' para el directorio home.
 */
public class CdCommand implements BuiltinCommand {
    @Override
    public String getName() {
        return "cd";
    }

    @Override
    public ExecutionResult execute(List<String> args) {
        if (args.isEmpty()) {
            return new ExecutionResult(null, "cd: missing operand", 1);
        }

        String path = args.get(0);

        if (path.startsWith("~")) {
            path = path.replace("~", System.getenv("HOME"));
        }

        File dir = new File(path);
        
        // Handle absolute path
        if (path.startsWith("/")) {
             if (dir.exists() && dir.isDirectory()) {
                System.setProperty("user.dir", dir.getAbsolutePath());
                return new ExecutionResult(null, null, 0);
            } else {
                return new ExecutionResult(null, "cd: " + path + ": No such file or directory", 1);
            }
        } else {
            // Handle relative path
            String currentDirString = System.getProperty("user.dir");
            File currentDirFile = new File(currentDirString);
            File newDir = new File(currentDirFile, path);

            try {
                File canonicalDir = newDir.getCanonicalFile();
                if (canonicalDir.exists() && canonicalDir.isDirectory()) {
                    System.setProperty("user.dir", canonicalDir.getPath());
                    return new ExecutionResult(null, null, 0);
                } else {
                    return new ExecutionResult(null, "cd: " + path + ": No such file or directory", 1);
                }
            } catch (IOException e) {
                return new ExecutionResult(null, "cd: error al resolver la ruta: " + e.getMessage(), 1);
            }
        }
    }
}

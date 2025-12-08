package com.codecrafters.shell;

/**
 * Representa el resultado de la ejecución de un comando.
 * Contiene la salida estándar (stdout), la salida de error (stderr) y el código de salida.
 */
public class ExecutionResult {
    public final String stdout;
    public final String stderr;
    public final int exitCode;

    /**
     * Crea un nuevo resultado de ejecución.
     * @param stdout Contenido de la salida estándar.
     * @param stderr Contenido de la salida de error.
     * @param exitCode Código de retorno del proceso (0 para éxito).
     */
    public ExecutionResult(String stdout, String stderr, int exitCode) {
        this.stdout = stdout;
        this.stderr = stderr;
        this.exitCode = exitCode;
    }
}

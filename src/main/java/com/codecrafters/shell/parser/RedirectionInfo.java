package com.codecrafters.shell.parser;

/**
 * Almacena información sobre la redirección de entrada/salida de un comando.
 * Contiene los archivos de destino para stdout y stderr, y el modo de apertura (append o overwrite).
 */
public class RedirectionInfo {
    private final String command;
    private final String stdoutFile;
    private final String stderrFile;
    private final boolean hasStdoutRedirection;
    private final boolean hasStderrRedirection;
    private final boolean stdoutAppend;
    private final boolean stderrAppend;

    public RedirectionInfo(String command, String stdoutFile, String stderrFile,
                           boolean hasStdoutRedirection, boolean hasStderrRedirection,
                           boolean stdoutAppend, boolean stderrAppend) {
        this.command = command;
        this.stdoutFile = stdoutFile;
        this.stderrFile = stderrFile;
        this.hasStdoutRedirection = hasStdoutRedirection;
        this.hasStderrRedirection = hasStderrRedirection;
        this.stdoutAppend = stdoutAppend;
        this.stderrAppend = stderrAppend;
    }

    public String getCommand() { return command; }
    public String getStdoutFile() { return stdoutFile; }
    public String getStderrFile() { return stderrFile; }
    public boolean hasStdoutRedirection() { return hasStdoutRedirection; }
    public boolean hasStderrRedirection() { return hasStderrRedirection; }
    public boolean isStdoutAppend() { return stdoutAppend; }
    public boolean isStderrAppend() { return stderrAppend; }
}

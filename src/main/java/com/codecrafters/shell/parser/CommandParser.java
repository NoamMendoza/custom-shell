package com.codecrafters.shell.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser para comandos del shell.
 * Maneja la tokenización de argumentos, comillas y detección de redirecciones.
 */
public class CommandParser {

    /**
     * Parsea una línea de comando para detectar redirecciones.
     * @param input Línea de comando completa.
     * @return Información sobre la redirección y el comando limpio.
     */
    public RedirectionInfo parseRedirection(String input) {
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        boolean isEscaped = false;

        String stdoutFile = null;
        String stderrFile = null;
        boolean hasStdoutRedirection = false;
        boolean hasStderrRedirection = false;
        boolean stdoutAppend = false;
        boolean stderrAppend = false;

        List<RedirectionToken> redirections = new ArrayList<>();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (isEscaped) {
                isEscaped = false;
                continue;
            }

            if (c == '\\') {
                isEscaped = true;
                continue;
            }

            if (c == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote;
                continue;
            }

            if (c == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
                continue;
            }

            if (!inSingleQuote && !inDoubleQuote && c == '>') {
                boolean isAppend = false;
                int nextIndex = i + 1;
                if (nextIndex < input.length() && input.charAt(nextIndex) == '>') {
                    isAppend = true;
                }

                int redirectStart = i;
                int checkIndex = i - 1;
                while (checkIndex >= 0 && Character.isWhitespace(input.charAt(checkIndex))) {
                    checkIndex--;
                }

                int fdNumber = 1;
                if (checkIndex >= 0 && Character.isDigit(input.charAt(checkIndex))) {
                    fdNumber = input.charAt(checkIndex) - '0';
                    redirectStart = checkIndex;
                }

                int fileStart = isAppend ? i + 2 : i + 1;
                while (fileStart < input.length() && Character.isWhitespace(input.charAt(fileStart))) {
                    fileStart++;
                }

                int fileEnd = fileStart;
                boolean inFileQuote = false;
                char quoteChar = 0;
                while (fileEnd < input.length()) {
                    char fc = input.charAt(fileEnd);

                    if (!inFileQuote && (fc == '\'' || fc == '"')) {
                        inFileQuote = true;
                        quoteChar = fc;
                        fileEnd++;
                        continue;
                    }

                    if (inFileQuote && fc == quoteChar) {
                        inFileQuote = false;
                        fileEnd++;
                        continue;
                    }

                    if (!inFileQuote && (fc == ' ' || fc == '>' || (Character.isDigit(fc) &&
                            fileEnd + 1 < input.length() && input.charAt(fileEnd + 1) == '>'))) {
                        break;
                    }

                    fileEnd++;
                }

                String file = input.substring(fileStart, fileEnd).trim();
                if ((file.startsWith("\"") && file.endsWith("\"")) ||
                        (file.startsWith("'") && file.endsWith("'"))) {
                    file = file.substring(1, file.length() - 1);
                }

                redirections.add(new RedirectionToken(redirectStart, fileEnd, fdNumber, file, isAppend));

                if (isAppend) {
                    i++;
                }
            }
        }

        if (redirections.isEmpty()) {
            return new RedirectionInfo(input, null, null, false, false, false, false);
        }

        redirections.sort((a, b) -> Integer.compare(a.start, b.start));

        StringBuilder commandBuilder = new StringBuilder();
        int lastPos = 0;

        for (RedirectionToken redir : redirections) {
            commandBuilder.append(input.substring(lastPos, redir.start));
            lastPos = redir.end;

            if (redir.fd == 1) {
                stdoutFile = redir.file;
                hasStdoutRedirection = true;
                stdoutAppend = redir.append;
            } else if (redir.fd == 2) {
                stderrFile = redir.file;
                hasStderrRedirection = true;
                stderrAppend = redir.append;
            }
        }

        commandBuilder.append(input.substring(lastPos));
        String command = commandBuilder.toString().trim();

        return new RedirectionInfo(command, stdoutFile, stderrFile,
                hasStdoutRedirection, hasStderrRedirection,
                stdoutAppend, stderrAppend);
    }

    /**
     * Parsea una cadena de comando en una lista de argumentos. ----
     * Respeta comillas simples y dobles, y caracteres de escape.
     * 
     * @param input Cadena de comando (sin redirecciones).
     * @return Lista de argumentos.
     */
    public List<String> parseArguments(String input) {
        List<String> arguments = new ArrayList<>();
        StringBuilder currentArg = new StringBuilder();
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        boolean isEscaped = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (isEscaped) {
                if (inDoubleQuote) {
                    // En bash, dentro de comillas dobles solo estos caracteres son especiales cuando escapados
                    // La comilla simple NO es especial, así que \' produce \ seguido de '
                    if (c == '"' || c == '\\' || c == '$' || c == '`') {
                        currentArg.append(c);
                    } else {
                        currentArg.append('\\');
                        currentArg.append(c);
                    }
                } else {
                    currentArg.append(c);
                }
                isEscaped = false;
                continue;
            }

            if (c == '\\' && !inSingleQuote) {
                isEscaped = true;
                continue;
            }

            if (c == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote;
                continue;
            }

            if (c == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
                continue;
            }

            if (Character.isWhitespace(c) && !inSingleQuote && !inDoubleQuote) {
                if (currentArg.length() > 0) {
                    arguments.add(currentArg.toString());
                    currentArg = new StringBuilder();
                }
            } else {
                currentArg.append(c);
            }
        }

        if (currentArg.length() > 0) {
            arguments.add(currentArg.toString());
        }

        return arguments;
    }

    private static class RedirectionToken {
        int start;
        int end;
        int fd;
        String file;
        boolean append;

        RedirectionToken(int start, int end, int fd, String file, boolean append) {
            this.start = start;
            this.end = end;
            this.fd = fd;
            this.file = file;
            this.append = append;
        }
    }
}

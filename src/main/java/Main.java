import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.AggregateCompleter;
import org.jline.reader.impl.completer.ArgumentCompleter;
import org.jline.reader.impl.completer.NullCompleter;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

/**
 * Implementación de un intérprete de comandos simple tipo shell.
 * Soporta comandos integrados (echo, type, exit, pwd, cd) y comandos externos del sistema.
 * También maneja redirección de salida con los operadores >, 1>, 2>, >> y 2>>.
 * Incluye autocompletado con Tab para comandos builtin.
 */
public class Main {
    /**
     * Punto de entrada del programa. Ejecuta un bucle REPL (Read-Eval-Print-Loop)
     * que lee comandos del usuario y los ejecuta.
     */
    public static void main(String[] args) throws Exception {
        
        ArrayList<String> commands = new ArrayList<>(Arrays.asList("echo", "type", "exit", "pwd", "cd"));
        
        // Configurar terminal y line reader con autocompletado
        Terminal terminal = TerminalBuilder.builder()
                .system(true)
                .build();
        
        // Crear completers para cada comando
        List<Completer> completers = new ArrayList<>();
        for (String cmd : commands) {
            completers.add(new ArgumentCompleter(
                new StringsCompleter(cmd),
                NullCompleter.INSTANCE
            ));
        }
        
        // Agregar completer agregado
        Completer completer = new AggregateCompleter(completers);
        
        LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(completer)
                .build();
        
        while (true) {
            String input;
            try {
                input = reader.readLine("$ ");
            } catch (Exception e) {
                break; // EOF o error
            }
            
            if (input == null || input.trim().isEmpty()) {
                continue;
            }
            
            // Buscar y manejar redirección de salida antes de procesar el comando
            RedirectionInfo redirectInfo = parseRedirection(input);
            String commandInput = redirectInfo.command;
            
            List<String> parsedArgs = parseArguments(commandInput);
            if (parsedArgs.isEmpty()) {
                continue;
            }
            String[] Detector = parsedArgs.toArray(new String[0]);

            // Capturar la salida si hay redirección
            String output = null;
            String errorOutput = null;
            
            if (input.equals("exit 0")) {
                System.exit(0);
            }
            else if (Detector[0].equals("echo")) {
                output = echo(commandInput);
            }
            else if (Detector[0].equals("type")) {
                output = type(commands, Detector);
            }
            else if (commandInput.equals("pwd")) {
                output = System.getProperty("user.dir");
            }
            else if (Detector[0].equals("cd")) {
                cd(Detector);
                continue;
            }
            else {
                // Para comandos externos, necesitamos capturar ambos stdout y stderr
                CommandOutput cmdOutput = executeAndCapture(Detector);
                output = cmdOutput.stdout;
                errorOutput = cmdOutput.stderr;
            }
            
            // Manejar la salida estándar (stdout)
            if (redirectInfo.hasStdoutRedirection) {
                writeToFile(redirectInfo.stdoutFile, output == null ? "" : output, redirectInfo.stdoutAppend);
            } else if (output != null && !output.isEmpty()) {
                System.out.println(output);
            }
            
            // Manejar la salida de error (stderr)
            if (redirectInfo.hasStderrRedirection) {
                writeToFile(redirectInfo.stderrFile, errorOutput == null ? "" : errorOutput, redirectInfo.stderrAppend);
            } else if (errorOutput != null && !errorOutput.isEmpty()) {
                System.err.println(errorOutput);
            }
        }
        
        terminal.close();
    }

    /**
     * Clase para almacenar la salida de un comando (stdout y stderr).
     */
    private static class CommandOutput {
        String stdout;
        String stderr;
        
        CommandOutput(String stdout, String stderr) {
            this.stdout = stdout;
            this.stderr = stderr;
        }
    }

    /**
     * Clase interna para almacenar información sobre redirección de salida.
     */
    private static class RedirectionInfo {
        String command;                 // Comando sin los operadores de redirección
        String stdoutFile;              // Archivo de destino para stdout (> o 1> o >>)
        String stderrFile;              // Archivo de destino para stderr (2> o 2>>)
        boolean hasStdoutRedirection;   // Indica si hay redirección de stdout
        boolean hasStderrRedirection;   // Indica si hay redirección de stderr
        boolean stdoutAppend;           // true si es >>, false si es >
        boolean stderrAppend;           // true si es 2>>, false si es 2>
        
        RedirectionInfo(String command, String stdoutFile, String stderrFile, 
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
    }

    /**
     * Parsea el comando de entrada para detectar operadores de redirección (>, 1>, 2>, >>, 2>>).
     * Soporta múltiples redirecciones en el mismo comando.
     * 
     * @param input Línea de comando completa
     * @return Objeto RedirectionInfo con el comando y la información de redirección
     */
    private static RedirectionInfo parseRedirection(String input) {
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
        
        // Primera pasada: encontrar todos los operadores de redirección
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
            
            // Buscar > fuera de comillas
            if (!inSingleQuote && !inDoubleQuote && c == '>') {
                // Verificar si es >> (append mode)
                boolean isAppend = false;
                int nextIndex = i + 1;
                if (nextIndex < input.length() && input.charAt(nextIndex) == '>') {
                    isAppend = true;
                }
                
                // Verificar si hay un número antes del >
                int redirectStart = i;
                int checkIndex = i - 1;
                while (checkIndex >= 0 && Character.isWhitespace(input.charAt(checkIndex))) {
                    checkIndex--;
                }
                
                int fdNumber = 1; // Por defecto stdout
                if (checkIndex >= 0 && Character.isDigit(input.charAt(checkIndex))) {
                    fdNumber = input.charAt(checkIndex) - '0';
                    redirectStart = checkIndex;
                }
                
                // Obtener el nombre del archivo
                int fileStart = isAppend ? i + 2 : i + 1;
                while (fileStart < input.length() && Character.isWhitespace(input.charAt(fileStart))) {
                    fileStart++;
                }
                
                // Encontrar el final del nombre de archivo
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
                // Remover comillas del nombre de archivo si existen
                if ((file.startsWith("\"") && file.endsWith("\"")) || 
                    (file.startsWith("'") && file.endsWith("'"))) {
                    file = file.substring(1, file.length() - 1);
                }
                
                redirections.add(new RedirectionToken(redirectStart, fileEnd, fdNumber, file, isAppend));
                
                // Saltar el segundo '>' si es append
                if (isAppend) {
                    i++;
                }
            }
        }
        
        // Si no hay redirecciones, retornar el input original
        if (redirections.isEmpty()) {
            return new RedirectionInfo(input, null, null, false, false, false, false);
        }
        
        // Ordenar las redirecciones por posición
        redirections.sort((a, b) -> Integer.compare(a.start, b.start));
        
        // Construir el comando sin las redirecciones
        StringBuilder commandBuilder = new StringBuilder();
        int lastPos = 0;
        
        for (RedirectionToken redir : redirections) {
            commandBuilder.append(input.substring(lastPos, redir.start));
            lastPos = redir.end;
            
            // Asignar el archivo según el descriptor
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
     * Clase auxiliar para almacenar información sobre un token de redirección.
     */
    private static class RedirectionToken {
        int start;      // Posición inicial del operador de redirección
        int end;        // Posición final del nombre de archivo
        int fd;         // Descriptor de archivo (1 = stdout, 2 = stderr)
        String file;    // Nombre del archivo
        boolean append; // true si es >>, false si es >
        
        RedirectionToken(int start, int end, int fd, String file, boolean append) {
            this.start = start;
            this.end = end;
            this.fd = fd;
            this.file = file;
            this.append = append;
        }
    }

    /**
     * Escribe el contenido dado a un archivo.
     * 
     * @param filename Nombre del archivo de destino
     * @param content Contenido a escribir (puede ser vacío o null)
     * @param append Si true, añade al final del archivo; si false, sobrescribe
     */
    private static void writeToFile(String filename, String content, boolean append) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, append))) {
            if (content != null && !content.isEmpty()) {
                writer.write(content);
                if (!content.endsWith("\n")) {
                    writer.write("\n");
                }
            }
            // Si content es null o vacío, el archivo se crea vacío (o no se modifica en modo append)
        } catch (IOException e) {
            System.err.println("Error: cannot write to file: " + filename);
        }
    }

    /**
     * Implementa el comando echo. Imprime argumentos con manejo especial de comillas
     * y caracteres escapados según las reglas de bash.
     * 
     * Reglas:
     * - Comillas simples: Todo es literal, backslash no escapa
     * - Comillas dobles: Backslash escapa solo ", \, $, `
     * - Sin comillas: Backslash escapa cualquier carácter, espacios se normalizan
     * 
     * @param input Línea de comando completa (incluyendo "echo")
     * @return Texto procesado para imprimir, o null si hay error
     */
    public static String echo(String input){
        String echoOutput = input.substring(5); // Remover "echo "

        if (!areQuotesBalanced(echoOutput)) {
            System.err.println("Error: unmatched quote");
            return null;
        }

        StringBuilder result = new StringBuilder();
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        boolean isEscaped = false;
        StringBuilder currentSegment = new StringBuilder();
        boolean hasEscapedChars = false;
        
        for (int i = 0; i < echoOutput.length(); i++) {
            char c = echoOutput.charAt(i);
            
            if (isEscaped) {
                if (inSingleQuote) {
                    currentSegment.append('\\').append(c);
                } else if (inDoubleQuote) {
                    // En comillas dobles, backslash solo escapa: " \ $ `
                    if (c == '"' || c == '\\' || c == '$' || c == '`') {
                        currentSegment.append(c);
                    } else {
                        // Para otros caracteres, mantener backslash literal
                        currentSegment.append('\\').append(c);
                    }
                } else {
                    // Fuera de comillas, backslash escapa todo
                    currentSegment.append(c);
                    hasEscapedChars = true;
                }
                isEscaped = false;
                continue;
            }
            
            if (c == '\\') {
                isEscaped = true;
                continue;
            }
            
            if (c == '\'' && !inDoubleQuote) {
                if (inSingleQuote) {
                    result.append(currentSegment);
                    currentSegment = new StringBuilder();
                    hasEscapedChars = false;
                    inSingleQuote = false;
                } else {
                    if (currentSegment.length() > 0) {
                        if (hasEscapedChars) {
                            result.append(currentSegment);
                        } else {
                            result.append(normalizeSpaces(currentSegment.toString()));
                        }
                        currentSegment = new StringBuilder();
                        hasEscapedChars = false;
                    }
                    inSingleQuote = true;
                }
                continue;
            }
            
            if (c == '"' && !inSingleQuote) {
                if (inDoubleQuote) {
                    result.append(currentSegment);
                    currentSegment = new StringBuilder();
                    hasEscapedChars = false;
                    inDoubleQuote = false;
                } else {
                    if (currentSegment.length() > 0) {
                        if (hasEscapedChars) {
                            result.append(currentSegment);
                        } else {
                            result.append(normalizeSpaces(currentSegment.toString()));
                        }
                        currentSegment = new StringBuilder();
                        hasEscapedChars = false;
                    }
                    inDoubleQuote = true;
                }
                continue;
            }
            
            currentSegment.append(c);
        }
        
        if (currentSegment.length() > 0) {
            if (inSingleQuote || inDoubleQuote) {
                result.append(currentSegment);
            } else {
                if (hasEscapedChars) {
                    result.append(currentSegment);
                } else {
                    result.append(normalizeSpaces(currentSegment.toString()));
                }
            }
        }
        
        return result.toString();
    }

    /**
     * Implementa el comando type. Indica si un comando es un builtin del shell
     * o un ejecutable externo en el PATH.
     * 
     * @param commands Lista de comandos builtin
     * @param Detector Array con los argumentos parseados
     * @return Descripción del tipo de comando
     */
    public static String type(ArrayList<String> commands, String [] Detector){
        String path = System.getenv("PATH");
        Boolean found = true;
        String [] path_commands = path.split(":");
        
        if(commands.contains(Detector[1])){
            return Detector[1] + " is a shell builtin";
        }else{
            for (String dir : path_commands) {
                File file = new File(dir, Detector[1]);
                if (file.exists() && file.canExecute()) {
                    return Detector[1]+" is "+file.getAbsolutePath();
                }else{
                    found = false;
                }
            }
            if (found==false) {
                return Detector[1] + ": not found";
            }
        }
        return "";
    }

    /**
     * Ejecuta un comando externo (no builtin) heredando la entrada/salida estándar.
     * 
     * @param Detector Array con el comando y sus argumentos
     * @throws IOException Si hay error al iniciar el proceso
     * @throws InterruptedException Si el proceso es interrumpido
     */
    public static void execute(String [] Detector) throws IOException, InterruptedException{
        String path = System.getenv("PATH");
        Boolean found = false;
        String [] path_commands = path.split(":");

        for (String dir : path_commands) {
            File file = new File(dir, Detector[0]);
            if (file.exists() && file.canExecute()) {
                ProcessBuilder pb = new ProcessBuilder(Detector);
                pb.inheritIO();
                Process process = pb.start();
                int exitCode = process.waitFor();
                found = true;

                if (exitCode!=0) {
                    //System.err.println("Failed to execute:  "+exitCode);
                }
                break;
            }
        }
        if (found==false) {
            System.out.println(Detector[0] + ": command not found");
        }
    }

    /**
     * Ejecuta un comando externo y captura tanto stdout como stderr.
     * Intenta múltiples variantes del nombre del comando para manejar
     * diferentes formas de escape de caracteres especiales.
     * * @param Detector Array con el comando y sus argumentos
     * @return CommandOutput con stdout y stderr del comando
     */
    public static CommandOutput executeAndCapture(String [] Detector) {
        String path = System.getenv("PATH");
        Boolean found = false;
        String [] path_commands = path.split(":");

        // Lista de variantes del nombre del comando a probar
        List<String> commandVariants = new ArrayList<>();
        commandVariants.add(Detector[0]); // Original
        
        // Agregar variante sin backslashes antes de comillas simples
        String withoutBackslashes = Detector[0].replace("\\'", "'");
        if (!withoutBackslashes.equals(Detector[0])) {
            commandVariants.add(withoutBackslashes);
        }
        
        // Agregar variante con backslashes antes de comillas simples
        String withBackslashes = Detector[0].replace("'", "\\'");
        if (!withBackslashes.equals(Detector[0])) {
            commandVariants.add(withBackslashes);
        }
        
        // --- INICIO CORRECCIÓN ---
        // Se elimina el bloque que interpretaba incorrectamente secuencias de escape.
        // El parser de argumentos ya nos da el nombre literal del archivo.
        /*
        // Agregar variante con secuencias de escape convertidas
        String withEscapes = Detector[0]
                .replace("\\n", "\n")
                .replace("\\t", "\t")
                .replace("\\r", "\r")
                .replace("\\\\", "\\");
        if (!withEscapes.equals(Detector[0])) {
            commandVariants.add(withEscapes);
        }
        */
        // --- FIN CORRECCIÓN ---

        for (String dir : path_commands) {
            for (String cmdVariant : commandVariants) {
                File file = new File(dir, cmdVariant);
                if (file.exists() && file.canExecute()) {
                    try {
                        // Crear nuevo array con la variante que funcionó
                        String[] execArgs = Detector.clone();
                        execArgs[0] = cmdVariant;
                        
                        ProcessBuilder pb = new ProcessBuilder(execArgs);
                        Process process = pb.start();
                        
                        // ... (El resto del método no cambia) ...
                        
                        // Leer stdout y stderr en hilos separados para evitar deadlock
                        StringBuilder output = new StringBuilder();
                        StringBuilder errors = new StringBuilder();
                        
                        // Hilo para leer stdout
                        Thread outputThread = new Thread(() -> {
                            try (BufferedReader reader = new BufferedReader(
                                    new InputStreamReader(process.getInputStream()))) {
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    output.append(line).append("\n");
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                        
                        // Hilo para leer stderr
                        Thread errorThread = new Thread(() -> {
                            try (BufferedReader reader = new BufferedReader(
                                    new InputStreamReader(process.getErrorStream()))) {
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
                        
                        found = true;
                        
                        // Procesar stdout
                        String stdoutResult = output.toString();
                        if (stdoutResult.endsWith("\n")) {
                            stdoutResult = stdoutResult.substring(0, stdoutResult.length() - 1);
                        }
                        // Si stdout está vacío, retornar null en lugar de string vacío
                        if (stdoutResult.isEmpty()) {
                            stdoutResult = null;
                        }
                        
                        // Procesar stderr
                        String stderrResult = errors.toString();
                        if (stderrResult.endsWith("\n")) {
                            stderrResult = stderrResult.substring(0, stderrResult.length() - 1);
                        }
                        // Si stderr está vacío, retornar null en lugar de string vacío
                        if (stderrResult.isEmpty()) {
                            stderrResult = null;
                        }
                        
                        return new CommandOutput(stdoutResult, stderrResult);
                        
                    } catch (IOException | InterruptedException e) {
                        return new CommandOutput(null, "Error executing command: " + e.getMessage());
                    }
                }
            }
        }
        
        if (found==false) {
            // Retornar el error como stderr, no como stdout
            return new CommandOutput(null, Detector[0] + ": command not found");
        }
        return new CommandOutput(null, null);
    }

    /**
     * Implementa el comando cd. Cambia el directorio de trabajo actual.
     * Soporta rutas absolutas, relativas y el atajo ~ para el directorio home.
     * 
     * @param Detector Array con el comando y sus argumentos
     */
    public static void cd(String [] Detector){
        if (Detector.length < 2) {
            System.err.println("cd: missing operand");
            return;
        }

        String path = Detector[1];

        if (path.startsWith("~")) {
            path = path.replace("~", System.getenv("HOME"));
        }
        
        File dir = new File(path);

        if (path.charAt(0)=='/') {
            if (dir.exists() && dir.isDirectory()) {
                System.setProperty("user.dir", dir.getAbsolutePath());
            }else{
                System.err.println("cd: " + path + ": No such file or directory");
            }
        }else{
            String currentDirString = System.getProperty("user.dir");
            File currentDirFile = new File(currentDirString);
            File newDir = new File(currentDirFile, path);

            try {
                File canonicalDir = newDir.getCanonicalFile();

                if (canonicalDir.exists() && canonicalDir.isDirectory()) {
                    System.setProperty("user.dir", canonicalDir.getPath());
                } else {
                    System.err.println("cd: " + path + ": No such file or directory");
                }
            } catch (IOException e) {
                System.err.println("cd: error al resolver la ruta: " + e.getMessage());
            }
        } 
    }

    /**
     * Parsea una línea de comando en argumentos individuales.
     * Respeta comillas simples, dobles y caracteres escapados según las reglas de bash.
     * * @param input Línea de comando a parsear
     * @return Lista de argumentos parseados
     */
    public static List<String> parseArguments(String input) {
        List<String> arguments = new ArrayList<>();
        StringBuilder currentArg = new StringBuilder();
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        boolean isEscaped = false;
        
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            
            if (isEscaped) {
                // --- INICIO CORRECCIÓN ---
                // El carácter anterior era '\' (y no estábamos en comillas simples),
                // así que añadimos este carácter literalmente, sea lo que sea.
                currentArg.append(c);
                isEscaped = false;
                continue;
                // --- FIN CORRECCIÓN ---
            }
            
            // --- INICIO CORRECCIÓN ---
            // La barra invertida SÓLO escapa si NO está dentro de comillas simples.
            if (c == '\\' && !inSingleQuote) {
                isEscaped = true;
                continue; // No añadas la barra invertida, solo activa el flag.
            }
            // --- FIN CORRECCIÓN ---
            
            if (c == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote;
                continue; // No añadas la comilla al argumento
            }
            
            if (c == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
                continue; // No añadas la comilla al argumento
            }
            
            if (c == ' ' && !inSingleQuote && !inDoubleQuote) {
                if (currentArg.length() > 0) {
                    arguments.add(currentArg.toString());
                    currentArg = new StringBuilder();
                }
                continue;
            }
            
            // Añade el carácter actual.
            // Esto incluirá:
            // - Caracteres normales.
            // - La barra invertida '\' si estábamos inSingleQuote (porque el check de escape falló).
            // - El carácter 'n' que sigue a '\' en inSingleQuote.
            currentArg.append(c);
        }
        
        if (currentArg.length() > 0) {
            arguments.add(currentArg.toString());
        }
        
        return arguments;
    }

    /**
     * Verifica si las comillas en una cadena están balanceadas.
     * Considera el escape de comillas con backslash.
     * 
     * @param input Cadena a verificar
     * @return true si las comillas están balanceadas, false en caso contrario
     */
    private static boolean areQuotesBalanced(String input) {
        int singleQuotes = 0;
        int doubleQuotes = 0;
        boolean inSingle = false;
        boolean inDouble = false;
        boolean isEscaped = false;
        
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            
            if (isEscaped && !inSingle) {
                isEscaped = false;
                continue;
            }
            
            if (c == '\\' && !inSingle) {
                isEscaped = true;
                continue;
            }
            
            if (c == '\'' && !inDouble) {
                inSingle = !inSingle;
                singleQuotes++;
            } else if (c == '"' && !inSingle) {
                inDouble = !inDouble;
                doubleQuotes++;
            }
        }
        
        return singleQuotes % 2 == 0 && doubleQuotes % 2 == 0;
    }

    /**
     * Normaliza múltiples espacios consecutivos a un solo espacio.
     * 
     * @param str Cadena a normalizar
     * @return Cadena con espacios normalizados
     * 
     */
    private static String normalizeSpaces(String str) {
        return str.replaceAll(" {2,}", " ");
    }
}
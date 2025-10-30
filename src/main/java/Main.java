import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Implementación de un intérprete de comandos simple tipo shell.
 * Soporta comandos integrados (echo, type, exit, pwd, cd) y comandos externos del sistema.
 * También maneja redirección de salida con los operadores > y 1>.
 */
public class Main {
    /**
     * Punto de entrada del programa. Ejecuta un bucle REPL (Read-Eval-Print-Loop)
     * que lee comandos del usuario y los ejecuta.
     */
    public static void main(String[] args) throws Exception {
        
        ArrayList<String> commands = new ArrayList<>(Arrays.asList("echo", "type", "exit", "pwd", "cd"));
        while (true) { 
            System.out.print("$ ");
            
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            
            // Buscar y manejar redirección de salida antes de procesar el comando
            RedirectionInfo redirectInfo = parseRedirection(input);
            String commandInput = redirectInfo.command;
            
            List<String> parsedArgs = parseArguments(commandInput);
            String[] Detector = parsedArgs.toArray(new String[0]);

            // Capturar la salida si hay redirección
            String output = null;
            
            if (input.equals("exit 0")) {
                System.exit(0);
            }
            else if (Detector[0].equals("echo")) {
                output = echo(commandInput);
            }
            else if (Detector[0].equals("type")) {
                output = type(commands, Detector);
            }
            else if (input.equals("pwd")) {
                output = System.getProperty("user.dir");
            }
            else if (Detector[0].equals("cd")) {
                cd(Detector);
                // cd no produce salida, continuar
                continue;
            }
            else {
                output = executeAndCapture(Detector);
            }
            
            // Manejar la salida (imprimir o redirigir a archivo)
            if (output != null) {
                if (redirectInfo.hasRedirection) {
                    writeToFile(redirectInfo.file, output);
                } else {
                    System.out.println(output);
                }
            }
        }
    }

    /**
     * Clase interna para almacenar información sobre redirección de salida.
     */
    private static class RedirectionInfo {
        String command;           // Comando sin el operador de redirección
        String file;              // Archivo de destino para la redirección
        boolean hasRedirection;   // Indica si hay redirección
        
        RedirectionInfo(String command, String file, boolean hasRedirection) {
            this.command = command;
            this.file = file;
            this.hasRedirection = hasRedirection;
        }
    }

    /**
     * Parsea el comando de entrada para detectar operadores de redirección (> o 1>).
     * 
     * @param input Línea de comando completa
     * @return Objeto RedirectionInfo con el comando y la información de redirección
     */
    private static RedirectionInfo parseRedirection(String input) {
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        boolean isEscaped = false;
        
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
                // Verificar si hay un 1 antes del >
                int redirectStart = i;
                int checkIndex = i - 1;
                while (checkIndex >= 0 && Character.isWhitespace(input.charAt(checkIndex))) {
                    checkIndex--;
                }
                
                if (checkIndex >= 0 && input.charAt(checkIndex) == '1') {
                    redirectStart = checkIndex;
                }
                
                // Obtener el comando sin la redirección
                String command = input.substring(0, redirectStart).trim();
                
                // Obtener el nombre del archivo
                int fileStart = i + 1;
                while (fileStart < input.length() && Character.isWhitespace(input.charAt(fileStart))) {
                    fileStart++;
                }
                
                String file = input.substring(fileStart).trim();
                
                return new RedirectionInfo(command, file, true);
            }
        }
        
        // No hay redirección
        return new RedirectionInfo(input, null, false);
    }

    /**
     * Escribe el contenido dado a un archivo.
     * 
     * @param filename Nombre del archivo de destino
     * @param content Contenido a escribir
     */
    private static void writeToFile(String filename, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(content);
            if (!content.isEmpty() && !content.endsWith("\n")) {
                writer.write("\n");
            }
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
     * Ejecuta un comando externo y captura su salida estándar.
     * Usado cuando se necesita capturar la salida para redirección.
     * 
     * @param Detector Array con el comando y sus argumentos
     * @return Salida del comando o mensaje de error
     */
    public static String executeAndCapture(String [] Detector) {
        String path = System.getenv("PATH");
        Boolean found = false;
        String [] path_commands = path.split(":");

        for (String dir : path_commands) {
            File file = new File(dir, Detector[0]);
            if (file.exists() && file.canExecute()) {
                try {
                    ProcessBuilder pb = new ProcessBuilder(Detector);
                    pb.redirectErrorStream(false);
                    Process process = pb.start();
                    
                    // Leer la salida estándar
                    BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream())
                    );
                    StringBuilder output = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                    
                    int exitCode = process.waitFor();
                    found = true;

                    if (exitCode != 0) {
                        // Leer errores
                        BufferedReader errorReader = new BufferedReader(
                            new InputStreamReader(process.getErrorStream())
                        );
                        String errorLine;
                        while ((errorLine = errorReader.readLine()) != null) {
                            System.err.println(errorLine);
                        }
                        //System.err.println("Failed to execute:  " + exitCode);
                        return null;
                    }
                    
                    // Remover el último salto de línea si existe
                    String result = output.toString();
                    if (result.endsWith("\n")) {
                        result = result.substring(0, result.length() - 1);
                    }
                    return result;
                    
                } catch (IOException | InterruptedException e) {
                    System.err.println("Error executing command: " + e.getMessage());
                    return null;
                }
            }
        }
        
        if (found==false) {
            return Detector[0] + ": command not found";
        }
        return null;
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
     * 
     * @param input Línea de comando a parsear
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
                if (inSingleQuote) {
                    currentArg.append('\\').append(c);
                } else if (inDoubleQuote) {
                    // En comillas dobles, backslash solo escapa: " \ $ `
                    if (c == '"' || c == '\\' || c == '$' || c == '`') {
                        currentArg.append(c);
                    } else {
                        // Para otros caracteres, mantener backslash literal
                        currentArg.append('\\').append(c);
                    }
                } else {
                    // Fuera de comillas, backslash escapa todo
                    currentArg.append(c);
                }
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
            
            if (c == ' ' && !inSingleQuote && !inDoubleQuote) {
                if (currentArg.length() > 0) {
                    arguments.add(currentArg.toString());
                    currentArg = new StringBuilder();
                }
                continue;
            }
            
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
     */
    private static String normalizeSpaces(String str) {
        return str.replaceAll(" {2,}", " ");
    }
}
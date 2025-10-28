
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Main {
    public static void main(String[] args) throws Exception {
        
        ArrayList<String> commands = new ArrayList<>(Arrays.asList("echo", "type", "exit", "pwd", "cd"));
        while (true) { 
            System.out.print("$ ");
            
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            List<String> parsedArgs = parseArguments(input);
            String[] Detector = parsedArgs.toArray(new String[0]);

            if (input.equals("exit 0")) {
                System.exit(0);
            }
            else if (Detector[0].equals("echo")) {
                System.out.println(echo(input));
            }
            else if (Detector[0].equals("type")) {
                System.out.println(type(commands,  Detector));
            }else if (input.equals("pwd")) {
                System.out.println(System.getProperty("user.dir"));
                
            }else if (Detector[0].equals("cd")) {
                cd(Detector);
                
            }else{
                execute(Detector);
            }
            
            /*else{
                System.out.println(input + ": command not found");
            } */
            
            

           
        }
        
    }

    public static String echo(String input){
        String echoOutput = input.substring(5);

        // Verificar comillas desbalanceadas - solo las que están fuera de otras comillas
        if (!areQuotesBalanced(echoOutput)) {
            System.err.println("Error: unmatched quote");
            return null;
        }

        List<String> result = new ArrayList<>();
        // Patrón actualizado: captura comillas dobles, simples, o contenido sin comillas
        Pattern pattern = Pattern.compile("\"([^\"]*)\"|'([^']*)'|([^\"']+)");
        Matcher matcher = pattern.matcher(echoOutput);

        while (matcher.find()) {
            if (matcher.group(1) != null) {
                // Contenido dentro de comillas dobles: eliminar comillas dobles, mantener comillas simples y espacios
                result.add(matcher.group(1));
            } else if (matcher.group(2) != null) {
                // Contenido dentro de comillas simples: eliminar comillas simples, mantener espacios
                result.add(matcher.group(2));
            } else if (matcher.group(3) != null) {
                // Contenido fuera de comillas: reducir múltiples espacios a uno
                String withoutQuotes = matcher.group(3);
                String normalized = withoutQuotes.replaceAll(" {2,}", " ");
                result.add(normalized);
            }
        }

        return String.join("", result);
    }

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

    public static void execute(String [] Detector) throws IOException, InterruptedException{
        String path = System.getenv("PATH");
        Boolean found = false;
        String [] path_commands = path.split(":");

        for (String dir : path_commands) {
            File file = new File(dir, Detector[0]);
            if (file.exists() && file.canExecute()) {
                //Ejecuta el programa
                ProcessBuilder pb = new ProcessBuilder(Detector);
                pb.inheritIO();
                Process process = pb.start();
                int exitCode = process.waitFor();
                found = true;

                if (exitCode!=0) {
                    System.err.println("Failed to execute:  "+exitCode);
                    
                }
                break;
            }
        }
        if (found==false) {
            System.out.println(Detector[0] + ": command not found");
        }
    }

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

    public static List<String> parseArguments(String input) {
        List<String> arguments = new ArrayList<>();
        // Patrón actualizado para manejar comillas dobles y simples
        Pattern pattern = Pattern.compile("\"([^\"]*)\"|'([^']*)'|([^\\s]+)");
        Matcher matcher = pattern.matcher(input);
        
        while (matcher.find()) {
            if (matcher.group(1) != null) {
                // Contenido dentro de comillas dobles (sin las comillas)
                arguments.add(matcher.group(1));
            } else if (matcher.group(2) != null) {
                // Contenido dentro de comillas simples (sin las comillas)
                arguments.add(matcher.group(2));
            } else if (matcher.group(3) != null) {
                // Contenido fuera de comillas
                arguments.add(matcher.group(3));
            }
        }
        
        return arguments;
    }

    private static boolean areQuotesBalanced(String input) {
        int singleQuotes = 0;
        int doubleQuotes = 0;
        boolean inSingle = false;
        boolean inDouble = false;
        
        for (char c : input.toCharArray()) {
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
}

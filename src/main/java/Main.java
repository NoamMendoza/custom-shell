
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;


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

    private static String normalizeSpaces(String str) {
        return str.replaceAll(" {2,}", " ");
    }
}

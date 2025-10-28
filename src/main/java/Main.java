
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            String Detector [] = input.split(" ");

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

        List<String> result = new ArrayList<>();
        Pattern pattern = Pattern.compile("'([^']*)'|([^']+)");
        Matcher matcher = pattern.matcher(echoOutput);

        while (matcher.find()) {
            if (matcher.group(1) != null) {
                result.add(matcher.group(1));
            } else if (matcher.group(2) != null) {
                String withoutQuotes = matcher.group(2);
                String normalized = withoutQuotes.replaceAll(" {2,}", " ");
                result.add(normalized);
            }
        }

        Map<Character, Integer> conteo = new HashMap<>();
        for (char c : echoOutput.toCharArray()) {
            conteo.put(c, conteo.getOrDefault(c, 0) + 1);
        }
        
        if (conteo.get('\'') != null && conteo.get('\'') % 2 != 0) {
            System.err.println("Error: unmatched single quote/s");
            return null;
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
                    break;
                }
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
}


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;


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
        return echoOutput;
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
            System.out.println("cd: missing operand");
            return;
        }
        String path = Detector[1];
        File dir = new File(path);
        if (dir.exists() && dir.isDirectory()) {
            System.setProperty("user.dir", dir.getAbsolutePath());
        } else {
            System.out.println("cd: " + path + ": No such file or directory/s");
        }
    }
}


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;


public class Main {
    public static void main(String[] args) throws Exception {
        
        ArrayList<String> commands = new ArrayList<>(Arrays.asList("echo", "type", "exit"));
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
            }else{
                System.out.println(input + ": command not found");
            }
            

           scanner.close();
        }
        
    }

    public static String echo(String input){
        String echoOutput = input.substring(5);
        return echoOutput;
    }

    public static String type(ArrayList<String> commands, String [] Detector){
        String path = System.getenv("PATH");
        String [] path_commands = path.split(":");
        if(commands.contains(Detector[1])){
            return Detector[1] + " is a shell builtin";
        }else{
            for (String dir : path_commands) {
                File file = new File(dir, Detector[1]);
                System.out.println(path);
                System.out.println(dir+"\n"+Detector[1] );
                if (file.exists() && file.canExecute()) {
                    return Detector[1]+" is "+file.getAbsolutePath();
                }else{
                    return Detector[1] + ": not found";
                }
            }
        }
        return "";
    }
}
